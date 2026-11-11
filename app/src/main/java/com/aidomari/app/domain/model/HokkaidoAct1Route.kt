package com.aidomari.app.domain.model

/**
 * アンカーポイント定義。
 * 公式測地GPS座標（経度・緯度）および純粋等角メルカトル図法によるSVG座標。
 */
data class RouteAnchor(
    val stepIndex: Int,
    val lon: Double,
    val lat: Double,
    val x: Float,
    val y: Float,
    val name: String,
)

/**
 * 北海道一周すごろく（第1部：函館駅前 〜 相泊終点）厳選ルートデータ。
 * 旅行の目的地としてふさわしい主要ランドマーク・景勝地・岬・道の駅・駅のみを厳選した全70マスのルートです。
 * 海岸線沿いの一筆書き順（函館 → 寿都 → 積丹 → 留萌 → 稚内・宗谷岬 → オホーツク → 知床相泊）で構成されます。
 */
object HokkaidoAct1Route {

    val totalNodes: Int = 70
    val maxStepIndex: Int = totalNodes - 1

    /** 公式一次測地データに基づくアンカーポイント（全70箇所） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(0, 140.7264, 41.7738, 185.64f, 342.97f, "函館駅前"),
        RouteAnchor(1, 140.7337, 41.8035, 186.11f, 340.43f, "五稜郭駅前"),
        RouteAnchor(2, 140.6723, 41.9814, 182.19f, 325.17f, "大沼国定公園"),
        RouteAnchor(3, 140.575, 42.109, 175.98f, 314.20f, "森駅前"),
        RouteAnchor(4, 140.2678, 42.2514, 156.36f, 301.94f, "JR八雲駅前"),
        RouteAnchor(5, 140.375, 42.5125, 163.21f, 279.37f, "長万部観光案内所"),
        RouteAnchor(6, 140.2285, 42.7938, 153.86f, 254.95f, "道の駅みなとま〜れ寿都"),
        RouteAnchor(7, 140.1832, 42.8174, 150.96f, 252.90f, "弁慶岬"),
        RouteAnchor(8, 140.0683, 42.7158, 143.63f, 261.73f, "道の駅よってけ！島牧"),
        RouteAnchor(9, 139.8322, 42.6045, 128.55f, 271.40f, "茂津多岬"),
        RouteAnchor(10, 140.4028, 42.9242, 164.98f, 243.60f, "雷電温泉郷"),
        RouteAnchor(11, 140.518, 42.987, 172.34f, 238.12f, "道の駅いわない（岩内港）"),
        RouteAnchor(12, 140.457, 43.107, 168.44f, 227.63f, "盃漁港（盃温泉郷）"),
        RouteAnchor(13, 140.432, 43.1435, 166.85f, 224.44f, "神恵内港（神恵内村）"),
        RouteAnchor(14, 140.3306, 43.2603, 160.37f, 214.21f, "ジュウボウ岬"),
        RouteAnchor(15, 140.348, 43.3337, 161.48f, 207.77f, "神威岬"),
        RouteAnchor(16, 140.4814, 43.3731, 170.00f, 204.31f, "積丹出岬灯台（島武意海岸）"),
        RouteAnchor(17, 140.5098, 43.3594, 171.81f, 205.52f, "幌武意港"),
        RouteAnchor(18, 140.6019, 43.3027, 177.69f, 210.49f, "黄金岬（美国）"),
        RouteAnchor(19, 140.6377, 43.2772, 179.98f, 212.73f, "古平港"),
        RouteAnchor(20, 140.771, 43.187, 188.49f, 220.63f, "JR余市駅前"),
        RouteAnchor(21, 140.8646, 43.2079, 194.47f, 218.80f, "忍路港"),
        RouteAnchor(22, 140.9937, 43.1977, 202.71f, 219.70f, "JR小樽駅前"),
        RouteAnchor(23, 141.0161, 43.2383, 204.14f, 216.14f, "日和山灯台（祝津）"),
        RouteAnchor(24, 141.275, 43.185, 220.67f, 220.81f, "石狩湾新港"),
        RouteAnchor(25, 141.3606, 43.2569, 226.13f, 214.51f, "石狩灯台（はまなすの丘公園）"),
        RouteAnchor(26, 141.432, 43.399, 230.69f, 202.04f, "厚田港（道の駅あいろーど厚田）"),
        RouteAnchor(27, 141.3926, 43.6068, 228.17f, 183.75f, "浜益海浜公園"),
        RouteAnchor(28, 141.353, 43.745, 225.65f, 171.55f, "雄冬岬展望台"),
        RouteAnchor(29, 141.5249, 43.8561, 236.62f, 161.72f, "旧JR増毛駅前"),
        RouteAnchor(30, 141.6296, 43.9461, 243.30f, 153.75f, "黄金岬（留萌）"),
        RouteAnchor(31, 141.6582, 44.1537, 245.13f, 135.31f, "道の駅おびら鰊番屋（小平町）"),
        RouteAnchor(32, 141.6508, 44.3092, 244.66f, 121.45f, "苫前岬"),
        RouteAnchor(33, 141.8436, 44.145, 256.97f, 136.08f, "三毛別羆事件復元地"),
        RouteAnchor(34, 141.701, 44.368, 247.86f, 116.20f, "羽幌港（フェリーターミナル）"),
        RouteAnchor(35, 141.7739, 44.5631, 252.52f, 98.75f, "金比羅岬（海中鳥居）"),
        RouteAnchor(36, 141.7922, 44.7222, 253.68f, 84.48f, "遠別（道の駅富士見）"),
        RouteAnchor(37, 141.7476, 44.8756, 250.84f, 70.67f, "天塩港（天塩川河口）"),
        RouteAnchor(38, 141.6813, 45.0023, 246.60f, 59.25f, "北緯45度モニュメント"),
        RouteAnchor(39, 141.64, 45.15, 243.97f, 45.89f, "サロベツ原野"),
        RouteAnchor(40, 141.6211, 45.3107, 242.76f, 31.33f, "抜海港（アザラシ越冬地）"),
        RouteAnchor(41, 141.6447, 45.449, 244.27f, 18.76f, "ノシャップ岬（野寒布岬）"),
        RouteAnchor(42, 141.677, 45.4168, 246.33f, 21.69f, "JR日本最北端の駅・稚内駅"),
        RouteAnchor(43, 141.802, 45.405, 254.31f, 22.76f, "稚内空港"),
        RouteAnchor(44, 141.9366, 45.5228, 262.90f, 12.04f, "日本最北端の地の碑（宗谷岬）"),
        RouteAnchor(45, 142.248, 45.245, 282.78f, 37.29f, "猿払村道エサヌカ線"),
        RouteAnchor(46, 142.35, 45.127, 289.30f, 47.98f, "クッチャロ湖（浜頓別）"),
        RouteAnchor(47, 142.5014, 45.0589, 298.96f, 54.13f, "北見神威岬公園"),
        RouteAnchor(48, 142.5823, 44.9589, 304.13f, 63.16f, "ウスタイベ岬（千畳岩）"),
        RouteAnchor(49, 142.5837, 44.9368, 304.22f, 65.16f, "枝幸港"),
        RouteAnchor(50, 142.963, 44.5832, 328.43f, 96.95f, "雄武港（道の駅おうむ）"),
        RouteAnchor(51, 143.062, 44.5321, 334.75f, 101.53f, "日の出岬（ガラスの展望台）"),
        RouteAnchor(52, 143.14, 44.47, 339.73f, 107.09f, "興部（道の駅おこっぺ）"),
        RouteAnchor(53, 143.358, 44.35, 353.65f, 117.81f, "紋別港（ガリンコ号乗り場）"),
        RouteAnchor(54, 143.404, 44.304, 356.59f, 121.92f, "オホーツク紋別空港"),
        RouteAnchor(55, 143.6189, 44.2296, 370.30f, 128.55f, "道の駅愛ランド湧別（湧別町）"),
        RouteAnchor(56, 143.9118, 44.1083, 389.00f, 139.35f, "キムアネップ岬（サロマ湖夕日展望地）"),
        RouteAnchor(57, 143.9382, 44.0805, 390.69f, 141.82f, "浜佐呂間（サロマ湖東岸）"),
        RouteAnchor(58, 144.0707, 44.1211, 399.15f, 138.21f, "常呂港（カーリングの聖地）"),
        RouteAnchor(59, 144.2431, 44.1122, 410.15f, 139.00f, "能取岬（流氷展望の白亜灯台）"),
        RouteAnchor(60, 144.275, 44.024, 412.19f, 146.84f, "網走港（道の駅流氷街道網走）"),
        RouteAnchor(61, 144.5022, 43.9135, 426.70f, 156.64f, "小清水原生花園"),
        RouteAnchor(62, 144.6628, 43.9122, 436.95f, 156.75f, "斜里（JR知床斜里駅前）"),
        RouteAnchor(63, 144.996, 44.068, 458.22f, 142.93f, "ウトロ港（知床観光船乗り場）"),
        RouteAnchor(64, 145.013, 44.09, 459.31f, 140.97f, "プユニ岬（夕日と流氷の名所）"),
        RouteAnchor(65, 145.0805, 44.1222, 463.62f, 138.11f, "知床五湖フィールドハウス"),
        RouteAnchor(66, 145.11, 44.05, 465.50f, 144.53f, "知床峠（標高738m）"),
        RouteAnchor(67, 145.191, 44.019, 470.67f, 147.28f, "道の駅知床・らうす（羅臼港）"),
        RouteAnchor(68, 145.2866, 44.1525, 476.77f, 135.42f, "相泊温泉（日本最北東端の温泉）"),
        RouteAnchor(69, 145.3174, 44.1868, 478.74f, 132.36f, "相泊（最果ての終着点・車道終点）"),
    )

    fun getAnchor(stepIndex: Int): RouteAnchor? =
        anchors.getOrNull(stepIndex.coerceIn(0, maxStepIndex))

    val routeNodes: List<SugorokuNode> by lazy { nodes }
    val startNode: SugorokuNode by lazy { nodes[0] }
    val goalNode: SugorokuNode by lazy { nodes[maxStepIndex] }

    fun getNodeByIndex(index: Int): SugorokuNode? = if (index in 0 until totalNodes) nodes[index] else null
    fun getNodeByStepIndex(stepIndex: Int): SugorokuNode? = if (stepIndex in 0 until totalNodes) nodes[stepIndex] else null
    fun getNodeById(id: String): SugorokuNode? = nodes.firstOrNull { it.id == id }

    val nodes: List<SugorokuNode> by lazy {
        anchors.mapIndexed { index, anchor ->
            val nodeType = when {
                index == 0 -> SugorokuNodeType.START_HAKODATE
                index == maxStepIndex -> SugorokuNodeType.GOAL_AIDOMARI
                anchor.name.contains("岬") || anchor.name.contains("灯台") || anchor.name.contains("碑") || anchor.name.contains("公園") || anchor.name.contains("展望") || anchor.name.contains("原野") || anchor.name.contains("湖") || anchor.name.contains("花園") -> SugorokuNodeType.SCENIC_SPOT
                anchor.name.contains("駅") || anchor.name.contains("通") || anchor.name.contains("町") || anchor.name.contains("空港") -> SugorokuNodeType.TOWN_CENTER
                else -> SugorokuNodeType.NORMAL
            }
            SugorokuNode(
                id = String.format("node_%04d", index),
                stepIndex = index,
                name = anchor.name,
                municipality = "",
                type = nodeType,
                x = anchor.x,
                y = anchor.y,
                description = "",
                roadName = "",
                isAnchor = true,
            )
        }
    }
}
