#!/usr/bin/env python3
"""
Convert hokkaido_roads_complete.json into high-density binary asset:
app/src/main/assets/hokkaido_roads_high_density.bin
using Thin Plate Spline (TPS) transformation matching HokkaidoSvgPaths coordinate space (612 x 387).
"""

import json
import struct
import math
import os

INPUT_JSON = "/tmp/hokkaido_build/hokkaido_roads_complete.json"
OUTPUT_BIN = "/home/txjp/dev/aidomari/app/src/main/assets/hokkaido_roads_high_density.bin"

# 48 Ground truth control points for Thin Plate Spline (GPS -> SVG 612x387)
pts = [
    ('宗谷岬', 141.9366, 45.5228, 186.5, 26.0),
    ('ノシャップ岬', 141.6432, 45.4487, 170.1, 33.5),
    ('稚内港', 141.6770, 45.4150, 170.6, 35.0),
    ('神威岬', 140.3500, 43.3333, 94.2, 203.5),
    ('白神岬', 140.2017, 41.3970, 119.0, 343.0),
    ('函館駅前', 140.7264, 41.7738, 105.0, 328.0),
    ('森駅前', 140.5750, 42.1090, 76.0, 295.5),
    ('八雲駅前', 140.2690, 42.2530, 76.0, 283.0),
    ('長万部駅前', 140.3750, 42.5120, 85.0, 272.5),
    ('黒松内', 140.3060, 42.6710, 76.0, 260.0),
    ('寿都', 140.2288, 42.7910, 74.0, 251.5),
    ('茂津多岬', 139.8300, 42.6100, 66.5, 261.5),
    ('雷電海岸', 140.4000, 42.9000, 87.92, 245.11),
    ('岩内港', 140.5100, 42.9800, 88.0, 219.0),
    ('余市駅前', 140.7710, 43.1870, 101.5, 210.0),
    ('小樽運河', 141.0000, 43.1970, 125.0, 216.0),
    ('札幌時計台', 141.3533, 43.0626, 136.0, 228.0),
    ('浜益', 141.3700, 43.6000, 149.0, 182.0),
    ('雄冬岬', 141.3300, 43.7200, 149.0, 175.0),
    ('増毛', 141.5200, 43.8500, 156.45, 165.77),
    ('留萌港', 141.6300, 43.9350, 167.5, 157.0),
    ('羽幌', 141.7000, 44.3600, 171.5, 126.5),
    ('初山別', 141.7800, 44.5600, 175.0, 115.0),
    ('遠別', 141.7900, 44.7200, 177.5, 98.0),
    ('天塩更岸', 141.7300, 44.8800, 175.5, 78.0),
    ('サロベツ', 141.6700, 45.1200, 172.5, 58.0),
    ('猿払', 142.1200, 45.3300, 193.0, 38.0),
    ('枝幸港', 142.5800, 44.9400, 208.0, 70.0),
    ('雄武', 142.9600, 44.5800, 222.0, 92.0),
    ('興部', 143.1200, 44.4700, 233.0, 107.0),
    ('紋別港', 143.3775, 44.3444, 245.0, 118.0),
    ('湧別', 143.6100, 44.2200, 260.0, 127.0),
    ('常呂', 144.0800, 44.1100, 285.0, 137.0),
    ('網走港', 144.2700, 44.0200, 308.0, 144.0),
    ('斜里', 144.6600, 43.9100, 328.0, 155.0),
    ('ウトロ港', 144.9960, 44.0680, 357.9, 159.7),
    ('知床峠', 145.1100, 44.0500, 373.5, 146.2),
    ('羅臼港', 145.1900, 44.0200, 379.5, 142.8),
    ('相泊', 145.3167, 44.1500, 387.5, 132.0),
]

N = len(pts)
K = [[0.0]*N for _ in range(N)]
for i in range(N):
    for j in range(N):
        if i != j:
            d = math.hypot(pts[i][1] - pts[j][1], pts[i][2] - pts[j][2])
            K[i][j] = d * d * math.log(d)

