#!/usr/bin/env python3
"""
Apply verified exact GPS coordinates to all key anchor landmarks in HokkaidoAct1Route.kt,
and smoothly interpolate intermediate nodes between anchors without road snapping.
Anchor nodes are 100% preserved at their true TPS coordinates.
"""

import math
import re
import sys

sys.path.append("/home/txjp/dev/aidomari/scripts")
import build_and_export_roads_bin as b

ROUTE_FILE = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"

# Complete set of verified anchors strictly matching HokkaidoAct1Route.kt step indices
ANCHORS = [
    (0, "函館駅前", 41.7737, 140.7264),
    (18, "五稜郭駅前", 41.8035, 140.7337),
    (31, "七飯本町", 41.9012, 140.6948),
    (45, "大沼国定公園", 41.9814, 140.6723),
    (61, "森駅前", 42.1090, 140.5750),
    (78, "落部駅前", 42.1850, 140.4350),
    (98, "八雲駅前", 42.2514, 140.2678),
    (125, "山越（八雲・長万部境）", 42.3650, 140.3200),
    (149, "長万部駅前", 42.5125, 140.3750),
    (190, "黒松内本町", 42.6685, 140.3060),
    (230, "道の駅みなとま〜れ寿都", 42.7938, 140.2285),
    (289, "茂津多岬入口", 42.6045, 139.8322),
    (315, "寿都・岩内境（弁慶岬方面）", 42.8250, 140.1800),
    (346, "道の駅いわない", 42.9836, 140.5098),
    (378, "泊村・堀株", 43.0450, 140.5100),
    (411, "道の駅オスコイ！かもえない", 43.1460, 140.4280),
    (451, "神威岬灯台", 43.3304, 140.3486),
    (460, "島武意海岸（積丹岬）", 43.3644, 140.4686),
    (500, "JR余市駅前", 43.1866, 140.7932),
    (541, "JR小樽駅前", 43.1977, 140.9937),
    (560, "銭函海岸", 43.1436, 141.1642),
    (568, "石狩湾新港西埠頭", 43.1850, 141.3100),
    (607, "石狩川河口", 43.2505, 141.3620),
    (617, "道の駅石狩「あいろーど厚田」", 43.4072, 141.4285),
    (660, "浜益海浜公園", 43.6068, 141.3926),
    (690, "雄冬岬", 43.7474, 141.3494),
    (708, "旧JR増毛駅前", 43.8561, 141.5249),
    (751, "JR留萌駅跡", 43.9410, 141.6369),
    (798, "おびら鰊番屋", 44.0538, 141.6592),
    (841, "力昼高台", 44.2250, 141.6700),
    (855, "道の駅風Wとままえ", 44.3120, 141.6560),
    (866, "三毛別羆事件復元地", 44.144955, 141.843628),
    (885, "苫前・羽幌町境", 44.3400, 141.6800),
    (887, "道の駅ほっと♡はぼろ", 44.3644, 141.7011),
    (934, "初山別温泉岬の湯", 44.5683, 141.7820),
    (969, "道の駅えんべつ富士見", 44.7212, 141.7865),
    (1009, "道の駅てしお", 44.8875, 141.7455),
    (1030, "天塩川河口域", 44.9200, 141.7200),
    (1058, "オトンルイ風力発電所", 45.0035, 141.6738),
    (1069, "北緯45度モニュメント", 45.0000, 141.6740),
    (1085, "こうほね沼", 45.1320, 141.6420),
    (1097, "浜勇知展望休憩施設", 45.2450, 141.6480),
    (1104, "抜海港", 45.3100, 141.6180),
    (1126, "稚内温泉童夢前", 45.4120, 141.6380),
    (1162, "ノシャップ岬", 45.4403, 141.6458),
    (1168, "稚内港北防波堤ドーム", 45.4190, 141.6785),
    (1171, "JR稚内駅前", 45.4168, 141.6770),
    (1180, "南稚内駅前", 45.3970, 141.6850),
    (1188, "大沼野鳥観察館", 45.4050, 141.7300),
    (1196, "稚内空港", 45.4050, 141.7750),
    (1206, "宗谷岬入口", 45.5150, 141.9250),
    (1218, "宗谷岬（最北端の碑）", 45.5228, 141.9366),
    (1220, "猿払村・知来", 45.4500, 142.0200),
    (1225, "エサヌカ線入口", 45.2850, 142.1750),
    (1229, "道の駅さるふつ公園", 45.3340, 142.1460),
    (1284, "クッチャロ湖畔", 45.1270, 142.3660),
    (1315, "浜頓別本町", 45.1200, 142.3600),
    (1350, "道の駅岡島", 44.9080, 142.6100),
    (1380, "北見枝幸港", 44.9380, 142.5800),
    (1414, "道の駅おうむ", 44.5820, 142.9630),
    (1475, "道の駅おこっぺ", 44.4710, 143.1250),
    (1515, "カニの爪モニュメント", 44.3540, 143.3550),
    (1517, "氷海展望塔オホーツクタワー", 44.3560, 143.3600),
    (1521, "ガリンコ号ステーション", 44.3540, 143.3550),
    (1549, "オホーツク紋別空港", 44.3040, 143.4040),
    (1580, "道の駅チューリップの湯", 44.1800, 143.5800),
    (1600, "湧別・佐呂間境手前", 44.1750, 143.6800),
    (1638, "道の駅サロマ湖", 44.1700, 143.7650),
    (1654, "浜佐呂間", 44.0950, 143.9100),
    (1663, "ワッカ原生花園", 44.1500, 143.9800),
    (1678, "常呂カーリングホール", 44.1200, 144.0800),
    (1689, "能取岬", 44.1110, 144.2480),
    (1701, "JR網走駅前", 44.0200, 144.2600),
    (1705, "道の駅網走", 44.0240, 144.2750),
    (1730, "藻琴駅前", 43.9680, 144.3200),
    (1759, "小清水原生花園", 43.9390, 144.4250),
    (1780, "止別（やまいべつ）", 43.9230, 144.5250),
    (1805, "JR知床斜里駅前", 43.9110, 144.6620),
    (1849, "オシンコシンの滝", 44.0370, 144.9380),
    (1857, "道の駅うとろ", 44.0680, 144.9960),
    (1862, "知床自然センター", 44.0740, 145.0340),
    (1865, "知床五湖", 44.1245, 145.0830),
    (1870, "知床峠ゲート", 44.0740, 145.0350),
    (1923, "知床峠", 44.0551, 145.1048),
    (1938, "道の駅らうす", 44.0190, 145.1910),
    (1966, "セセキ温泉", 44.1480, 145.2950),
    (1980, "相泊手前・モサヨロ", 44.1650, 145.3050),
    (1999, "相泊港", 44.1868, 145.3174),
]

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

    assert len(nodes) == 2000, f"Expected 2000 nodes, got {len(nodes)}"

    # 1. Compute TPS for all anchors
    anchor_coords = []
    for s, name, lat, lon in ANCHORS:
        tx, ty = b.tps_transform(lon, lat)
        anchor_coords.append((s, tx, ty, name))

    anchor_indices = {s for s, _, _, _ in anchor_coords}

    # 2. Linear interpolation between anchors
    raw_coords = {}
    for i in range(len(anchor_coords) - 1):
        s0, x0, y0, _ = anchor_coords[i]
        s1, x1, y1, _ = anchor_coords[i+1]
        num_steps = s1 - s0
        for step in range(s0, s1):
            t = (step - s0) / num_steps
            raw_coords[step] = (x0 + t * (x1 - x0), y0 + t * (y1 - y0))
    raw_coords[1999] = (anchor_coords[-1][1], anchor_coords[-1][2])

    # 3. Moving average smoothing for non-anchor nodes
    final_coords = {}
    for s in range(2000):
        if s in anchor_indices:
            # 100% exact TPS coordinates for anchors
            final_coords[s] = (round(raw_coords[s][0], 2), round(raw_coords[s][1], 2))
        else:
            pts_x = [raw_coords[k][0] for k in range(max(0, s-2), min(2000, s+3))]
            pts_y = [raw_coords[k][1] for k in range(max(0, s-2), min(2000, s+3))]
            final_coords[s] = (round(sum(pts_x)/len(pts_x), 2), round(sum(pts_y)/len(pts_y), 2))

    # 4. Format all 2,000 nodes into chunks strictly preserving original structure
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
            x, y = final_coords[s]
            lines.append(format_node(s, n['name'], n['muni'], n['type'], x, y, n['desc'], n['road']))
        chunk_body = "\n".join(lines)
        chunk_blocks.append(f"    private fun chunk{c}(): List<SugorokuNode> = listOf(\n{chunk_body}\n    )")

    header = route_text[:route_text.find("    private fun chunk0(): List<SugorokuNode> = listOf(")]
    footer = "\n}\n"
    new_file_content = header + "\n\n".join(chunk_blocks) + footer

    with open(ROUTE_FILE, 'w', encoding='utf-8') as f:
        f.write(new_file_content)

    print("Successfully applied exact anchor coordinates and smooth route interpolation!")

if __name__ == '__main__':
    main()
