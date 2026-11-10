import re
import math

MIN_MX = 2.43309194
MAX_MY = 0.89434710
SCALE = 3657.87025447
OFFSET_X = 101.33892580
OFFSET_Y = 12.00000000

def to_mercator(lon, lat):
    x = math.radians(lon)
    lat_rad = math.radians(lat)
    lat_rad = max(-1.4844, min(1.4844, lat_rad))
    y = math.log(math.tan(math.pi / 4.0 + lat_rad / 2.0))
    return x, y

def gps_to_svg(lon, lat):
    mx, my = to_mercator(lon, lat)
    sx = (mx - MIN_MX) * SCALE + OFFSET_X
    sy = (MAX_MY - my) * SCALE + OFFSET_Y
    return sx, sy

# 1. Base essential stations & landmarks
KEY_MILESTONES = {
    0: (140.7264, 41.7738, "函館駅前"),
    18: (140.7337, 41.8035, "五稜郭駅前"),
    44: (140.6723, 41.9814, "大沼国定公園"),
    61: (140.5750, 42.1090, "森駅前"),
    98: (140.2678, 42.2514, "JR八雲駅前"),
    148: (140.3750, 42.5125, "長万部観光案内所"),
    230: (140.2285, 42.7938, "道の駅みなとま〜れ寿都"),
    247: (140.0683, 42.7158, "道の駅よってけ！島牧"),
    329: (140.4028, 42.9242, "雷電温泉郷"),
    495: (140.7710, 43.1870, "JR余市駅前"),
    541: (140.9937, 43.1977, "JR小樽駅前"),
    654: (141.3926, 43.6068, "浜益海浜公園"),
    708: (141.5249, 43.8561, "旧JR増毛駅前"),
    751: (141.6369, 43.9410, "JR留萌駅跡"),
    866: (141.8436, 44.1450, "三毛別羆事件復元地"),
    966: (141.7761, 44.5639, "みさき台公園"),
    1065: (141.6500, 45.0000, "北緯45度モニュメント"),
    1090: (141.6400, 45.1500, "サロベツ原野"),
    1101: (141.6205, 45.3082, "抜海岩"),
    1152: (141.6445, 45.4528, "ノシャップ寒流水族館"),
    1171: (141.6770, 45.4168, "JR日本最北端の駅・稚内駅"),
    1253: (142.1850, 45.3120, "エサヌカ線入口"),
    1260: (142.2480, 45.2450, "エサヌカ線中央"),
    1267: (142.3061, 45.1954, "エサヌカ線出口"),
    1294: (142.1460, 45.3340, "クッチャロ湖入口"),
    1515: (143.3550, 44.3540, "オホーツク流氷公園"),
    1716: (144.2588, 44.0202, "JR網走駅前"),
    1765: (144.5022, 43.9135, "小清水原生花園"),
    1875: (145.0805, 44.1222, "知床五湖フィールドハウス"),
    1909: (145.1100, 44.0500, "知床峠（標高738m）"),
    1985: (145.2871, 44.1525, "相泊温泉（日本最北東端の温泉）"),
    1999: (145.3174, 44.1868, "相泊（最果ての終着点・相泊温泉）"),
}

