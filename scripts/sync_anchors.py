#!/usr/bin/env python3
"""
Sync anchors_master.json with the rectified HokkaidoAct1Route.kt nodes.
Ensures every anchor's stepIndex, name, municipality, and type match exactly.
"""

import json
import re

ROUTE_FILE = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"
ANCHORS_FILE = "/home/txjp/dev/aidomari/scripts/anchors_master.json"

with open(ROUTE_FILE, 'r', encoding='utf-8') as f:
    route_text = f.read()

# Parse all 2,000 route nodes
nodes = {}
pattern = re.compile(
    r'SugorokuNode\(\s*id\s*=\s*\"[^\"]+\",\s*stepIndex\s*=\s*(\d+),\s*name\s*=\s*\"([^\"]+)\",\s*municipality\s*=\s*\"([^\"]+)\",\s*type\s*=\s*SugorokuNodeType\.([A-Z_]+),.*?roadName\s*=\s*\"([^\"]+)\"',
    re.DOTALL
)
for m in pattern.finditer(route_text):
    step = int(m.group(1))
    nodes[step] = {
        'name': m.group(2),
        'muni': m.group(3),
        'type': m.group(4),
        'road': m.group(5),
    }

with open(ANCHORS_FILE, 'r', encoding='utf-8') as f:
    anchors = json.load(f)

# Specific manual anchor mappings for rectified Act 1 points
KEY_ANCHORS = {
    690: {"name": "雄冬岬（雄冬冷水トンネル）", "lat": 43.7474, "lon": 141.3494, "road": "国道231号（オロロンライン）"},
    855: {"name": "道の駅風Wとままえ（ふわっと）", "lat": 44.3120, "lon": 141.6560, "road": "国道232号（オロロンライン）"},
    866: {"name": "三毛別羆事件復元地", "lat": 44.144955, "lon": 141.843628, "road": "道道1049号（苫前小平線）"},
    1162: {"name": "ノシャップ岬（野寒布岬）", "lat": 45.4403, "lon": 141.6458, "road": "道道254号"},
}

for a in anchors:
    s = a['stepIndex']
    if s in KEY_ANCHORS:
        info = KEY_ANCHORS[s]
        a['name'] = nodes[s]['name']
        a['municipality'] = nodes[s]['muni']
        a['type'] = nodes[s]['type']
        a['roadName'] = nodes[s]['road']
        a['lat'] = info['lat']
        a['lon'] = info['lon']
        a['source'] = 'exact_landmark'
    elif s in nodes:
        a['name'] = nodes[s]['name']
        a['municipality'] = nodes[s]['muni']
        a['type'] = nodes[s]['type']
        a['roadName'] = nodes[s]['road']

with open(ANCHORS_FILE, 'w', encoding='utf-8') as f:
    json.dump(anchors, f, ensure_ascii=False, indent=2)

print(f"Successfully synced {len(anchors)} anchors in anchors_master.json!")
