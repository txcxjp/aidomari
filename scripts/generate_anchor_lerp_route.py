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

# 1. Parse anchors
with open('/home/txjp/dev/aidomari/scripts/rebuild_pure_mercator_act1_route.py') as f:
    text = f.read()

m = re.search(r'ANCHORS = \[(.*?)\]\n\ndef', text, re.DOTALL)
lines = m.group(1).strip().split('\n')
anchors = []
for l in lines:
    l = l.strip()
    if not l or l.startswith('#'):
        continue
    mat = re.search(r'\((\d+),\s*([0-9.]+),\s*([0-9.]+),\s*\"([^\"]+)\"\)', l)
    if mat:
        step = int(mat.group(1))
        lon = float(mat.group(2))
        lat = float(mat.group(3))
        name = mat.group(4)
        sx, sy = gps_to_svg(lon, lat)
        anchors.append((step, lon, lat, round(sx, 2), round(sy, 2), name))

# 2. Parse existing 2,000 node metadata
with open('/home/txjp/dev/aidomari/app/src/main/java/com/aidomari/app/domain/model/HokkaidoAct1Route.kt') as f:
    route_text = f.read()

pattern = re.compile(
    r'SugorokuNode\(\s*id\s*=\s*\"([^\"]+)\",\s*stepIndex\s*=\s*(\d+),\s*name\s*=\s*\"([^\"]+)\",\s*municipality\s*=\s*\"([^\"]+)\",\s*type\s*=\s*SugorokuNodeType\.([A-Z_]+),.*?description\s*=\s*\"([^\"]*)\",\s*roadName\s*=\s*\"([^\"]*)\"',
    re.DOTALL
)
metas = []
for mat in pattern.finditer(route_text):
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

# 3. Generate Kotlin code
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
out.append(" * アプリ上にはアンカー（92箇所）の公式測地座標のみを保持し、")
out.append(" * 全2,000マスの座標はアンカーとアンカーを結ぶ直線上で均等に線形補間（Lerp）して実行時に動的算出します。")
out.append(" */")
out.append("object HokkaidoAct1Route {")
out.append("")
out.append("    val totalNodes: Int = 2000")
out.append("    val maxStepIndex: Int = totalNodes - 1")
out.append("")
out.append("    /** 公式一次測地データに基づく92箇所のアンカーポイント */")
out.append("    val anchors: List<RouteAnchor> = listOf(")
for a in anchors:
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

# Generate chunks
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

print("Successfully generated HokkaidoAct1Route.kt with dynamic anchor interpolation!")
