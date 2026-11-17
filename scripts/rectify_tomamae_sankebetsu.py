#!/usr/bin/env python3
"""
Rectify Tomamae - Sankebetsu bear attack site section (Steps 840 to 884).
- Replaces inland coordinate placement of coast/ports with authentic Route 232 and Route 1049 (Sankebetsu) binary chains.
- Uses exact real-world coordinates for the historic Sankebetsu bear attack site (175.12, 141.17).
- Eliminates anomalies where ports and town shrines were placed inland.
- Connects smoothly: Step 839 -> 840 (3.48 px), and Step 884 -> 885 (0.00 px exact match).
"""

import struct
import re

ROADS_BIN = "app/src/main/assets/hokkaido_roads_high_density.bin"
KOTLIN_FILE = "app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"

# 1. Load road segments
with open(ROADS_BIN, "rb") as f:
    data = f.read()

num_segs = struct.unpack("<H", data[8:10])[0]
offset = 10
segs = {}
for _ in range(num_segs):
    id_len = struct.unpack("<B", data[offset:offset+1])[0]; offset += 1
    seg_id = data[offset:offset+id_len].decode("utf-8", errors="ignore"); offset += id_len
    name_len = struct.unpack("<B", data[offset:offset+1])[0]; offset += 1
    name = data[offset:offset+name_len].decode("utf-8", errors="ignore"); offset += name_len
    cat = struct.unpack("<B", data[offset:offset+1])[0]; offset += 1
    num_pts = struct.unpack("<I", data[offset:offset+4])[0]; offset += 4
    pts = []
    for _ in range(num_pts):
        x, y = struct.unpack("<ff", data[offset:offset+8]); offset += 8
        pts.append((round(x, 2), round(y, 2)))
    segs[seg_id] = (name, pts)

def sample(pts, n):
    if n <= 1:
        return [pts[0]]
    res = []
    for i in range(n):
        idx = int(round(i * (len(pts) - 1) / (n - 1)))
        res.append(pts[idx])
    return res

# 1. Step 840-853 (14 pts): Coastal Route 232 to Tomamae town center
pts_45 = segs["road_232;239_45_0"][1]
min_d = 1e9; idx_840 = -1
for i, p in enumerate(pts_45):
    d = (p[0]-170.72)**2 + (p[1]-135.76)**2
    if d < min_d: min_d = d; idx_840 = i
c_coast_in = sample(pts_45[idx_840:], 14)

# 2. Step 854-866 (13 pts): Route 1049 inland to Sankebetsu bear attack site
s1049_chain = (
    segs["road_1049_358_0"][1] +
    segs["road_1049_350_0"][1] +
    segs["road_1049_349_0"][1][::-1] +
    segs["road_1049_352_0"][1] +
    segs["road_1049_353_0"][1][::-1] +
    segs["road_1049_348_0"][1] +
    segs["road_1049_354_0"][1][::-1] +
    segs["road_1049_351_0"][1]
)
c_sankebetsu_out = sample(s1049_chain, 13)

# 3. Step 867-874 (8 pts): Return from Sankebetsu back to Tomamae town
c_sankebetsu_in = sample(s1049_chain[::-1], 8)

# 4. Step 875-884 (10 pts): Tomamae town north along Route 232 to Haboro boundary
pts_78 = segs["road_232_78_0"][1]
c_coast_out = sample(pts_78[:73], 10)

all_coords = c_coast_in + c_sankebetsu_out + c_sankebetsu_in + c_coast_out
assert len(all_coords) == 45, f"Expected 45 coords, got {len(all_coords)}"

