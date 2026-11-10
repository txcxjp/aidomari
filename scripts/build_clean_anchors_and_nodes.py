#!/usr/bin/env python3
"""
Generate clean, non-duplicated ~200 anchors from EXACT_LANDMARKS and GSI geocoding,
then apply exact TPS coordinates to anchor nodes in HokkaidoAct1Route.kt,
interpolating intermediate nodes smoothly between anchors.
NO road snapping (no polyline projection).
"""

import os
import re
import json
import math
import sys

sys.path.append('/home/txjp/dev/aidomari/scripts')
import build_and_export_roads_bin as b

ROUTE_FILE = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"
CACHE_FILE = "/home/txjp/dev/aidomari/scripts/gsi_cache.json"
ANCHORS_FILE = "/home/txjp/dev/aidomari/scripts/anchors_master.json"

# Key verified landmarks with exact real-world coordinates
EXACT_LANDMARKS = {
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
    660: {"name": "浜益海浜公園", "muni": "石狩市浜益", "lat": 43.6068, "lon": 141.3926, "road": "国道231号（オロロンライン）"},
    690: {"name": "雄冬岬（おふゆみさき）", "muni": "増毛町", "lat": 43.7474, "lon": 141.3494, "road": "国道231号（オロロンライン）"},
    708: {"name": "旧JR増毛駅前", "muni": "増毛町", "lat": 43.8561, "lon": 141.5249, "road": "国道231号（オロロンライン）"},
    751: {"name": "留萌駅前", "muni": "留萌市", "lat": 43.9410, "lon": 141.6369, "road": "国道232号"},
    820: {"name": "道の駅おびら鰊番屋", "muni": "小平町", "lat": 44.0538, "lon": 141.6592, "road": "国道232号"},
    842: {"name": "力昼（りきびる）", "muni": "苫前町", "lat": 44.2250, "lon": 141.6700, "road": "国道232号（オロロンライン）"},
    855: {"name": "道の駅風Wとままえ（ふわっと）", "muni": "苫前町", "lat": 44.3120, "lon": 141.6560, "road": "国道232号（オロロンライン）"},
    866: {"name": "三毛別羆事件復元地", "muni": "苫前町", "lat": 44.144955, "lon": 141.843628, "road": "道道1049号（苫前小平線）"},
    885: {"name": "苫前・羽幌町境", "muni": "苫前町", "lat": 44.3400, "lon": 141.6800, "road": "国道232号（オロロンライン）"},
    900: {"name": "道の駅ほっと♡はぼろ", "muni": "羽幌町", "lat": 44.3644, "lon": 141.7011, "road": "国道232号"},
    920: {"name": "夕陽ヶ丘パーキング（羽幌海岸）", "muni": "羽幌町", "lat": 44.4097, "lon": 141.7562, "road": "国道232号"},
    966: {"name": "初山別村（岬台公園・しょさんべつ温泉）", "muni": "初山別村", "lat": 44.5683, "lon": 141.7820, "road": "国道232号"},
    990: {"name": "道の駅☆富士見（遠別町）", "muni": "遠別町", "lat": 44.7212, "lon": 141.7865, "road": "国道232号"},
    1040: {"name": "道の駅てしお（天塩町）", "muni": "天塩町", "lat": 44.8875, "lon": 141.7455, "road": "国道232号"},
    1070: {"name": "天塩川河口大橋", "muni": "幌延町", "lat": 44.9120, "lon": 141.7250, "road": "道道106号"},
    1090: {"name": "オトンルイ風力発電所", "muni": "幌延町", "lat": 45.0035, "lon": 141.6738, "road": "道道106号"},
    1110: {"name": "サロベツ原野（北緯45度モニュメント）", "muni": "幌延町", "lat": 45.0000, "lon": 141.6740, "road": "道道106号"},
    1120: {"name": "こうほね沼（豊富町稚咲内）", "muni": "豊富町", "lat": 45.1320, "lon": 141.6420, "road": "道道106号"},
    1135: {"name": "浜勇知展望休憩施設", "muni": "稚内市西岸", "lat": 45.2450, "lon": 141.6480, "road": "道道106号（オロロンライン）"},
    1141: {"name": "抜海港", "muni": "稚内市西岸", "lat": 45.3100, "lon": 141.6180, "road": "道道254号（宗谷サンセットロード）"},
    1152: {"name": "稚内温泉童夢前", "muni": "稚内市西岸", "lat": 45.4120, "lon": 141.6380, "road": "道道254号（宗谷サンセットロード）"},
    1162: {"name": "ノシャップ岬（野寒布岬）", "muni": "稚内市街", "lat": 45.4403, "lon": 141.6458, "road": "道道254号"},
    1168: {"name": "稚内港北防波堤ドーム", "muni": "稚内市街", "lat": 45.4190, "lon": 141.6785, "road": "道道254号"},
    1171: {"name": "JR稚内駅前（キタカラ）", "muni": "稚内市街", "lat": 45.4168, "lon": 141.6770, "road": "国道40号"},
    1190: {"name": "声問（稚内空港入口）", "muni": "稚内市東岸", "lat": 45.4050, "lon": 141.7750, "road": "国道238号"},
    1220: {"name": "宗谷岬（日本最北端の地）", "muni": "稚内市東岸", "lat": 45.5228, "lon": 141.9366, "road": "国道238号"},
    1250: {"name": "村道エサヌカ線（直線道路入口）", "muni": "猿払村", "lat": 45.2850, "lon": 142.1750, "road": "村道エサヌカ線"},
    1300: {"name": "道の駅さるふつ公園", "muni": "猿払村", "lat": 45.3340, "lon": 142.1460, "road": "国道238号"},
    1360: {"name": "道の駅マリーンアイランド岡島", "muni": "枝幸町", "lat": 44.9080, "lon": 142.6100, "road": "国道238号"},
    1440: {"name": "道の駅おうむ", "muni": "雄武町", "lat": 44.5820, "lon": 142.9630, "road": "国道238号"},
    1480: {"name": "道の駅おこっぺ", "muni": "興部町", "lat": 44.4710, "lon": 143.1250, "road": "国道238号"},
    1520: {"name": "紋別港（ガリンコ号ステーション）", "muni": "紋別市", "lat": 44.3540, "lon": 143.3550, "road": "国道238号"},
    1570: {"name": "道の駅サロマ湖", "muni": "湧別町", "lat": 44.1700, "lon": 143.7650, "road": "国道238号"},
    1640: {"name": "能取岬入口", "muni": "網走市", "lat": 44.0950, "lon": 144.2400, "road": "道道76号"},
    1660: {"name": "能取岬（美岬灯台）", "muni": "網走市", "lat": 44.1110, "lon": 144.2480, "road": "道道76号"},
    1680: {"name": "網走駅前", "muni": "網走市", "lat": 44.0200, "lon": 144.2600, "road": "国道39号"},
    1700: {"name": "道の駅流氷街道網走（網走港）", "muni": "網走市", "lat": 44.0240, "lon": 144.2750, "road": "国道244号"},
    1740: {"name": "知床斜里駅前", "muni": "斜里町", "lat": 43.9110, "lon": 144.6620, "road": "国道244号"},
    1820: {"name": "オシンコシンの滝", "muni": "斜里町", "lat": 44.0370, "lon": 144.9380, "road": "国道334号"},
    1840: {"name": "道の駅うとろ・シリエトク（ウトロ港）", "muni": "斜里町ウトロ", "lat": 44.0680, "lon": 144.9960, "road": "国道334号"},
    1880: {"name": "知床五湖（知床フィールドハウス）", "muni": "斜里町ウトロ", "lat": 44.1245, "lon": 145.0830, "road": "道道93号"},
    1920: {"name": "知床峠（知床横断道路頂上）", "muni": "羅臼町", "lat": 44.0551, "lon": 145.1048, "road": "国道334号"},
    1940: {"name": "道の駅知床・らうす（羅臼港）", "muni": "羅臼町", "lat": 44.0190, "lon": 145.1910, "road": "国道335号"},
    1980: {"name": "セセキ温泉（相泊手前）", "muni": "羅臼町", "lat": 44.1480, "lon": 145.2950, "road": "道道87号"},
    1999: {"name": "相泊港（知床半島道路終点・ゴール）", "muni": "羅臼町", "lat": 44.1868, "lon": 145.3174, "road": "道道87号"},
}

