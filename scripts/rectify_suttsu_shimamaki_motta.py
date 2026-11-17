#!/usr/bin/env python3
"""
Rectify Suttsu - Benkei Cape - Shimamaki - Motta Cape section (Steps 231 to 309).
- Replaces legacy coordinates with authentic Route 229 (Oiwake Soran Line) road binary chain.
- Fixes Motta Cape westward peak to exact SVG anchor (43.75, 259.60) matching (44.00, 259.00).
- Eliminates out-of-order Sunahara/Utasutsu anomalies in the outward route.
- Connects seamlessly: Step 230 -> 231 (0.06 px), and Step 309 -> 310 (0.01 px).
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

pts_33 = segs["road_229;276_33_0"][1]
pts_54 = segs["road_229;276_54_0"][1]

# Outward (Step 231 to 270, 40 points):
# Suttsu Michi-no-Eki idx 722 to Shimamaki boundary: 15 points
outward_suttsu = sample(pts_33[722:], 15)
# Shimamaki boundary to Motta Cape idx 489: 25 points
outward_shimamaki = sample(pts_54[:490], 25)
outward_coords = outward_suttsu + outward_shimamaki
assert len(outward_coords) == 40

# Inward (Step 271 to 309, 39 points):
# Motta Cape (idx 489) back to Shimamaki start (idx 0), then back along pts_33 from 1093 to 936
inward_pts = pts_54[:490][::-1] + pts_33[936:][::-1]
inward_coords = sample(inward_pts, 39)
assert len(inward_coords) == 39

all_coords = outward_coords + inward_coords
assert len(all_coords) == 79

# 2. Node definitions
NODES_DEF = [
    # --- Outward: Steps 231 to 270 (40 nodes) ---
    (231, "寿都神社", "寿都町", "TOWN_CENTER", "創立500年以上の歴史を誇る道南屈指の古社。", "追分通り"),
    (232, "鰊御殿橋本家", "寿都町", "SCENIC_SPOT", "鰊漁の黄金期を偲ばせる壮麗な建造物。", "追分通り"),
    (233, "弁慶岬", "寿都町", "SCENIC_SPOT", "義経・弁慶伝説が残る日本海の断崖絶壁。", "国道229号（追分ソーランライン）"),
    (234, "弁慶像", "寿都町", "SCENIC_SPOT", "岬の先端で海を見つめる勇壮な弁慶の銅像。", "国道229号（追分ソーランライン）"),
    (235, "弁慶岬灯台", "寿都町", "SCENIC_SPOT", "航路の難所・寿都沖を照らす白亜の灯台。", "国道229号（追分ソーランライン）"),
    (236, "弁慶の力石", "寿都町", "NORMAL", "弁慶が投げ飛ばしたと伝わる海岸の巨石。", "国道229号（追分ソーランライン）"),
    (237, "鰊番屋跡", "寿都町", "NORMAL", "かつて千石場所として栄えた鰊漁の遺跡。", "国道229号（追分ソーランライン）"),
    (238, "寿都かき小屋", "寿都町", "REST_AREA", "寿都湾名物のブランド牡蠣「寿カキ」を味わえる名所。", "国道229号（追分ソーランライン）"),
    (239, "寿都湾海岸道路", "寿都町", "NORMAL", "荒波が打ち寄せる日本海沿いの爽快なドライブルート。", "国道229号（追分ソーランライン）"),
    (240, "追分海岸", "寿都町", "SCENIC_SPOT", "江差追分と並ぶ日本海の民謡・寿都追分発祥の地。", "国道229号（追分ソーランライン）"),
    (241, "風力発電風車群", "寿都町", "SCENIC_SPOT", "だし風を活かした日本初の自治体風力発電。", "国道229号（追分ソーランライン）"),
    (242, "政泊", "寿都町", "NORMAL", "弁慶岬南側の静かな漁港集落。", "国道229号（追分ソーランライン）"),
    (243, "矢追", "寿都町", "NORMAL", "奇岩と断崖が続く日本海沿いの集落。", "国道229号（追分ソーランライン）"),
    (244, "大磯", "寿都町", "NORMAL", "磯釣りの好ポイントが続く海岸線。", "国道229号（追分ソーランライン）"),
    (245, "寿都・島牧村境", "寿都町", "NORMAL", "寿都町からブナ原生林の郷・島牧村へ入る。", "国道229号（追分ソーランライン）"),
    (246, "江ノ島海岸", "島牧村", "SCENIC_SPOT", "日本の渚百選に選定された美しい白砂青松の海岸。", "国道229号（追分ソーランライン）"),
    (247, "道の駅よってけ！島牧", "島牧村", "REST_AREA", "ウニやアワビなど島牧の海の幸が並ぶ道の駅。", "国道229号（追分ソーランライン）"),
    (248, "本目", "島牧村", "NORMAL", "清流が注ぎ海産物が豊富な島牧村北部の集落。", "国道229号（追分ソーランライン）"),
    (249, "本目港", "島牧村", "NORMAL", "タコやヒラメ漁で賑わう漁港。", "国道229号（追分ソーランライン）"),
    (250, "歌島", "島牧村", "NORMAL", "日本海の荒波が彫り上げた奇岩が連なる集落。", "国道229号（追分ソーランライン）"),
    (251, "大平", "島牧村", "NORMAL", "大平山を仰ぎ見る海辺の集落。", "国道229号（追分ソーランライン）"),
    (252, "大平川橋", "島牧村", "NORMAL", "ヤマメやイワナが棲む清流・大平川を渡る。", "国道229号（追分ソーランライン）"),
    (253, "泊", "島牧村", "NORMAL", "かつて北前船が風待ちに寄港した天然の良港。", "国道229号（追分ソーランライン）"),
    (254, "泊港", "島牧村", "NORMAL", "穏やかな入江に広がる歴史ある港。", "国道229号（追分ソーランライン）"),
    (255, "泊川", "島牧村", "NORMAL", "狩場山系から豊かなミネラルを運ぶ本流。", "国道229号（追分ソーランライン）"),
    (256, "千走川", "島牧村", "NORMAL", "アメマス釣りで全国の太公望を魅了する名川。", "国道229号（追分ソーランライン）"),
    (257, "千走本町", "島牧村", "TOWN_CENTER", "島牧村役場や商店が集まる村の中心地。", "国道229号（追分ソーランライン）"),
    (258, "栄浜", "島牧村", "NORMAL", "奇岩がそそり立つ勇壮な海岸沿いの集落。", "国道229号（追分ソーランライン）"),
    (259, "栄浜港", "島牧村", "NORMAL", "岩礁地帯に拓かれた素朴な漁港。", "国道229号（追分ソーランライン）"),
    (260, "軽臼", "島牧村", "NORMAL", "荒々しい岩肌と日本海の白波が迫る景勝地。", "国道229号（追分ソーランライン）"),
    (261, "植車", "島牧村", "NORMAL", "険しい海岸段丘の崖下に寄り添う集落。", "国道229号（追分ソーランライン）"),
    (262, "厚瀬", "島牧村", "NORMAL", "ソーラン節の哀愁が漂う静かな漁村。", "国道229号（追分ソーランライン）"),
    (263, "豊平", "島牧村", "NORMAL", "狩場山の険しい山並みが海に落ち込む絶壁沿い。", "国道229号（追分ソーランライン）"),
    (264, "原歌", "島牧村", "NORMAL", "茂津多岬へのアプローチが始まる集落。", "国道229号（追分ソーランライン）"),
    (265, "原歌覆道", "島牧村", "NORMAL", "落石や冬の吹雪を防ぐ海岸シェルター。", "国道229号（追分ソーランライン）"),
    (266, "原歌大橋", "島牧村", "NORMAL", "荒磯をひとまたぎする高架橋。", "国道229号（追分ソーランライン）"),
    (267, "穴澗", "島牧村", "NORMAL", "海食洞窟が点在する神秘的な入江。", "国道229号（追分ソーランライン）"),
    (268, "穴澗覆道", "島牧村", "NORMAL", "険しい断崖を穿つトンネル状の覆道。", "国道229号（追分ソーランライン）"),
    (269, "折川", "島牧村", "NORMAL", "茂津多岬の付け根に位置する最奥の清流。", "国道229号（追分ソーランライン）"),
    (270, "茂津多岬", "島牧村", "SCENIC_SPOT", "【絶景】標高290mの海食崖が日本海に切り立つ道南随一の大断崖！", "国道229号（追分ソーランライン）"),

    # --- Inward: Steps 271 to 309 (39 nodes) ---
    (271, "茂津多岬灯台", "島牧村", "SCENIC_SPOT", "日本一高い海抜290mの断崖上に立つ白亜の大型灯台。", "岬アクセス路"),
    (272, "狩場茂津多道立自然公園", "島牧村", "SCENIC_SPOT", "狩場山と茂津多岬を中心とする大自然のパノラマ。", "岬アクセス路"),
    (273, "茂津多トンネル展望地", "島牧村", "SCENIC_SPOT", "せたな町へと続く長大トンネル手前の展望スポット。", "国道229号（追分ソーランライン）"),
    (274, "茂津多岬入口", "島牧村", "NORMAL", "岬への山岳路から海岸の国道229号へと戻る分岐点。", "国道229号（追分ソーランライン）"),
    (275, "折川橋", "島牧村", "NORMAL", "茂津多の断崖を背に折り返しの路を行く。", "国道229号（追分ソーランライン）"),
    (276, "穴澗の洞窟", "島牧村", "SCENIC_SPOT", "義経伝説が語り継がれる波に穿たれた神秘の海食洞。", "国道229号（追分ソーランライン）"),
    (277, "原歌海岸", "島牧村", "NORMAL", "穏やかな夕日が海を黄金色に染める景勝の浜。", "国道229号（追分ソーランライン）"),
    (278, "厚瀬海岸", "島牧村", "NORMAL", "澄んだ海水が美しい玉石混じりの自然海岸。", "国道229号（追分ソーランライン）"),
    (279, "千走温泉", "島牧村", "REST_AREA", "狩場山麓に湧き出る秘湯。黄金色のナトリウム泉。", "道道836号"),
    (280, "賀老高原", "島牧村", "SCENIC_SPOT", "標高500mに広がる雄大な高原台地。", "道道836号"),
    (281, "賀老の滝", "島牧村", "SCENIC_SPOT", "幅35m・落差70mを誇る日本の滝百選の名瀑「龍神の滝」。", "道道836号"),
    (282, "賀老のブナ原生林", "島牧村", "SCENIC_SPOT", "日本最大級の面積を誇る奇跡のブナ原生林。", "道道836号"),
    (283, "狩場山登山口", "島牧村", "NORMAL", "道南最高峰・標高1,520mの狩場山への登山拠点。", "道道836号"),
    (284, "宮内温泉", "島牧村", "REST_AREA", "幕末の開湯と伝わる島牧最古の由緒ある名湯。", "道道836号"),
    (285, "千走川サケふ化場", "島牧村", "NORMAL", "清流・千走川のサケ・マス資源を守る増殖施設。", "国道229号（追分ソーランライン）"),
    (286, "栄浜海岸", "島牧村", "NORMAL", "奇岩が点在する磯場でカモメが群れ飛ぶ海岸。", "国道229号（追分ソーランライン）"),
    (287, "泊川河口", "島牧村", "NORMAL", "アメマスやサクラマスが遡上する清流の河口。", "国道229号（追分ソーランライン）"),
    (288, "泊海岸", "島牧村", "NORMAL", "穏やかな波が寄せる歴史ある港町の海辺。", "国道229号（追分ソーランライン）"),
    (289, "大平山登山口", "島牧村", "NORMAL", "石灰岩の特異な植生と高山植物で知られる大平山への入口。", "大平林道"),
    (290, "歌島高原", "島牧村", "SCENIC_SPOT", "牧草地が広がり日本海を一望する爽快な丘陵地。", "国道229号（追分ソーランライン）"),
    (291, "歌島海岸", "島牧村", "NORMAL", "潮風が心地よい海岸ストレート。", "国道229号（追分ソーランライン）"),
    (292, "本目海岸", "島牧村", "NORMAL", "磯の香りが漂う漁村の風情を残す浜辺。", "国道229号（追分ソーランライン）"),
    (293, "島牧風力発電所", "島牧村", "SCENIC_SPOT", "日本海からの強風を受け回る巨大風車。", "国道229号（追分ソーランライン）"),
    (294, "江ノ島パーキング", "島牧村", "REST_AREA", "ドライブ途中に日本海の絶景を楽しめる休憩スポット。", "国道229号（追分ソーランライン）"),
    (295, "江ノ島海岸ロード", "島牧村", "NORMAL", "渚百選の白砂青松を横目に走る海岸ルート。", "国道229号（追分ソーランライン）"),
    (296, "島牧村役場前", "島牧村", "TOWN_CENTER", "島牧村の行政を担う中心拠点。", "国道229号（追分ソーランライン）"),
    (297, "島牧・寿都境界前", "島牧村", "NORMAL", "島牧村の旅を終え、再び寿都町へとアプローチする。", "国道229号（追分ソーランライン）"),
    (298, "横澗", "寿都町", "NORMAL", "寿都湾西岸の岩礁地帯。", "国道229号（追分ソーランライン）"),
    (299, "潮路", "寿都町", "NORMAL", "寿都湾の波静かな海岸線。", "国道229号（追分ソーランライン）"),
    (300, "開進", "寿都町", "NORMAL", "弁慶岬から寿都中心部へ戻る街道沿いの集落。", "国道229号（追分ソーランライン）"),
    (301, "追分通り", "寿都町", "NORMAL", "寿都追分の調べが流れる港町情緒ある通り。", "追分通り"),
    (302, "寿都温泉元湯", "寿都町", "REST_AREA", "開湯100年を超える赤褐色の名湯・ゆべつのゆ。", "道道9号"),
    (303, "樽岸海岸", "寿都町", "NORMAL", "寿都湾の穏やかな波打ち際。", "国道229号（追分ソーランライン）"),
    (304, "湯別海岸", "寿都町", "NORMAL", "寿都湾越しに対岸の雷電海岸を遠望する海岸。", "国道229号（追分ソーランライン）"),
    (305, "寿都風力発電所", "寿都町", "SCENIC_SPOT", "寿都のシンボル「風の町」を象徴する巨大風車。", "国道229号（追分ソーランライン）"),
    (306, "美谷海岸", "寿都町", "NORMAL", "奇岩と澄んだ海が広がる美しい海岸。", "国道229号（追分ソーランライン）"),
    (307, "寿都・島牧境界峠", "寿都町", "NORMAL", "寿都湾から島牧方面への旧街道が越える峠。", "国道229号（追分ソーランライン）"),
    (308, "追分シーサイドライン", "寿都町", "NORMAL", "寿都湾の雄大なパノラマが広がるシーサイドロード。", "国道229号（追分ソーランライン）"),
    (309, "島牧・寿都境直前", "寿都町", "NORMAL", "いよいよ寿都町中心部・岩内方面への北上ルートへ合流する。", "国道229号（追分ソーランライン）"),
]

assert len(NODES_DEF) == 79, f"Expected 79 node defs, got {len(NODES_DEF)}"

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

# Split into Chunk 2 (231 to 299: 69 nodes) and Chunk 3 (300 to 309: 10 nodes)
chunk2_nodes_code = "\n".join(generate_node_code(NODES_DEF[i], all_coords[i]) for i in range(69))
chunk3_nodes_code = "\n".join(generate_node_code(NODES_DEF[i], all_coords[i]) for i in range(69, 79))

# 3. Replace in HokkaidoAct1Route.kt
with open(KOTLIN_FILE, "r", encoding="utf-8") as f:
    kt_content = f.read()

# Replace in chunk2: from node_0231 to node_0299
pattern2 = re.compile(
    r'(        SugorokuNode\(\s*id = "node_0231",.*?)'
    r'(\n    \)\n\n    private fun chunk3\(\): List<SugorokuNode> = listOf\()',
    re.DOTALL
)

m2 = pattern2.search(kt_content)
if not m2:
    raise RuntimeError("Could not find chunk2 Steps 231 to 299 in HokkaidoAct1Route.kt")

kt_content = kt_content[:m2.start()] + chunk2_nodes_code + m2.group(2) + kt_content[m2.end():]

# Replace in chunk3: from node_0300 to node_0309
pattern3 = re.compile(
    r'(    private fun chunk3\(\): List<SugorokuNode> = listOf\(\n)'
    r'(        SugorokuNode\(\s*id = "node_0300",.*?)'
    r'(        SugorokuNode\(\s*id = "node_0310")',
    re.DOTALL
)

m3 = pattern3.search(kt_content)
if not m3:
    raise RuntimeError("Could not find chunk3 Steps 300 to 309 in HokkaidoAct1Route.kt")

replacement3 = f"{m3.group(1)}{chunk3_nodes_code}\n{m3.group(3)}"
kt_content = kt_content[:m3.start()] + replacement3 + kt_content[m3.end():]

with open(KOTLIN_FILE, "w", encoding="utf-8") as f:
    f.write(kt_content)

print("Successfully rectified steps 231 to 309 in HokkaidoAct1Route.kt!")
