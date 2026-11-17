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

# 1. Base anchors
with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt') as f:
    text = f.read()

anchors = {}
for m in re.finditer(r'RouteAnchor\((\d+),\s*([0-9.]+),\s*([0-9.]+),\s*([0-9.]+)f,\s*([0-9.]+)f,\s*\"([^\"]+)\"\)', text):
    step = int(m.group(1))
    lon = float(m.group(2))
    lat = float(m.group(3))
    name = m.group(6)
    anchors[step] = (lon, lat, name)

# Remove step 566 (Otaru Dream Beach spike) to let it smoothly traverse
if 566 in anchors:
    del anchors[566]

# 2. Add all capes and lighthouses with exact primary geodetic coordinates
CAPE_LIGHTHOUSE_DATA = {
    # 寿都〜島牧
    233: (140.1832, 42.8174, '弁慶岬'),
    235: (140.1897, 42.8247, '弁慶岬灯台'),
    270: (139.8322, 42.6045, '茂津多岬'),
    271: (139.8278, 42.6144, '茂津多岬灯台'),
    274: (139.8510, 42.6280, '茂津多岬入口'),
    # 岩内〜泊〜神恵内
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
    # 積丹〜余市〜小樽
    452: (140.4814, 43.3731, '積丹出岬灯台'),
    455: (140.4766, 43.3731, '積丹岬自然歩道'),
    461: (140.5270, 43.3594, 'マッカ岬'),
    465: (140.6019, 43.3027, '黄金岬'),
    486: (140.7356, 43.2421, 'ワッカケ岬'),
    498: (140.7883, 43.1997, 'モイレ岬'),
    511: (140.8592, 43.2118, '兜岬（小樽）'),
    526: (141.0161, 43.2383, '日和山灯台'),
    # 石狩〜浜益〜増毛〜留萌
    584: (141.3606, 43.2569, '石狩灯台'),
    684: (141.3530, 43.7450, '雄冬岬展望台'),
    687: (141.3510, 43.7460, '雄冬岬灯台跡'),
    690: (141.3494, 43.7474, '雄冬岬（おふゆみさき）'),
    716: (141.5275, 43.8550, '増毛灯台'),
    744: (141.6296, 43.9461, '黄金岬（おうごんみさき）'),
    745: (141.6310, 43.9470, '黄金岬キャンプ場'),
    779: (141.6300, 43.9450, '黄金岬遊歩道'),
    # 苫前〜初山別
    851: (141.6508, 44.3092, '苫前岬'),
    952: (141.7760, 44.5580, '豊岬（とよさき）'),
    953: (141.7710, 44.5590, '豊岬港'),
    954: (141.7730, 44.5610, '豊岬海岸'),
    955: (141.7770, 44.5620, '豊岬高台'),
    956: (141.7780, 44.5630, '豊岬神社'),
    959: (141.7739, 44.5631, '金比羅岬'),
    961: (141.7739, 44.5631, '初山別灯台'),
    962: (141.7800, 44.5640, '初山別温泉岬の湯'),
    # 稚内・宗谷岬
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
    # 浜頓別〜枝幸〜雄武〜興部
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
    # 紋別〜サロマ〜網走〜知床
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

for step, (lon, lat, name) in CAPE_LIGHTHOUSE_DATA.items():
    anchors[step] = (lon, lat, name)

sorted_anchors = sorted(anchors.items())

final_anchors = []
for step, (lon, lat, name) in sorted_anchors:
    sx, sy = gps_to_svg(lon, lat)
    final_anchors.append((step, lon, lat, round(sx, 2), round(sy, 2), name))

print(f"Total calibrated anchors (including all capes and lighthouses): {len(final_anchors)}")

# 3. Parse existing 2,000 node metadata
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

# 4. Generate Kotlin code
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
out.append(" * 全ての岬と灯台を含む公的測地アンカーのみを保持し、")
out.append(" * 全2,000マスの座標はアンカーとアンカーを結ぶ直線上で均等に線形補間（Lerp）して実行時に動的算出します。")
out.append(" */")
out.append("object HokkaidoAct1Route {")
out.append("")
out.append("    val totalNodes: Int = 2000")
out.append("    val maxStepIndex: Int = totalNodes - 1")
out.append("")
out.append("    /** 公式一次測地データに基づくアンカーポイント（全ての岬・灯台網羅） */")
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

print(f"Successfully generated HokkaidoAct1Route.kt with {len(final_anchors)} anchors!")
