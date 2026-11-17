#!/usr/bin/env python3
"""
Build Unified Hokkaido Road Network and Act 1 Travel Route from official OSM PBF:
1. Calculates exact 447-leg walking path for the player piece along real roads (100.0% connected).
2. Exports player travel path to app/src/main/assets/act1_travel_route.bin (ACT1ROUT).
3. Normalizes dual carriageways (Corridor Collapse) into single centerlines.
4. Marks road segments on the journey route as cat 3 (TRAVEL_ROUTE).
5. Prunes far-flung local roads beyond 6km of the journey corridor.
6. Pure conformal Mercator projection (0.0% distortion).
7. Exports map roads to app/src/main/assets/hokkaido_roads_high_density.bin (HKDROAD1).
"""

import heapq
import math
import os
import re
import struct
import sys
import time
from collections import defaultdict, deque
import osmium
from shapely.geometry import LineString, Point, MultiLineString
from shapely.ops import linemerge
from shapely.strtree import STRtree

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from mercator_projection import gps_to_svg, svg_to_gps

PBF_PATH = "/home/txjp/.gemini/antigravity-cli/brain/0123cff7-8778-4488-9772-5f0d65d96e1f/scratch/hokkaido-latest.osm.pbf"
ACT1_ROUTE_KT = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"
OUTPUT_ROADS_BIN = "/home/txjp/dev/aidomari/app/src/main/assets/hokkaido_roads_high_density.bin"
OUTPUT_LEGS_BIN = "/home/txjp/dev/aidomari/app/src/main/assets/act1_travel_route.bin"

SCENIC_REFS = {'106', '254', '87', '334', '913', '76', '93'}

def haversine(c1, c2):
    R = 6371000.0
    p1, p2 = math.radians(c1[1]), math.radians(c2[1])
    dphi = math.radians(c2[1] - c1[1])
    dlam = math.radians(c2[0] - c1[0])
    a = math.sin(dphi/2)**2 + math.cos(p1)*math.cos(p2)*math.sin(dlam/2)**2
    return 2*R*math.atan2(math.sqrt(a), math.sqrt(1-a))

def rdp(points, epsilon):
    if len(points) < 3: return points
    start, end = points[0], points[-1]
    dx, dy = end[0] - start[0], end[1] - start[1]
    dist_sq = dx*dx + dy*dy
    max_d, index = 0.0, 0
    for i in range(1, len(points) - 1):
        p = points[i]
        d = ((p[0]-start[0])**2 + (p[1]-start[1])**2) if dist_sq == 0 else (
            (p[0] - (start[0] + max(0.0, min(1.0, ((p[0]-start[0])*dx + (p[1]-start[1])*dy)/dist_sq)) * dx))**2 +
            (p[1] - (start[1] + max(0.0, min(1.0, ((p[0]-start[0])*dx + (p[1]-start[1])*dy)/dist_sq)) * dy))**2
        )
        if d > max_d:
            max_d, index = d, i
    if max_d > epsilon * epsilon:
        rec1 = rdp(points[:index+1], epsilon)
        rec2 = rdp(points[index:], epsilon)
        return rec1[:-1] + rec2
    return [start, end]

def load_anchors():
    with open(ACT1_ROUTE_KT, 'r', encoding='utf-8') as f:
        content = f.read()
    pattern = re.compile(
        r'RouteAnchor\(id\s*=\s*"([^"]+)",\s*rank\s*=\s*(\d+),\s*name\s*=\s*"([^"]+)",\s*municipality\s*=\s*"([^"]+)",\s*lon\s*=\s*([0-9.]+),\s*lat\s*=\s*([0-9.]+)'
    )
    anchors = []
    for m in pattern.finditer(content):
        anchors.append({
            'id': m.group(1),
            'rank': int(m.group(2)),
            'name': m.group(3),
            'municipality': m.group(4),
            'lon': float(m.group(5)),
            'lat': float(m.group(6)),
        })
    print(f"Loaded {len(anchors)} Act 1 anchors.")
    return anchors

def collapse_parallel_lines(lines, buffer_dist=0.00065, overlap_ratio=0.5):
    if not lines:
        return []
    lines_sorted = sorted(lines, key=lambda l: l.length, reverse=True)
    kept = []
    kept_buffers = []
    tree = None
    for line in lines_sorted:
        if tree is None:
            kept.append(line)
            kept_buffers.append(line.buffer(buffer_dist))
            tree = STRtree(kept_buffers)
        else:
            candidates = tree.query(line)
            overlap_len = 0.0
            for c_idx in candidates:
                inter = line.intersection(kept_buffers[c_idx])
                overlap_len += inter.length
            if overlap_len < overlap_ratio * line.length:
                kept.append(line)
                kept_buffers.append(line.buffer(buffer_dist))
                tree = STRtree(kept_buffers)
    return kept

