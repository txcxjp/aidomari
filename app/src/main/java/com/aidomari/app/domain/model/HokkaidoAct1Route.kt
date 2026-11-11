package com.aidomari.app.domain.model

/**
 * アンカーポイント定義。
 * 階層ランク（Rank 1: 主要拠点, Rank 2: 中間拠点, Rank 3: 詳細拠点）、所属自治体（市区町村）および公式一次測地GPS座標・SVG座標。
 */
data class RouteAnchor(
    val id: String,
    val rank: Int,
    val name: String,
    val municipality: String,
    val lon: Double,
    val lat: Double,
    val x: Float,
    val y: Float,
)

/**
 * 北海道一周すごろく（第1部：函館駅前 〜 相泊終点）厳選ルートデータ。
 * 旅行の目的地としてふさわしい主要ランドマーク・景勝地・岬・道の駅・温泉のみを厳選したルートです。
 * 海岸線沿いの一筆書き順（函館 → 寿都 → 積丹 → 留萌 → 稚内・宗谷岬 → オホーツク → 知床相泊）の順序付きリストで管理されます。
 */
object HokkaidoAct1Route {

    /** 公式一次測地データに基づくアンカーポイント（初期全67箇所のRank 1アンカー） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(id = "hakodate_station", rank = 1, name = "函館駅前", municipality = "函館市", lon = 140.7264, lat = 41.7738, x = 185.64f, y = 342.97f),
        RouteAnchor(id = "goryokaku_park", rank = 1, name = "五稜郭公園（五稜郭タワー）", municipality = "函館市", lon = 140.7538, lat = 41.7945, x = 187.39f, y = 341.20f),
        RouteAnchor(id = "onuma_park", rank = 1, name = "大沼国定公園", municipality = "七飯町", lon = 140.6723, lat = 41.9814, x = 182.19f, y = 325.17f),
        RouteAnchor(id = "mori_station", rank = 1, name = "森駅（名物いかめし）", municipality = "森町", lon = 140.5750, lat = 42.1090, x = 175.98f, y = 314.20f),
        RouteAnchor(id = "yakumo", rank = 1, name = "八雲（木彫り熊発祥の地）", municipality = "八雲町", lon = 140.2678, lat = 42.2514, x = 156.36f, y = 301.94f),
        RouteAnchor(id = "oshamambe", rank = 1, name = "長万部（名物かにめし）", municipality = "長万部町", lon = 140.3750, lat = 42.5125, x = 163.21f, y = 279.37f),
        RouteAnchor(id = "suttsu_michinoeki", rank = 1, name = "道の駅みなとま〜れ寿都", municipality = "寿都町", lon = 140.2318, lat = 42.7954, x = 154.07f, y = 254.81f),
        RouteAnchor(id = "benkei_cape", rank = 1, name = "弁慶岬", municipality = "寿都町", lon = 140.1895, lat = 42.8253, x = 151.37f, y = 252.21f),
        RouteAnchor(id = "shimamaki_michinoeki", rank = 1, name = "道の駅よってけ！島牧", municipality = "島牧村", lon = 140.0134, lat = 42.6867, x = 140.12f, y = 264.26f),
        RouteAnchor(id = "motsuta_cape", rank = 1, name = "茂津多岬", municipality = "せたな町", lon = 139.8278, lat = 42.6144, x = 128.27f, y = 270.54f),
        RouteAnchor(id = "raiden_onsen", rank = 1, name = "雷電温泉郷", municipality = "岩内町", lon = 140.4028, lat = 42.9242, x = 164.98f, y = 243.60f),
        RouteAnchor(id = "iwanai_michinoeki", rank = 1, name = "道の駅いわない（岩内港）", municipality = "岩内町", lon = 140.5168, lat = 42.9834, x = 172.26f, y = 238.43f),
        RouteAnchor(id = "sakazuki_onsen", rank = 1, name = "盃温泉郷", municipality = "泊村", lon = 140.4531, lat = 43.1129, x = 168.19f, y = 227.12f),
        RouteAnchor(id = "kamoenai_michinoeki", rank = 1, name = "道の駅オスコイ！かもえない", municipality = "神恵内村", lon = 140.3880, lat = 43.1653, x = 164.04f, y = 222.53f),
        RouteAnchor(id = "jubo_cape", rank = 1, name = "ジュウボウ岬（西の河原）", municipality = "積丹町", lon = 140.3380, lat = 43.2603, x = 160.85f, y = 214.21f),
        RouteAnchor(id = "kamui_cape", rank = 1, name = "神威岬", municipality = "積丹町", lon = 140.3480, lat = 43.3337, x = 161.48f, y = 207.77f),
        RouteAnchor(id = "shakotan_demisaki_lighthouse", rank = 1, name = "積丹出岬灯台（島武意海岸）", municipality = "積丹町", lon = 140.4814, lat = 43.3731, x = 170.00f, y = 204.31f),
        RouteAnchor(id = "ogon_cape_bikuni", rank = 1, name = "黄金岬（美国）", municipality = "積丹町", lon = 140.6019, lat = 43.3027, x = 177.69f, y = 210.49f),
        RouteAnchor(id = "setakamui_rock", rank = 1, name = "セタカムイ岩（義経伝説の奇岩）", municipality = "古平町", lon = 140.6872, lat = 43.2554, x = 183.14f, y = 214.64f),
        RouteAnchor(id = "nikka_yoichi_distillery", rank = 1, name = "ニッカウヰスキー余市蒸溜所", municipality = "余市町", lon = 140.7917, lat = 43.1875, x = 189.81f, y = 220.59f),
        RouteAnchor(id = "oshoro_port", rank = 1, name = "忍路港", municipality = "小樽市", lon = 140.8646, lat = 43.2079, x = 194.47f, y = 218.80f),
        RouteAnchor(id = "otaru_canal", rank = 1, name = "小樽運河（浅草橋街園）", municipality = "小樽市", lon = 141.0030, lat = 43.1979, x = 203.30f, y = 219.68f),
        RouteAnchor(id = "hiyoriyama_lighthouse", rank = 1, name = "日和山灯台（祝津）", municipality = "小樽市", lon = 141.0161, lat = 43.2383, x = 204.14f, y = 216.14f),
        RouteAnchor(id = "zenibako", rank = 1, name = "銭函（縁起の良いレトロ駅・海岸）", municipality = "小樽市", lon = 141.1600, lat = 43.1452, x = 213.32f, y = 224.29f),
        RouteAnchor(id = "ishikari_lighthouse", rank = 1, name = "石狩灯台（はまなすの丘公園）", municipality = "石狩市", lon = 141.3606, lat = 43.2569, x = 226.13f, y = 214.51f),
        RouteAnchor(id = "atsuta_michinoeki", rank = 1, name = "道の駅石狩「あいろーど厚田」", municipality = "石狩市", lon = 141.4313, lat = 43.4046, x = 230.64f, y = 201.55f),
        RouteAnchor(id = "hamamasu_beach_park", rank = 1, name = "浜益海浜公園（はまますピリカ・ビーチ）", municipality = "石狩市", lon = 141.3868, lat = 43.5848, x = 227.80f, y = 185.69f),
        RouteAnchor(id = "ofuyu_cape_observatory", rank = 1, name = "雄冬岬展望台", municipality = "増毛町", lon = 141.3450, lat = 43.7407, x = 225.13f, y = 171.93f),
        RouteAnchor(id = "kunimare_brewery", rank = 1, name = "国稀酒造（日本最北の造り酒屋）", municipality = "増毛町", lon = 141.5246, lat = 43.8579, x = 236.60f, y = 161.56f),
        RouteAnchor(id = "ogon_cape_rumoi", rank = 1, name = "黄金岬（留萌）", municipality = "留萌市", lon = 141.6296, lat = 43.9461, x = 243.30f, y = 153.75f),
        RouteAnchor(id = "obira_michinoeki", rank = 1, name = "道の駅おびら鰊番屋（小平町）", municipality = "小平町", lon = 141.6561, lat = 44.1374, x = 245.00f, y = 136.76f),
        RouteAnchor(id = "tomamae_michinoeki", rank = 1, name = "道の駅風Wとままえ（風車と夕日の温泉）", municipality = "苫前町", lon = 141.6565, lat = 44.3133, x = 245.02f, y = 121.09f),
        RouteAnchor(id = "sankebetsu_bear_incident_site", rank = 1, name = "三毛別羆事件復元地", municipality = "苫前町", lon = 141.7995, lat = 44.1315, x = 254.15f, y = 137.28f),
        RouteAnchor(id = "haboro_port", rank = 1, name = "羽幌港（フェリーターミナル）", municipality = "羽幌町", lon = 141.7010, lat = 44.3680, x = 247.86f, y = 116.20f),
        RouteAnchor(id = "konpira_cape", rank = 1, name = "金比羅岬（海中鳥居）", municipality = "初山別村", lon = 141.7739, lat = 44.5631, x = 252.52f, y = 98.75f),
        RouteAnchor(id = "embetsu_michinoeki", rank = 1, name = "遠別（道の駅富士見）", municipality = "遠別町", lon = 141.7921, lat = 44.7137, x = 253.68f, y = 85.24f),
        RouteAnchor(id = "teshio_michinoeki", rank = 1, name = "天塩（天塩川河口・道の駅てしお）", municipality = "天塩町", lon = 141.7478, lat = 44.8845, x = 250.85f, y = 69.87f),
        RouteAnchor(id = "latitude_45_monument", rank = 1, name = "北緯45度モニュメント", municipality = "幌延町", lon = 141.6813, lat = 45.0023, x = 246.60f, y = 59.25f),
        RouteAnchor(id = "sarobetsu_plain", rank = 1, name = "サロベツ原野", municipality = "豊富町", lon = 141.6400, lat = 45.1500, x = 243.97f, y = 45.89f),
        RouteAnchor(id = "bakkai_port", rank = 1, name = "抜海港（アザラシ越冬地）", municipality = "稚内市", lon = 141.6211, lat = 45.3107, x = 242.76f, y = 31.33f),
        RouteAnchor(id = "noshappu_cape", rank = 1, name = "ノシャップ岬（野寒布岬）", municipality = "稚内市", lon = 141.6447, lat = 45.4490, x = 244.27f, y = 18.76f),
        RouteAnchor(id = "wakkanai_station", rank = 1, name = "JR日本最北端の駅・稚内駅", municipality = "稚内市", lon = 141.6770, lat = 45.4168, x = 246.33f, y = 21.69f),
        RouteAnchor(id = "soya_hills_white_path", rank = 1, name = "宗谷丘陵（白い道・風車群）", municipality = "稚内市", lon = 141.8942, lat = 45.4911, x = 260.20f, y = 14.92f),
        RouteAnchor(id = "soya_cape_monument", rank = 1, name = "日本最北端の地の碑（宗谷岬）", municipality = "稚内市", lon = 141.9366, lat = 45.5228, x = 262.90f, y = 12.04f),
        RouteAnchor(id = "esanuka_line", rank = 1, name = "猿払村道エサヌカ線", municipality = "猿払村", lon = 142.2480, lat = 45.2450, x = 282.78f, y = 37.29f),
        RouteAnchor(id = "kucharo_lake", rank = 1, name = "クッチャロ湖（浜頓別）", municipality = "浜頓別町", lon = 142.3500, lat = 45.1270, x = 289.30f, y = 47.98f),
        RouteAnchor(id = "kitami_kamui_cape_park", rank = 1, name = "北見神威岬公園", municipality = "枝幸町", lon = 142.5014, lat = 45.0589, x = 298.96f, y = 54.13f),
        RouteAnchor(id = "usutaibe_cape", rank = 1, name = "ウスタイベ岬（千畳岩）", municipality = "枝幸町", lon = 142.5823, lat = 44.9589, x = 304.13f, y = 63.16f),
        RouteAnchor(id = "oumu_michinoeki", rank = 1, name = "雄武港（道の駅おうむ）", municipality = "雄武町", lon = 142.9630, lat = 44.5832, x = 328.43f, y = 96.95f),
        RouteAnchor(id = "hinode_cape", rank = 1, name = "日の出岬（ガラスの展望台）", municipality = "雄武町", lon = 143.0620, lat = 44.5321, x = 334.75f, y = 101.53f),
        RouteAnchor(id = "okoppe_michinoeki", rank = 1, name = "興部（道の駅おこっぺ）", municipality = "興部町", lon = 143.1239, lat = 44.4699, x = 338.70f, y = 107.10f),
        RouteAnchor(id = "mombetsu_port_garinko", rank = 1, name = "紋別港（ガリンコ号乗り場）", municipality = "紋別市", lon = 143.3680, lat = 44.3350, x = 354.29f, y = 119.15f),
        RouteAnchor(id = "crab_claw_sculpture", rank = 1, name = "カニの爪オブジェ（紋別のシンボル）", municipality = "紋別市", lon = 143.3774, lat = 44.3277, x = 354.89f, y = 119.80f),
        RouteAnchor(id = "yubetsu_michinoeki", rank = 1, name = "道の駅愛ランド湧別（湧別町）", municipality = "湧別町", lon = 143.7191, lat = 44.1314, x = 376.70f, y = 137.29f),
        RouteAnchor(id = "kimuaneppu_cape", rank = 1, name = "キムアネップ岬（サロマ湖夕日展望地）", municipality = "佐呂間町", lon = 143.9118, lat = 44.1083, x = 389.00f, y = 139.35f),
        RouteAnchor(id = "hamasaroma", rank = 1, name = "浜佐呂間（サロマ湖東岸）", municipality = "佐呂間町", lon = 143.9382, lat = 44.0805, x = 390.69f, y = 141.82f),
        RouteAnchor(id = "tokoro_curling_hall", rank = 1, name = "アドヴィックス常呂カーリングホール（カーリングの聖地）", municipality = "北見市", lon = 144.0560, lat = 44.1213, x = 398.21f, y = 138.19f),
        RouteAnchor(id = "notoro_cape", rank = 1, name = "能取岬（流氷展望の白亜灯台）", municipality = "網走市", lon = 144.2431, lat = 44.1122, x = 410.15f, y = 139.00f),
        RouteAnchor(id = "abashiri_michinoeki", rank = 1, name = "網走港（道の駅流氷街道網走）", municipality = "網走市", lon = 144.2750, lat = 44.0240, x = 412.19f, y = 146.84f),
        RouteAnchor(id = "koshimizu_primeval_garden", rank = 1, name = "小清水原生花園", municipality = "小清水町", lon = 144.4134, lat = 43.9419, x = 421.03f, y = 154.12f),
        RouteAnchor(id = "road_to_heaven", rank = 1, name = "天に続く道（遥かなる一本道）", municipality = "斜里町", lon = 144.7987, lat = 43.9068, x = 445.63f, y = 157.23f),
        RouteAnchor(id = "utoro_port", rank = 1, name = "ウトロ港（知床観光船乗り場）", municipality = "斜里町", lon = 144.9960, lat = 44.0680, x = 458.22f, y = 142.93f),
        RouteAnchor(id = "shiretoko_five_lakes", rank = 1, name = "知床五湖フィールドハウス", municipality = "斜里町", lon = 145.0805, lat = 44.1222, x = 463.62f, y = 138.11f),
        RouteAnchor(id = "shiretoko_pass", rank = 1, name = "知床峠（標高738m）", municipality = "羅臼町", lon = 145.1100, lat = 44.0500, x = 465.50f, y = 144.53f),
        RouteAnchor(id = "rausu_michinoeki", rank = 1, name = "道の駅知床・らうす（羅臼港）", municipality = "羅臼町", lon = 145.1921, lat = 44.0174, x = 470.74f, y = 147.42f),
        RouteAnchor(id = "aidomari_onsen", rank = 1, name = "相泊温泉（日本最北東端の温泉）", municipality = "羅臼町", lon = 145.3175, lat = 44.1845, x = 478.75f, y = 132.57f),
        RouteAnchor(id = "aidomari_bridge", rank = 1, name = "相泊橋（最果ての終着点・車道終点）", municipality = "羅臼町", lon = 145.3230, lat = 44.1904, x = 479.10f, y = 132.04f),
    )

    val totalNodes: Int get() = anchors.size
    val maxStepIndex: Int get() = totalNodes - 1

    val startAnchor: RouteAnchor get() = anchors.first()
    val goalAnchor: RouteAnchor get() = anchors.last()

    fun getAnchor(index: Int): RouteAnchor? = anchors.getOrNull(index.coerceIn(0, maxStepIndex))
    fun getAnchorById(id: String): RouteAnchor? = anchors.firstOrNull { it.id == id }
    fun getAnchorByIndex(index: Int): RouteAnchor? = anchors.getOrNull(index)

    /** 指定したアンカーIDの次のアンカーを取得します */
    fun getNextAnchor(currentId: String): RouteAnchor? {
        val index = anchors.indexOfFirst { it.id == currentId }
        return if (index in 0 until anchors.lastIndex) anchors[index + 1] else null
    }

