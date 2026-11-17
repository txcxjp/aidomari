#!/usr/bin/env python3
"""
Pure Conformal Mercator Map Projection for Hokkaido (SVG ViewBox 612 x 387).
Mathematically rigorous projection with 0.0% deformation and 0 manual control points.
Directly maps official WGS84 GPS coordinates (longitude, latitude) to SVG canvas coordinates.
"""

import math

# Bounding box of Hokkaido Mainland and Scope Islands (Okushiri, Rishiri, Rebun, Teuri, Yagishiri)
MIN_MX = 2.43309194
MAX_MX = 2.54499370
MIN_MY = 0.79510902
MAX_MY = 0.89434710

SCALE = 3657.87025447
OFFSET_X = 101.33892580
OFFSET_Y = 12.00000000

def to_mercator(lon, lat):
    """Converts WGS84 (lon, lat in degrees) to Mercator conformal coordinates (radians)."""
    x = math.radians(lon)
    lat_rad = math.radians(lat)
    # Clamp latitude to avoid singularity at poles
    lat_rad = max(-1.4844, min(1.4844, lat_rad))
    y = math.log(math.tan(math.pi / 4.0 + lat_rad / 2.0))
    return x, y

def gps_to_svg(lon, lat):
    """
    Projects WGS84 GPS (lon, lat) to exact SVG coordinate (x, y) on 612 x 387 canvas.
    Zero deformation, isotropic (equal aspect ratio).
    """
    mx, my = to_mercator(lon, lat)
    sx = (mx - MIN_MX) * SCALE + OFFSET_X
    sy = (MAX_MY - my) * SCALE + OFFSET_Y
    return sx, sy

def svg_to_gps(sx, sy):
    """Inverse projection: SVG (x, y) back to WGS84 GPS (lon, lat)."""
    mx = (sx - OFFSET_X) / SCALE + MIN_MX
    my = MAX_MY - (sy - OFFSET_Y) / SCALE
    lon = math.degrees(mx)
    lat = math.degrees(2.0 * math.atan(math.exp(my)) - math.pi / 2.0)
    return lon, lat

if __name__ == '__main__':
    print("Testing Pure Conformal Mercator Projection:")
    key_points = [
        ('白神岬', 140.2017, 41.3970),
        ('函館駅前', 140.7264, 41.7738),
        ('恵山岬', 141.1650, 41.8050),
        ('襟裳岬', 143.2490, 41.9240),
        ('納沙布岬', 145.8167, 43.3850),
        ('知床岬', 145.3400, 44.3400),
        ('相泊', 145.3174, 44.1868),
        ('宗谷岬', 141.9366, 45.5228),
        ('ノシャップ岬', 141.6432, 45.4487),
        ('神威岬', 140.3500, 43.3333),
    ]
    for name, lon, lat in key_points:
        sx, sy = gps_to_svg(lon, lat)
        inv_lon, inv_lat = svg_to_gps(sx, sy)
        err = math.hypot(lon - inv_lon, lat - inv_lat)
        print(f"  {name:12}: GPS=({lon:8.4f}, {lat:7.4f}) -> SVG=({sx:6.2f}, {sy:6.2f}) (inv err: {err:.1e})")