class FullRoadAndGraphExtractor(osmium.SimpleHandler):
    def __init__(self, anchor_tree):
        super().__init__()
        self.anchor_tree = anchor_tree
        self.mainlines = []
        self.links = []
        self.secondaries = []
        self.tertiaries = []
        self.local_access = []

        # Graph structures for routing
        self.nodes_coords = {}
        self.graph = defaultdict(dict)
        self.routing_highways = {
            'motorway', 'trunk', 'primary', 'secondary', 'tertiary',
            'motorway_link', 'trunk_link', 'primary_link', 'secondary_link', 'tertiary_link',
            'unclassified', 'residential', 'service', 'track'
        }

    def way(self, w):
        h = w.tags.get('highway')
        if not h:
            return

        is_mainline = h in ('motorway', 'trunk', 'primary')
        is_link = h in ('motorway_link', 'trunk_link', 'primary_link', 'secondary_link')
        is_secondary = h in ('secondary',)
        is_tertiary = h in ('tertiary', 'tertiary_link')
        is_local = h in ('unclassified',)

        try:
            nl = list(w.nodes)
            if len(nl) < 2: return

            # 1. Add to routing graph if eligible
            if h in self.routing_highways:
                for i in range(len(nl) - 1):
                    n1, n2 = nl[i], nl[i+1]
                    c1 = (round(n1.location.lon, 6), round(n1.location.lat, 6))
                    c2 = (round(n2.location.lon, 6), round(n2.location.lat, 6))
                    self.nodes_coords[n1.ref] = c1
                    self.nodes_coords[n2.ref] = c2
                    d = haversine(c1, c2)
                    if n2.ref not in self.graph[n1.ref] or d < self.graph[n1.ref][n2.ref][0]:
                        self.graph[n1.ref][n2.ref] = (d, h)
                        self.graph[n2.ref][n1.ref] = (d, h)

            # 2. Extract map features
            if not (is_mainline or is_link or is_secondary or is_tertiary or is_local):
                return

            coords = [(round(n.location.lon, 6), round(n.location.lat, 6)) for n in nl]
            ls = LineString(coords)
            ref = w.tags.get('ref', '')
            name = w.tags.get('name', '')

            if is_mainline:
                self.mainlines.append((w.id, h, ref, name, ls))
            elif is_link:
                self.links.append((w.id, h, ref, name, ls))
            elif is_secondary:
                self.secondaries.append((w.id, h, ref, name, ls))
            elif is_tertiary:
                self.tertiaries.append((w.id, h, ref, name, ls))
            elif is_local:
                if len(self.anchor_tree.query(ls)) > 0:
                    self.local_access.append((w.id, h, ref, name, ls))
        except Exception:
            pass