# 2. All Capes and Lighthouses (Official Primary Geodetic Coordinates)
CAPES_AND_LIGHTHOUSES = {
    233: (140.1832, 42.8174, '弁慶岬'),
    235: (140.1897, 42.8247, '弁慶岬灯台'),
    270: (139.8322, 42.6045, '茂津多岬'),
    271: (139.8278, 42.6144, '茂津多岬灯台'),
    274: (139.8510, 42.6280, '茂津多岬入口'),
    328: (140.4037, 42.9276, 'カスペの岬'),
    377: (140.4653, 43.0841, '兜岬'),
    394: (140.4720, 43.1180, '滝ノ澗ノ岬'),
    426: (140.3306, 43.2603, 'ジュウボウ岬'),
    428: (140.3540, 43.3220, '神威岬アプローチ'),
    429: (140.4280, 43.1440, '神恵内灯台前'),
    431: (140.3540, 43.3280, '神威岬入口'),
    432: (140.3477, 43.3335, '神威岬駐車場'),
    435: (140.3495, 43.3300, '神威岬尾根道'),
    436: (140.3486, 43.3304, '神威岬灯台'),
    438: (140.3462, 43.3338, '神威岬展望広場'),
    439: (140.3505, 43.3310, '神威岬自然遊歩道'),
    452: (140.4814, 43.3731, '積丹出岬灯台'),
    455: (140.4766, 43.3731, '積丹岬自然歩道'),
    461: (140.5270, 43.3594, 'マッカ岬'),
    465: (140.6019, 43.3027, '黄金岬'),
    486: (140.7356, 43.2421, 'ワッカケ岬'),
    498: (140.7883, 43.1997, 'モイレ岬'),
    511: (140.8592, 43.2118, '兜岬（小樽）'),
    526: (141.0161, 43.2383, '日和山灯台'),
    584: (141.3606, 43.2569, '石狩灯台'),
    684: (141.3530, 43.7450, '雄冬岬展望台'),
    687: (141.3510, 43.7460, '雄冬岬灯台跡'),
    690: (141.3494, 43.7474, '雄冬岬（おふゆみさき）'),
    716: (141.5275, 43.8550, '増毛灯台'),
    744: (141.6296, 43.9461, '黄金岬（おうごんみさき）'),
    745: (141.6310, 43.9470, '黄金岬キャンプ場'),
    779: (141.6300, 43.9450, '黄金岬遊歩道'),
    851: (141.6508, 44.3092, '苫前岬'),
    952: (141.7760, 44.5580, '豊岬（とよさき）'),
    953: (141.7710, 44.5590, '豊岬港'),
    954: (141.7730, 44.5610, '豊岬海岸'),
    955: (141.7770, 44.5620, '豊岬高台'),
    956: (141.7780, 44.5630, '豊岬神社'),
    959: (141.7739, 44.5631, '金比羅岬'),
    961: (141.7739, 44.5631, '初山別灯台'),
    962: (141.7800, 44.5640, '初山別温泉岬の湯'),
    1110: (141.6500, 45.4200, 'ルエラン岬'),
    1141: (141.6460, 45.4450, 'ノシャップ岬手前海岸'),
    1155: (141.6452, 45.4495, '稚内灯台'),
    1159: (141.6433, 45.4542, 'ノシャップ岬先端'),
    1160: (141.6435, 45.4540, 'ノシャップ岬碑'),
    1161: (141.6433, 45.4542, 'ノシャップ岬（野寒布岬）'),
    1162: (141.6450, 45.4520, 'ノシャップ岬東アプローチ'),
    1206: (141.8780, 45.4634, '宗谷岬入口'),
    1214: (141.9340, 45.5190, '宗谷岬郵便局'),
    1215: (141.9355, 45.5210, '宗谷岬展望台'),
    1216: (141.9360, 45.5220, '宗谷岬音楽碑'),
    1217: (141.9364, 45.5214, '宗谷岬灯台'),
    1218: (141.9366, 45.5228, '日本最北端の地の碑（宗谷岬）'),
    1330: (142.4950, 45.0560, '神威岬旧道跡'),
    1331: (142.4970, 45.0570, '神威岬覆道'),
    1332: (142.4990, 45.0580, '神威岬公園'),
    1333: (142.5014, 45.0589, '北見神威岬'),
    1334: (142.5014, 45.0589, '神威岬灯台'),
    1335: (142.5020, 45.0595, '神威岬灯台下遊歩道'),
    1336: (142.4980, 45.0575, '神威岬展望パーキング'),
    1355: (142.5823, 44.9589, 'ウスタイベ岬'),
    1358: (142.5850, 44.9370, '枝幸灯台'),
    1406: (142.8181, 44.7081, '音標岬'),
    1459: (143.0620, 44.5321, '日の出岬'),
    1461: (143.0600, 44.5310, 'ホテル日の出岬'),
    1462: (143.0610, 44.5315, '日の出岬キャンプ場'),
    1463: (143.0618, 44.5320, '日の出岬遊歩道'),
    1464: (143.0622, 44.5322, '日の出岬モニュメント'),
    1465: (143.0625, 44.5325, '日の出岬遠望台'),
    1499: (143.2230, 44.4413, '沙留岬'),
    1527: (143.3687, 44.3508, '紋別港赤灯台プロムナード'),
    1648: (143.9118, 44.1083, 'キムアネップ岬'),
    1695: (144.2400, 44.0950, '美岬（みさき）アプローチ'),
    1696: (144.2420, 44.1050, '美岬牧場'),
    1697: (144.2431, 44.1122, '能取岬（のとろみさき）'),
    1698: (144.2431, 44.1122, '能取岬灯台'),
    1730: (144.2950, 44.0250, '網走港赤灯台'),
    1854: (144.9833, 44.0625, 'チャシコツ岬'),
    1865: (144.9850, 44.0800, 'ウトロ灯台'),
    1867: (145.0050, 44.0900, 'プユニ岬アプローチ'),
    1868: (145.0093, 44.0951, 'プユニ岬'),
    1948: (145.2000, 44.0300, '知床岬遠望コース'),
    1987: (145.2900, 44.1600, '知床岬航路案内所'),
    1990: (145.3050, 44.1750, '知床岬遠望岩礁'),
}