    /** 指定したアンカーIDの前のアンカーを取得します */
    fun getPrevAnchor(currentId: String): RouteAnchor? {
        val index = anchors.indexOfFirst { it.id == currentId }
        return if (index in 1..anchors.lastIndex) anchors[index - 1] else null
    }

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
                anchor.name.contains("岬") || anchor.name.contains("灯台") || anchor.name.contains("碑") || anchor.name.contains("公園") || anchor.name.contains("展望") || anchor.name.contains("原野") || anchor.name.contains("湖") || anchor.name.contains("花園") || anchor.name.contains("丘") || anchor.name.contains("道") || anchor.name.contains("岩") || anchor.name.contains("オブジェ") || anchor.name.contains("運河") -> SugorokuNodeType.SCENIC_SPOT
                anchor.name.contains("駅") || anchor.name.contains("通") || anchor.name.contains("町") || anchor.name.contains("蒸溜所") || anchor.name.contains("酒造") || anchor.name.contains("ホール") -> SugorokuNodeType.TOWN_CENTER
                else -> SugorokuNodeType.NORMAL
            }
            SugorokuNode(
                id = anchor.id,
                stepIndex = index,
                name = anchor.name,
                municipality = anchor.municipality,
                type = nodeType,
                x = anchor.x,
                y = anchor.y,
                description = "",
                roadName = "",
                isAnchor = true,
                rank = anchor.rank,
            )
        }
    }
}
