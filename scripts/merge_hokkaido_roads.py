#!/usr/bin/env python3
"""
Merge missing key coastal routes into Hokkaido roads network:
- 道道254号（抜海港稚内線／宗谷サンセットロード・ノシャップ岬）
- 道道913号（野塚婦美線・積丹半島先端・神威岬・積丹岬）
- 道道76号（網走公園線・能取岬）
- 道道93号（知床公園線・知床五湖）
- 道道9号（寿都黒松内線）
- 猿払村道浜猿払エサヌカ線（エサヌカ直線道路）
Outputs: /tmp/hokkaido_build/hokkaido_roads_complete.json
"""

import gzip
import json
import math

INPUT_ROADS = "/tmp/hokkaido_build/hokkaido_roads_raw.json"
OUTPUT_ROADS = "/tmp/hokkaido_build/hokkaido_roads_complete.json"
ANCHORS_FILE = "/home/txjp/dev/aidomari/scripts/anchors_master.json"

def haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = math.sin(dlat/2)**2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon/2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

def main():
    with open(INPUT_ROADS, 'r', encoding='utf-8') as f:
        existing_roads = json.load(f)
    print(f"Loaded {len(existing_roads)} existing roads.")

    # 1. Extract missing secondary/primary routes from OSM gz files
    needed_routes = [
        ('254', '抜海港線', '道道254号（抜海港稚内線・宗谷サンセットロード）'),
        ('913', '野塚婦美線', '道道913号（野塚婦美線・積丹半島先端）'),
        ('76', '網走公園線', '道道76号（網走公園線・能取岬）'),
        ('93', '知床公園線', '道道93号（知床公園線・知床五湖）'),
        ('9', '寿都黒松内線', '道道9号（寿都黒松内線）'),
    ]

    extracted_routes = []
    for fn in ['/tmp/hokkaido_build/osm_secondary.geojson.gz', '/tmp/hokkaido_build/osm_primary.geojson.gz']:
        with gzip.open(fn, 'rt') as f:
            data = json.load(f)
            for feat in data.get('features', []):
                props = feat.get('properties', {})
                geom = feat.get('geometry', {})
                ref = str(props.get('ref', ''))
                name = str(props.get('name', ''))
                coords = geom.get('coordinates', [])
                if not coords: continue
                
                pt = coords[0] if isinstance(coords[0][0], (int, float)) else coords[0][0]
                if not (41.3 < pt[1] < 45.6 and 139.5 < pt[0] < 145.5):
                    continue

                for target_ref, target_name, label in needed_routes:
                    if (target_ref == ref or target_ref in ref.split(';')) or (target_name in name):
                        extracted_routes.append({
                            'type': 'Feature',
                            'properties': {
                                'ref': target_ref,
                                'name': target_name,
                                'label': label,
                                'highway': props.get('highway', 'secondary')
                            },
                            'geometry': geom
                        })
                        break

    print(f"Extracted {len(extracted_routes)} missing road segments from OSM.")

    # 2. Add Esanuka line (famous straight coastal line in Sarufutsu)
    # Coordinates along Sarufutsu coast connecting National Route 238 to Hama-Sarufutsu
    esanuka_coords = [
        [142.3061, 45.1954], # South junction with R238 (near Hamatonbetsu)
        [142.2980, 45.2050],
        [142.2850, 45.2200], # Start of main dead-straight section
        [142.2680, 45.2400],
        [142.2510, 45.2550],
        [142.2371, 45.2626], # Hama-Sarufutsu settlement
        [142.2150, 45.2800], # Second straight section
        [142.1900, 45.2950],
        [142.1700, 45.3100], # North junction with R238 near Asajino
    ]
    esanuka_feature = {
        'type': 'Feature',
        'properties': {
            'ref': '村道',
            'name': '猿払村道浜猿払エサヌカ線',
            'label': '村道エサヌカ線',
            'highway': 'unclassified'
        },
        'geometry': {
            'type': 'LineString',
            'coordinates': esanuka_coords
        }
    }
    extracted_routes.append(esanuka_feature)
    print("Added Esanuka line.")

    # Combine all
    all_roads = existing_roads + extracted_routes
    with open(OUTPUT_ROADS, 'w', encoding='utf-8') as f:
        json.dump(all_roads, f, ensure_ascii=False, indent=2)

    print(f"Saved complete roads network ({len(all_roads)} roads) to {OUTPUT_ROADS}.")

    # 3. Re-verify distance to all 201 anchors
    with open(ANCHORS_FILE, 'r', encoding='utf-8') as f:
        anchors = json.load(f)

    road_lines = []
    for r in all_roads:
        geom = r.get('geometry', {})
        coords = geom.get('coordinates', [])
        if geom.get('type') == 'LineString':
            road_lines.append(coords)
        elif geom.get('type') == 'MultiLineString':
            for l in coords:
                road_lines.append(l)

    # Check key anchors
    test_steps = [470, 500, 1140, 1150, 1162, 1250, 1700, 1890, 1999]
    print("\n--- Key Scenic Anchors Distance to Completed Roads ---")
    for a in anchors:
        if a['stepIndex'] in test_steps:
            alat, alon = a['lat'], a['lon']
            min_d = 999999.0
            for line in road_lines:
                for pt in line:
                    if abs(pt[1] - alat) > 0.1 or abs(pt[0] - alon) > 0.1: continue
                    d = haversine(alat, alon, pt[1], pt[0])
                    if d < min_d: min_d = d
            print(f"Step {a['stepIndex']:4d} | {a['municipality']:10s} | {a['name']:25s} | Distance to road: {min_d*1000:6.1f} meters")

if __name__ == '__main__':
    main()
