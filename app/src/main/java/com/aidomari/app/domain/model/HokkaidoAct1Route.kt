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
    val description: String = "",
)

/**
 * 北海道一周すごろく（第1部：函館駅前 〜 相泊終点）厳選ルートデータ。
 * 旅行の目的地としてふさわしい主要ランドマーク・景勝地・岬・道の駅・温泉のみを厳選したルートです。
 * 海岸線沿いの一筆書き順（函館 → 寿都 → 積丹 → 留萌 → 稚内・宗谷岬 → オホーツク → 知床相泊）の順序付きリストで管理されます。
 */
object HokkaidoAct1Route {

    /** 公式一次測地データに基づくアンカーポイント（Rank 1主要拠点およびRank 2中間拠点） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(id = "hakodate_station", rank = 1, name = "函館駅前", municipality = "函館市", lon = 140.7264, lat = 41.7738, x = 185.64f, y = 342.97f, description = "函館駅前。北海道一周すごろく第1部の出発点。"),
        RouteAnchor(id = "goryokaku_park", rank = 1, name = "五稜郭タワー", municipality = "函館市", lon = 140.7538, lat = 41.7945, x = 187.39f, y = 341.20f, description = "特別史跡五稜郭公園と、星形の城郭を一望できる五稜郭タワー。"),
        RouteAnchor(id = "onuma_park", rank = 1, name = "大沼国定公園", municipality = "七飯町", lon = 140.6723, lat = 41.9814, x = 182.19f, y = 325.17f, description = "秀峰・駒ヶ岳を望む大沼・小沼の絶景が広がる国定公園。"),
        RouteAnchor(id = "mori_station", rank = 1, name = "森駅", municipality = "森町", lon = 140.5750, lat = 42.1090, x = 175.98f, y = 314.20f, description = "名物「元祖いかめし」で全国的に有名な函館本線の駅。"),
        RouteAnchor(id = "yakumo", rank = 1, name = "八雲", municipality = "八雲町", lon = 140.2678, lat = 42.2514, x = 156.36f, y = 301.94f, description = "北海道の木彫り熊発祥の地。"),
        RouteAnchor(id = "oshamambe", rank = 1, name = "長万部", municipality = "長万部町", lon = 140.3750, lat = 42.5125, x = 163.21f, y = 279.37f, description = "名物「かにめし」や温泉街で知られる交通の要衝。"),
        RouteAnchor(id = "suttsu_michinoeki", rank = 1, name = "みなとま〜れ寿都", municipality = "寿都町", lon = 140.2318, lat = 42.7954, x = 154.07f, y = 254.81f, description = "寿都湾を望む道の駅。新鮮な生しらすや海産物が名物。"),
        RouteAnchor(id = "benkei_cape", rank = 1, name = "弁慶岬", municipality = "寿都町", lon = 140.1895, lat = 42.8253, x = 151.37f, y = 252.21f, description = "義経・弁慶伝説が残る寿都湾屈指の景勝地。"),
        RouteAnchor(id = "shimamaki_michinoeki", rank = 1, name = "よってけ！島牧", municipality = "島牧村", lon = 140.0134, lat = 42.6867, x = 140.12f, y = 264.26f, description = "後志南部の海岸線に位置する道の駅。海産物直売所が人気。"),
        RouteAnchor(id = "motsuta_cape", rank = 1, name = "茂津多岬", municipality = "せたな町", lon = 139.8278, lat = 42.6144, x = 128.27f, y = 270.54f, description = "日本一高い海抜290mの断崖絶壁に立つ茂津多岬灯台。"),
        RouteAnchor(id = "raiden_onsen", rank = 1, name = "雷電温泉", municipality = "岩内町", lon = 140.4028, lat = 42.9242, x = 164.98f, y = 243.60f, description = "弁慶の隠れ岩や断崖絶壁が連なる雷電海岸の秘湯温泉郷。"),
        RouteAnchor(id = "iwanai_michinoeki", rank = 1, name = "道の駅いわない", municipality = "岩内町", lon = 140.5168, lat = 42.9834, x = 172.26f, y = 238.43f, description = "旧国鉄岩内駅跡地にある道の駅。日本海の好漁場として栄えた岩内港近く。"),
        RouteAnchor(id = "sakazuki_onsen", rank = 1, name = "盃温泉", municipality = "泊村", lon = 140.4531, lat = 43.1129, x = 168.19f, y = 227.12f, description = "盃海岸の入江に面した風情ある温泉地。"),
        RouteAnchor(id = "kamoenai_michinoeki", rank = 1, name = "オスコイ！かもえない", municipality = "神恵内村", lon = 140.3880, lat = 43.1653, x = 164.04f, y = 222.53f, description = "西積丹の海岸線に佇む道の駅。ホタテや海産物が名物。"),
        RouteAnchor(id = "jubo_cape", rank = 1, name = "西の河原", municipality = "積丹町", lon = 140.3380, lat = 43.2603, x = 160.85f, y = 214.21f, description = "ジュウボウ岬の南に位置する積丹半島屈指の海岸霊場。"),
        RouteAnchor(id = "kamui_cape", rank = 1, name = "神威岬", municipality = "積丹町", lon = 140.3480, lat = 43.3337, x = 161.48f, y = 207.77f, description = "積丹ブルーの海に鋭く突き出るシャコタン半島随一の絶景岬。"),
        RouteAnchor(id = "shakotan_demisaki_lighthouse", rank = 1, name = "島武意海岸", municipality = "積丹町", lon = 140.4814, lat = 43.3731, x = 170.00f, y = 204.31f, description = "日本の渚百選に選ばれた透明度抜群の断崖海岸。積丹出岬灯台を望む。"),
        RouteAnchor(id = "ogon_cape_bikuni", rank = 1, name = "黄金岬", municipality = "積丹町", lon = 140.6019, lat = 43.3027, x = 177.69f, y = 210.49f, description = "美国港にある黄金岬。夕日に輝く奇岩と宝島を望む展望地。"),
        RouteAnchor(id = "setakamui_rock", rank = 1, name = "セタカムイ岩", municipality = "古平町", lon = 140.6872, lat = 43.2554, x = 183.14f, y = 214.64f, description = "遠吠えする犬の姿に見える義経伝説の奇岩。"),
        RouteAnchor(id = "nikka_yoichi_distillery", rank = 1, name = "余市蒸溜所", municipality = "余市町", lon = 140.7917, lat = 43.1875, x = 189.81f, y = 220.59f, description = "ニッカウヰスキー創業者・竹鶴政孝が築いた歴史あるウイスキー蒸溜所。"),
        RouteAnchor(id = "oshoro_port", rank = 1, name = "忍路港", municipality = "小樽市", lon = 140.8646, lat = 43.2079, x = 194.47f, y = 218.80f, description = "古代の忍路環状列石や美しい天然の良港として知られる入り江。"),
        RouteAnchor(id = "otaru_canal", rank = 1, name = "小樽運河", municipality = "小樽市", lon = 141.0030, lat = 43.1979, x = 203.30f, y = 219.68f, description = "浅草橋街園から望む歴史的石造倉庫群が連なる小樽随一の観光名所。"),
        RouteAnchor(id = "hiyoriyama_lighthouse", rank = 1, name = "日和山灯台", municipality = "小樽市", lon = 141.0161, lat = 43.2383, x = 204.14f, y = 216.14f, description = "映画「喜びも悲しみも幾歳月」の舞台となった祝津の高台に立つ赤白灯台。"),
        RouteAnchor(id = "zenibako", rank = 1, name = "銭函", municipality = "小樽市", lon = 141.1600, lat = 43.1452, x = 213.32f, y = 224.29f, description = "鰊漁の豊漁で銭箱が積み上がったとされる縁起の良いレトロ駅と海岸。"),
        RouteAnchor(id = "ishikari_lighthouse", rank = 1, name = "石狩灯台", municipality = "石狩市", lon = 141.3606, lat = 43.2569, x = 226.13f, y = 214.51f, description = "石狩川河口のはまなすの丘公園に立つ道内最古の赤白灯台。"),
        RouteAnchor(id = "atsuta_michinoeki", rank = 1, name = "あいろーど厚田", municipality = "石狩市", lon = 141.4313, lat = 43.4046, x = 230.64f, y = 201.55f, description = "恋人の聖地・厚田展望台と日本海に沈む美しい夕日を望む道の駅。"),
        RouteAnchor(id = "hamamasu_beach_park", rank = 1, name = "浜益海浜公園", municipality = "石狩市", lon = 141.3868, lat = 43.5848, x = 227.80f, y = 185.69f, description = "はまますピリカ・ビーチが広がる日本海沿いの海浜公園。"),
        RouteAnchor(id = "ofuyu_cape_observatory", rank = 1, name = "雄冬岬展望台", municipality = "増毛町", lon = 141.3450, lat = 43.7407, x = 225.13f, y = 171.93f, description = "かつて陸の孤島と呼ばれた雄冬岬の断崖絶壁を見渡す展望台。"),
        RouteAnchor(id = "kunimare_brewery", rank = 1, name = "国稀酒造", municipality = "増毛町", lon = 141.5246, lat = 43.8579, x = 236.60f, y = 161.56f, description = "明治35年創業、暑寒別岳の伏流水で仕込む日本最北の造り酒屋。"),
        RouteAnchor(id = "ogon_cape_rumoi", rank = 1, name = "留萌 黄金岬", municipality = "留萌市", lon = 141.6296, lat = 43.9461, x = 243.30f, y = 153.75f, description = "夕日に染まる日本海が美しい、かつて鰊の見張り台だった名勝。"),
        RouteAnchor(id = "obira_michinoeki", rank = 1, name = "おびら鰊番屋", municipality = "小平町", lon = 141.6561, lat = 44.1374, x = 245.00f, y = 136.76f, description = "国指定重要文化財「旧花田家番屋」が隣接する鰊漁の歴史を伝える道の駅。"),
        RouteAnchor(id = "tomamae_michinoeki", rank = 1, name = "風Wとままえ", municipality = "苫前町", lon = 141.6565, lat = 44.3133, x = 245.02f, y = 121.09f, description = "巨大風車と日本海、天売・焼尻島を望む温泉付き道の駅。"),
        RouteAnchor(id = "sankebetsu_bear_incident_site", rank = 1, name = "三毛別羆事件跡", municipality = "苫前町", lon = 141.7995, lat = 44.1315, x = 254.15f, y = 137.28f, description = "大正4年に発生した日本最大の獣害事件を伝える復元現地。"),
        RouteAnchor(id = "haboro_port", rank = 1, name = "羽幌港", municipality = "羽幌町", lon = 141.7010, lat = 44.3680, x = 247.86f, y = 116.20f, description = "日本海の離島・天売島と焼尻島へのフェリーが発着する港。"),
        RouteAnchor(id = "konpira_cape", rank = 1, name = "金比羅岬", municipality = "初山別村", lon = 141.7739, lat = 44.5631, x = 252.52f, y = 98.75f, description = "みさき台公園の岬突端、日本海の波打ち際に立つ海中鳥居が有名。"),
        RouteAnchor(id = "embetsu_michinoeki", rank = 1, name = "道の駅えんべつ富士見", municipality = "遠別町", lon = 141.7921, lat = 44.7137, x = 253.68f, y = 85.24f, description = "富士見ヶ丘公園の高台に位置し、利尻富士を一望できる道の駅。"),
        RouteAnchor(id = "teshio_michinoeki", rank = 1, name = "道の駅てしお", municipality = "天塩町", lon = 141.7478, lat = 44.8845, x = 250.85f, y = 69.87f, description = "天塩町の中心市街地にあり、特産しじみを使った料理が人気の道の駅。"),
        RouteAnchor(id = "teshio_river_mouth", rank = 2, name = "天塩川河口", municipality = "天塩町", lon = 141.7286, lat = 44.9235, x = 249.44f, y = 66.35f, description = "日本海に注ぐ北海道第二の大河・天塩川の河口。オロロンライン天塩河口大橋が架かる絶景。"),
        RouteAnchor(id = "latitude_45_monument", rank = 1, name = "北緯45度モニュメント", municipality = "幌延町", lon = 141.6813, lat = 45.0023, x = 246.60f, y = 59.25f, description = "オロロンライン沿いに立つ、地球の北極点と赤道のちょうど中間を示す記念碑。"),
        RouteAnchor(id = "sarobetsu_plain", rank = 1, name = "サロベツ原野", municipality = "豊富町", lon = 141.6400, lat = 45.1500, x = 243.97f, y = 45.89f, description = "見渡す限りの大湿原が広がる利尻礼文サロベツ国立公園の原生花園。"),
        RouteAnchor(id = "bakkai_port", rank = 1, name = "抜海港", municipality = "稚内市", lon = 141.6211, lat = 45.3107, x = 242.76f, y = 31.33f, description = "冬になると野生のゴマフアザラシが越冬のために群れ集まる日本最北の漁港。"),
        RouteAnchor(id = "noshappu_cape", rank = 1, name = "ノシャップ岬", municipality = "稚内市", lon = 141.6447, lat = 45.4490, x = 244.27f, y = 18.76f, description = "宗谷海峡を隔てて利尻富士と礼文島を望む野寒布岬。"),
        RouteAnchor(id = "wakkanai_station", rank = 1, name = "稚内駅", municipality = "稚内市", lon = 141.6770, lat = 45.4168, x = 246.33f, y = 21.69f, description = "JR日本最北端の駅。駅構内には最北端の線路終端標が残る。"),
        RouteAnchor(id = "soya_hills_white_path", rank = 1, name = "白い道", municipality = "稚内市", lon = 141.8942, lat = 45.4911, x = 260.20f, y = 14.92f, description = "宗谷丘陵に敷き詰められたホタテの貝殻が白く輝く絶景の小道。"),
        RouteAnchor(id = "soya_cape_monument", rank = 1, name = "宗谷岬", municipality = "稚内市", lon = 141.9366, lat = 45.5228, x = 262.90f, y = 12.04f, description = "日本最北端の地の碑が立つ、日本の本土最北端の地。"),
        RouteAnchor(id = "esanuka_line", rank = 1, name = "エサヌカ線", municipality = "猿払村", lon = 142.2480, lat = 45.2450, x = 282.78f, y = 37.29f, description = "電柱もガードレールもない地平線へ続くストレートロード。"),
        RouteAnchor(id = "kucharo_lake", rank = 1, name = "クッチャロ湖", municipality = "浜頓別町", lon = 142.3500, lat = 45.1270, x = 289.30f, y = 47.98f, description = "オオハクチョウが数千羽飛来するラムサール条約湿地。"),
        RouteAnchor(id = "kitami_kamui_cape_park", rank = 1, name = "北見神威岬", municipality = "枝幸町", lon = 142.5014, lat = 45.0589, x = 298.96f, y = 54.13f, description = "オホーツク海に鋭く落ち込む断崖。公園展望台から海を一望。"),
        RouteAnchor(id = "usutaibe_cape", rank = 1, name = "ウスタイベ千畳岩", municipality = "枝幸町", lon = 142.5823, lat = 44.9589, x = 304.13f, y = 63.16f, description = "畳を敷き詰めたような柱状節理の岩が広がるウスタイベ岬の名勝。"),
        RouteAnchor(id = "oumu_michinoeki", rank = 1, name = "道の駅おうむ", municipality = "雄武町", lon = 142.9630, lat = 44.5832, x = 328.43f, y = 96.95f, description = "旧国鉄興浜南線雄武駅跡地に立つ道の駅。オホーツク海を望む展望塔がある。"),
        RouteAnchor(id = "hinode_cape", rank = 1, name = "日の出岬", municipality = "雄武町", lon = 143.0620, lat = 44.5321, x = 334.75f, y = 101.53f, description = "オホーツク海の水平線と総ガラス張りの展望台ラ・ルーナが名所。"),
        RouteAnchor(id = "okoppe_michinoeki", rank = 1, name = "道の駅おこっぺ", municipality = "興部町", lon = 143.1239, lat = 44.4699, x = 338.70f, y = 107.10f, description = "名峰・名産品と乳製品が人気の旧興部駅跡地の道の駅。"),
        RouteAnchor(id = "mombetsu_port_garinko", rank = 1, name = "ガリンコ号のりば", municipality = "紋別市", lon = 143.3680, lat = 44.3350, x = 354.29f, y = 119.15f, description = "流氷砕氷船ガリンコ号が発着する紋別海洋交流館。"),
        RouteAnchor(id = "crab_claw_sculpture", rank = 1, name = "カニの爪オブジェ", municipality = "紋別市", lon = 143.3774, lat = 44.3277, x = 354.89f, y = 119.80f, description = "高さ12m・幅6mを誇る紋別のシンボル的巨大モニュメント。"),
        RouteAnchor(id = "yubetsu_michinoeki", rank = 1, name = "愛ランド湧別", municipality = "湧別町", lon = 143.7191, lat = 44.1314, x = 376.70f, y = 137.29f, description = "サロマ湖を望む遊園地ファミリー愛ランドが併設された道の駅。"),
        RouteAnchor(id = "kimuaneppu_cape", rank = 1, name = "キムアネップ岬", municipality = "佐呂間町", lon = 143.9118, lat = 44.1083, x = 389.00f, y = 139.35f, description = "サロマ湖に細長く突き出た岬。夕日とサンゴ草（アッケシソウ）の群生地。"),
        RouteAnchor(id = "hamasaroma", rank = 1, name = "浜佐呂間", municipality = "佐呂間町", lon = 143.9382, lat = 44.0805, x = 390.69f, y = 141.82f, description = "サロマ湖東岸に位置するホタテ・カキ養殖が盛んな港町。"),
        RouteAnchor(id = "tokoro_curling_hall", rank = 1, name = "常呂カーリング", municipality = "北見市", lon = 144.0560, lat = 44.1213, x = 398.21f, y = 138.19f, description = "日本を代表するトップ選手を輩出した国内最大級のカーリング専用ホール。"),
        RouteAnchor(id = "notoro_cape", rank = 1, name = "能取岬", municipality = "網走市", lon = 144.2431, lat = 44.1122, x = 410.15f, y = 139.00f, description = "オホーツク海に突き出す断崖と白黒ツートンの能取岬灯台。"),
        RouteAnchor(id = "abashiri_michinoeki", rank = 1, name = "流氷街道網走", municipality = "網走市", lon = 144.2750, lat = 44.0240, x = 412.19f, y = 146.84f, description = "網走港に面し、流氷観光砕氷船おーろら号が発着する道の駅。"),
        RouteAnchor(id = "koshimizu_primeval_garden", rank = 1, name = "小清水原生花園", municipality = "小清水町", lon = 144.4134, lat = 43.9419, x = 421.03f, y = 154.12f, description = "オホーツク海と濤沸湖に挟まれた砂丘に色とりどりの野花が咲き誇る名所。"),
        RouteAnchor(id = "road_to_heaven", rank = 1, name = "天に続く道", municipality = "斜里町", lon = 144.7987, lat = 43.9068, x = 445.63f, y = 157.23f, description = "約28kmにわたり直線道路が遥か彼方の天へと続くかのように見える絶景ロード。"),
        RouteAnchor(id = "utoro_port", rank = 1, name = "ウトロ港", municipality = "斜里町", lon = 144.9960, lat = 44.0680, x = 458.22f, y = 142.93f, description = "知床観光船が発着する世界自然遺産・知床半島の西海岸の拠点港。"),
        RouteAnchor(id = "shiretoko_five_lakes", rank = 1, name = "知床五湖", municipality = "斜里町", lon = 145.0805, lat = 44.1222, x = 463.62f, y = 138.11f, description = "知床連山と原生林に囲まれた幻想的な5つの湖。高架木道と地上遊歩道。"),
        RouteAnchor(id = "shiretoko_pass", rank = 1, name = "知床峠", municipality = "羅臼町", lon = 145.1100, lat = 44.0500, x = 465.50f, y = 144.53f, description = "標高738mから羅臼岳と北方領土・国後島を一望する知床横断道路の頂上。"),
        RouteAnchor(id = "rausu_michinoeki", rank = 1, name = "道の駅知床らうす", municipality = "羅臼町", lon = 145.1921, lat = 44.0174, x = 470.74f, y = 147.42f, description = "羅臼港の目の前にあり、羅臼昆布や新鮮な海産物が並ぶ知床東海岸の拠点。"),
        RouteAnchor(id = "aidomari_onsen", rank = 1, name = "相泊温泉", municipality = "羅臼町", lon = 145.3175, lat = 44.1845, x = 478.75f, y = 132.57f, description = "知床半島東海岸、日本最北東端にある野趣あふれる海岸の露天風呂。"),
        RouteAnchor(id = "aidomari_bridge", rank = 1, name = "相泊橋", municipality = "羅臼町", lon = 145.3230, lat = 44.1904, x = 479.10f, y = 132.04f, description = "道道87号知床公園羅臼線の車道終点。北海道一周すごろく第1部のゴール。"),
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
                anchor.name.contains("温泉") -> SugorokuNodeType.REST_AREA
                anchor.name.contains("岬") || anchor.name.contains("灯台") || anchor.name.contains("碑") ||
                    anchor.name.contains("公園") || anchor.name.contains("展望") || anchor.name.contains("原野") ||
                    anchor.name.contains("湖") || anchor.name.contains("花園") || anchor.name.contains("丘") ||
                    anchor.name.contains("道") || anchor.name.contains("岩") || anchor.name.contains("オブジェ") ||
                    anchor.name.contains("運河") || anchor.name.contains("タワー") || anchor.name.contains("海岸") ||
                    anchor.name.contains("河口") -> SugorokuNodeType.SCENIC_SPOT
                anchor.name.contains("駅") || anchor.name.contains("通") || anchor.name.contains("町") ||
                    anchor.name.contains("蒸溜所") || anchor.name.contains("酒造") || anchor.name.contains("ホール") ||
                    anchor.name.contains("カーリング") || anchor.name.contains("のりば") -> SugorokuNodeType.TOWN_CENTER
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
                description = anchor.description,
                roadName = "",
                isAnchor = true,
                rank = anchor.rank,
            )
        }
    }
}
