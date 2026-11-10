#!/usr/bin/env python3
"""
Rectify Hakodate - Nanae - Onuma - Mori section (Steps 0 to 84).
- Fixes Mori town reverse/loop anomaly (removes out-of-order Sunahara loop).
- Connects Hakodate, Shindo, Onuma lake, Mori town, and Route 5 coastal chain directly to Step 85 (Yakumo boundary).
- Uses exact real-world road points from hokkaido_roads_high_density.bin.
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

# 2. Coordinates planning for Steps 0 to 84 (85 points)
# Step 0: Hakodate Station (START_HAKODATE)
pts_0 = [(105.01, 328.01)]

# Step 1 to 20: Hakodate city streetcar / central avenue (20 points)
pts_hakodate = [
    (104.84, 327.81), (104.78, 327.74), (104.67, 327.61), (104.56, 327.48), (104.45, 327.35),
    (104.33, 327.22), (104.22, 327.09), (104.11, 326.95), (104.00, 326.82), (103.88, 326.69),
    (103.77, 326.56), (103.66, 326.43), (103.55, 326.29), (103.43, 326.16), (103.32, 326.03),
    (103.21, 325.90), (102.97, 325.65), (102.99, 325.63), (102.11, 324.78), (101.49, 324.17),
]

# Step 21 to 38: Nanae / Hakodate Shindo (18 points) from road_5_24_0
pts_nanae = sample(segs["road_5_24_0"][1], 18)

# Step 39 to 60: Onuma Lake to Mori (22 points) from road_5_6_0
pts_onuma_mori = sample(segs["road_5_6_0"][1], 22)

# Step 61: Mori Station (76.00, 295.50)
pts_mori_station = [(76.00, 295.50)]

# Step 62 to 65: Mori center / Bypass (4 points) from road_5_184_0
pts_mori_center = sample(segs["road_5_184_0"][1], 4)

# Step 66 to 84: Mori coast Route 5 to Yakumo boundary (19 points) from road_5_53_0[:168]
pts_mori_coast = sample(segs["road_5_53_0"][1][:168], 19)

all_coords = pts_0 + pts_hakodate + pts_nanae + pts_onuma_mori + pts_mori_station + pts_mori_center + pts_mori_coast
assert len(all_coords) == 85, f"Expected 85 coords, got {len(all_coords)}"

# 3. Node Definitions
NODES_DEF = [
    # 0 to 20: Hakodate
    (0, "函館駅前", "函館市", "START_HAKODATE", "北海道冒険の始発駅。ここから最果て・相泊を目指す旅が始まる！", "国道5号"),
    (1, "若松町", "函館市", "NORMAL", "函館朝市の活気あふれる市場通り。新鮮な海の幸が並ぶ。", "国道5号"),
    (2, "大門", "函館市", "TOWN_CENTER", "古くからの歓楽街。赤提灯と昭和の風情が残る。", "国道5号"),
    (3, "松風町", "函館市", "NORMAL", "市電が往来するレトロな交差点。", "国道5号"),
    (4, "新川町", "函館市", "NORMAL", "静かな住宅と商店が並ぶ下町通り。", "国道5号"),
    (5, "千歳町", "函館市", "NORMAL", "市電沿いに古くからの町並みが続く。", "国道5号"),
    (6, "堀川町", "函館市", "NORMAL", "中島廉売へと続く下町の商店街。", "国道5号"),
    (7, "中島町", "函館市", "TOWN_CENTER", "市民の台所・中島廉売の活気ある通り。", "国道5号"),
    (8, "千代台公園", "函館市", "SCENIC_SPOT", "野球場や陸上競技場が集まる市民のスポーツ拠点。", "国道5号"),
    (9, "杉並町", "函館市", "NORMAL", "閑静な住宅街と文教施設が並ぶ通り。", "国道5号"),
    (10, "柏木町", "函館市", "NORMAL", "桜並木の美しい落ち着いた住宅地。", "国道5号"),
    (11, "五稜郭交差点", "函館市", "NORMAL", "本町繁華街と五稜郭を結ぶ交通の要衝。", "国道5号"),
    (12, "五稜郭タワー前", "函館市", "SCENIC_SPOT", "星形城郭を一望する高さ107mの展望タワー。", "道道571号"),
    (13, "五稜郭公園", "函館市", "SCENIC_SPOT", "幕末の歴史を刻む特別史跡・五稜郭跡。桜の名所。", "公園通り"),
    (14, "本町", "函館市", "TOWN_CENTER", "函館最大の歓楽街・繁華街が広がる中心地区。", "行啓通"),
    (15, "梁川町", "函館市", "NORMAL", "商業施設やオフィスが並ぶ市街地。", "国道5号"),
    (16, "テーオー前", "函館市", "NORMAL", "市内幹線道路沿いの大型商業エリア。", "国道5号"),
    (17, "亀田本町", "函館市", "NORMAL", "JR函館本線と並走する歴史ある本町通り。", "国道5号"),
    (18, "五稜郭駅前", "函館市", "TOWN_CENTER", "道南いさりび鉄道の分岐駅。鉄道交通の要衝。", "国道5号"),
    (19, "昭和タウンプラザ", "函館市", "TOWN_CENTER", "郊外型の大型ショッピングモールが集まる商業地。", "国道5号"),
    (20, "亀田川橋", "函館市", "NORMAL", "函館市街地を流れる亀田川を渡る大橋。", "国道5号"),

    # 21 to 38: Nanae / Hakodate Shindo
    (21, "西桔梗", "函館市", "NORMAL", "流通業務団地が広がる物流の拠点。", "国道5号（函館新道）"),
    (22, "桔梗町", "函館市", "NORMAL", "函館北部の住宅街と果樹園が広がる地域。", "国道5号（函館新道）"),
    (23, "桔梗駅前", "函館市", "NORMAL", "JR函館本線の駅前。郊外住宅地が広がる。", "国道5号（函館新道）"),
    (24, "桔梗北", "函館市", "NORMAL", "函館市北端の緩やかな丘陵地帯。", "国道5号（函館新道）"),
    (25, "道南四季の杜公園", "函館市", "SCENIC_SPOT", "広大な敷地に草花と展望台が広がる道立公園。", "国道5号（函館新道）"),
    (26, "七飯町境", "七飯町", "NORMAL", "函館市から西洋農業発祥の地・七飯町へ越境する。", "国道5号（函館新道）"),
    (27, "大中山", "七飯町", "NORMAL", "りんご畑が広がるのどかな田園地帯。", "国道5号（函館新道）"),
    (28, "大中山神社前", "七飯町", "NORMAL", "地域の鎮守として信仰を集める古社。", "国道5号（函館新道）"),
    (29, "中島通", "七飯町", "NORMAL", "大中山から七飯本町へと続く幹線道路。", "国道5号（函館新道）"),
    (30, "七飯駅通", "七飯町", "NORMAL", "JR七飯駅へと続く駅前大通り。", "国道5号（函館新道）"),
    (31, "七飯本町", "七飯町", "TOWN_CENTER", "七飯町役場が位置する行政と商業の中心。", "国道5号（赤松街道）"),
    (32, "鳴川", "七飯町", "NORMAL", "清流・鳴川沿いに広がる住宅と農地。", "国道5号（函館新道）"),
    (33, "赤松街道", "七飯町", "SCENIC_SPOT", "日本の道100選に選定された約14kmに及ぶ美しい赤松並木。", "国道5号（赤松街道）"),
    (34, "赤松並木北", "七飯町", "NORMAL", "樹齢100年を超える立派な赤松が続く街道北端。", "国道5号（赤松街道）"),
    (35, "峠下", "七飯町", "NORMAL", "大沼への峠越えアプローチが始まる交通結節点。", "国道5号（函館新道）"),
    (36, "道の駅なないろ・ななえ", "七飯町", "REST_AREA", "七飯町のりんごスイーツや特産品が並ぶ人気の道の駅。", "国道5号"),
    (37, "ラッキーピエロ峠下総本店", "七飯町", "SCENIC_SPOT", "テーマパークのような広大な敷地を誇るご当地バーガー総本店。", "国道5号"),
    (38, "藤城", "七飯町", "NORMAL", "新道大沼トンネルへの上り勾配が続く山麓地区。", "国道5号（函館新道）"),

    # 39 to 60: Onuma to Mori
    (39, "新道大沼トンネル北口", "七飯町", "NORMAL", "峠を貫く長いトンネルを抜け、大沼国定公園の玄関口へ。", "国道5号"),
    (40, "仁山高原", "七飯町", "SCENIC_SPOT", "スキー場や展望台が整備された駒ヶ岳山麓の高台。", "国道5号"),
    (41, "仁山駅前", "七飯町", "NORMAL", "急勾配のスイッチバックの歴史を残す山間の駅。", "国道5号"),
    (42, "大沼学園前", "七飯町", "NORMAL", "自然豊かな樹林帯に広がる文教施設。", "国道5号"),
    (43, "大沼公園駅前", "七飯町", "TOWN_CENTER", "大沼国定公園への観光客で賑わうクラシックな駅舎。", "道道338号"),
    (44, "大沼国定公園", "七飯町", "SCENIC_SPOT", "駒ヶ岳と湖沼群が織りなす南北海道屈指の絶景リゾート。", "湖畔周遊道路"),
    (45, "月見橋", "七飯町", "SCENIC_SPOT", "大沼と小沼を結ぶ水路にかかるビューポイント。", "湖畔周遊道路"),
    (46, "小沼湖畔", "七飯町", "SCENIC_SPOT", "スイレンや水鳥が憩う静寂の湖水風景。", "国道5号"),
    (47, "蓴菜沼", "七飯町", "SCENIC_SPOT", "天然のじゅんさいが自生する静かな湖沼。", "国道5号"),
    (48, "西大沼", "七飯町", "NORMAL", "国道5号とJR函館本線が並走する湖西岸の森。", "国道5号"),
    (49, "大沼湖水通", "七飯町", "NORMAL", "湖畔の爽やかな風が吹き抜けるドライブウェイ。", "国道5号"),
    (50, "軍川", "七飯町", "NORMAL", "駒ヶ岳西麓の高原農業が広がる集落。", "国道5号"),
    (51, "東大沼", "七飯町", "NORMAL", "キャンプ場や温泉が点在する東岸エリア。", "道道338号"),
    (52, "東大沼温泉", "七飯町", "REST_AREA", "大沼の自然に抱かれた素朴な名湯。", "道道338号"),
    (53, "鹿野温泉", "七飯町", "REST_AREA", "駒ヶ岳の恵みを受ける静かな隠れ湯。", "道道43号"),
    (54, "銚子口", "七飯町", "NORMAL", "砂原支線が分岐する七飯・森境界近くの集落。", "道道43号"),
    (55, "赤井川", "森町", "NORMAL", "七飯町から森町へ。駒ヶ岳西麓の高原集落。", "国道5号"),
    (56, "駒ヶ岳駅前", "森町", "NORMAL", "雄大な駒ヶ岳の山体を間近に仰ぐ駅前。", "国道5号"),
    (57, "姫川", "森町", "NORMAL", "清流・姫川沿いに続く静かな山間集落。", "国道5号"),
    (58, "森川橋", "森町", "NORMAL", "森川を渡り、内浦湾沿いの市街地へと向かう橋。", "国道5号"),
    (59, "東間", "森町", "NORMAL", "森町中心部へ至る国道5号沿いの街並み。", "国道5号"),
    (60, "森港", "森町", "TOWN_CENTER", "ホタテ養殖とサケ漁で活気にあふれる噴火湾の良港。", "港町通り"),

    # 61 to 65: Mori Center
    (61, "森駅前", "森町", "TOWN_CENTER", "名物「いかめし」の発祥地として全国に名高い駅前。", "国道5号"),
    (62, "オニウシ公園", "森町", "SCENIC_SPOT", "アイヌ語の「樹木の多い所」に由来する桜とツツジの名所。", "国道5号（森バイパス）"),
    (63, "青葉ヶ丘公園", "森町", "SCENIC_SPOT", "固有種「森小町」など千本を超える桜が咲き誇る名勝。", "国道5号"),
    (64, "道の駅YOU・遊・もり", "森町", "REST_AREA", "屋上展望台から噴火湾と駒ヶ岳を360度一望できる道の駅。", "国道5号（森バイパス）"),
    (65, "森バイパス", "森町", "NORMAL", "市街地を抜け、内浦湾沿いを快走するバイパス道路。", "国道5号（森バイパス）"),

    # 66 to 84: Mori coast Route 5 to Yakumo boundary
    (66, "白坂", "森町", "NORMAL", "噴火湾の波打ち際を行く国道5号沿いの集落。", "国道5号"),
    (67, "鳥崎渓谷", "森町", "SCENIC_SPOT", "鳥崎川上流の険しい渓谷と紅葉の名所。", "道道606号"),
    (68, "鳥崎八景", "森町", "SCENIC_SPOT", "屏風岩や駒ヶ岳を望む奇岩・景勝が続く名勝地。", "道道606号"),
    (69, "駒嶺", "森町", "NORMAL", "駒ヶ岳の裾野が海に迫る緑豊かな丘陵地。", "国道5号"),
    (70, "鷲ノ木史跡", "森町", "SCENIC_SPOT", "1868年、榎本武揚・土方歳三率いる旧幕府脱走軍が蝦夷地上陸を果たした歴史の地。", "国道5号"),
    (71, "鷲ノ木沢", "森町", "NORMAL", "鷲ノ木史跡から北へ伸びる沢沿いの集落。", "国道5号"),
    (72, "桂川", "森町", "NORMAL", "かつてJR函館本線の駅があった静かな海沿いの集落。", "国道5号"),
    (73, "蛯谷", "森町", "NORMAL", "噴火湾の断崖と海岸線が織りなす険しい地形を行く。", "国道5号"),
    (74, "蛯谷海岸", "森町", "SCENIC_SPOT", "内浦湾越しに対岸の羊蹄山や室蘭方面の山並みを望む海岸。", "国道5号"),
    (75, "石倉港", "森町", "NORMAL", "ホタテやサケの水揚げが行われる漁港。", "国道5号"),
    (76, "石倉駅前", "森町", "NORMAL", "国道5号と函館本線が並走する海沿いの集落。", "国道5号"),
    (77, "本茅部", "森町", "NORMAL", "古くからの漁業と温泉の歴史を持つ集落。", "国道5号"),
    (78, "茅部神社前", "森町", "NORMAL", "航海安全を祈願して祀られた由緒ある神社。", "国道5号"),
    (79, "濁川温泉入口", "森町", "NORMAL", "カルデラ盆地に湧く名湯・濁川温泉への分岐点。", "国道5号"),
    (80, "濁川温泉", "森町", "REST_AREA", "国内でも珍しいカルデラ盆地内に広がる歴史ある温泉郷。", "道道840号"),
    (81, "森地熱発電所", "森町", "SCENIC_SPOT", "濁川カルデラの豊富な地熱を利用した北海道初の地熱発電所。", "道道840号"),
    (82, "内浦湾シーサイドロード", "森町", "NORMAL", "穏やかな噴火湾を右手に望む爽快なシーサイドストレート。", "国道5号"),
    (83, "森町北端海岸", "森町", "NORMAL", "噴火湾越しに有珠山や昭和新山を遠望する海岸沿い。", "国道5号"),
    (84, "森・八雲町境直前", "森町", "NORMAL", "森町を走破し、いよいよ渡島総合振興局北部の要衝・八雲町へ向かう。", "国道5号"),
]

assert len(NODES_DEF) == 85, f"Expected 85 node defs, got {len(NODES_DEF)}"

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

new_nodes_code = "\n".join(generate_node_code(NODES_DEF[i], all_coords[i]) for i in range(85))

# 4. Replace in HokkaidoAct1Route.kt
with open(KOTLIN_FILE, "r", encoding="utf-8") as f:
    kt_content = f.read()

pattern = re.compile(
    r'(private fun chunk0\(\): List<SugorokuNode> = listOf\(\n)'
    r'(.*?)(        SugorokuNode\(\s*id = "node_0085")',
    re.DOTALL
)

match = pattern.search(kt_content)
if not match:
    raise RuntimeError("Could not find chunk0 Steps 0 to 84 block in HokkaidoAct1Route.kt")

replacement = f"{match.group(1)}{new_nodes_code}\n{match.group(3)}"
updated_content = kt_content[:match.start()] + replacement + kt_content[match.end():]

with open(KOTLIN_FILE, "w", encoding="utf-8") as f:
    f.write(updated_content)

print("Successfully rectified steps 0 to 84 in HokkaidoAct1Route.kt!")
