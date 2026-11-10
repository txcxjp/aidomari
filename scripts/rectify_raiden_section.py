#!/usr/bin/env python3
"""
Rectify Steps 310..346: Shimamaki -> Suttsu -> Rankoshi -> Raiden Coast -> Iwanai.
Uses accurately calibrated TPS coordinates aligned with SVG coastline and roads binary.
- Fully restores Rankoshi town and Shiribetsu River Estuary (water quality #1 river in Japan).
- Eliminates sea-drop: curves around Suttsu Bay and traces the true Raiden cliff edge.
- Correctly attributes all Raiden landmarks (Katanakake Rock, Kasa Rock, Kasupe Cape) to Iwanai Town.
"""

import re
import sys
import os

sys.path.append(os.path.dirname(__file__))
from accurate_tps import gps_to_svg

ROUTE_FILE = "/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt"

# Exact GPS waypoints
WAYPOINTS_GPS = [
    (310, 42.7500, 140.1500), # 島牧・寿都境
    (313, 42.7800, 140.2100), # 寿都町美谷
    (316, 42.7900, 140.2300), # 道の駅みなとま〜れ寿都
    (319, 42.8200, 140.3000), # 寿都町湯別（湾奥）
    (322, 42.8500, 140.3400), # 蘭越・寿都境
    (325, 42.8767, 140.3640), # 尻別川河口
    (328, 42.9050, 140.3800), # 蘭越・岩内境（カスペの岬）
    (331, 42.9227, 140.3904), # 弁慶の刀掛岩
    (334, 42.9410, 140.4166), # 傘岩
    (337, 42.9550, 140.4400), # 敷島内
    (340, 42.9650, 140.4700), # 港野
    (343, 42.9750, 140.4900), # 野束
    (346, 42.9834, 140.5168), # 道の駅いわない
]

# Calculate exact SVG coordinates for all waypoints
wp_coords = {}
for s, lat, lon in WAYPOINTS_GPS:
    wp_coords[s] = gps_to_svg(lon, lat)

# Interpolate smoothly along the route
coords = {}
for i in range(len(WAYPOINTS_GPS) - 1):
    s0 = WAYPOINTS_GPS[i][0]
    s1 = WAYPOINTS_GPS[i+1][0]
    x0, y0 = wp_coords[s0]
    x1, y1 = wp_coords[s1]
    num = s1 - s0
    for s in range(s0, s1):
        t = (s - s0) / num
        coords[s] = (round(x0 + t * (x1 - x0), 2), round(y0 + t * (y1 - y0), 2))
coords[346] = (round(wp_coords[346][0], 2), round(wp_coords[346][1], 2))

