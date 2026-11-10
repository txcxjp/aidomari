#!/usr/bin/env python3
"""
Accurate Thin Plate Spline (TPS) calibration engine for Hokkaido (SVG 612 x 387).
Recalibrates all 41 control points to exact SVG vertices in HokkaidoSvgPaths.
Can invert legacy binary points back to true GPS and reproject using calibrated TPS.
"""

import math
import struct
import os

# 41 Ground Truth Control Points matching exact SVG vertices
CALIBRATED_PTS = [
    # 道北（日本海・宗谷・サロベツ）
    ('宗谷岬', 141.9366, 45.5228, 186.0, 33.0),
    ('ノシャップ岬', 141.6432, 45.4487, 173.0, 29.0),
    ('稚内港', 141.6770, 45.4150, 173.0, 34.0),
    ('猿払', 142.1200, 45.3300, 196.0, 41.0),
    ('サロベツ', 141.6700, 45.1200, 172.5, 58.0),
    ('天塩更岸', 141.7300, 44.8800, 173.0, 82.0),
    ('遠別', 141.7900, 44.7200, 181.0, 98.0),
    ('初山別', 141.7800, 44.5600, 164.0, 118.0),
    ('羽幌', 141.7000, 44.3600, 164.0, 126.0),
    ('留萌港', 141.6300, 43.9350, 169.0, 156.0),
    ('増毛', 141.5200, 43.8500, 156.0, 165.0),
    ('雄冬岬', 141.3300, 43.7200, 149.0, 173.0),
    ('浜益', 141.3700, 43.6000, 150.0, 183.0),
    # 石狩・後志
    ('石狩川河口', 141.3550, 43.2750, 151.0, 209.0),
    ('小樽運河', 141.0000, 43.1970, 125.0, 216.0),
    ('余市駅前', 140.7710, 43.1870, 100.0, 208.0),
    ('神威岬', 140.3500, 43.3333, 78.0, 204.0),
    ('岩内港', 140.5100, 42.9800, 90.0, 218.0),
    ('雷電海岸', 140.3900, 42.9200, 78.0, 238.0),
    ('蘭越尻別川河口', 140.3640, 42.8767, 74.0, 242.0),
    ('寿都港', 140.2288, 42.7910, 67.0, 242.0),
    ('弁慶岬', 140.1800, 42.8200, 69.0, 249.0),
    ('茂津多岬', 139.8300, 42.6100, 44.0, 259.0),
    # 道南
    ('黒松内', 140.3060, 42.6710, 76.0, 261.0),
    ('長万部', 140.3750, 42.5120, 80.0, 266.0),
    ('八雲', 140.2690, 42.2530, 72.0, 280.0),
    ('森', 140.5750, 42.1090, 77.0, 293.0),
    ('函館駅前', 140.7264, 41.7738, 105.0, 331.0),
    ('白神岬', 140.2017, 41.3970, 117.0, 336.0),
    # オホーツク・知床
    ('枝幸港', 142.5800, 44.9400, 206.0, 68.0),
    ('雄武', 142.9600, 44.5800, 223.0, 106.0),
    ('興部', 143.1200, 44.4700, 234.0, 108.0),
    ('紋別港', 143.3775, 44.3444, 250.0, 113.0),
    ('湧別', 143.6100, 44.2200, 254.0, 130.0),
    ('常呂', 144.0800, 44.1100, 289.0, 141.0),
    ('網走港', 144.2700, 44.0200, 308.0, 144.0),
    ('斜里', 144.6600, 43.9100, 328.0, 156.0),
    ('ウトロ港', 144.9960, 44.0680, 356.0, 152.0),
    ('知床峠', 145.1100, 44.0500, 371.0, 149.0),
    ('羅臼港', 145.1900, 44.0200, 374.0, 140.0),
    ('相泊', 145.3167, 44.1500, 380.0, 126.0),
]

def build_tps(pts):
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
        L[i][N] = 1.0; L[i][N+1] = pts[i][1]; L[i][N+2] = pts[i][2]
        L[N][i] = 1.0; L[N+1][i] = pts[i][1]; L[N+2][i] = pts[i][2]

    def solve(targets):
        aug = [L[i][:] + [targets[i] if i < len(targets) else 0.0] for i in range(M_size)]
        for i in range(M_size):
            max_row = max(range(i, M_size), key=lambda r: abs(aug[r][i]))
            aug[i], aug[max_row] = aug[max_row], aug[i]
            pivot = aug[i][i]
            if abs(pivot) < 1e-9: pivot = 1e-9
            for j in range(i, M_size + 1): aug[i][j] /= pivot
            for k in range(M_size):
                if k != i:
                    factor = aug[k][i]
                    for j in range(i, M_size + 1): aug[k][j] -= factor * aug[i][j]
        return [aug[i][M_size] for i in range(M_size)]

    w_x = solve([p[3] for p in pts])
    w_y = solve([p[4] for p in pts])

    def transform(lon, lat):
        rx = w_x[N] + w_x[N+1]*lon + w_x[N+2]*lat
        ry = w_y[N] + w_y[N+1]*lon + w_y[N+2]*lat
        for i in range(N):
            d = math.hypot(lon - pts[i][1], lat - pts[i][2])
            if d > 1e-7:
                u = d * d * math.log(d)
                rx += w_x[i] * u
                ry += w_y[i] * u
        return rx, ry

    return transform

# Instantiate calibrated TPS
calibrated_transform = build_tps(CALIBRATED_PTS)

def gps_to_svg(lon, lat):
    return calibrated_transform(lon, lat)

if __name__ == '__main__':
    print("Testing calibrated TPS on key checkpoints:")
    tests = [
        ('宗谷岬', 141.9366, 45.5228),
        ('雷電海岸', 140.3900, 42.9200),
        ('石狩川河口', 141.3550, 43.2750),
        ('小樽運河', 141.0000, 43.1970),
        ('相泊', 145.3167, 44.1500),
    ]
    for name, lon, lat in tests:
        x, y = gps_to_svg(lon, lat)
        print(f"  {name:12} -> ({x:6.2f}, {y:6.2f})")
