package com.aidomari.app.domain.model

import androidx.compose.ui.geometry.Offset

/**
 * Classification of Hokkaido Highway network.
 */
enum class HighwayCategory {
    NATIONAL_HIGHWAY, // 国道（主要幹線）
    PREFECTURAL_ROAD, // 主要道道
    SCENIC_ROAD, // 絶景・観光主要ルート（エサヌカ線、知床横断道路等）
}

/**
 * A real road segment connecting points across Hokkaido.
 */
data class HighwaySegment(
    val id: String,
    val name: String,
    val category: HighwayCategory,
    val routeNumber: String,
    val waypoints: List<Offset>,
)

/**
 * Verified Hokkaido Highway Network model.
 * Traced directly along the real geography of Hokkaido (Wikipedia standard SVG coordinates).
 */
object HokkaidoRoadNetwork {
    val segments: List<HighwaySegment> = listOf(
        HighwaySegment(
            id = "route_5_south",
            name = "国道5号（道南・函館〜長万部）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "5",
            waypoints = listOf(
                Offset(105.0f, 328.0f), // 函館駅前
                Offset(103.0f, 324.0f), // 五稜郭
                Offset(98.0f, 317.0f), // 七飯
                Offset(87.5f, 307.5f), // 大沼公園
                Offset(76.0f, 295.5f), // 森
                Offset(75.5f, 293.0f),
                Offset(75.0f, 290.5f),
                Offset(74.8f, 288.0f),
                Offset(75.2f, 285.5f),
                Offset(76.0f, 283.0f), // 八雲
                Offset(77.5f, 280.5f),
                Offset(79.5f, 278.0f),
                Offset(81.5f, 275.5f),
                Offset(83.5f, 273.5f),
                Offset(85.0f, 272.5f), // 長万部
            ),
        ),
        HighwaySegment(
            id = "route_9_suttsu",
            name = "道道9号（寿都黒松内線: 長万部〜寿都）",
            category = HighwayCategory.PREFECTURAL_ROAD,
            routeNumber = "道道9",
            waypoints = listOf(
                Offset(85.0f, 272.5f), // 長万部
                Offset(82.0f, 268.0f), // 二股
                Offset(78.5f, 264.0f), // 蕨岱
                Offset(76.0f, 260.0f), // 黒松内
                Offset(75.2f, 256.0f), // 朱太川沿い
                Offset(74.5f, 253.0f),
                Offset(74.0f, 251.5f), // 寿都湾・寿都役場
            ),
        ),
        HighwaySegment(
            id = "route_229_motsuta",
            name = "国道229号・追分ソーランライン（寿都〜茂津多岬）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "229",
            waypoints = listOf(
                Offset(74.0f, 251.5f), // 寿都
                Offset(72.0f, 254.5f), // 樽岸
                Offset(70.0f, 257.0f), // 美谷
                Offset(68.5f, 258.5f), // 島牧役場
                Offset(66.5f, 261.5f), // 茂津多岬入口
            ),
        ),
        HighwaySegment(
            id = "route_229_raiden",
            name = "国道229号・雷電国道（寿都〜雷電海岸〜岩内）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "229",
            waypoints = listOf(
                Offset(74.0f, 251.5f), // 寿都
                Offset(84.67f, 249.99f), // 磯谷・寿都境
                Offset(87.92f, 245.11f), // 見返り坂（雷電海岸）
                Offset(95.10f, 235.90f), // 東山
                Offset(93.58f, 225.73f), // 栄町
                Offset(88.0f, 219.0f), // 岩内港
            ),
        ),
        HighwaySegment(
            id = "route_229_shakotan_west",
            name = "国道229号（積丹西岸: 岩内〜神威岬）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "229",
            waypoints = listOf(
                Offset(88.0f, 219.0f), // 岩内
                Offset(91.0f, 217.5f),
                Offset(94.0f, 215.0f),
                Offset(96.0f, 212.5f), // 泊
                Offset(96.5f, 210.0f), // 兜岩
                Offset(95.5f, 208.0f),
                Offset(94.0f, 207.0f),
                Offset(93.0f, 206.0f), // 神恵内
                Offset(93.6f, 204.0f),
                Offset(94.2f, 203.5f), // 神威岬
            ),
        ),
        HighwaySegment(
            id = "route_shakotan_east",
            name = "道道913号〜国道229号（積丹東岸: 神威岬〜余市〜小樽）",
            category = HighwayCategory.PREFECTURAL_ROAD,
            routeNumber = "229",
            waypoints = listOf(
                Offset(94.2f, 203.5f), // 神威岬
                Offset(96.52f, 205.16f), // 島武意海岸
                Offset(99.62f, 210.45f), // 美国
                Offset(101.50f, 210.00f), // 余市駅
                Offset(111.05f, 220.21f), // 長橋
                Offset(123.13f, 219.33f), // メルヘン交差点
                Offset(125.0f, 216.0f), // 小樽運河
            ),
        ),
        HighwaySegment(
            id = "route_5_sapporo",
            name = "国道5号（小樽〜札幌）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "5",
            waypoints = listOf(
                Offset(125.0f, 216.0f), // 小樽
                Offset(127.6f, 220.0f), // 銭函
                Offset(132.0f, 223.0f), // 手稲
                Offset(136.0f, 228.0f), // 札幌時計台
            ),
        ),
        HighwaySegment(
            id = "route_231",
            name = "国道231号（オロロンライン: 札幌〜留萌）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "231",
            waypoints = listOf(
                Offset(136.0f, 228.0f), // 札幌
                Offset(136.92f, 223.77f), // 星置川河口
                Offset(148.0f, 214.0f), // 石狩湾新港
                Offset(152.84f, 201.15f), // 石狩中央橋
                Offset(149.0f, 181.97f), // 浜益海浜公園
                Offset(149.0f, 175.0f), // 雄冬岬
                Offset(153.0f, 168.0f),
                Offset(156.45f, 165.77f), // 増毛
                Offset(167.5f, 157.0f), // 留萌
            ),
        ),
        HighwaySegment(
            id = "route_232",
            name = "国道232号（オロロンライン: 留萌〜初山別〜遠別〜天塩更岸）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "232",
            waypoints = listOf(
                Offset(167.5f, 157.0f), // 留萌
                Offset(169.0f, 146.5f), // 小平
                Offset(169.0f, 139.5f), // 苫前
                Offset(171.5f, 126.5f), // 羽幌
                Offset(175.0f, 115.0f), // 初山別
                Offset(176.5f, 105.0f),
                Offset(177.5f, 98.0f), // 遠別
                Offset(177.0f, 92.0f),
                Offset(176.5f, 87.0f), // 天塩
                Offset(176.0f, 82.5f),
                Offset(175.5f, 78.0f), // 更岸川橋
            ),
        ),
        HighwaySegment(
            id = "route_106",
            name = "道道106号（オロロンライン: 天塩更岸〜ノシャップ岬）",
            category = HighwayCategory.PREFECTURAL_ROAD,
            routeNumber = "道道106",
            waypoints = listOf(
                Offset(175.5f, 78.0f), // 更岸
                Offset(174.0f, 68.0f), // オトンルイ風力発電所
                Offset(172.5f, 58.0f), // サロベツ原野
                Offset(171.0f, 46.0f), // 抜海
                Offset(170.1f, 33.5f), // ノシャップ岬
            ),
        ),
        HighwaySegment(
            id = "route_238_soya_west",
            name = "国道238号（稚内市街〜宗谷岬）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "238",
            waypoints = listOf(
                Offset(170.1f, 33.5f), // ノシャップ岬
                Offset(170.6f, 35.0f), // 稚内港・稚内駅
                Offset(174.0f, 36.5f), // 潮見
                Offset(180.0f, 36.0f), // 声問
                Offset(184.0f, 31.0f), // 宗谷
                Offset(186.5f, 26.0f), // 宗谷岬
            ),
        ),
        HighwaySegment(
            id = "route_esanuka",
            name = "国道238号・エサヌカ線（宗谷岬〜猿払〜枝幸）",
            category = HighwayCategory.SCENIC_ROAD,
            routeNumber = "238",
            waypoints = listOf(
                Offset(186.5f, 26.0f), // 宗谷岬
                Offset(193.0f, 38.0f), // 猿払
                Offset(198.0f, 48.0f), // エサヌカ線直線
                Offset(202.0f, 58.0f), // 浜鬼志別
                Offset(208.0f, 70.0f), // 枝幸
            ),
        ),
        HighwaySegment(
            id = "route_238_okhotsk",
            name = "国道238号（オホーツクライン: 枝幸〜紋別〜網走）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "238",
            waypoints = listOf(
                Offset(208.0f, 70.0f), // 枝幸
                Offset(213.0f, 76.0f), // 北見神威岬
                Offset(222.0f, 92.0f), // 雄武
                Offset(227.0f, 100.0f), // 日の出岬
                Offset(233.0f, 107.0f), // 興部
                Offset(238.0f, 111.0f), // 沙留岬
                Offset(245.0f, 118.0f), // 紋別
                Offset(254.0f, 123.0f), // コムケ湖
                Offset(260.0f, 127.0f), // 湧別
                Offset(272.0f, 131.0f), // サロマ湖
                Offset(285.0f, 137.0f), // 常呂
                Offset(291.53f, 145.86f),
                Offset(303.07f, 147.50f),
                Offset(308.0f, 144.0f), // 網走
            ),
        ),
        HighwaySegment(
            id = "route_244_334",
            name = "国道244号・334号（網走〜斜里〜ウトロ）",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "334",
            waypoints = listOf(
                Offset(308.0f, 144.0f), // 網走
                Offset(312.0f, 151.0f), // 小清水原生花園
                Offset(325.0f, 158.0f), // 斜里
                Offset(337.4f, 162.5f), // 知床斜里
                Offset(345.0f, 162.5f), // オシンコシン
                Offset(357.9f, 159.7f), // ウトロ港
            ),
        ),
        HighwaySegment(
            id = "route_334_shiretoko",
            name = "国道334号（知床横断道路: ウトロ〜知床峠〜羅臼）",
            category = HighwayCategory.SCENIC_ROAD,
            routeNumber = "334",
            waypoints = listOf(
                Offset(357.9f, 159.7f), // ウトロ港
                Offset(362.0f, 157.0f), // 知床五湖分岐
                Offset(370.0f, 146.5f), // 知床峠入口
                Offset(373.5f, 146.2f), // 知床峠頂上
                Offset(377.0f, 143.0f), // 羅臼ゲート
                Offset(379.5f, 142.8f), // 羅臼町街
            ),
        ),
        HighwaySegment(
            id = "route_87",
            name = "道道87号（知床公園羅臼線: 羅臼〜相泊）",
            category = HighwayCategory.PREFECTURAL_ROAD,
            routeNumber = "道道87",
            waypoints = listOf(
                Offset(379.5f, 142.8f), // 羅臼
                Offset(381.5f, 143.2f), // ネイチャークルーズ
                Offset(384.0f, 139.0f), // 昆布干し場
                Offset(383.5f, 136.0f), // セセキ温泉
                Offset(387.5f, 132.0f), // 相泊（道路終点）
            ),
        ),
    )
}
