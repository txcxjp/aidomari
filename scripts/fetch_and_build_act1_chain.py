import urllib.request
import gzip
import json
import math
import sys
import os

sys.path.append('/home/txjp/dev/aidomari/scripts')
from accurate_tps import gps_to_svg
from fast_polygon import load_gsi_polygon

poly = load_gsi_polygon()

BASE_URL = 'https://raw.githubusercontent.com/toruseo/road-viewer-finder/main/public/'
FILES = ['osm_trunk.geojson.gz', 'osm_primary.geojson.gz', 'osm_secondary.geojson.gz']
TARGET_REFS = {'5', '9', '229', '231', '232', '106', '238', '244', '334', '87'}

raw_lines = {r: [] for r in TARGET_REFS}

for fname in FILES:
    print(f"Downloading {fname}...")
    url = BASE_URL + fname
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req, timeout=60) as resp:
        data = json.loads(gzip.decompress(resp.read()).decode('utf-8'))
        for f in data.get('features', []):
            coords = f.get('geometry', {}).get('coordinates', [])
            if not coords:
                continue
            first_pt = coords[0] if not isinstance(coords[0][0], list) else coords[0][0]
            lon, lat = first_pt[0], first_pt[1]
            if not (139.3 <= lon <= 146.0 and 41.3 <= lat <= 45.6):
                continue
            
            ref = str(f.get('properties', {}).get('ref', ''))
            refs = [r.strip() for r in ref.replace(';', ',').split(',')]
            for r in TARGET_REFS:
                if r in refs:
                    if isinstance(coords[0][0], list):
                        for line in coords:
                            raw_lines[r].append([(pt[0], pt[1]) for pt in line])
                    else:
                        raw_lines[r].append([(pt[0], pt[1]) for pt in coords])

print("\n--- Downloaded OSM Raw Lines Summary ---")
for r in sorted(raw_lines.keys()):
    total_pts = sum(len(l) for l in raw_lines[r])
    print(f"Route {r:4s}: {len(raw_lines[r]):4d} segments, {total_pts:6d} raw GPS points")

out_file = '/home/txjp/dev/aidomari/scripts/osm_hokkaido_raw_roads.json'
with open(out_file, 'w') as f:
    json.dump(raw_lines, f)
print(f"Saved raw OSM lines to {out_file} successfully.")