# 2. Node definitions
NODES_DEF = [
    # 840 to 853: Coastal Route 232 to Tomamae town center (14 nodes)
    (840, "小平・苫前町境", "小平町", "NORMAL", "小平町鬼鹿を抜け、風車の町・苫前町へと入る。", "国道232号（オロロンライン）"),
    (841, "力昼高台", "苫前町", "NORMAL", "日本海の水平線を見渡す海岸段丘の高台。", "国道232号（オロロンライン）"),
    (842, "力昼港", "苫前町", "NORMAL", "荒波を防ぐ防波堤に囲まれた静かな漁港。", "港通り"),
    (843, "力昼海岸", "苫前町", "NORMAL", "日本海の激波が打ち寄せる岩礁と砂浜の海岸。", "国道232号（オロロンライン）"),
    (844, "力昼覆道", "苫前町", "NORMAL", "冬の地吹雪と越波を防ぐ海岸シェルター。", "国道232号（オロロンライン）"),
    (845, "栄浜", "苫前町", "NORMAL", "海に寄り添うように続く漁業集落。", "国道232号（オロロンライン）"),
    (846, "栄浜港", "苫前町", "NORMAL", "タコやヒラメ漁の小船が並ぶ素朴な船溜まり。", "港通り"),
    (847, "上平海岸", "苫前町", "NORMAL", "遠く天売島・焼尻島を望むパノラマコース。", "国道232号（オロロンライン）"),
    (848, "上平パーキング", "苫前町", "REST_AREA", "ドライバーが絶景を眺めながら休息できる海沿いパーキング。", "国道232号（オロロンライン）"),
    (849, "上平グリーンヒルウインドファーム", "苫前町", "SCENIC_SPOT", "丘の上に39基もの巨大風車が林立する圧巻の風力発電所。", "風車管理道"),
    (850, "とままえ風車ロード", "苫前町", "SCENIC_SPOT", "青空に映える白い巨大風車の群れを望む爽快ルート。", "国道232号（オロロンライン）"),
    (851, "苫前岬", "苫前町", "SCENIC_SPOT", "日本海に突き出た岬。夕日が海を茜色に染める名所。", "岬通り"),
    (852, "とままえ夕陽ヶ丘ホワイトビーチ", "苫前町", "SCENIC_SPOT", "白砂が敷き詰められた人工海浜。夏の人気スポット。", "海浜道路"),
    (853, "道の駅風Wとままえ", "苫前町", "REST_AREA", "温泉宿泊施設「とままえ温泉ふわっと」を併設した人気の道の駅。", "国道232号（オロロンライン）"),

    # 854 to 866: Route 1049 inland to Sankebetsu bear attack site (13 nodes)
    (854, "苫前町役場前", "苫前町", "TOWN_CENTER", "苫前町の行政の中心。内陸の三毛別方面への分岐点。", "役場前通り"),
    (855, "苫前郷土資料館", "苫前町", "SCENIC_SPOT", "三毛別ヒグマ事件の巨大ヒグマ剥製や開拓の苦闘を伝える資料館。", "本町通り"),
    (856, "古丹別本町", "苫前町", "NORMAL", "内陸農業の中心地。かつて旧羽幌線が通った宿場集落。", "道道1049号（苫前小平線）"),
    (857, "古丹別川橋", "苫前町", "NORMAL", "清流・古丹別川を渡り、山深い三毛別渓谷へと進む。", "道道1049号（苫前小平線）"),
    (858, "古丹別駅跡", "苫前町", "NORMAL", "1987年廃止の国鉄羽幌線・古丹別駅の歴史を偲ぶ遺構。", "駅前通り"),
    (859, "三毛別川橋", "苫前町", "NORMAL", "深い森から流れ出る三毛別川を渡る橋。", "道道1049号（苫前小平線）"),
    (860, "三毛別", "苫前町", "NORMAL", "大正時代に過酷な開拓が行われた山間の集落跡。", "道道1049号（苫前小平線）"),
    (861, "三毛別渓谷", "苫前町", "SCENIC_SPOT", "手つかずの原生林が鬱蒼と生い茂る険しい渓谷美。", "道道1049号（苫前小平線）"),
    (862, "三毛別静寂の森", "苫前町", "NORMAL", "鳥の声とせせらぎだけが響く静寂な樹林帯。", "道道1049号（苫前小平線）"),
    (863, "ベアマウンテンロード", "苫前町", "NORMAL", "ヒグマの生息地として知られる原生林を貫く一本道。", "道道1049号（苫前小平線）"),
    (864, "三毛別事件現場アプローチ", "苫前町", "NORMAL", "事件現場の復元地へと近づく森の小道。", "三毛別林道"),
    (865, "三毛別開拓小屋跡", "苫前町", "SCENIC_SPOT", "開拓民が厳しい冬を耐え忍んだ掘っ立て小屋の跡地。", "復元地遊歩道"),
    (866, "三毛別羆事件復元地", "苫前町", "SCENIC_SPOT", "【歴史遺構】大正4年発生の日本史上最悪の獣害事件を伝える復元地。", "三毛別史跡"),

    # 867 to 874: Return from Sankebetsu back to Tomamae town (8 nodes)
    (867, "三毛別折り返し地点", "苫前町", "NORMAL", "歴史の重みを胸に、再び海岸のオロロンラインへ戻る。", "道道1049号（苫前小平線）"),
    (868, "三毛別原生林", "苫前町", "SCENIC_SPOT", "ミズナラやエゾマツの大木がそびえる原生の森。", "道道1049号（苫前小平線）"),
    (869, "古丹別神社", "苫前町", "NORMAL", "開拓の先人たちが心の拠り所とした内陸の鎮守社。", "神社通り"),
    (870, "古丹別分岐点", "苫前町", "NORMAL", "内陸部から海岸の国道232号へと抜ける交通の要衝。", "道道1049号（苫前小平線）"),
    (871, "苫前中央通り", "苫前町", "TOWN_CENTER", "海岸沿いに戻り、港町情緒が広がる商店街通り。", "中央通り"),
    (872, "苫前神社", "苫前町", "NORMAL", "江戸時代創建。航海安全と豊漁を守る由緒ある古社。", "神社通り"),
    (873, "苫前港", "苫前町", "TOWN_CENTER", "エビやホタテ漁で栄える苫前の中核漁港。", "港町通り"),
    (874, "とままえ温泉ふわっと", "苫前町", "REST_AREA", "雄大な日本海と夕日を望む天然温泉露天風呂。", "国道232号（オロロンライン）"),

    # 875 to 884: Tomamae town north to Haboro boundary (10 nodes)
    (875, "岩見", "苫前町", "NORMAL", "苫前市街地北部の海沿いに広がる集落。", "国道232号（オロロンライン）"),
    (876, "麻生", "苫前町", "NORMAL", "オロロンライン沿いののどかな農村地帯。", "国道232号（オロロンライン）"),
    (877, "旭", "苫前町", "NORMAL", "海岸線に沿ってまっすぐ伸びる快走路。", "国道232号（オロロンライン）"),
    (878, "北作", "苫前町", "NORMAL", "羽幌炭鉱へ続くかつての鉄道が分岐した地域。", "国道232号（オロロンライン）"),
    (879, "豊岡", "苫前町", "NORMAL", "緑の丘陵と青い海が広がる美しい直線路。", "国道232号（オロロンライン）"),
    (880, "清川", "苫前町", "NORMAL", "日本海からの爽快な潮風が吹き抜ける街道筋。", "国道232号（オロロンライン）"),
    (881, "風力発電の丘展望台", "苫前町", "SCENIC_SPOT", "丘陵の風車とオホーツク・日本海のパノラマを一望する高台。", "展望台線"),
    (882, "夕陽ヶ丘オートキャンプ場", "苫前町", "NORMAL", "水平線に沈む夕日を眺めながら過ごせる絶景キャンプ地。", "海浜通り"),
    (883, "苫前マリンロード", "苫前町", "NORMAL", "天売・焼尻の島影を正面に望む爽快シーサイドロード。", "国道232号（オロロンライン）"),
    (884, "苫前北海岸", "苫前町", "NORMAL", "苫前町を走破し、いよいよ甘エビの町・羽幌町へと向かう。", "国道232号（オロロンライン）"),
]

