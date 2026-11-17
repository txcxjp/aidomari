#!/usr/bin/env python3
"""
Build high-fidelity hokkaido_roads_high_density.bin directly from pure OSM GPS GeoJSON/JSON
using Pure Conformal Mercator Map Projection.
Completely discards legacy TPS and binary files to ensure 100% distortion-free highway rendering.
"""

import json
import struct
import math
import sys
import os

sys.path.append('/home/txjp/dev/aidomari/scripts')
from mercator_projection import gps_to_svg

RAW_JSON = "/home/txjp/dev/aidomari/scripts/osm_hokkaido_raw_roads.json"
OUTPUT_BIN = "/home/txjp/dev/aidomari/app/src/main/assets/hokkaido_roads_high_density.bin"

CATEGORY_MAP = {
    '5': 0,    # NATIONAL_HIGHWAY
    '229': 0,
    '231': 0,
    '232': 0,
    '238': 0,
    '244': 0,
    '334': 2,  # SCENIC_ROAD (Shiretoko Pass)
    '9': 1,    # PREFECTURAL_ROAD
    '106': 2,  # SCENIC_ROAD (Sarobetsu / Ororon Line)
    '254': 2,  # SCENIC_ROAD (Soya Sunset Road / Noshappu)
    '87': 2,   # SCENIC_ROAD (Shiretoko Cape Aidomari Line)
}

ROAD_NAME_MAP = {
    '5': "国道5号",
    '9': "道道9号（寿都黒松内線）",
    '229': "国道229号（追分ソーランライン）",
    '231': "国道231号（オロロンライン）",
    '232': "国道232号（オロロンライン）",
    '106': "道道106号（稚内天塩線・オロロンライン）",
    '254': "道道254号（抜海港稚内線・宗谷サンセットロード）",
    '238': "国道238号（オホーツクライン）",
    '244': "国道244号",
    '334': "国道334号（知床横断道路）",
    '87': "道道87号（知床公園羅臼線）",
}

def main():
    print(f"Loading raw OSM roads: {RAW_JSON}")
    with open(RAW_JSON, 'r', encoding='utf-8') as f:
        roads = json.load(f)

    all_segments = []
    total_pts = 0

    for ref, ways in roads.items():
        cat = CATEGORY_MAP.get(ref, 0)
        name = ROAD_NAME_MAP.get(ref, f"国道{ref}号")

        for w_idx, way in enumerate(ways):
            if len(way) < 2:
                continue

            proj_pts = []
            for lon, lat in way:
                sx, sy = gps_to_svg(lon, lat)
                proj_pts.append((sx, sy))

            seg_id = f"osm_road_{ref}_{w_idx}"
            seg_name = f"{name} #{w_idx+1}"
            all_segments.append((seg_id, seg_name, cat, proj_pts))
            total_pts += len(proj_pts)

    print(f"Total exported road segments: {len(all_segments)}")
    print(f"Total Mercator projected road points: {total_pts}")

    # Write out clean binary asset
    os.makedirs(os.path.dirname(OUTPUT_BIN), exist_ok=True)
    with open(OUTPUT_BIN, 'wb') as f:
        # Header: Magic "HKDROAD1"
        f.write(b"HKDROAD1")
        # uint16 number of segments
        f.write(struct.pack("<H", len(all_segments)))

        for seg_id, seg_name, cat, line in all_segments:
            id_bytes = seg_id.encode("utf-8")
            name_bytes = seg_name.encode("utf-8")

            # id_len (uint8), id
            f.write(struct.pack("<B", len(id_bytes)))
            f.write(id_bytes)

            # name_len (uint8), name
            f.write(struct.pack("<B", len(name_bytes)))
            f.write(name_bytes)

            # category (uint8)
            f.write(struct.pack("<B", cat))

            # num_points (uint32)
            f.write(struct.pack("<I", len(line)))

            # points (float32 x, float32 y)
            for x, y in line:
                f.write(struct.pack("<ff", float(x), float(y)))

    size_kb = os.path.getsize(OUTPUT_BIN) / 1024
    print(f"Successfully generated pure Mercator road binary: {OUTPUT_BIN} ({size_kb:.1f} KB).")

if __name__ == '__main__':
    main()
