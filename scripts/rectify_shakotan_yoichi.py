#!/usr/bin/env python3
"""
Rectify Shakotan Peninsula - Yoichi - Otaru West section (Steps 430 to 520).
- Snaps Cape Kamui to authentic SVG peak (77.87, 204.23).
- Uses authentic road binary points for Cape Shakotan (road_913_314_0), Route 229 (road_229_55_0), and Route 5 (road_5;229_22_0 / road_5;229_12_0).
- Eliminates duplicate Kamoenai entries.
- Connects seamlessly: Step 429 -> 430 (0.00 px), and Step 520 -> 521 (2.08 px).
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

# 1. Kamui Cape (16 pts): from (89.69, 199.87) to (77.87, 204.23) and back to (88.0, 199.07)
peak = (77.87, 204.23)
start_kamui = (89.69, 199.87)
pts_kamui_out = [(round(start_kamui[0] + (peak[0]-start_kamui[0])*i/7, 2),
                  round(start_kamui[1] + (peak[1]-start_kamui[1])*i/7, 2)) for i in range(8)]
end_kamui = (88.0, 199.07)
pts_kamui_back = [(round(peak[0] + (end_kamui[0]-peak[0])*(i+1)/8, 2),
                   round(peak[1] + (end_kamui[1]-peak[1])*(i+1)/8, 2)) for i in range(8)]
c_kamui = pts_kamui_out + pts_kamui_back
assert len(c_kamui) == 16

# 2. Shakotan Cape / Bikuni: 20 pts from road_913_314_0
pts_913 = segs["road_913_314_0"][1]
c_shakotan = sample(pts_913, 20)

# 3. Furubira & Yoichi: 40 pts from road_229_55_0[::-1]
pts_55_rev = segs["road_229_55_0"][1][::-1]
c_yoichi = sample(pts_55_rev, 40)

# 4. Otaru west (15 pts): road_5;229_22_0 + road_5;229_12_0[::-1]
pts_otaru = segs["road_5;229_22_0"][1] + segs["road_5;229_12_0"][1][::-1]
c_otaru = sample(pts_otaru, 15)

all_coords = c_kamui + c_shakotan + c_yoichi + c_otaru
assert len(all_coords) == 91, f"Expected 91 coords, got {len(all_coords)}"

# 2. Node definitions
NODES_DEF = [
    # --- 430 to 445: Cape Kamui (16 nodes) ---
    (430, "神恵内・積丹町境", "積丹町", "NORMAL", "神恵内村から、積丹ブルーと絶壁が続く積丹町へ入る。", "国道229号（追分ソーランライン）"),
    (431, "神威岬入口", "積丹町", "NORMAL", "積丹半島最西端・神威岬へのアプローチ道路入口。", "岬アクセス路"),
    (432, "神威岬駐車場", "積丹町", "REST_AREA", "広大な駐車場とお土産・食事処が揃う観光拠点。", "岬アクセス路"),
    (433, "女人禁制の門", "積丹町", "SCENIC_SPOT", "かつて女性の立ち入りが禁じられていた歴史を伝える門。", "チャレンカの小道"),
    (434, "チャレンカの小道", "積丹町", "SCENIC_SPOT", "両側が切り立った断崖の尾根を行く約800mの絶景遊歩道。", "チャレンカの小道"),
    (435, "神威岬尾根道", "積丹町", "SCENIC_SPOT", "エゾカンゾウが咲き乱れ、300度の水平線が広がる尾根。", "チャレンカの小道"),
    (436, "神威岬灯台", "積丹町", "SCENIC_SPOT", "明治21年点灯。過酷な日本海を見守り続ける歴史的灯台。", "神威岬灯台"),
    (437, "神威岩", "積丹町", "SCENIC_SPOT", "【絶景】神威岬の先端にそびえる積丹ブルーの象徴！", "神威岬展望地"),
    (438, "神威岬展望広場", "積丹町", "SCENIC_SPOT", "日本海のコバルトブルーと奇岩を一望するパノラマ展望台。", "神威岬展望地"),
    (439, "神威岬自然遊歩道", "積丹町", "SCENIC_SPOT", "海風を感じながら岬の雄大な自然を体感する散策路。", "チャレンカの小道"),
    (440, "積丹ブルーパノラマ", "積丹町", "SCENIC_SPOT", "澄み切った海の底まで見える圧倒的な透明度。", "岬アクセス路"),
    (441, "カムイ番屋", "積丹町", "REST_AREA", "積丹名物・積丹ブルーソフトや海鮮丼が人気のレストハウス。", "岬アクセス路"),
    (442, "余別アプローチ", "積丹町", "NORMAL", "神威岬を後にし、積丹半島北岸の余別へと向かう。", "国道229号"),
    (443, "余別", "積丹町", "NORMAL", "ウニ漁の中心地として知られる活気ある集落。", "国道229号"),
    (444, "余別港", "積丹町", "NORMAL", "早朝からウニ漁船が往来する活気あふれる漁港。", "港通り"),
    (445, "余別前浜", "積丹町", "NORMAL", "奇岩と磯浜が広がるウニの宝庫。", "道道913号（野塚婦美線）"),

    # --- 446 to 465: Cape Shakotan & Shimamui Coast (20 nodes) ---
    (446, "草木", "積丹町", "NORMAL", "積丹半島北端沿いの静かな海岸集落。", "道道913号（野塚婦美線）"),
    (447, "西河", "積丹町", "NORMAL", "澄んだ海と岩礁が広がる風光明媚な地。", "道道913号（野塚婦美線）"),
    (448, "野塚", "積丹町", "NORMAL", "広大な野塚海岸が広がるリゾートエリア。", "道道913号（野塚婦美線）"),
    (449, "野塚野営場", "積丹町", "NORMAL", "砂浜のすぐそばでキャンプが楽しめる野営地。", "道道913号（野塚婦美線）"),
    (450, "日司", "積丹町", "NORMAL", "海産物の加工場が並ぶ伝統ある漁村。", "道道913号（野塚婦美線）"),
    (451, "日司港", "積丹町", "NORMAL", "積丹の豊かな海の幸が水揚げされる港。", "港通り"),
    (452, "積丹出岬灯台", "積丹町", "SCENIC_SPOT", "積丹半島最北端の海を照らす白亜の灯台。", "積丹岬アクセス路"),
    (453, "島武意海岸", "積丹町", "SCENIC_SPOT", "日本の渚百選。積丹ブルーに輝く奇岩と絶壁の入り江。", "島武意遊歩道"),
    (454, "島武意トンネル", "積丹町", "SCENIC_SPOT", "素掘りのトンネルを抜けると広がる積丹ブルーの絶景。", "島武意歩道"),
    (455, "積丹岬自然歩道", "積丹町", "SCENIC_SPOT", "断崖絶壁の上を縫うように続く雄大なパノラマ歩道。", "積丹岬遊歩道"),
    (456, "女郎子岩", "積丹町", "SCENIC_SPOT", "悲しい伝説を秘めた海上に立つスレンダーな奇岩柱。", "積丹岬遊歩道"),
    (457, "入舸", "積丹町", "NORMAL", "島武意海岸の東側に位置する漁業集落。", "道道913号（野塚婦美線）"),
    (458, "入舸港", "積丹町", "NORMAL", "ウニやイカ漁で賑わう天然の良港。", "港通り"),
    (459, "幌武意", "積丹町", "NORMAL", "険しい断崖の下に切り拓かれた漁業の町。", "道道913号（野塚婦美線）"),
    (460, "幌武意港", "積丹町", "NORMAL", "釣り人にも親しまれる美しい港。", "港通り"),
    (461, "マッカ岬", "積丹町", "SCENIC_SPOT", "日本海に突き出た断崖と岩礁の景勝地。", "道道913号（野塚婦美線）"),
    (462, "美国町中心街", "積丹町", "TOWN_CENTER", "積丹町役場が位置する積丹観光と行政の中心拠点。", "国道229号"),
    (463, "美国港", "積丹町", "TOWN_CENTER", "観光船が発着する積丹半島最大の港。", "港町通り"),
    (464, "水中展望船ニューしゃこたん号", "積丹町", "SCENIC_SPOT", "船底から積丹ブルーの海底とウニを観察できる遊覧船。", "美国港桟橋"),
    (465, "黄金岬", "積丹町", "SCENIC_SPOT", "ニシンの大群が海を黄金色に染めたという伝説の岬。", "黄金岬遊歩道"),

    # --- 466 to 480: Furubira town (15 nodes) ---
    (466, "宝代", "積丹町", "NORMAL", "美国から古平町へ向かう海岸道路沿いの集落。", "国道229号"),
    (467, "船澗", "積丹町", "NORMAL", "北前船が風待ち停泊した歴史ある入江。", "国道229号"),
    (468, "小泊", "積丹町", "NORMAL", "海に迫る山裾を行くシーサイドロード。", "国道229号"),
    (469, "厚苫", "積丹町", "NORMAL", "断崖と砂浜が交互に現れる海岸線。", "国道229号"),
    (470, "古平町境", "古平町", "NORMAL", "積丹町からタラコと歴史の町・古平町へ入る。", "国道229号"),
    (471, "古平港", "古平町", "NORMAL", "スケトウダラやサケ漁で活気あふれる良港。", "港町通り"),
    (472, "古平本町", "古平町", "TOWN_CENTER", "古平町役場や商店が集まる中心市街地。", "本町通り"),
    (473, "セタカムイ岩", "古平町", "SCENIC_SPOT", "遠吠えする犬の姿をした哀話の奇岩巨石。", "国道229号"),
    (474, "新家鰊番屋", "古平町", "SCENIC_SPOT", "古平のニシン漁の繁栄を今に伝える歴史的建造物。", "国道229号"),
    (475, "浜町", "古平町", "NORMAL", "海岸沿いに広がる漁港の町並み。", "国道229号"),
    (476, "古平川橋", "古平町", "NORMAL", "サケが遡上する清流・古平川を渡る大橋。", "国道229号"),
    (477, "稲倉石", "古平町", "NORMAL", "かつてマンガン鉱山として栄えた歴史を持つ地域。", "道道998号"),
    (478, "丸山公園", "古平町", "SCENIC_SPOT", "桜の名所として親しまれる町民憩いの丘陵公園。", "丸山通り"),
    (479, "家族旅行村", "古平町", "REST_AREA", "温泉とコテージが整備された体験型観光拠点。", "公園道路"),
    (480, "歌棄海岸", "古平町", "NORMAL", "古平と余市の境に広がる雄大な岩礁海岸。", "国道229号"),

    # --- 481 to 505: Yoichi town (25 nodes) ---
    (481, "古平出戸", "古平町", "NORMAL", "古平町東端の静かな海岸集落。", "国道229号"),
    (482, "余市町境", "余市町", "NORMAL", "古平町からウイスキーとフルーツの郷・余市町へ越境する。", "国道229号"),
    (483, "豊浜", "余市町", "NORMAL", "日本海の荒波が打ち寄せる海岸集落。", "国道229号"),
    (484, "ローソク岩", "余市町", "SCENIC_SPOT", "海中から天を突くようにそそり立つ高さ46mの奇岩。", "国道229号"),
    (485, "白岩", "余市町", "NORMAL", "白い岩肌の断崖が続く景勝の海岸。", "国道229号"),
    (486, "ワッカケ岬", "余市町", "SCENIC_SPOT", "岩礁地帯が広がる磯釣りの名所。", "国道229号"),
    (487, "沖村", "余市町", "NORMAL", "フゴッペ洞窟近くの歴史ある集落。", "国道229号"),
    (488, "フゴッペ洞窟", "余市町", "SCENIC_SPOT", "続縄文時代の岩面刻画が残る国指定史跡の洞窟。", "国道229号"),
    (489, "栄町", "余市町", "NORMAL", "余市川河口近くの閑静な住宅街。", "国道229号"),
    (490, "梅川", "余市町", "NORMAL", "果樹園が広がる緑豊かな地域。", "フルーツ街道"),
    (491, "大川", "余市町", "NORMAL", "余市川沿いの落ち着いた街並み。", "国道229号"),
    (492, "余市川橋", "余市町", "NORMAL", "アユやサケが泳ぐ清流・余市川を渡る大橋。", "国道229号"),
    (493, "黒川町", "余市町", "NORMAL", "JR余市駅前の主要幹線街区。", "国道229号"),
    (494, "ニッカウヰスキー余市蒸溜所", "余市町", "SCENIC_SPOT", "日本のウイスキーの父・竹鶴政孝が創業した聖地。", "リタロード"),
    (495, "JR余市駅前", "余市町", "TOWN_CENTER", "北のフルーツ王国・余市の玄関口となる主要駅前。", "駅前通り"),
    (496, "道の駅スペース・アップルよいち", "余市町", "REST_AREA", "宇宙記念館を併設した人気の道の駅。リンゴ製品が充実。", "国道229号"),
    (497, "余市宇宙記念館", "余市町", "SCENIC_SPOT", "余市出身の宇宙飛行士・毛利衛氏の偉業を伝える記念館。", "駅前通り"),
    (498, "モイレ岬", "余市町", "SCENIC_SPOT", "余市港の突端に位置する景勝地。", "臨港道路"),
    (499, "余市港", "余市町", "TOWN_CENTER", "古くからニシン漁とサケ漁で栄えた良港。", "港町通り"),
    (500, "余市水産博物館", "余市町", "SCENIC_SPOT", "ニシン漁の歴史や海産資源の展示が充実した博物館。", "富沢通り"),
    (501, "旧下ヨイチ運上家", "余市町", "SCENIC_SPOT", "国指定重要文化財。唯一現存する江戸時代の運上家建築。", "国道229号"),
    (502, "浜中", "余市町", "NORMAL", "海水浴場が広がる海辺の集落。", "国道5号"),
    (503, "登川橋", "余市町", "NORMAL", "小樽方面へと向かう国道5号の幹線橋。", "国道5号"),
    (504, "潮見町", "余市町", "NORMAL", "小樽への峠道が始まる海沿いの高台。", "国道5号"),
    (505, "余市・小樽市境", "余市町", "NORMAL", "余市町から歴史と坂の街・小樽市へ入る。", "国道5号"),

    # --- 506 to 520: Otaru city west (15 nodes) ---
    (506, "蘭島", "小樽市", "NORMAL", "明治期に開かれた北海道最古の海水浴場。", "国道5号"),
    (507, "蘭島駅前", "小樽市", "NORMAL", "JR函館本線の駅前。海水浴シーズンで賑わう。", "国道5号"),
    (508, "忍路", "小樽市", "NORMAL", "天然の良港と古代遺跡を抱く静かな入り江。", "道道1178号"),
    (509, "忍路環状列石", "小樽市", "SCENIC_SPOT", "縄文時代後期の巨大なストーンサークル遺跡。", "忍路林道"),
    (510, "忍路港", "小樽市", "NORMAL", "三方を山に囲まれた箱庭のような美しい港。", "港通り"),
    (511, "兜岬（小樽）", "小樽市", "SCENIC_SPOT", "忍路湾を見下ろす断崖の岬。", "道道1178号"),
    (512, "塩谷", "小樽市", "NORMAL", "伊藤整の文学碑が立つ風光明媚な海岸集落。", "国道5号"),
    (513, "塩谷海岸", "小樽市", "NORMAL", "シーカヤックのメッカとして知られる美しい砂浜。", "国道5号"),
    (514, "塩谷駅前", "小樽市", "NORMAL", "小樽西部の山間に位置する静かな無人駅。", "国道5号"),
    (515, "文洋", "小樽市", "NORMAL", "塩谷からオタモイへと続く峠道の集落。", "国道5号"),
    (516, "オタモイ海岸入口", "小樽市", "NORMAL", "断崖絶壁の名勝・オタモイ海岸への分岐点。", "オタモイ通り"),
    (517, "オタモイ海岸", "小樽市", "SCENIC_SPOT", "龍宮閣の歴史を秘めた切り立つ断崖絶壁の景勝地。", "オタモイ遊歩道"),
    (518, "オタモイ地蔵尊", "小樽市", "SCENIC_SPOT", "海上安全と子宝を祈願して祀られた霊場。", "オタモイ遊歩道"),
    (519, "幸町", "小樽市", "NORMAL", "小樽市街地へと下る落ち着いた住宅街。", "国道5号"),
    (520, "長橋", "小樽市", "NORMAL", "小樽市街地への西の玄関口となる街並み。", "国道5号"),
]

assert len(NODES_DEF) == 91, f"Expected 91 node defs, got {len(NODES_DEF)}"

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

# Split into Chunk 4 (430 to 499: 70 nodes) and Chunk 5 (500 to 520: 21 nodes)
chunk4_nodes_code = "\n".join(generate_node_code(NODES_DEF[i], all_coords[i]) for i in range(70))
chunk5_nodes_code = "\n".join(generate_node_code(NODES_DEF[i], all_coords[i]) for i in range(70, 91))

# 3. Replace in HokkaidoAct1Route.kt
with open(KOTLIN_FILE, "r", encoding="utf-8") as f:
    kt_content = f.read()

# Replace in chunk4: from node_0430 to node_0499
pattern4 = re.compile(
    r'(        SugorokuNode\(\s*id = "node_0430",.*?)'
    r'(\n    \)\n\n    private fun chunk5\(\): List<SugorokuNode> = listOf\()',
    re.DOTALL
)

m4 = pattern4.search(kt_content)
if not m4:
    raise RuntimeError("Could not find chunk4 Steps 430 to 499 in HokkaidoAct1Route.kt")

kt_content = kt_content[:m4.start()] + chunk4_nodes_code + m4.group(2) + kt_content[m4.end():]

# Replace in chunk5: from node_0500 to node_0520
pattern5 = re.compile(
    r'(    private fun chunk5\(\): List<SugorokuNode> = listOf\(\n)'
    r'(        SugorokuNode\(\s*id = "node_0500",.*?)'
    r'(        SugorokuNode\(\s*id = "node_0521")',
    re.DOTALL
)

m5 = pattern5.search(kt_content)
if not m5:
    raise RuntimeError("Could not find chunk5 Steps 500 to 520 in HokkaidoAct1Route.kt")

replacement5 = f"{m5.group(1)}{chunk5_nodes_code}\n{m5.group(3)}"
kt_content = kt_content[:m5.start()] + replacement5 + kt_content[m5.end():]

with open(KOTLIN_FILE, "w", encoding="utf-8") as f:
    f.write(kt_content)

print("Successfully rectified steps 430 to 520 in HokkaidoAct1Route.kt!")
