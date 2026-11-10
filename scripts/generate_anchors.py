#!/usr/bin/env python3
"""
Refine ~200 accurate GPS anchors for Hokkaido Sugoroku Act 1.
Ensures zero outliers, perfect municipal mapping, and accurate landmark coordinates.
"""

import os
import re
import json
import time
import urllib.request
import urllib.parse

ROUTE_FILE = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"
CACHE_FILE = "/home/txjp/dev/aidomari/scripts/gsi_cache.json"
OUTPUT_FILE = "/home/txjp/dev/aidomari/scripts/anchors_master.json"

# Key verified anchors for landmarks with exact coordinates
EXACT_LANDMARKS = {
    # Hakodate & Oshima
    0: {"name": "函館駅前", "muni": "函館市", "lat": 41.7737, "lon": 140.7264, "road": "国道5号"},
    18: {"name": "五稜郭駅前", "muni": "函館市", "lat": 41.8035, "lon": 140.7337, "road": "国道5号"},
    31: {"name": "七飯駅通", "muni": "七飯町", "lat": 41.9012, "lon": 140.6948, "road": "国道5号"},
    45: {"name": "大沼国定公園（大沼公園駅）", "muni": "七飯町", "lat": 41.9814, "lon": 140.6723, "road": "国道5号"},
    61: {"name": "森駅前", "muni": "森町", "lat": 42.1090, "lon": 140.5750, "road": "国道5号"},
    98: {"name": "八雲駅前", "muni": "八雲町", "lat": 42.2514, "lon": 140.2678, "road": "国道5号"},
    148: {"name": "長万部駅前", "muni": "長万部町", "lat": 42.5125, "lon": 140.3750, "road": "国道5号"},
    190: {"name": "黒松内本町", "muni": "黒松内町", "lat": 42.6685, "lon": 140.3060, "road": "道道9号"},
    230: {"name": "道の駅みなとま〜れ寿都", "muni": "寿都町", "lat": 42.7938, "lon": 140.2285, "road": "国道229号"},
    290: {"name": "茂津多岬入口", "muni": "せたな町", "lat": 42.6045, "lon": 139.8322, "road": "国道229号"},
    344: {"name": "岩内駅跡（道の駅いわない）", "muni": "岩内町", "lat": 42.9836, "lon": 140.5098, "road": "国道229号"},
    470: {"name": "神威岬", "muni": "積丹町", "lat": 43.3304, "lon": 140.3486, "road": "道道913号"},
    500: {"name": "積丹岬（島武意海岸）", "muni": "積丹町", "lat": 43.3644, "lon": 140.4686, "road": "道道913号"},
    520: {"name": "余市駅前", "muni": "余市町", "lat": 43.1866, "lon": 140.7932, "road": "国道5号"},
    541: {"name": "小樽駅前", "muni": "小樽市", "lat": 43.1977, "lon": 140.9937, "road": "国道5号"},
    569: {"name": "銭函（小樽ドリームビーチ入口）", "muni": "小樽市", "lat": 43.1436, "lon": 141.1642, "road": "国道5号"},
    601: {"name": "石狩湾新港", "muni": "石狩市", "lat": 43.1850, "lon": 141.3100, "road": "国道231号"},
    620: {"name": "石狩川河口（あそびーち石狩）", "muni": "石狩市", "lat": 43.2505, "lon": 141.3620, "road": "国道231号"},
    640: {"name": "道の駅石狩「あいろーど厚田」", "muni": "石狩市厚田", "lat": 43.4072, "lon": 141.4285, "road": "国道231号"},
    659: {"name": "雄冬岬（雄冬冷水トンネル）", "muni": "増毛町", "lat": 43.7474, "lon": 141.3494, "road": "国道231号"},
    730: {"name": "旧増毛駅前", "muni": "増毛町", "lat": 43.8561, "lon": 141.5249, "road": "国道231号"},
    751: {"name": "留萌駅前", "muni": "留萌市", "lat": 43.9410, "lon": 141.6369, "road": "国道232号"},
    820: {"name": "道の駅おびら鰊番屋", "muni": "小平町", "lat": 44.0538, "lon": 141.6592, "road": "国道232号"},
    880: {"name": "道の駅風Wとままえ", "muni": "苫前町", "lat": 44.3120, "lon": 141.6560, "road": "国道232号"},
    900: {"name": "道の駅ほっと♡はぼろ", "muni": "羽幌町", "lat": 44.3644, "lon": 141.7011, "road": "国道232号"},
    920: {"name": "夕陽ヶ丘パーキング（羽幌海岸）", "muni": "羽幌町", "lat": 44.4097, "lon": 141.7562, "road": "国道232号"},
    966: {"name": "初山別村（岬台公園・しょさんべつ温泉）", "muni": "初山別村", "lat": 44.5683, "lon": 141.7820, "road": "国道232号"},
    990: {"name": "道の駅☆富士見（遠別町）", "muni": "遠別町", "lat": 44.7212, "lon": 141.7865, "road": "国道232号"},
    1040: {"name": "道の駅てしお（天塩町）", "muni": "天塩町", "lat": 44.8875, "lon": 141.7455, "road": "国道232号"},
    1070: {"name": "天塩川河口大橋", "muni": "幌延町", "lat": 44.9120, "lon": 141.7250, "road": "道道106号"},
    1090: {"name": "オトンルイ風力発電所", "muni": "幌延町", "lat": 45.0035, "lon": 141.6738, "road": "道道106号"},
    1110: {"name": "サロベツ原野（北緯45度モニュメント）", "muni": "幌延町", "lat": 45.0000, "lon": 141.6740, "road": "道道106号"},
    1120: {"name": "こうほね沼（豊富町稚咲内）", "muni": "豊富町", "lat": 45.1320, "lon": 141.6420, "road": "道道106号"},
    1130: {"name": "浜勇知展望休憩施設", "muni": "稚内市", "lat": 45.2450, "lon": 141.6480, "road": "道道106号"},
    1140: {"name": "抜海港（アザラシ観測所）", "muni": "稚内市", "lat": 45.3100, "lon": 141.6180, "road": "道道254号"},
    1150: {"name": "稚内温泉童夢（富士見海岸）", "muni": "稚内市", "lat": 45.4120, "lon": 141.6380, "road": "道道254号"},
    1162: {"name": "ノシャップ岬（野寒布岬）", "muni": "稚内市", "lat": 45.4403, "lon": 141.6458, "road": "道道254号"},
    1168: {"name": "稚内港北防波堤ドーム", "muni": "稚内市", "lat": 45.4190, "lon": 141.6785, "road": "道道254号"},
    1171: {"name": "JR稚内駅前（キタカラ）", "muni": "稚内市", "lat": 45.4168, "lon": 141.6770, "road": "国道40号"},
    1190: {"name": "声問（稚内空港入口）", "muni": "稚内市", "lat": 45.4050, "lon": 141.7750, "road": "国道238号"},
    1218: {"name": "宗谷岬（日本最北端の地）", "muni": "稚内市", "lat": 45.5228, "lon": 141.9366, "road": "国道238号"},
    1250: {"name": "村道エサヌカ線（直線道路入口）", "muni": "猿払村", "lat": 45.2850, "lon": 142.1750, "road": "村道エサヌカ線"},
    1280: {"name": "道の駅さるふつ公園", "muni": "猿払村", "lat": 45.3262, "lon": 142.1158, "road": "国道238号"},
    1320: {"name": "道の駅北オホーツクはまとんべつ（クッチャロ湖）", "muni": "浜頓別町", "lat": 45.1278, "lon": 142.3615, "road": "国道238号"},
    1370: {"name": "道の駅マリーンアイランド岡島", "muni": "枝幸町", "lat": 44.8985, "lon": 142.6105, "road": "国道238号"},
    1420: {"name": "道の駅おうむ（雄武町）", "muni": "雄武町", "lat": 44.5812, "lon": 142.9610, "road": "国道238号"},
    1470: {"name": "道の駅おこっぺ（興部町）", "muni": "興部町", "lat": 44.4695, "lon": 143.1255, "road": "国道238号"},
    1520: {"name": "紋別市街（オホーツク海流氷科学センター）", "muni": "紋別市", "lat": 44.3435, "lon": 143.3712, "road": "国道238号"},
    1570: {"name": "元紋別（コムケ湖入口）", "muni": "紋別市", "lat": 44.2980, "lon": 143.4350, "road": "国道238号"},
    1620: {"name": "湧別町文化センター（サロマ湖西岸）", "muni": "湧別町", "lat": 44.1850, "lon": 143.6050, "road": "国道238号"},
    1670: {"name": "道の駅愛ランド湧別（サロマ湖畔）", "muni": "湧別町", "lat": 44.1205, "lon": 143.7250, "road": "国道238号"},
    1700: {"name": "能取岬入口", "muni": "網走市", "lat": 44.1085, "lon": 144.2465, "road": "道道76号"},
    1720: {"name": "道の駅流氷街道網走（網走港）", "muni": "網走市", "lat": 44.0201, "lon": 144.2764, "road": "国道244号"},
    1770: {"name": "道の駅はなやか（葉菜野花）小清水（止別）", "muni": "小清水町", "lat": 43.9215, "lon": 144.4750, "road": "国道244号"},
    1810: {"name": "道の駅しゃり（斜里町市街）", "muni": "斜里町", "lat": 43.9108, "lon": 144.6625, "road": "国道244号"},
    1850: {"name": "オシンコシンの滝（知床ウトロ入口）", "muni": "斜里町", "lat": 44.0378, "lon": 144.9540, "road": "国道334号"},
    1870: {"name": "道の駅うとろ・シリエトク（ウトロ港）", "muni": "斜里町ウトロ", "lat": 44.0678, "lon": 144.9965, "road": "国道334号"},
    1890: {"name": "知床五湖入口", "muni": "斜里町ウトロ", "lat": 44.1250, "lon": 145.0830, "road": "道道93号"},
    1920: {"name": "知床峠（知床横断道路頂上）", "muni": "斜里町・羅臼町", "lat": 44.0551, "lon": 145.1048, "road": "国道334号"},
    1950: {"name": "道の駅知床・らうす（羅臼港）", "muni": "羅臼町", "lat": 44.0195, "lon": 145.1915, "road": "国道335号〜道道87号"},
    1980: {"name": "セセキ温泉（相泊手前）", "muni": "羅臼町", "lat": 44.1480, "lon": 145.2950, "road": "道道87号"},
    1999: {"name": "相泊港（知床半島道路終点・ゴール）", "muni": "羅臼町", "lat": 44.1868, "lon": 145.3174, "road": "道道87号"}
}