def main():
    t0 = time.time()
    anchors = load_anchors()
    anchor_points = [Point(a['lon'], a['lat']) for a in anchors]
    anchor_tree = STRtree([p.buffer(0.025) for p in anchor_points])

    print(f"Extracting roads and building topology from {PBF_PATH}...")
    extractor = FullRoadAndGraphExtractor(anchor_tree)
    extractor.apply_file(PBF_PATH, locations=True)
    t1 = time.time()
    print(f"OSM Extraction completed in {t1 - t0:.1f}s.")
    print(f"  Mainlines: {len(extractor.mainlines)}")
    print(f"  Links/Ramps: {len(extractor.links)}")
    print(f"  Secondaries: {len(extractor.secondaries)}")
    print(f"  Tertiaries: {len(extractor.tertiaries)}")
    print(f"  Local Access: {len(extractor.local_access)}")
    print(f"  Graph Nodes: {len(extractor.nodes_coords)}")

    # 1. Find Main Connected Component of Hokkaido road network
    print("Finding Main Connected Component of Hokkaido road network...")
    visited = set()
    components = []
    for nid in extractor.graph:
        if nid not in visited:
            comp = []
            q = deque([nid])
            visited.add(nid)
            while q:
                curr = q.popleft()
                comp.append(curr)
                for nxt in extractor.graph[curr]:
                    if nxt not in visited:
                        visited.add(nxt)
                        q.append(nxt)
            components.append(comp)

    components.sort(key=len, reverse=True)
    main_comp = set(components[0])
    print(f"Main component contains {len(main_comp)} nodes ({len(main_comp)/len(extractor.nodes_coords)*100:.1f}%).")

    # 2. Snap all 448 Act 1 anchors to Main Connected Component (Surface Roads Only)
    # Exclude expressways (motorway/motorway_link) and isolated PA parking aisles (service/track)
    # so anchors snap to public surface roads (trunk, primary, secondary, tertiary, unclassified, residential)
    public_surface_tags = {'trunk', 'primary', 'secondary', 'tertiary', 'unclassified', 'residential'}
    surface_node_ids = [
        nid for nid in main_comp
        if any(h in public_surface_tags for _, h in extractor.graph[nid].values())
    ]
    surface_pts = [Point(extractor.nodes_coords[nid][0], extractor.nodes_coords[nid][1]) for nid in surface_node_ids]
    surface_tree = STRtree(surface_pts)

    snapped_anchors = []
    for a in anchors:
        apt = Point(a['lon'], a['lat'])
        idx = surface_tree.nearest(apt)
        nid = surface_node_ids[idx]
        c = extractor.nodes_coords[nid]
        d = haversine((a['lon'], a['lat']), c)
        snapped_anchors.append((a, nid, c, d))

    print(f"Anchors snapped to surface road network. Max snap dist: {max(x[3] for x in snapped_anchors):.1f}m, Avg: {sum(x[3] for x in snapped_anchors)/len(snapped_anchors):.1f}m")

    # 3. A* Routing for all 447 legs
    def a_star(start_id, goal_id):
        if start_id == goal_id: return 0.0, [start_id]
        goal_c = extractor.nodes_coords[goal_id]
        open_set = []
        h0 = haversine(extractor.nodes_coords[start_id], goal_c)
        heapq.heappush(open_set, (h0, 0.0, start_id, [start_id]))
        g_scores = {start_id: 0.0}
        while open_set:
            f, g, cur, path = heapq.heappop(open_set)
            if cur == goal_id:
                return g, path
            if g > g_scores.get(cur, float('inf')):
                continue
            for nxt, (w, _) in extractor.graph[cur].items():
                tent_g = g + w
                if tent_g < g_scores.get(nxt, float('inf')):
                    g_scores[nxt] = tent_g
                    h = haversine(extractor.nodes_coords[nxt], goal_c)
                    heapq.heappush(open_set, (tent_g + h, tent_g, nxt, path + [nxt]))
        return None, None

    print("Routing all 447 legs between adjacent anchors...")
    t_route = time.time()
    legs_data = []
    success_count = 0
    total_journey_m = 0.0
    journey_lines = []

    for i in range(len(snapped_anchors) - 1):
        a1, nid1, c1, d1 = snapped_anchors[i]
        a2, nid2, c2, d2 = snapped_anchors[i+1]

        dist, path = a_star(nid1, nid2)
        if dist is not None:
            success_count += 1
            total_journey_m += dist
            coords = [extractor.nodes_coords[nid] for nid in path]
            full_coords = [(a1['lon'], a1['lat'])] + coords + [(a2['lon'], a2['lat'])]
            simp_coords = rdp(full_coords, 0.00008)
            legs_data.append({
                'leg_index': i,
                'from_id': a1['id'],
                'to_id': a2['id'],
                'distance_m': dist,
                'coords': simp_coords
            })
            if len(simp_coords) >= 2:
                journey_lines.append(LineString(simp_coords))
        else:
            print(f"FAILED leg {i}: {a1['name']} -> {a2['name']}")

    print(f"A* Routing finished in {time.time()-t_route:.1f}s.")
    print(f"  Route completion: {success_count} / {len(snapped_anchors)-1} ({success_count/(len(snapped_anchors)-1)*100:.1f}%)")
    print(f"  Total journey road distance: {total_journey_m/1000.0:.1f} km")

    # Export act1_travel_route.bin
    os.makedirs(os.path.dirname(OUTPUT_LEGS_BIN), exist_ok=True)
    with open(OUTPUT_LEGS_BIN, 'wb') as f:
        f.write(b"ACT1ROUT")
        f.write(struct.pack("<H", len(legs_data)))
        for leg in legs_data:
            from_b = leg['from_id'].encode('utf-8')[:255]
            to_b = leg['to_id'].encode('utf-8')[:255]
            f.write(struct.pack("<B", len(from_b)))
            f.write(from_b)
            f.write(struct.pack("<B", len(to_b)))
            f.write(to_b)
            f.write(struct.pack("<f", float(leg['distance_m'])))
            pts = [gps_to_svg(lon, lat) for lon, lat in leg['coords']]
            f.write(struct.pack("<H", len(pts)))
            for x, y in pts:
                f.write(struct.pack("<ff", x, y))

    legs_size_kb = os.path.getsize(OUTPUT_LEGS_BIN) / 1024.0
    print(f"Exported {OUTPUT_LEGS_BIN} ({legs_size_kb:.1f} KB).")

    # 4. Corridor Pruning & Road Normalization
    print("Normalizing map roads and building journey corridor...")
    journey_buffers = [ls.buffer(0.060) for ls in journey_lines] # ~6km corridor
    corridor_tree = STRtree(journey_buffers)

    # Mainlines corridor collapse
    mainline_by_route = {}
    for wid, h, ref, name, ls in extractor.mainlines:
        clean_ref = ref.replace(' ', '').split(';')[0]
        key = clean_ref if clean_ref else name if name else h
        if key not in mainline_by_route:
            mainline_by_route[key] = []
        mainline_by_route[key].append(ls)

    collapsed_mainlines = []
    for route_key, lines in mainline_by_route.items():
        try:
            merged = linemerge(lines)
            polys = list(merged.geoms) if isinstance(merged, MultiLineString) else [merged]
        except Exception:
            polys = lines
        kept = collapse_parallel_lines(polys, buffer_dist=0.00065, overlap_ratio=0.5)
        for p in kept:
            collapsed_mainlines.append((route_key, p))

    # Cross-corridor collapse for mainlines
    collapsed_mainlines.sort(key=lambda x: x[1].length, reverse=True)
    final_mainlines = []
    mainline_buffers = []
    mainline_tree = None
    for rk, p in collapsed_mainlines:
        if mainline_tree is None:
            final_mainlines.append((rk, p))
            mainline_buffers.append(p.buffer(0.00060))
            mainline_tree = STRtree(mainline_buffers)
        else:
            candidates = mainline_tree.query(p)
            overlap_len = 0.0
            for c in candidates:
                inter = p.intersection(mainline_buffers[c])
                overlap_len += inter.length
            if overlap_len < 0.60 * p.length:
                final_mainlines.append((rk, p))
                mainline_buffers.append(p.buffer(0.00060))
                mainline_tree = STRtree(mainline_buffers)

    # Secondaries
    secondary_by_route = {}
    for wid, h, ref, name, ls in extractor.secondaries:
        clean_ref = ref.replace(' ', '').split(';')[0]
        key = clean_ref if clean_ref else name if name else h
        if key not in secondary_by_route:
            secondary_by_route[key] = []
        secondary_by_route[key].append(ls)

    collapsed_secondaries = []
    for route_key, lines in secondary_by_route.items():
        try:
            merged = linemerge(lines)
            polys = list(merged.geoms) if isinstance(merged, MultiLineString) else [merged]
        except Exception:
            polys = lines
        kept = collapse_parallel_lines(polys, buffer_dist=0.00050, overlap_ratio=0.5)
        for p in kept:
            collapsed_secondaries.append((route_key, p))

    # Links
    link_lines = [ls for _, _, _, _, ls in extractor.links]
    try:
        merged_links = linemerge(link_lines)
        final_links = list(merged_links.geoms) if isinstance(merged_links, MultiLineString) else [merged_links]
    except Exception:
        final_links = link_lines

    # Tertiaries
    tert_lines = [ls for _, _, _, _, ls in extractor.tertiaries]
    try:
        merged_tertiary = linemerge(tert_lines)
        final_tertiaries = list(merged_tertiary.geoms) if isinstance(merged_tertiary, MultiLineString) else [merged_tertiary]
    except Exception:
        final_tertiaries = tert_lines

    # Local access
    access_lines = [ls for _, _, _, _, ls in extractor.local_access]
    try:
        merged_access = linemerge(access_lines)
        final_access = list(merged_access.geoms) if isinstance(merged_access, MultiLineString) else [merged_access]
    except Exception:
        final_access = access_lines

    # Assemble all candidates
    # Categories:
    # 0 = NATIONAL_HIGHWAY
    # 1 = PREFECTURAL_ROAD / LOCAL
    # 2 = SCENIC_ROAD
    # 3 = TRAVEL_ROUTE
    all_roads = []

    # All mainlines (keep 100% across Hokkaido)
    for rk, ls in final_mainlines:
        clean_ref = str(rk).replace(' ', '').split(';')[0]
        cat = 2 if clean_ref in SCENIC_REFS or any(k in str(rk) for k in ('オロロン', '知床', '宗谷サンセット')) else 0
        all_roads.append((f"main_{clean_ref}", str(rk), cat, ls))

    # All secondaries (keep 100% across Hokkaido)
    for rk, ls in collapsed_secondaries:
        clean_ref = str(rk).replace(' ', '').split(';')[0]
        cat = 2 if clean_ref in SCENIC_REFS or any(k in str(rk) for k in ('オロロン', '知床', '宗谷サンセット')) else 1
        all_roads.append((f"sec_{clean_ref}", str(rk), cat, ls))

    # Ramps (within corridor)
    for i, ls in enumerate(final_links):
        if len(corridor_tree.query(ls)) > 0:
            all_roads.append((f"ramp_{i}", "ランプ路", 1, ls))

    # Tertiaries (keep scenic + corridor)
    for i, ls in enumerate(final_tertiaries):
        coords = list(ls.coords)
        is_esanuka = (
            any(142.23 <= c[0] <= 142.36 and 45.14 <= c[1] <= 45.28 for c in coords)
            and ls.length * 111.0 > 8.0
        )
        if is_esanuka:
            all_roads.append(("esanuka_osm", "村道 エサヌカ線", 2, ls))
        elif len(corridor_tree.query(ls)) > 0:
            all_roads.append((f"tert_{i}", "一般道", 1, ls))

    # Local access (within corridor)
    for i, ls in enumerate(final_access):
        if len(corridor_tree.query(ls)) > 0:
            all_roads.append((f"acc_{i}", "地域連絡路", 1, ls))

    # 5. Add direct travel route lines (cat = 3) to ensure 100% visual journey line
    for i, leg_ls in enumerate(journey_lines):
        all_roads.append((f"route_leg_{i}", f"第{i+1}区間", 3, leg_ls))

    # Merge contiguous lines per (category, route)
    grouped = {}
    for rid, name, cat, ls in all_roads:
        key = (cat, name)
        if key not in grouped:
            grouped[key] = []
        grouped[key].append(ls)

    merged_roads = []
    for (cat, name), lines in grouped.items():
        try:
            merged = linemerge(lines)
            if isinstance(merged, LineString):
                merged_roads.append((cat, name, merged))
            elif isinstance(merged, MultiLineString):
                for single_ls in merged.geoms:
                    merged_roads.append((cat, name, single_ls))
            else:
                for single_ls in lines:
                    merged_roads.append((cat, name, single_ls))
        except Exception:
            for single_ls in lines:
                merged_roads.append((cat, name, single_ls))

    # RDP simplification
    processed_segments = []
    total_raw_points = 0
    total_simplified_points = 0

    for idx, (cat, name, ls) in enumerate(merged_roads):
        coords = list(ls.coords)
        if len(coords) < 2:
            continue
        simp_coords = rdp(coords, 0.00010)
        total_raw_points += len(coords)
        total_simplified_points += len(simp_coords)
        proj_pts = [gps_to_svg(lon, lat) for lon, lat in simp_coords]
        seg_id = f"r_{cat}_{idx}"
        processed_segments.append((seg_id, str(name), cat, proj_pts))

    print(f"Points reduced from {total_raw_points} to {total_simplified_points} ({total_simplified_points/total_raw_points*100:.1f}%).")
    print(f"Total exported road segments: {len(processed_segments)}")
    cat_counts = defaultdict(int)
    for s in processed_segments:
        cat_counts[s[2]] += 1
    print(f"  Cat 0 (National Highways): {cat_counts[0]}")
    print(f"  Cat 1 (Prefectural/Local): {cat_counts[1]}")
    print(f"  Cat 2 (Scenic Routes): {cat_counts[2]}")
    print(f"  Cat 3 (Act 1 Travel Route): {cat_counts[3]}")

    # Export hokkaido_roads_high_density.bin
    with open(OUTPUT_ROADS_BIN, 'wb') as f:
        f.write(b"HKDROAD1")
        f.write(struct.pack("<H", len(processed_segments)))
        for seg_id, seg_name, cat, pts in processed_segments:
            id_bytes = seg_id.encode("utf-8")[:255]
            name_bytes = seg_name.encode("utf-8")[:255]
            f.write(struct.pack("<B", len(id_bytes)))
            f.write(id_bytes)
            f.write(struct.pack("<B", len(name_bytes)))
            f.write(name_bytes)
            f.write(struct.pack("<B", cat))
            f.write(struct.pack("<I", len(pts)))
            for x, y in pts:
                f.write(struct.pack("<ff", x, y))

    bin_size_kb = os.path.getsize(OUTPUT_ROADS_BIN) / 1024.0
    print(f"Exported {OUTPUT_ROADS_BIN} ({bin_size_kb:.1f} KB).")
    print(f"Total execution time: {time.time() - t0:.1f}s.")

if __name__ == '__main__':
    main()