# 3. All Ports and Harbors (Official Primary Geodetic Coordinates)
PORTS = {
    60: (140.5919, 42.1111, "森港"),
    75: (140.4721, 42.1595, "石倉港"),
    87: (140.4256, 42.1895, "落部港"),
    93: (140.3792, 42.2179, "野田生港"),
    108: (140.3540, 42.2450, "山崎港"),
    132: (140.3300, 42.4389, "国縫漁港"),
    225: (140.2450, 42.7910, "樽岸港"),
    229: (140.2361, 42.7944, "寿都港"),
    249: (140.1124, 42.7397, "本目港"),
    254: (140.0615, 42.7005, "泊港（島牧）"),
    259: (140.0050, 42.6680, "栄浜港（島牧）"),
    315: (140.2361, 42.7944, "寿都港"),
    348: (140.5119, 42.9964, "岩内港"),
    357: (140.5120, 43.0010, "岩内港北防波堤"),
    362: (140.5050, 42.9920, "岩内西港"),
    382: (140.4000, 43.1073, "盃港"),
    389: (140.4989, 43.0630, "泊港（泊村）"),
    399: (140.4320, 43.1435, "神恵内港"),
    414: (140.3269, 43.2206, "川白港"),
    444: (140.3793, 43.3275, "余別港"),
    451: (140.4614, 43.3459, "日司港"),
    458: (140.4752, 43.3672, "入舸港"),
    460: (140.4180, 43.3577, "幌武意港"),
    463: (140.5994, 43.2968, "美国港"),
    471: (140.6377, 43.2772, "古平港"),
    499: (140.7775, 43.2114, "余市港"),
    510: (140.8646, 43.2079, "忍路港"),
    528: (141.0121, 43.2299, "祝津港"),
    570: (141.2800, 43.2000, "石狩湾新港西埠頭"),
    571: (141.2860, 43.2040, "石狩湾新港中央埠頭"),
    574: (141.3100, 43.2150, "石狩湾新港東埠頭"),
    613: (141.4320, 43.3990, "厚田港"),
    652: (141.3880, 43.5950, "川下港"),
    659: (141.3840, 43.6020, "浜益港"),
    672: (141.3650, 43.6800, "幌港"),
    675: (141.3420, 43.7420, "雄冬港"),
    693: (141.4760, 43.8050, "岩尾港"),
    705: (141.5120, 43.8420, "別苅港"),
    712: (141.5340, 43.8540, "増毛港"),
    748: (141.6380, 43.9560, "留萌港"),
    774: (141.6350, 43.9620, "留萌港北防波堤"),
    781: (141.6390, 43.9580, "留萌港フェリーターミナル跡"),
    794: (141.6420, 43.9530, "留萌港展望の丘"),
    796: (141.6628, 44.0154, "小平港"),
    801: (141.6582, 44.1537, "鬼鹿港"),
    837: (141.6545, 43.9979, "臼谷港"),
    842: (141.6629, 44.2264, "力昼港"),
    846: (141.6600, 44.2400, "栄浜港（苫前）"),
    873: (141.6525, 44.3167, "苫前港"),
    901: (141.6980, 44.3750, "羽幌港フェリー待合所"),
    902: (141.6983, 44.3756, "羽幌港"),
    917: (141.7377, 44.4128, "築別港"),
    943: (141.7676, 44.5332, "初山別港"),
    953: (141.7710, 44.5590, "豊岬港"),
    982: (141.7922, 44.7222, "遠別港"),
    1029: (141.7364, 44.8703, "天塩港"),
    1030: (141.7320, 44.8720, "天塩港防波堤"),
    1078: (141.6402, 45.0838, "稚咲内漁港"),
    1104: (141.6211, 45.3107, "抜海港"),
    1116: (141.6528, 45.3875, "坂の下港"),
    1134: (141.6583, 45.3833, "西浜港"),
    1143: (141.6433, 45.4520, "ノシャップ漁港"),
    1165: (141.6708, 45.4028, "恵比須港"),
    1168: (141.6785, 45.4190, "稚内港北防波堤ドーム"),
    1169: (141.6790, 45.4180, "稚内港フェリーターミナル"),
    1170: (141.6806, 45.4119, "稚内港中央埠頭"),
    1196: (141.8020, 45.4050, "稚内空港"),
    1200: (141.8740, 45.4520, "富磯漁港"),
    1201: (141.8748, 45.4528, "富磯港"),
    1207: (141.9486, 45.5200, "宗谷港"),
    1222: (142.1200, 45.3600, "知来港"),
    1227: (142.1603, 45.3427, "浜鬼志別港"),
    1247: (142.2292, 45.2760, "浜猿払港"),
    1306: (142.3940, 45.1305, "浜頓別港"),
    1325: (142.4523, 45.0742, "斜内港"),
    1347: (142.5400, 45.0000, "山臼港"),
    1350: (142.5600, 44.9800, "目梨泊港"),
    1364: (142.5837, 44.9368, "枝幸港"),
    1393: (142.7211, 44.7788, "乙忠部港"),
    1399: (142.7800, 44.7200, "音標港"),
    1411: (142.8800, 44.6500, "幌内港"),
    1434: (142.9630, 44.5832, "雄武港（おうむこう）"),
    1452: (143.0400, 44.5400, "沢木港"),
    1483: (143.1400, 44.4700, "興部港"),
    1494: (143.2261, 44.4324, "沙留港"),
    1526: (143.3656, 44.3291, "紋別港第3埠頭"),
    1529: (143.3687, 44.3508, "紋別本港"),
    1553: (143.4040, 44.3040, "オホーツク紋別空港"),
    1586: (143.7374, 44.1887, "登栄床港"),
    1597: (143.6189, 44.2296, "湧別港"),
    1628: (143.6918, 44.1507, "芭露港"),
    1641: (143.8068, 44.0910, "富武士港"),
    1656: (143.8500, 44.0800, "浜佐呂間港"),
    1669: (144.0707, 44.1211, "常呂港防波堤"),
    1670: (144.0707, 44.1211, "常呂港"),
    1694: (144.1187, 44.0939, "能取漁港"),
    1727: (144.2750, 44.0240, "網走港本港地区"),
    1816: (144.6628, 43.9122, "斜里港"),
    1860: (144.9960, 44.0680, "ウトロ港（知床観光船乗り場）"),
    1941: (145.1910, 44.0190, "羅臼港本港地区"),
    1943: (145.1950, 44.0150, "羅臼港南防波堤"),
    1950: (145.1623, 43.9897, "松法漁港"),
    1981: (145.3174, 44.1868, "相泊漁港"),
    1982: (145.3174, 44.1868, "相泊港船揚場"),
}