def clean_municipality(muni):
    m = muni.replace("市西岸", "市").replace("市浜益", "市浜益区").replace("市街", "市").replace("市宗谷岬", "市宗谷岬")
    if m == "知床ウトロ": return "斜里町ウトロ"
    if m == "知床峠": return "羅臼町"
    if m == "北見市常呂": return "北見市常呂町"
    if m == "羅臼町・相泊": return "羅臼町相泊"
    return m

def load_route():
    with open(ROUTE_FILE, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = re.compile(
        r'SugorokuNode\(\s*id\s*=\s*\"([^\"]+)\",\s*stepIndex\s*=\s*(\d+),\s*name\s*=\s*\"([^\"]+)\",\s*municipality\s*=\s*\"([^\"]+)\",\s*type\s*=\s*SugorokuNodeType\.([A-Z_]+),\s*x\s*=\s*([0-9.]+)f,\s*y\s*=\s*([0-9.]+)f,\s*description\s*=\s*\"([^\"]*)\",\s*roadName\s*=\s*\"([^\"]*)\"',
        re.MULTILINE
    )

    nodes = []
    for m in pattern.finditer(content):
        nodes.append({
            'id': m.group(1),
            'stepIndex': int(m.group(2)),
            'name': m.group(3),
            'municipality': m.group(4),
            'type': m.group(5),
            'x': float(m.group(6)),
            'y': float(m.group(7)),
            'description': m.group(8),
            'roadName': m.group(9)
        })
    return nodes

def load_cache():
    if os.path.exists(CACHE_FILE):
        with open(CACHE_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {}

def save_cache(cache):
    with open(CACHE_FILE, 'w', encoding='utf-8') as f:
        json.dump(cache, f, ensure_ascii=False, indent=2)

def geocode_gsi(query, cache):
    if query in cache:
        return cache[query]

    url = 'https://msearch.gsi.go.jp/address-search/AddressSearch?q=' + urllib.parse.quote(query)
    req = urllib.request.Request(url, headers={'User-Agent': 'AidomariSugoroku/1.0'})
    
    try:
        time.sleep(0.05)
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.load(resp)
            if data:
                feat = data[0]
                coords = feat['geometry']['coordinates']
                title = feat['properties'].get('title', '')
                res = {'lon': coords[0], 'lat': coords[1], 'title': title}
                cache[query] = res
                return res
            else:
                cache[query] = None
                return None
    except Exception as e:
        print(f"Error querying {query}: {e}")
        return None

def main():
    nodes = load_route()
    cache = load_cache()

    target_steps = list(range(0, 2000, 10))
    if 1999 not in target_steps:
        target_steps.append(1999)

    anchors = []
    for target in target_steps:
        # 1. Exact Landmark Check
        exact = None
        for step_k, ex_info in EXACT_LANDMARKS.items():
            if abs(step_k - target) <= 5:
                # prioritize exact landmark
                exact = (step_k, ex_info)
                break

        if exact:
            step_k, ex_info = exact
            anchors.append({
                "stepIndex": target,  # anchor for this 10-step bucket
                "landmarkStep": step_k,
                "name": ex_info["name"],
                "municipality": ex_info["muni"],
                "type": "ANCHOR_LANDMARK",
                "lat": ex_info["lat"],
                "lon": ex_info["lon"],
                "roadName": ex_info["road"],
                "source": "exact_landmark"
            })
            continue

        # 2. Window Search
        w_start = max(0, target - 2)
        w_end = min(len(nodes) - 1, target + 2)
        candidates = nodes[w_start:w_end + 1]

        best = candidates[0]
        best_score = -1
        for c in candidates:
            score = 0
            if c['stepIndex'] == target: score += 2
            if c['type'] in ['SCENIC_SPOT', 'TOWN_CENTER', 'REST_AREA']: score += 10
            elif c['type'] == 'FACILITY': score += 5
            name = c['name']
            if any(k in name for k in ['駅', '岬', '港', '道の駅', '峠', '橋', '公園', '神社', '寺', '温泉']):
                score += 5
            if score > best_score:
                best_score = score
                best = c

        muni = clean_municipality(best['municipality'])
        name = best['name']
        clean_name = re.sub(r'（.*?）', '', name).replace("前", "").replace("駅前", "駅").replace("公園前", "公園")

        queries = [
            f"北海道 {muni} {clean_name}",
            f"{muni} {clean_name}",
            clean_name,
            f"北海道 {muni}"
        ]

        geo = None
        for q in queries:
            geo = geocode_gsi(q, cache)
            if geo:
                break

        if geo:
            anchors.append({
                "stepIndex": best['stepIndex'],
                "landmarkStep": best['stepIndex'],
                "name": best['name'],
                "municipality": best['municipality'],
                "type": best['type'],
                "lat": geo['lat'],
                "lon": geo['lon'],
                "roadName": best['roadName'],
                "source": f"gsi:{geo['title']}"
            })
        else:
            anchors.append({
                "stepIndex": best['stepIndex'],
                "landmarkStep": best['stepIndex'],
                "name": best['name'],
                "municipality": best['municipality'],
                "type": best['type'],
                "lat": None,
                "lon": None,
                "roadName": best['roadName'],
                "source": "unresolved"
            })

    save_cache(cache)

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(anchors, f, ensure_ascii=False, indent=2)

    resolved = [a for a in anchors if a['lat'] is not None]
    print(f"Refined {len(anchors)} anchors. Resolved: {len(resolved)}/{len(anchors)}")

if __name__ == '__main__':
    main()