def clean_municipality(muni):
    return muni.replace("石狩市厚田", "石狩市厚田区").replace("石狩市浜益", "石狩市浜益区").replace("稚内市西岸", "稚内市").replace("稚内市街", "稚内市").replace("稚内市東岸", "稚内市").replace("北見市常呂", "北見市常呂町").replace("知床ウトロ", "斜里町ウトロ").replace("知床峠", "斜里町").replace("羅臼町・相泊", "羅臼町")

def main():
    with open(ROUTE_FILE, 'r', encoding='utf-8') as f:
        route_text = f.read()

    # Parse route nodes
    nodes = []
    pattern = re.compile(
        r'SugorokuNode\(\s*id\s*=\s*\"([^\"]+)\",\s*stepIndex\s*=\s*(\d+),\s*name\s*=\s*\"([^\"]+)\",\s*municipality\s*=\s*\"([^\"]+)\",\s*type\s*=\s*SugorokuNodeType\.([A-Z_]+),\s*x\s*=\s*([\d\.]+)f,\s*y\s*=\s*([\d\.]+)f,\s*description\s*=\s*\"([^\"]+)\",\s*roadName\s*=\s*\"([^\"]+)\"',
        re.DOTALL
    )
    for m in pattern.finditer(route_text):
        nodes.append({
            'id': m.group(1),
            'step': int(m.group(2)),
            'name': m.group(3),
            'muni': m.group(4),
            'type': m.group(5),
            'x': float(m.group(6)),
            'y': float(m.group(7)),
            'desc': m.group(8),
            'road': m.group(9),
        })

    with open(CACHE_FILE, 'r', encoding='utf-8') as f:
        cache = json.load(f)

    # 1. Determine anchor steps (EXACT_LANDMARKS + every 10 steps, strictly increasing)
    target_steps = set(range(0, 2000, 10))
    target_steps.add(1999)
    target_steps.update(EXACT_LANDMARKS.keys())
    sorted_steps = sorted(list(target_steps))

    anchors = []
    for s in sorted_steps:
        n = nodes[s]
        if s in EXACT_LANDMARKS:
            info = EXACT_LANDMARKS[s]
            tx, ty = b.tps_transform(info['lon'], info['lat'])
            anchors.append({
                'stepIndex': s,
                'name': n['name'],
                'muni': n['muni'],
                'type': n['type'],
                'lat': info['lat'],
                'lon': info['lon'],
                'tps_x': round(tx, 2),
                'tps_y': round(ty, 2),
                'road': n['road'],
                'source': 'exact_landmark'
            })
        else:
            # Check GSI cache
            muni = clean_municipality(n['muni'])
            clean_name = re.sub(r'（.*?）', '', n['name']).replace("前", "").replace("駅前", "駅").replace("公園前", "公園")
            queries = [
                f"北海道 {muni} {clean_name}",
                f"{muni} {clean_name}",
                clean_name,
                f"北海道 {muni}"
            ]
            geo = None
            for q in queries:
                if q in cache and cache[q]:
                    geo = cache[q]
                    break
            
            if geo:
                tx, ty = b.tps_transform(geo['lon'], geo['lat'])
                anchors.append({
                    'stepIndex': s,
                    'name': n['name'],
                    'muni': n['muni'],
                    'type': n['type'],
                    'lat': geo['lat'],
                    'lon': geo['lon'],
                    'tps_x': round(tx, 2),
                    'tps_y': round(ty, 2),
                    'road': n['road'],
                    'source': 'gsi_cache'
                })
            else:
                # Fallback to existing node coordinates inverted or interpolated
                anchors.append({
                    'stepIndex': s,
                    'name': n['name'],
                    'muni': n['muni'],
                    'type': n['type'],
                    'lat': 0.0,
                    'lon': 0.0,
                    'tps_x': n['x'],
                    'tps_y': n['y'],
                    'road': n['road'],
                    'source': 'existing_xy'
                })

    # Save anchors_master.json
    with open(ANCHORS_FILE, 'w', encoding='utf-8') as f:
        json.dump(anchors, f, ensure_ascii=False, indent=2)

    print(f"Generated {len(anchors)} clean anchors in {ANCHORS_FILE}!")

    # 2. Update HokkaidoAct1Route.kt: Set exact coordinates on anchor nodes,
    # and interpolate intermediate nodes between consecutive anchors smoothly.
    new_coords = {}
    for i in range(len(anchors) - 1):
        a0 = anchors[i]
        a1 = anchors[i+1]
        s0, x0, y0 = a0['stepIndex'], a0['tps_x'], a0['tps_y']
        s1, x1, y1 = a1['stepIndex'], a1['tps_x'], a1['tps_y']
        new_coords[s0] = (x0, y0)
        num_steps = s1 - s0
        for step in range(s0 + 1, s1):
            t = (step - s0) / num_steps
            cx = x0 + t * (x1 - x0)
            cy = y0 + t * (y1 - y0)
            new_coords[step] = (round(cx, 2), round(cy, 2))
    
    last_anchor = anchors[-1]
    new_coords[last_anchor['stepIndex']] = (last_anchor['tps_x'], last_anchor['tps_y'])

    # Format all 2,000 nodes into chunks strictly preserving original structure
    def format_node(step, name, muni, ntype, x, y, desc, road):
        return f"""        SugorokuNode(
            id = "node_{step:04d}",
            stepIndex = {step},
            name = "{name}",
            municipality = "{muni}",
            type = SugorokuNodeType.{ntype},
            x = {x:.2f}f,
            y = {y:.2f}f,
            description = "{desc}",
            roadName = "{road}",
        ),"""

    chunk_blocks = []
    for c in range(20):
        c_start = c * 100
        c_end = (c + 1) * 100 - 1
        lines = []
        for s in range(c_start, c_end + 1):
            n = nodes[s]
            x, y = new_coords[s]
            lines.append(format_node(s, n['name'], n['muni'], n['type'], x, y, n['desc'], n['road']))
        chunk_body = "\n".join(lines)
        chunk_blocks.append(f"    private fun chunk{c}(): List<SugorokuNode> = listOf(\n{chunk_body}\n    )")

    header = route_text[:route_text.find("    private fun chunk0(): List<SugorokuNode> = listOf(")]
    footer = "\n}\n"
    new_file_content = header + "\n\n".join(chunk_blocks) + footer

    with open(ROUTE_FILE, 'w', encoding='utf-8') as f:
        f.write(new_file_content)

    print("Successfully updated HokkaidoAct1Route.kt with clean exact anchors and smooth interpolation!")

if __name__ == '__main__':
    main()