# Merge all valid anchors
all_anchors = {}
all_anchors.update(KEY_MILESTONES)
all_anchors.update(CAPES_AND_LIGHTHOUSES)
all_anchors.update(PORTS)

sorted_items = sorted(all_anchors.items())
final_anchors = []
for step, (lon, lat, name) in sorted_items:
    sx, sy = gps_to_svg(lon, lat)
    final_anchors.append((step, lon, lat, round(sx, 2), round(sy, 2), name))

print(f"Total master anchors (Key milestones + Capes + Lighthouses + Ports): {len(final_anchors)}")

# Verify monotonic and bounds
steps = [a[0] for a in final_anchors]
assert steps == sorted(steps), "Steps must be monotonic!"
assert steps[0] == 0, "Step 0 must be Hakodate!"
assert steps[-1] == 1999, "Step 1999 must be Aidomari!"
assert len(steps) == len(set(steps)), "Steps must be unique!"

# Parse existing 2,000 node metadata
with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt') as f:
    text = f.read()

pattern = re.compile(
    r'SugorokuNodeMeta\(\s*id\s*=\s*\"([^\"]+)\",\s*stepIndex\s*=\s*(\d+),\s*name\s*=\s*\"([^\"]+)\",\s*municipality\s*=\s*\"([^\"]+)\",\s*type\s*=\s*SugorokuNodeType\.([A-Z_]+),.*?description\s*=\s*\"([^\"]*)\",\s*roadName\s*=\s*\"([^\"]*)\"',
    re.DOTALL
)
metas = []
for mat in pattern.finditer(text):
    metas.append({
        'id': mat.group(1),
        'stepIndex': int(mat.group(2)),
        'name': mat.group(3),
        'municipality': mat.group(4),
        'type': mat.group(5),
        'description': mat.group(6),
        'roadName': mat.group(7),
    })