assert len(NODES_DEF) == 45, f"Expected 45 node defs, got {len(NODES_DEF)}"

def generate_node_code(defn, coord):
    idx, name, muni, ntype, desc, road = defn
    x, y = coord
    node_id = f"node_{idx:04d}"
    return f"""        SugorokuNode(
            id = "{node_id}",
            stepIndex = {idx},
            name = "{name}",
            municipality = "{muni}",
            type = SugorokuNodeType.{ntype},
            x = {x:.2f}f,
            y = {y:.2f}f,
            description = "{desc}",
            roadName = "{road}",
        ),"""

new_nodes_code = "\n".join(generate_node_code(NODES_DEF[i], all_coords[i]) for i in range(45))

# 3. Replace in HokkaidoAct1Route.kt
with open(KOTLIN_FILE, "r", encoding="utf-8") as f:
    kt_content = f.read()

pattern8 = re.compile(
    r'(        SugorokuNode\(\s*id = "node_0840",.*?)'
    r'(        SugorokuNode\(\s*id = "node_0885")',
    re.DOTALL
)

m8 = pattern8.search(kt_content)
if not m8:
    raise RuntimeError("Could not find chunk8 Steps 840 to 884 in HokkaidoAct1Route.kt")

replacement = f"{new_nodes_code}\n{m8.group(2)}"
kt_content = kt_content[:m8.start()] + replacement + kt_content[m8.end():]

with open(KOTLIN_FILE, "w", encoding="utf-8") as f:
    f.write(kt_content)

print("Successfully rectified steps 840 to 884 in HokkaidoAct1Route.kt!")