M_size = N + 3
L = [[0.0]*M_size for _ in range(M_size)]
for i in range(N):
    for j in range(N):
        L[i][j] = K[i][j]
    L[i][N] = 1.0
    L[i][N+1] = pts[i][1]
    L[i][N+2] = pts[i][2]
    L[N][i] = 1.0
    L[N+1][i] = pts[i][1]
    L[N+2][i] = pts[i][2]

def solve_system(L, targets):
    size = len(L)
    b = targets + [0.0, 0.0, 0.0]
    aug = [L[i][:] + [b[i]] for i in range(size)]
    for i in range(size):
        max_row = max(range(i, size), key=lambda r: abs(aug[r][i]))
        aug[i], aug[max_row] = aug[max_row], aug[i]
        pivot = aug[i][i]
        if abs(pivot) < 1e-9:
            pivot = 1e-9
        for j in range(i, size + 1):
            aug[i][j] /= pivot
        for k in range(size):
            if k != i:
                factor = aug[k][i]
                for j in range(i, size + 1):
                    aug[k][j] -= factor * aug[i][j]
    return [aug[i][size] for i in range(size)]

w_x = solve_system(L, [p[3] for p in pts])
w_y = solve_system(L, [p[4] for p in pts])

def tps_transform(lon, lat):
    res_x = w_x[N] + w_x[N+1]*lon + w_x[N+2]*lat
    res_y = w_y[N] + w_y[N+1]*lon + w_y[N+2]*lat
    for i in range(N):
        d = math.hypot(lon - pts[i][1], lat - pts[i][2])
        if d > 1e-7:
            u = d * d * math.log(d)
            res_x += w_x[i] * u
            res_y += w_y[i] * u
    return res_x, res_y

def get_category(ref, name):
    r_str = str(ref)
    n_str = str(name)
    if 'エサヌカ' in n_str or '334' in r_str or 'サンセット' in n_str or 'オロロン' in n_str:
        return 2 # SCENIC_ROAD
    if any(r in r_str for r in ['5', '40', '228', '229', '231', '232', '238', '244']):
        return 0 # NATIONAL_HIGHWAY
    return 1 # PREFECTURAL_ROAD

def main():
    with open(INPUT_JSON, 'r', encoding='utf-8') as f:
        roads = json.load(f)

    print(f"Loaded {len(roads)} roads from {INPUT_JSON}.")

    all_subsegments = []
    total_pts = 0

    for idx, r in enumerate(roads):
        props = r.get('properties', {})
        geom = r.get('geometry', {})
        ref = props.get('ref', '')
        name = props.get('label', props.get('name', f"Road_{ref}"))
        cat = get_category(ref, name)

        coords = geom.get('coordinates', [])
        if not coords:
            continue

        lines = [coords] if geom.get('type') == 'LineString' else coords

        for l_idx, line in enumerate(lines):
            if len(line) < 2:
                continue

            # Project line using TPS
            proj_line = []
            for pt in line:
                px, py = tps_transform(pt[0], pt[1])
                proj_line.append((px, py))

            sub_id = f"road_{ref}_{idx}_{l_idx}"
            sub_name = f"{name} #{l_idx+1}"
            all_subsegments.append((sub_id, sub_name, cat, proj_line))
            total_pts += len(proj_line)

    print(f"Total subsegments: {len(all_subsegments)}")
    print(f"Total projected high-density points: {total_pts}")

    # Serialize to binary (HKDROAD1 format)
    os.makedirs(os.path.dirname(OUTPUT_BIN), exist_ok=True)
    with open(OUTPUT_BIN, 'wb') as f:
        # Header: Magic "HKDROAD1"
        f.write(b"HKDROAD1")
        # uint16 number of segments
        f.write(struct.pack("<H", len(all_subsegments)))

        for sub_id, sub_name, cat, line in all_subsegments:
            id_bytes = sub_id.encode("utf-8")
            name_bytes = sub_name.encode("utf-8")

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

    file_size_kb = os.path.getsize(OUTPUT_BIN) / 1024
    print(f"Successfully serialized {len(all_subsegments)} road segments to {OUTPUT_BIN} ({file_size_kb:.1f} KB).")

if __name__ == '__main__':
    main()
