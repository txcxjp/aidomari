#!/usr/bin/env python3
"""
Rebuild hokkaido_roads_high_density.bin using the calibrated accurate TPS engine.
Inverts legacy road points to true OSM GPS (lon, lat) via Newton-Raphson,
then reprojects through calibrated_transform to exact SVG geography.
"""

import math
import struct
import sys
import os

from accurate_tps import calibrated_transform

BIN_FILE = "/home/txjp/dev/aidomari/app/src/main/assets/hokkaido_roads_high_density.bin"

# 1. Load legacy TPS from build_and_export_roads_bin.py to accurately invert
with open('/home/txjp/dev/aidomari/scripts/build_and_export_roads_bin.py') as f:
    lines = f.readlines()

tps_lines = []
for line in lines:
    if line.startswith('def convert_road') or line.startswith('def main'):
        break
    tps_lines.append(line)
exec(''.join(tps_lines))

def invert_legacy_tps(target_x, target_y, guess_lon=142.0, guess_lat=43.5, max_iter=25, tol=1e-5):
    lon, lat = guess_lon, guess_lat
    h = 1e-5
    for _ in range(max_iter):
        x, y = tps_transform(lon, lat)
        err_x = x - target_x
        err_y = y - target_y
        if math.hypot(err_x, err_y) < tol:
            return lon, lat

        x_lon, y_lon = tps_transform(lon + h, lat)
        x_lat, y_lat = tps_transform(lon, lat + h)

        j11 = (x_lon - x) / h
        j12 = (x_lat - x) / h
        j21 = (y_lon - y) / h
        j22 = (y_lat - y) / h

        det = j11 * j22 - j12 * j21
        if abs(det) < 1e-12:
            break

        d_lon = (j22 * err_x - j12 * err_y) / det
        d_lat = (-j21 * err_x + j11 * err_y) / det

        lon -= d_lon
        lat -= d_lat
    return lon, lat

def main():
    print(f"Reading legacy binary: {BIN_FILE}")
    with open(BIN_FILE, 'rb') as f:
        data = f.read()

    magic = data[:8]
    if magic != b"HKDROAD1":
        print(f"Invalid magic: {magic}")
        sys.exit(1)

    num_segs = struct.unpack('<H', data[8:10])[0]
    print(f"Found {num_segs} segments.")

    offset = 10
    recalibrated_segments = []
    total_pts = 0

    last_lon, last_lat = 142.0, 43.5

    for s_idx in range(num_segs):
        id_len = data[offset]; offset += 1
        seg_id = data[offset:offset+id_len].decode('utf-8', errors='ignore'); offset += id_len
        name_len = data[offset]; offset += 1
        name = data[offset:offset+name_len].decode('utf-8', errors='ignore'); offset += name_len
        cat = data[offset]; offset += 1
        num_pts = struct.unpack('<I', data[offset:offset+4])[0]; offset += 4

        new_pts = []
        for _ in range(num_pts):
            old_x, old_y = struct.unpack('<ff', data[offset:offset+8]); offset += 8
            # Invert to true GPS
            lon, lat = invert_legacy_tps(old_x, old_y, guess_lon=last_lon, guess_lat=last_lat)
            last_lon, last_lat = lon, lat
            # Reproject using calibrated accurate TPS
            new_x, new_y = calibrated_transform(lon, lat)
            new_pts.append((new_x, new_y))

        recalibrated_segments.append((seg_id, name, cat, new_pts))
        total_pts += len(new_pts)

        if (s_idx + 1) % 50 == 0 or s_idx + 1 == num_segs:
            print(f"  Processed {s_idx + 1}/{num_segs} segments ({total_pts} points)...")

    # Write out newly calibrated binary
    print(f"Writing calibrated binary to {BIN_FILE}...")
    with open(BIN_FILE, 'wb') as f:
        f.write(b"HKDROAD1")
        f.write(struct.pack("<H", len(recalibrated_segments)))

        for seg_id, name, cat, line in recalibrated_segments:
            id_bytes = seg_id.encode("utf-8")
            name_bytes = name.encode("utf-8")

            f.write(struct.pack("<B", len(id_bytes)))
            f.write(id_bytes)
            f.write(struct.pack("<B", len(name_bytes)))
            f.write(name_bytes)
            f.write(struct.pack("<B", cat))
            f.write(struct.pack("<I", len(line)))

            for x, y in line:
                f.write(struct.pack("<ff", float(x), float(y)))

    print(f"Successfully recalibrated all {num_segs} road segments and {total_pts} points!")

if __name__ == '__main__':
    main()
