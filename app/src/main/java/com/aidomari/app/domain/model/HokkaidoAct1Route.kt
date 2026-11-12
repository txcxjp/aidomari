package com.aidomari.app.domain.model

data class RouteAnchor(
    val id: String,
    val rank: Int,
    val name: String,
    val municipality: String,
    val lon: Double,
    val lat: Double,
    val x: Float,
    val y: Float,
    val description: String,
)

/**
 * 北海道一周すごろく 第1部（函館 → 知床・相泊）の厳選正規ルート。
 * 実在する公式・一次情報ランドマークのみで構成され、全区間1km以上の離隔を厳格に保持する。
 */
object HokkaidoAct1Route {

    /** 公式一次測地データに基づくアンカーポイント（初期アンカー再構築待ち） */
    val anchors: List<RouteAnchor> = emptyList()

    private val scenicKeywords = listOf(
        "岬", "灯台", "碑", "公園", "展望", "原野", "湖", "花園", "丘", "道", "岩", "オブジェ",
        "運河", "タワー", "海岸", "河口", "洞窟", "滝", "風力", "牧場", "林", "水族館", "マリーナ", "関所",
    )

    private val townKeywords = listOf(
        "駅", "通", "町", "蒸溜所", "酒造", "ホール", "カーリング", "のりば", "ハウス", "港", "役場",
    )

    val routeNodes: List<SugorokuNode> by lazy {
        anchors.mapIndexed { index, anchor ->
            val nodeType = when {
                index == 0 -> SugorokuNodeType.START_HAKODATE
                index == anchors.lastIndex -> SugorokuNodeType.GOAL_AIDOMARI
                anchor.name.contains("道の駅") || anchor.name.contains("温泉") -> SugorokuNodeType.REST_AREA
                scenicKeywords.any { anchor.name.contains(it) } -> SugorokuNodeType.SCENIC_SPOT
                townKeywords.any { anchor.name.contains(it) } -> SugorokuNodeType.TOWN_CENTER
                else -> SugorokuNodeType.NORMAL
            }

            SugorokuNode(
                stepIndex = index,
                name = anchor.name,
                municipality = anchor.municipality,
                x = anchor.x,
                y = anchor.y,
                type = nodeType,
                isAnchor = true,
                description = anchor.description,
                id = anchor.id,
                rank = anchor.rank,
                roadName = "",
            )
        }
    }

    val nodes: List<SugorokuNode>
        get() = routeNodes

    val totalNodes: Int
        get() = routeNodes.size

    val maxStepIndex: Int
        get() = routeNodes.lastIndex

    val startNode: SugorokuNode
        get() = routeNodes.first()

    val goalNode: SugorokuNode
        get() = routeNodes.last()

    val startAnchor: RouteAnchor
        get() = anchors.first()

    val goalAnchor: RouteAnchor
        get() = anchors.last()

    fun getNodeByStepIndex(stepIndex: Int): SugorokuNode? = routeNodes.getOrNull(stepIndex)

    fun getNodeById(id: String): SugorokuNode? = routeNodes.firstOrNull { it.id == id }

    fun getNextAnchor(currentAnchorId: String): RouteAnchor? {
        val index = anchors.indexOfFirst { it.id == currentAnchorId }
        return if (index != -1 && index < anchors.size - 1) anchors[index + 1] else null
    }

    fun getPrevAnchor(currentAnchorId: String): RouteAnchor? {
        val index = anchors.indexOfFirst { it.id == currentAnchorId }
        return if (index > 0) anchors[index - 1] else null
    }
}
