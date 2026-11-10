import json
import re
import sys

sys.path.append('/home/txjp/dev/aidomari/scripts')
from accurate_tps import gps_to_svg

# 1. Load GSI Mainland Polygon (3,201 points)
with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/ui/sugoroku/HokkaidoSvgPaths.kt') as f:
    text = f.read()

m = re.search('MAINLAND_D = \"([^\"]+)\"', text)
d = m.group(1)
poly = []
for part in d.replace('M', '').replace('Z', '').split('L'):
    part = part.strip()
    if part:
        x, y = part.split(',')
        poly.append((float(x), float(y)))

def point_in_poly(x, y, p):
    n = len(p)
    inside = False
    p1x, p1y = p[0]
    for i in range(1, n + 1):
        p2x, p2y = p[i % n]
        if y > min(p1y, p2y):
            if y <= max(p1y, p2y):
                if x <= max(p1x, p2x):
                    if p1y != p2y:
                        xinters = (y - p1y) * (p2x - p1x) / (p2y - p1y) + p1x
                    if p1x == p2x or x <= xinters:
                        inside = not inside
        p1x, p1y = p2x, p2y
    return inside

def dist_to_segment(px, py, x1, y1, x2, y2):
    dx = x2 - x1
    dy = y2 - y1
    if dx == 0 and dy == 0:
        return ((px - x1)**2 + (py - y1)**2)**0.5
    t = max(0.0, min(1.0, ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)))
    proj_x = x1 + t * dx
    proj_y = y1 + t * dy
    return ((px - proj_x)**2 + (py - proj_y)**2)**0.5

def min_dist_to_polygon_border(px, py, p):
    min_d = 1e9
    for i in range(len(p)):
        x1, y1 = p[i]
        x2, y2 = p[(i + 1) % len(p)]
        d = dist_to_segment(px, py, x1, y1, x2, y2)
        if d < min_d: min_d = d
    return min_d

# 2. Load all 2,000 nodes from HokkaidoAct1Route.kt
with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt') as f:
    route_kt = f.read()

node_blocks = re.findall(r'SugorokuNode\((.*?)\)', route_kt, re.DOTALL)
nodes = []
for block in node_blocks:
    step_m = re.search(r'stepIndex\s*=\s*(\d+)', block)
    name_m = re.search(r'name\s*=\s*"([^"]+)"', block)
    muni_m = re.search(r'municipality\s*=\s*"([^"]+)"', block)
    x_m = re.search(r'x\s*=\s*([0-9.]+)f', block)
    y_m = re.search(r'y\s*=\s*([0-9.]+)f', block)
    road_m = re.search(r'roadName\s*=\s*"([^"]+)"', block)
    if step_m and name_m and x_m and y_m:
        nodes.append({
            'step': int(step_m.group(1)),
            'name': name_m.group(1),
            'muni': muni_m.group(1) if muni_m else '',
            'road': road_m.group(1) if road_m else '',
            'x': float(x_m.group(1)),
            'y': float(y_m.group(1)),
        })

print(f"Total nodes loaded: {len(nodes)}")

# 3. Check sea status for all 2,000 nodes
ocean_nodes = []
land_nodes = []

for n in nodes:
    x = n['x']
    y = n['y']
    if point_in_poly(x, y, poly):
        land_nodes.append(n)
    else:
        d = min_dist_to_polygon_border(x, y, poly)
        ocean_nodes.append((n, d))

print(f"Nodes inside GSI mainland: {len(land_nodes)}")
print(f"Nodes outside GSI mainland: {len(ocean_nodes)}")

# 4. Load GSI cache and find ground-truth discrepancy
with open('/home/txjp/dev/aidomari/scripts/gsi_cache.json') as f:
    gsi_cache = json.load(f)

# Also check exact landmarks from accurate_tps.py
from accurate_tps import CALIBRATED_PTS
calibrated_dict = {p[0]: (p[1], p[2], p[3], p[4]) for p in CALIBRATED_PTS}

# Group ocean nodes by continuous sections
from collections import defaultdict
section_ocean = defaultdict(list)
for n, d in ocean_nodes:
    # categorize by step range (hundreds)
    sec_key = (n['step'] // 50) * 50
    section_ocean[sec_key].append((n, d))

print("\n=======================================================")
print("=== OCEAN-FLOATING NODES BREAKDOWN BY 50-STEP CHUNKS ===")
print("=======================================================")
for sec in sorted(section_ocean.keys()):
    nlist = section_ocean[sec]
    max_d = max(item[1] for item in nlist)
    avg_d = sum(item[1] for item in nlist) / len(nlist)
    steps = [item[0]['step'] for item in nlist]
    munis = set(item[0]['muni'] for item in nlist)
    print(f"Steps {sec:4d} ~ {sec+49:4d} ({len(nlist):2d}/50 outside): {', '.join(munis)} | max sea depth={max_d:.2f}px, avg={avg_d:.2f}px")

print("\n=======================================================")
print("=== ALL NODES WITH SIGNIFICANT SEA JUMP (> 1.0 px) ===")
print("=======================================================")
sig_ocean = [item for item in ocean_nodes if item[1] > 1.0]
print(f"Count of nodes > 1.0 px into sea: {len(sig_ocean)}")
for n, d in sorted(sig_ocean, key=lambda x: x[1], reverse=True)[:40]:
    print(f"  Step {n['step']:4d} [{n['name']:15}] ({n['muni']:10}) at ({n['x']:6.2f}, {n['y']:6.2f}) -> {d:5.2f} px into sea")

# 5. Check discrepancy for GSI-cached spots
print("\n=======================================================")
print("=== DISCREPANCY FROM GSI GROUND TRUTH (lon, lat) ===")
print("=======================================================")
gsi_discrepancies = []
for n in nodes:
    # search in gsi_cache by name
    match = None
    for k, v in gsi_cache.items():
        if n['name'] in k or k.endswith(n['name']):
            match = v
            break
    if match:
        true_x, true_y = gps_to_svg(match['lon'], match['lat'])
        diff = ((n['x'] - true_x)**2 + (n['y'] - true_y)**2)**0.5
        if diff > 0.5:
            gsi_discrepancies.append((n, match, true_x, true_y, diff))

print(f"Total geocoded spots checked: {len(gsi_discrepancies)} have diff > 0.5 px")
for n, m, tx, ty, diff in sorted(gsi_discrepancies, key=lambda x: x[4], reverse=True)[:30]:
    print(f"  Step {n['step']:4d} [{n['name']:15}] ({n['muni']:8}): current=({n['x']:6.2f}, {n['y']:6.2f}) vs GSI_true=({tx:6.2f}, {ty:6.2f}) -> diff: {diff:5.2f} px")