assert len(metas) == 2000, f"Expected 2000 nodes, got {len(metas)}"

# Generate Kotlin code
out = []
out.append("package com.aidomari.app.domain.model")
out.append("")
out.append("/**")
out.append(" * アンカーポイント定義。")
out.append(" * 公式測地GPS座標（経度・緯度）および純粋等角メルカトル図法によるSVG座標。")
out.append(" */")
out.append("data class RouteAnchor(")
out.append("    val stepIndex: Int,")
out.append("    val lon: Double,")
out.append("    val lat: Double,")
out.append("    val x: Float,")
out.append("    val y: Float,")
out.append("    val name: String,")
out.append(")")
out.append("")
out.append("/**")
out.append(" * ノードメタデータ（座標を持たず、名称や説明文等の属性のみ保持）。")
out.append(" */")
out.append("data class SugorokuNodeMeta(")
out.append("    val id: String,")
out.append("    val stepIndex: Int,")
out.append("    val name: String,")
out.append("    val municipality: String,")
out.append("    val type: SugorokuNodeType,")
out.append("    val description: String = \"\",")
out.append("    val roadName: String = \"\",")
out.append(")")
out.append("")
out.append("/**")
out.append(" * 北海道一周すごろく（第1部：函館駅前 〜 相泊終点）全2,000マスのルートデータ。")
out.append(" * 全ての岬・灯台・港湾・主要ランドマークのみを厳選した公的測地アンカーを保持し、")
out.append(" * 全2,000マスの座標はアンカーとアンカーを結ぶ直線上で均等に線形補間（Lerp）して実行時に動的算出します。")
out.append(" */")
out.append("object HokkaidoAct1Route {")
out.append("")
out.append("    val totalNodes: Int = 2000")
out.append("    val maxStepIndex: Int = totalNodes - 1")
out.append("")
out.append("    /** 公式一次測地データに基づくアンカーポイント（全ての岬・灯台・港湾網羅） */")
out.append("    val anchors: List<RouteAnchor> = listOf(")
for a in final_anchors:
    out.append(f'        RouteAnchor({a[0]}, {a[1]}, {a[2]}, {a[3]}f, {a[4]}f, "{a[5]}"),')