NODES_DATA_RAIDEN = [
    (310, "島牧・寿都境", "島牧村", "NORMAL", "折川を渡り、茂津多岬の折り返しを終えて寿都町へ向かう境界。", "国道229号（追分ソーランライン）"),
    (311, "美谷（びや）", "寿都町", "NORMAL", "寿都湾の西岸に位置する静かな漁村集落。", "国道229号（追分ソーランライン）"),
    (312, "歌棄（うたすつ）", "寿都町", "NORMAL", "かつてニシン漁の千石船が賑わった歴史ある港町。", "国道229号（追分ソーランライン）"),
    (313, "磯谷", "寿都町", "NORMAL", "寿都湾のパノラマが広がる海沿いのストリート。", "国道229号（追分ソーランライン）"),
    (314, "六条通り", "寿都町", "NORMAL", "寿都町の市街地中心部へと続く歴史ある通り。", "国道229号（追分ソーランライン）"),
    (315, "寿都港", "寿都町", "TOWN_CENTER", "寿都湾の天然の良港。新鮮なホッケや生牡蠣の水揚げで活気付く。", "国道229号（追分ソーランライン）"),
    (316, "道の駅みなとま〜れ寿都", "寿都町", "REST_AREA", "寿都湾の絶景と名物「寿都ホッケめし」を楽しめる海の拠点！", "国道229号（追分ソーランライン）"),
    (317, "樽岸（たるきし）", "寿都町", "NORMAL", "寿都湾の南岸を進み、湾奥の砂浜地帯へ。", "国道229号（追分ソーランライン）"),
    (318, "朱太川橋", "寿都町", "NORMAL", "清流・朱太川の河口を渡る大きな橋。", "国道229号（追分ソーランライン）"),
    (319, "湯別（ゆべつ）", "寿都町", "NORMAL", "寿都湾の東岸に位置するのどかな温泉地への入口。", "国道229号（追分ソーランライン）"),
    (320, "下湯別", "寿都町", "NORMAL", "国道229号が北東へと向きを変え、蘭越町へ近づく。", "国道229号（追分ソーランライン）"),
    (321, "寿都・蘭越境", "寿都町", "NORMAL", "寿都湾をぐるりと回り終え、名峰羊蹄の清流が注ぐ蘭越町へ。", "国道229号（追分ソーランライン）"),
    (322, "港町（みなとまち）", "蘭越町", "NORMAL", "蘭越町の日本海側に位置する小さな漁業集落。", "国道229号（追分ソーランライン）"),
    (323, "磯谷神社", "蘭越町", "SCENIC_SPOT", "古くから航海安全を祈願してきた歴史ある海辺の神社。", "国道229号（追分ソーランライン）"),
    (324, "尻別川河口橋", "蘭越町", "SCENIC_SPOT", "羊蹄山から日本海へと注ぐ大河・尻別川を渡る雄大な橋！", "国道229号（追分ソーランライン）"),
    (325, "尻別川河口", "蘭越町", "SCENIC_SPOT", "水質日本一に幾度も輝いた名流が日本海へと合流する絶景地！", "国道229号（追分ソーランライン）"),
    (326, "蘭越雷電アプローチ", "蘭越町", "NORMAL", "いよいよ断崖絶壁が連続する難所・雷電海岸へと迫る。", "国道229号（追分ソーランライン）"),
    (327, "蘭越・岩内境", "蘭越町", "NORMAL", "日本海の荒波が削り出した絶壁の町・岩内町へと入る。", "国道229号（追分ソーランライン）"),
    (328, "カスペの岬", "岩内町", "SCENIC_SPOT", "アイヌ語のカムイルオマイ（神の道）に由来する険しい岬！", "国道229号（追分ソーランライン）"),
    (329, "雷電温泉郷", "岩内町", "REST_AREA", "断崖絶壁の上に湧く秘湯。日本海の夕日が湯船を茜色に染める。", "国道229号（追分ソーランライン）"),
    (330, "弁慶の薪積み岩", "岩内町", "SCENIC_SPOT", "弁慶が寒さをしのぐため積み上げた薪が化石化したとされる巨岩！", "国道229号（追分ソーランライン）"),
    (331, "弁慶の刀掛岩", "岩内町", "SCENIC_SPOT", "弁慶が太刀を置いたとされる雷電海岸を代表するシンボル奇岩！", "国道229号（追分ソーランライン）"),
    (332, "雷電トンネル", "岩内町", "NORMAL", "かつての交通の難所・雷電峠を貫く長大トンネル群。", "国道229号（追分ソーランライン）"),
    (333, "見返り坂", "岩内町", "SCENIC_SPOT", "義経主従が名残惜しそうに振り返ったとされる絶景の坂道。", "国道229号（追分ソーランライン）"),
    (334, "傘岩（かさいわ）", "岩内町", "SCENIC_SPOT", "キノコのような不思議な形をした旧道沿いの名物奇岩！", "国道229号（追分ソーランライン）"),
    (335, "雷電新道展望地", "岩内町", "SCENIC_SPOT", "雄大な日本海の青原と断崖のコントラストを一望する展望所。", "国道229号（追分ソーランライン）"),
    (336, "雷電峠", "岩内町", "SCENIC_SPOT", "ソーラン節の故郷・岩内の海風が吹き抜ける険しい峠路。", "国道229号（追分ソーランライン）"),
    (337, "敷島内（しきしまない）", "岩内町", "NORMAL", "断崖絶壁の雷電海岸を抜け、平野部へと差し掛かる最初の集落。", "国道229号（追分ソーランライン）"),
    (338, "敷島内神社", "岩内町", "NORMAL", "海岸沿いに佇む地域の守り神。", "国道229号（追分ソーランライン）"),
    (339, "港野（みなとの）", "岩内町", "NORMAL", "岩内港へ向けて開けた視界の先に街並みが見え始める。", "国道229号（追分ソーランライン）"),
    (340, "大浜海岸", "岩内町", "SCENIC_SPOT", "日本海に面した白波が美しい砂利浜海岸。", "国道229号（追分ソーランライン）"),
    (341, "野束（のづか）", "岩内町", "NORMAL", "水産加工場や民家が立ち並ぶ活気ある海辺のエリア。", "国道229号（追分ソーランライン）"),
    (342, "野束川橋", "岩内町", "NORMAL", "岩内岳から流れ下る清流・野束川を渡る。", "国道229号（追分ソーランライン）"),
    (343, "いわない温泉郷入口", "岩内町", "REST_AREA", "岩内岳の裾野に湧き出る開湯の歴史ある名湯への分岐点。", "国道229号（追分ソーランライン）"),
    (344, "有島武郎記念碑前", "岩内町", "SCENIC_SPOT", "名作『生まれ出づる悩み』の舞台・岩内を讃える文学碑。", "国道229号（追分ソーランライン）"),
    (345, "岩内市街アプローチ", "岩内町", "NORMAL", "後志の中心港湾都市・岩内の市街地へと合流する。", "国道229号（追分ソーランライン）"),
    (346, "道の駅いわない", "岩内町", "REST_AREA", "旧JR岩内駅跡地に位置する町の中心観光拠点。たらこや身欠きニシンが名産。", "国道229号（追分ソーランライン）"),
]

with open(ROUTE_FILE, 'r', encoding='utf-8') as f:
    content = f.read()

def format_node(step, name, muni, n_type, desc, road, x, y):
    return f'''        SugorokuNode(
            id = "node_{step:04d}",
            stepIndex = {step},
            name = "{name}",
            municipality = "{muni}",
            type = SugorokuNodeType.{n_type},
            x = {x:.2f}f,
            y = {y:.2f}f,
            description = "{desc}",
            roadName = "{road}",
        ),'''

for item in NODES_DATA_RAIDEN:
    step, name, muni, ntype, desc, road = item
    x, y = coords[step]
    pattern = re.compile(rf'SugorokuNode\(\s*id\s*=\s*"node_{step:04d}",.*?\),', re.DOTALL)
    new_text = format_node(step, name, muni, ntype, desc, road, x, y).strip()
    content, count = pattern.subn(new_text, content)
    if count != 1:
        print(f"Error replacing step {step}: count = {count}")
        sys.exit(1)

with open(ROUTE_FILE, 'w', encoding='utf-8') as f:
    f.write(content)

print("Successfully rectified Steps 310 to 346 with calibrated accurate TPS coordinates!")
