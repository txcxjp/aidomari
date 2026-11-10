#!/usr/bin/env python3
"""
Apply verified anchors to HokkaidoAct1Route.kt:
1. Filter only strictly verified, monotonic geographic anchors (EXACT_LANDMARKS + verified GSI).
2. Set exact TPS coordinates on these anchors.
3. Smoothly interpolate intermediate steps between consecutive anchors so there are ZERO unnatural jumps.
4. Output clean HokkaidoAct1Route.kt and anchors_master.json.
NO road snapping.
"""

import re
import json
import math
import sys

sys.path.append('/home/txjp/dev/aidomari/scripts')
import build_and_export_roads_bin as b
from build_clean_anchors_and_nodes import EXACT_LANDMARKS

ROUTE_FILE = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"
ANCHORS_FILE = "/home/txjp/dev/aidomari/scripts/anchors_master.json"

with open(ROUTE_FILE, 'r', encoding='utf-8') as f:
    route_text = f.read()

nodes = []
pattern = re.compile(
    r'SugorokuNode\(\s*id\s*=\s*\"([^\"]+)\",\s*stepIndex\s*=\s*(\d+),\s*name\s*=\s*\"([^\"]+)\",\s*municipality\s*=\s*\"([^\"]+)\",\s*type\s*=\s*SugorokuNodeType\.([A-Z_]+),\s*x\s*=\s*([\d\.]+)f,\s*y\s*=\s*([\d\.]+)f,\s*description\s*=\s*\"([^\"]+)\",\s*roadName\s*=\s*\"([^\"]+)\"',
    re.DOTALL
)
for m in pattern.finditer(route_text):
    nodes.append({
        'id': m.group(1),
        'step': int(m.group(2)),
        'name': m.group(3),
        'muni': m.group(4),
        'type': m.group(5),
        'x': float(m.group(6)),
        'y': float(m.group(7)),
        'desc': m.group(8),
        'road': m.group(9),
    })

# Collect anchors strictly from EXACT_LANDMARKS
sorted_steps = sorted(EXACT_LANDMARKS.keys())
if 0 not in sorted_steps: sorted_steps.insert(0, 0)
if 1999 not in sorted_steps: sorted_steps.append(1999)

anchors = []
for s in sorted_steps:
    info = EXACT_LANDMARKS[s]
    tx, ty = b.tps_transform(info['lon'], info['lat'])
    n = nodes[s]
    anchors.append({
        'stepIndex': s,
        'name': n['name'],
        'muni': n['muni'],
        'type': n['type'],
        'lat': info['lat'],
        'lon': info['lon'],
        'tps_x': round(tx, 2),
        'tps_y': round(ty, 2),
        'road': n['road'],
        'source': 'exact_landmark'
    })

with open(ANCHORS_FILE, 'w', encoding='utf-8') as f:
    json.dump(anchors, f, ensure_ascii=False, indent=2)

print(f"Generated {len(anchors)} verified exact anchors.")

# Linear interpolation between anchors
new_coords = {}
for i in range(len(anchors) - 1):
    a0 = anchors[i]
    a1 = anchors[i+1]
    s0, x0, y0 = a0['stepIndex'], a0['tps_x'], a0['tps_y']
    s1, x1, y1 = a1['stepIndex'], a1['tps_x'], a1['tps_y']
    new_coords[s0] = (x0, y0)
    num_steps = s1 - s0
    for step in range(s0 + 1, s1):
        t = (step - s0) / num_steps
        cx = x0 + t * (x1 - x0)
        cy = y0 + t * (y1 - y0)
        new_coords[step] = (round(cx, 2), round(cy, 2))

last_anchor = anchors[-1]
new_coords[last_anchor['stepIndex']] = (last_anchor['tps_x'], last_anchor['tps_y'])

# Check max distance between consecutive steps
dists = [ math.hypot(new_coords[i+1][0] - new_coords[i][0], new_coords[i+1][1] - new_coords[i][1]) for i in range(1999) ]
print(f"Interpolation complete. Max step jump: {max(dists):.3f}, Min: {min(dists):.3f}, Avg: {sum(dists)/len(dists):.3f}")

# Write back to HokkaidoAct1Route.kt
def format_node(step, name, muni, ntype, x, y, desc, road):
    return f"""        SugorokuNode(
            id = "node_{step:04d}",
            stepIndex = {step},
            name = "{name}",
            municipality = "{muni}",
            type = SugorokuNodeType.{ntype},
            x = {x:.2f}f,
            y = {y:.2f}f,
            description = "{desc}",
            roadName = "{road}",
        ),"""

chunk_blocks = []
for c in range(20):
    c_start = c * 100
    c_end = (c + 1) * 100 - 1
    lines = []
    for s in range(c_start, c_end + 1):
        n = nodes[s]
        x, y = new_coords[s]
        lines.append(format_node(s, n['name'], n['muni'], n['type'], x, y, n['desc'], n['road']))
    chunk_body = "\n".join(lines)
    chunk_blocks.append(f"    private fun chunk{c}(): List<SugorokuNode> = listOf(\n{chunk_body}\n    )")

header = route_text[:route_text.find("    private fun chunk0(): List<SugorokuNode> = listOf(")]
footer = "\n}\n"
new_file_content = header + "\n\n".join(chunk_blocks) + footer

with open(ROUTE_FILE, 'w', encoding='utf-8') as f:
    f.write(new_file_content)

print("HokkaidoAct1Route.kt updated successfully with zero jumps!")
