#!/usr/bin/env python3
"""
Generates official Hokkaido major lake polygons from Ministry of Land, Infrastructure,
Transport and Tourism (MLIT 国土数値情報 湖沼データ W09) and adds LAKES_D to HokkaidoSvgPaths.kt.
"""

import math
import os
import struct
import sys
import zipfile

sys.path.append("/home/txjp/dev/aidomari/scripts")
from mercator_projection import gps_to_svg


def rdp(points, epsilon):
    """Ramer-Douglas-Peucker line simplification."""
    if len(points) < 3:
        return points
    start, end = points[0], points[-1]
    dx = end[0] - start[0]
    dy = end[1] - start[1]
    dist_sq = dx * dx + dy * dy
    max_d = 0.0
    index = 0
    for i in range(1, len(points) - 1):
        p = points[i]
        if dist_sq == 0:
            d = (p[0] - start[0]) ** 2 + (p[1] - start[1]) ** 2
        else:
            t = max(
                0.0,
                min(
                    1.0,
                    ((p[0] - start[0]) * dx + (p[1] - start[1]) * dy)
                    / dist_sq,
                ),
            )
            proj_x = start[0] + t * dx
            proj_y = start[1] + t * dy
            d = (p[0] - proj_x) ** 2 + (p[1] - proj_y) ** 2
        if d > max_d:
            max_d = d
            index = i
    if max_d > epsilon * epsilon:
        rec1 = rdp(points[: index + 1], epsilon)
        rec2 = rdp(points[index:], epsilon)
        return rec1[:-1] + rec2
    else:
        return [start, end]


def main():
    zip_path = os.path.join(os.path.dirname(__file__), "W09-05_GML.zip")
    fallback_zip_path = "/home/txjp/.gemini/antigravity-cli/brain/0123cff7-8778-4488-9772-5f0d65d96e1f/scratch/W09-05_GML.zip"
    if os.path.exists(fallback_zip_path):
        target_zip = fallback_zip_path
    elif os.path.exists(zip_path):
        target_zip = zip_path
    else:
        import urllib.request
        url = "https://nlftp.mlit.go.jp/ksj/gml/data/W09/W09-05/W09-05_GML.zip"
        print(f"Downloading MLIT lake dataset from {url}...")
        urllib.request.urlretrieve(url, zip_path)
        target_zip = zip_path

    with zipfile.ZipFile(target_zip) as z:
        shp_bytes = z.read("W09-05-g_Lake.shp")
        dbf_bytes = z.read("W09-05-g_Lake.dbf")

    num_records, header_len, record_len = struct.unpack(
        "<IHH", dbf_bytes[4:12]
    )
    names = []
    offset = header_len
    for r_idx in range(num_records):
        rec_data = dbf_bytes[offset : offset + record_len]
        offset += record_len
        name = rec_data[1:29].decode("cp932", errors="ignore").strip()
        muni = rec_data[29:130].decode("cp932", errors="ignore").strip()
        names.append((name, muni))

    major_lakes = [
        "サロマ湖",
        "屈斜路湖",
        "支笏湖",
        "洞爺湖",
        "能取湖",
        "網走湖",
        "涛沸湖",
        "摩周湖",
        "阿寒湖",
        "クッチャロ湖（大沼）",
        "大沼",
        "風蓮湖",
    ]

    offset = 100
    record_idx = 0
    all_subpaths = []
    total_pts = 0

    while offset < len(shp_bytes):
        rec_num, content_len = struct.unpack(">II", shp_bytes[offset : offset + 8])
        offset += 8
        rec_content = shp_bytes[offset : offset + content_len * 2]
        offset += content_len * 2

        name, muni = names[record_idx]
        record_idx += 1
        if not muni.startswith("01"):
            continue
        if name not in major_lakes:
            continue

        shape_type = struct.unpack("<I", rec_content[:4])[0]
        if shape_type != 5:
            continue

        min_x, min_y, max_x, max_y = struct.unpack("<dddd", rec_content[4:36])
        num_parts, num_points = struct.unpack("<II", rec_content[36:44])
        parts = struct.unpack(
            f"<{num_parts}I", rec_content[44 : 44 + num_parts * 4]
        )
        pts_offset = 44 + num_parts * 4

        pts = []
        for p_idx in range(num_points):
            px, py = struct.unpack(
                "<dd",
                rec_content[
                    pts_offset + p_idx * 16 : pts_offset + (p_idx + 1) * 16
                ],
            )
            pts.append((px, py))

        for p_i in range(num_parts):
            start = parts[p_i]
            end = parts[p_i + 1] if p_i + 1 < num_parts else num_points
            raw_part = pts[start:end]
            if len(raw_part) >= 15:
                simp = rdp(raw_part, 0.0003)
                svg_coords = [gps_to_svg(lon, lat) for lon, lat in simp]
                all_subpaths.append(
                    "M "
                    + " L ".join(f"{x:.2f},{y:.2f}" for x, y in svg_coords)
                    + " Z"
                )
                total_pts += len(simp)

    combined_d = " ".join(all_subpaths)
    print(
        f"Generated {len(all_subpaths)} subpaths, {total_pts} points, {len(combined_d)} characters"
    )

    kt_path = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/ui/sugoroku/HokkaidoSvgPaths.kt"
    with open(kt_path) as f:
        kt_code = f.read()

    # Append LAKES_D before closing brace
    lake_const = f"""
    /**
     * Major Hokkaido inland lakes (サロマ湖, 屈斜路湖, 支笏湖, 洞爺湖, 網走湖, 能取湖, 摩周湖, クッチャロ湖, 阿寒湖, 大沼, 涛沸湖, 風蓮湖).
     * Derived directly from Ministry of Land, Infrastructure, Transport and Tourism (国土交通省 国土数値情報 湖沼データ W09).
     * Projected via pure conformal Mercator projection (0.0% deformation).
     */
    const val LAKES_D = "{combined_d}"
"""
    if "const val LAKES_D =" in kt_code:
        # Replace existing
        import re

        kt_code = re.sub(
            r'const val LAKES_D = "[^"]+"',
            f'const val LAKES_D = "{combined_d}"',
            kt_code,
        )
    else:
        # Insert before closing brace
        last_brace = kt_code.rfind("}")
        kt_code = kt_code[:last_brace] + lake_const + kt_code[last_brace:]

    with open(kt_path, "w") as f:
        f.write(kt_code)
    print(f"Updated {kt_path} successfully!")


if __name__ == "__main__":
    main()