out.append("    )")
out.append("")
out.append("    val routeNodes: List<SugorokuNode>")
out.append("        get() = nodes")
out.append("")
out.append("    val startNode: SugorokuNode by lazy { nodes[0] }")
out.append("    val goalNode: SugorokuNode by lazy { nodes[1999] }")
out.append("")
out.append("    fun getNodeByIndex(index: Int): SugorokuNode? = if (index in 0 until totalNodes) nodes[index] else null")
out.append("    fun getNodeByStepIndex(stepIndex: Int): SugorokuNode? = if (stepIndex in 0 until totalNodes) nodes[stepIndex] else null")
out.append("    fun getNodeById(id: String): SugorokuNode? = nodes.firstOrNull { it.id == id }")
out.append("")
out.append("    /**")
out.append("     * 実行時にアンカー間を直線補間して全2,000ノードの座標を動的算出。")
out.append("     */")
out.append("    val nodes: List<SugorokuNode> by lazy {")
out.append("        computeInterpolatedNodes()")
out.append("    }")
out.append("")
out.append("    private fun computeInterpolatedNodes(): List<SugorokuNode> {")
out.append("        val result = ArrayList<SugorokuNode>(totalNodes)")
out.append("        val metas = rawNodeMetas()")
out.append("        for (i in 0 until anchors.size - 1) {")
out.append("            val a1 = anchors[i]")
out.append("            val a2 = anchors[i + 1]")
out.append("            val sStart = a1.stepIndex")
out.append("            val sEnd = a2.stepIndex")
out.append("            val count = sEnd - sStart")
out.append("            for (stepOffset in 0 until count) {")
out.append("                val step = sStart + stepOffset")
out.append("                val t = stepOffset.toFloat() / count.toFloat()")
out.append("                val x = a1.x + t * (a2.x - a1.x)")
out.append("                val y = a1.y + t * (a2.y - a1.y)")
out.append("                val meta = metas[step]")
out.append("                result.add(")
out.append("                    SugorokuNode(")
out.append("                        id = meta.id,")
out.append("                        stepIndex = step,")
out.append("                        name = meta.name,")
out.append("                        municipality = meta.municipality,")
out.append("                        type = meta.type,")
out.append("                        x = x,")
out.append("                        y = y,")
out.append("                        description = meta.description,")
out.append("                        roadName = meta.roadName,")
out.append("                        isAnchor = (stepOffset == 0),")
out.append("                    )")
out.append("                )")
out.append("            }")
out.append("        }")
out.append("        val lastAnchor = anchors.last()")
out.append("        val lastMeta = metas[lastAnchor.stepIndex]")
out.append("        result.add(")
out.append("            SugorokuNode(")
out.append("                id = lastMeta.id,")
out.append("                stepIndex = lastAnchor.stepIndex,")
out.append("                name = lastMeta.name,")
out.append("                municipality = lastMeta.municipality,")
out.append("                type = lastMeta.type,")
out.append("                x = lastAnchor.x,")
out.append("                y = lastAnchor.y,")
out.append("                description = lastMeta.description,")
out.append("                roadName = lastMeta.roadName,")
out.append("                isAnchor = true,")
out.append("            )")
out.append("        )")
out.append("        return result")
out.append("    }")
out.append("")
out.append("    private fun rawNodeMetas(): List<SugorokuNodeMeta> = buildList(2000) {")
for chunk_idx in range(20):
    out.append(f"        addAll(chunk{chunk_idx}())")
out.append("    }")
out.append("")

CHUNK_SIZE = 100
for chunk_idx in range(20):
    start = chunk_idx * CHUNK_SIZE
    end = start + CHUNK_SIZE
    chunk_nodes = metas[start:end]
    out.append(f"    private fun chunk{chunk_idx}(): List<SugorokuNodeMeta> = listOf(")
    for node in chunk_nodes:
        desc = node['description'].replace('"', '\\"')
        road = node['roadName'].replace('"', '\\"')
        out.append(f"        SugorokuNodeMeta(")
        out.append(f'            id = "{node["id"]}",')
        out.append(f'            stepIndex = {node["stepIndex"]},')
        out.append(f'            name = "{node["name"]}",')
        out.append(f'            municipality = "{node["municipality"]}",')
        out.append(f'            type = SugorokuNodeType.{node["type"]},')
        out.append(f'            description = "{desc}",')
        out.append(f'            roadName = "{road}",')
        out.append(f"        ),")
    out.append("    )")
    out.append("")

out.append("}")

with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt', 'w') as f:
    f.write('\n'.join(out) + '\n')

print(f"Successfully generated HokkaidoAct1Route.kt with {len(final_anchors)} master anchors!")
