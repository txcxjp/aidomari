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

    /** 公式一次測地データに基づくアンカーポイント（Rank 1主要拠点: 67, Rank 2中間拠点: 58, Rank 3詳細拠点: 1：全126箇所） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(id = "hakodate_station", rank = 1, name = "函館駅前", municipality = "函館市", lon = 140.7264, lat = 41.7738, x = 185.64f, y = 342.97f, description = "函館駅前。北海道一周すごろく第1部の出発点。"),
        RouteAnchor(id = "chiyogadai_park", rank = 2, name = "千代台公園", municipality = "函館市", lon = 140.7473, lat = 41.7855, x = 186.98f, y = 341.97f, description = "オーシャンスタジアムや陸上競技場を擁する函館市民のスポーツ拠点。"),
        RouteAnchor(id = "goryokaku_park", rank = 1, name = "五稜郭タワー", municipality = "函館市", lon = 140.7540, lat = 41.7946, x = 187.40f, y = 341.19f, description = "特別史跡五稜郭公園と、星形の城郭を一望できる五稜郭タワー。"),
        RouteAnchor(id = "michinoeki_nanairo_nanae", rank = 2, name = "道の駅なないろ・ななえ", municipality = "七飯町", lon = 140.6575, lat = 41.9255, x = 181.24f, y = 329.97f, description = "国道5号沿い道南屈指の人気道の駅。特産リンゴスイーツが有名。"),
        RouteAnchor(id = "onuma_park", rank = 1, name = "大沼国定公園", municipality = "七飯町", lon = 140.6701, lat = 41.9808, x = 182.05f, y = 325.23f, description = "秀峰・駒ヶ岳を望む大沼・小沼の絶景が広がる国定公園。"),
        RouteAnchor(id = "mori_station", rank = 1, name = "森駅", municipality = "森町", lon = 140.5734, lat = 42.1090, x = 175.87f, y = 314.20f, description = "名物「元祖いかめし」で全国的に有名な函館本線の駅。"),
        RouteAnchor(id = "funka_wan_panorama_park", rank = 2, name = "噴火湾パノラマパーク", municipality = "八雲町", lon = 140.3101, lat = 42.2306, x = 159.06f, y = 303.73f, description = "噴火湾（内浦湾）を一望する丘の上の大規模公園。"),
        RouteAnchor(id = "yakumo_station", rank = 1, name = "八雲駅", municipality = "八雲町", lon = 140.2729, lat = 42.2538, x = 156.69f, y = 301.73f, description = "北海道の木彫り熊発祥の地・八雲町の中心駅。"),
        RouteAnchor(id = "kuroiwa_odd_rocks", rank = 2, name = "黒岩奇岩", municipality = "八雲町", lon = 140.2884, lat = 42.3667, x = 157.68f, y = 291.98f, description = "八雲町北部の国道5号沿い、噴火湾の波打ち際にそびえる歴史ある奇岩名勝。"),
        RouteAnchor(id = "oshamambe_station", rank = 1, name = "長万部駅", municipality = "長万部町", lon = 140.3751, lat = 42.5125, x = 163.21f, y = 279.37f, description = "名物「かにめし」や温泉街で知られる交通の要衝駅。"),
        RouteAnchor(id = "michinoeki_kuromatsunai", rank = 2, name = "道の駅くろまつない", municipality = "黒松内町", lon = 140.3800, lat = 42.6727, x = 163.53f, y = 265.48f, description = "天然記念物「歌才ブナ自生北限地帯」に位置する人気の道の駅。"),
        RouteAnchor(id = "suttsu_wind_park", rank = 2, name = "寿都風力発電所", municipality = "寿都町", lon = 140.2369, lat = 42.7822, x = 154.39f, y = 255.96f, description = "風のまち寿都を象徴する海岸沿いの巨大風車群。"),
        RouteAnchor(id = "suttsu_michinoeki", rank = 1, name = "みなとま〜れ寿都", municipality = "寿都町", lon = 140.2320, lat = 42.7956, x = 154.08f, y = 254.80f, description = "寿都湾を望む道の駅。新鮮な生しらすや海産物が名物。"),
        RouteAnchor(id = "benkei_cape", rank = 1, name = "弁慶岬", municipality = "寿都町", lon = 140.1898, lat = 42.8249, x = 151.38f, y = 252.25f, description = "義経・弁慶伝説が残る寿都湾屈指の景勝地。"),
        RouteAnchor(id = "honme_cape", rank = 2, name = "本目岬", municipality = "島牧村", lon = 140.1328, lat = 42.7469, x = 147.75f, y = 259.03f, description = "白黒六角形の本目岬灯台が立ち、日本海と夕日を一望する名勝。"),
        RouteAnchor(id = "shimamaki_michinoeki", rank = 1, name = "よってけ！島牧", municipality = "島牧村", lon = 140.0108, lat = 42.6670, x = 139.96f, y = 265.97f, description = "後志南部の海岸線に位置する道の駅。海産物直売所が人気。"),
        RouteAnchor(id = "motta_coast_onsen", rank = 2, name = "モッタ海岸温泉", municipality = "島牧村", lon = 139.9152, lat = 42.6776, x = 133.85f, y = 265.05f, description = "日本海を望む海岸沿いに湧く国内有数の天然ラジウム秘湯。"),
        RouteAnchor(id = "motsuta_cape", rank = 1, name = "茂津多岬", municipality = "せたな町", lon = 139.8279, lat = 42.6145, x = 128.28f, y = 270.53f, description = "日本一高い海抜290mの断崖絶壁に立つ茂津多岬灯台。"),
        RouteAnchor(id = "michinoeki_shell_plaza", rank = 2, name = "道の駅シェルプラザ・港", municipality = "蘭越町", lon = 140.3697, lat = 42.8798, x = 162.87f, y = 247.46f, description = "国道229号沿い、貝の博物館を併設した蘭越町の海岸の道の駅。"),
        RouteAnchor(id = "raiden_coast", rank = 1, name = "雷電海岸", municipality = "岩内町", lon = 140.4028, lat = 42.9242, x = 164.98f, y = 243.60f, description = "弁慶の隠れ岩や断崖絶壁が連なる雷電海岸の景勝地。"),
        RouteAnchor(id = "iwanai_onsen", rank = 2, name = "いわない温泉", municipality = "岩内町", lon = 140.5074, lat = 42.9378, x = 171.66f, y = 242.41f, description = "日本海と積丹半島を見下ろす岩内岳山麓の風光明媚な名湯郷。"),
        RouteAnchor(id = "iwanai_michinoeki", rank = 1, name = "道の駅いわない", municipality = "岩内町", lon = 140.5124, lat = 42.9813, x = 171.98f, y = 238.61f, description = "旧国鉄岩内駅跡地にある道の駅。日本海の好漁場として栄えた岩内港近く。"),
        RouteAnchor(id = "kabuto_senjojiki", rank = 2, name = "兜千畳敷", municipality = "泊村", lon = 140.4989, lat = 43.0630, x = 171.12f, y = 231.48f, description = "カブトライン沿い、兜の形をした奇岩と平らな岩盤が広がる名勝。"),
        RouteAnchor(id = "sakazuki_onsen", rank = 1, name = "盃温泉", municipality = "泊村", lon = 140.4728, lat = 43.1129, x = 169.45f, y = 227.12f, description = "盃海岸の入江に面した風情ある温泉地。"),
        RouteAnchor(id = "ryujin_cape", rank = 2, name = "竜神岬", municipality = "神恵内村", lon = 140.4300, lat = 43.1370, x = 166.72f, y = 225.01f, description = "竜神伝説が残り、日本海に沈む夕日が美しい西積丹の景勝岬。"),
        RouteAnchor(id = "kamoenai_michinoeki", rank = 1, name = "オスコイ！かもえない", municipality = "神恵内村", lon = 140.3880, lat = 43.1653, x = 164.04f, y = 222.53f, description = "西積丹の海岸線に佇む道の駅。ホタテや海産物が名物。"),
        RouteAnchor(id = "sannai_nukumori_onsen", rank = 2, name = "珊内ぬくもり温泉", municipality = "神恵内村", lon = 140.3482, lat = 43.1967, x = 161.50f, y = 219.78f, description = "西積丹の海岸沿いに佇み、日本海と夕日を望む天然温泉。"),
        RouteAnchor(id = "jubo_cape", rank = 1, name = "西の河原", municipality = "積丹町", lon = 140.3400, lat = 43.2620, x = 160.97f, y = 214.06f, description = "ジュウボウ岬の南に位置する積丹半島屈指の海岸霊場。"),
        RouteAnchor(id = "kamui_cape", rank = 1, name = "神威岬", municipality = "積丹町", lon = 140.3547, lat = 43.3300, x = 161.91f, y = 208.10f, description = "積丹ブルーの海に鋭く突き出るシャコタン半島随一の絶景岬。"),
        RouteAnchor(id = "nozuka_camp_site", rank = 2, name = "道営野塚野営場", municipality = "積丹町", lon = 140.4448, lat = 43.3343, x = 167.66f, y = 207.72f, description = "積丹ブルーの美しい砂浜と夕日を望む道営の無料野営場。"),
        RouteAnchor(id = "shakotan_demisaki_lighthouse", rank = 1, name = "島武意海岸", municipality = "積丹町", lon = 140.4774, lat = 43.3731, x = 169.75f, y = 204.31f, description = "日本の渚百選に選ばれた透明度抜群の断崖海岸。積丹出岬灯台を望む。"),
        RouteAnchor(id = "joroko_rock", rank = 2, name = "女郎子岩", municipality = "積丹町", lon = 140.5005, lat = 43.3707, x = 171.22f, y = 204.52f, description = "義経伝説が残る、積丹の海上に立ち尽くす伝説の奇岩。"),
        RouteAnchor(id = "ogon_cape_bikuni", rank = 1, name = "黄金岬", municipality = "積丹町", lon = 140.6013, lat = 43.3032, x = 177.66f, y = 210.45f, description = "美国港にある黄金岬。夕日に輝く奇岩と宝島を望む展望地。"),
        RouteAnchor(id = "furubira_port", rank = 2, name = "古平港", municipality = "古平町", lon = 140.6358, lat = 43.2746, x = 179.86f, y = 212.96f, description = "たらこ発祥の地として知られる日本海の良港。丸山公園隣接。"),
        RouteAnchor(id = "setakamui_rock", rank = 1, name = "セタカムイ岩", municipality = "古平町", lon = 140.6872, lat = 43.2555, x = 183.14f, y = 214.63f, description = "遠吠えする犬の姿に見える義経伝説の奇岩。"),
        RouteAnchor(id = "candle_rock", rank = 2, name = "ローソク岩", municipality = "余市町", lon = 140.7038, lat = 43.2336, x = 184.20f, y = 216.55f, description = "余市沖の海上にそびえ立つ高さ約46mの幻想的な一本奇岩。"),
        RouteAnchor(id = "nikka_yoichi_distillery", rank = 1, name = "余市蒸溜所", municipality = "余市町", lon = 140.7895, lat = 43.1862, x = 189.67f, y = 220.70f, description = "ニッカウヰスキー創業者・竹鶴政孝が築いた歴史あるウイスキー蒸溜所。"),
        RouteAnchor(id = "ranshima_beach", rank = 2, name = "蘭島海水浴場", municipality = "小樽市", lon = 140.8510, lat = 43.1988, x = 193.60f, y = 219.60f, description = "明治36年に開設された北海道屈指の歴史を誇る海水浴場。"),
        RouteAnchor(id = "oshoro_port", rank = 1, name = "忍路港", municipality = "小樽市", lon = 140.8625, lat = 43.2078, x = 194.33f, y = 218.81f, description = "古代の忍路環状列石や美しい天然の良港として知られる入り江。"),
        RouteAnchor(id = "otamoi_coast", rank = 2, name = "オタモイ海岸", municipality = "小樽市", lon = 140.9586, lat = 43.2208, x = 200.47f, y = 217.67f, description = "断崖絶壁が連なるニセコ積丹小樽海岸国定公園の秘境名勝。"),
        RouteAnchor(id = "otaru_canal", rank = 1, name = "小樽運河", municipality = "小樽市", lon = 141.0006, lat = 43.2020, x = 203.15f, y = 219.32f, description = "浅草橋街園から望む歴史的石造倉庫群が連なる小樽随一の観光名所。"),
        RouteAnchor(id = "temiya_cave", rank = 2, name = "手宮洞窟保存館", municipality = "小樽市", lon = 141.0031, lat = 43.2121, x = 203.31f, y = 218.44f, description = "続縄文時代の古代文字や陰刻画が壁面に残る国指定史跡。"),
        RouteAnchor(id = "otaru_aquarium", rank = 1, name = "おたる水族館", municipality = "小樽市", lon = 141.0114, lat = 43.2370, x = 203.84f, y = 216.25f, description = "トドの豪快なダイビングショーや海獣公園で知られる北海道屈指の大規模水族館。"),
        RouteAnchor(id = "otaru_marina", rank = 2, name = "小樽港マリーナ", municipality = "小樽市", lon = 141.0246, lat = 43.1851, x = 204.68f, y = 220.80f, description = "ヨットハーバーやベイエリア商業施設が広がる小樽の港湾スポット。"),
        RouteAnchor(id = "zenibako_station", rank = 1, name = "銭函駅", municipality = "小樽市", lon = 141.1597, lat = 43.1437, x = 213.30f, y = 224.42f, description = "鰊漁の豊漁で銭箱が積み上がったとされる縁起の良い駅。"),
        RouteAnchor(id = "ishikari_shinko", rank = 2, name = "石狩湾新港", municipality = "石狩市", lon = 141.3079, lat = 43.2149, x = 222.77f, y = 218.19f, description = "日本海沿岸の物流・エネルギーを支える北海道屈指の重要港湾。"),
        RouteAnchor(id = "ishikari_lighthouse", rank = 1, name = "石狩灯台", municipality = "石狩市", lon = 141.3606, lat = 43.2569, x = 226.13f, y = 214.51f, description = "石狩川河口のはまなすの丘公園に立つ道内最古の赤白灯台。"),
        RouteAnchor(id = "atsuta_michinoeki", rank = 1, name = "あいろーど厚田", municipality = "石狩市", lon = 141.4313, lat = 43.4046, x = 230.64f, y = 201.55f, description = "恋人の聖地・厚田展望台と日本海に沈む美しい夕日を望む道の駅。"),
        RouteAnchor(id = "gokibiru_coast", rank = 2, name = "濃昼海岸", municipality = "石狩市", lon = 141.4037, lat = 43.4788, x = 228.88f, y = 195.02f, description = "かつて陸の孤島だった歴史を伝える濃昼山道が残る秘境海岸。"),
        RouteAnchor(id = "hamamasu_beach_park", rank = 1, name = "浜益海浜公園", municipality = "石狩市", lon = 141.3868, lat = 43.5848, x = 227.80f, y = 185.69f, description = "はまますピリカ・ビーチが広がる日本海沿いの海浜公園。"),
        RouteAnchor(id = "shiragane_waterfall", rank = 2, name = "白銀の滝", municipality = "石狩市", lon = 141.3368, lat = 43.7313, x = 224.61f, y = 172.76f, description = "国道231号沿いに直接流れ落ちる落差25mの雄大な名瀑。"),
        RouteAnchor(id = "ofuyu_cape_observatory", rank = 1, name = "雄冬岬展望台", municipality = "増毛町", lon = 141.3450, lat = 43.7407, x = 225.13f, y = 171.93f, description = "かつて陸の孤島と呼ばれた雄冬岬の断崖絶壁を見渡す展望台。"),
        RouteAnchor(id = "iwao_onsen", rank = 2, name = "岩尾温泉", municipality = "増毛町", lon = 141.3687, lat = 43.7741, x = 226.65f, y = 168.98f, description = "雄冬と増毛の中間に湧く秘湯。日本海と夕日を望む温泉宿。"),
        RouteAnchor(id = "kunimare_brewery", rank = 1, name = "国稀酒造", municipality = "増毛町", lon = 141.5246, lat = 43.8579, x = 236.60f, y = 161.56f, description = "明治35年創業、暑寒別岳の伏流水で仕込む日本最北の造り酒屋。"),
        RouteAnchor(id = "reuke_pasture", rank = 2, name = "礼受牧場", municipality = "留萌市", lon = 141.6237, lat = 43.9173, x = 242.93f, y = 156.30f, description = "日本海と増毛連峰を見下ろす丘の上の風力発電パノラマ牧場。"),
        RouteAnchor(id = "ogon_cape_rumoi", rank = 1, name = "留萌 黄金岬", municipality = "留萌市", lon = 141.6296, lat = 43.9461, x = 243.30f, y = 153.75f, description = "夕日に染まる日本海が美しい、かつて鰊の見張り台だった名勝。"),
        RouteAnchor(id = "usutani_beach", rank = 2, name = "臼谷海水浴場", municipality = "小平町", lon = 141.6539, lat = 43.9988, x = 244.86f, y = 149.07f, description = "小平町南部の遠浅で穏やかな海水浴場。オロロンラインのオアシス。"),
        RouteAnchor(id = "obira_michinoeki", rank = 1, name = "おびら鰊番屋", municipality = "小平町", lon = 141.6766, lat = 44.1380, x = 246.30f, y = 136.71f, description = "国指定重要文化財「旧花田家番屋」が隣接する鰊漁の歴史を伝える道の駅。"),
        RouteAnchor(id = "kamihira_wind_farm", rank = 2, name = "上平グリーンヒル", municipality = "苫前町", lon = 141.6774, lat = 44.2574, x = 246.36f, y = 126.07f, description = "オロロンライン沿いの丘陵に39基の巨大風車が林立する壮大な牧場。"),
        RouteAnchor(id = "kotanbetsu_midorigaoka_park", rank = 2, name = "古丹別緑ヶ丘公園", municipality = "苫前町", lon = 141.7151, lat = 44.2595, x = 248.76f, y = 125.89f, description = "約1000本の桜が咲き誇る古丹別地区の高台公園。"),
        RouteAnchor(id = "sankebetsu_bear_incident_site", rank = 1, name = "三毛別羆事件跡", municipality = "苫前町", lon = 141.7963, lat = 44.1389, x = 253.95f, y = 136.63f, description = "大正4年に発生した日本最大の獣害事件を伝える復元現地。"),
        RouteAnchor(id = "tomamae_michinoeki", rank = 1, name = "風Wとままえ", municipality = "苫前町", lon = 141.6562, lat = 44.3128, x = 245.00f, y = 121.13f, description = "巨大風車と日本海、天売・焼尻島を望む温泉付き道の駅。"),
        RouteAnchor(id = "haboro_port", rank = 1, name = "羽幌港", municipality = "羽幌町", lon = 141.6967, lat = 44.3608, x = 247.59f, y = 116.85f, description = "日本海の離島・天売島と焼尻島へのフェリーが発着する港。"),
        RouteAnchor(id = "chikubetsu_station_ruins", rank = 2, name = "旧築別駅跡", municipality = "羽幌町", lon = 141.7383, lat = 44.4124, x = 250.24f, y = 112.24f, description = "かつて羽幌炭鉱鉄道が分岐した歴史を伝える駅跡。"),
        RouteAnchor(id = "konpira_cape", rank = 1, name = "金比羅岬", municipality = "初山別村", lon = 141.7778, lat = 44.5614, x = 252.77f, y = 98.90f, description = "みさき台公園の岬突端、日本海の波打ち際に立つ海中鳥居が有名。"),
        RouteAnchor(id = "kanaura_primeval_garden", rank = 2, name = "金浦原生花園", municipality = "遠別町", lon = 141.7932, lat = 44.6796, x = 253.75f, y = 88.30f, description = "初夏に黄色いエゾカンゾウが咲き誇り利尻富士を望む海辺の原生花園。"),
        RouteAnchor(id = "embetsu_michinoeki", rank = 1, name = "道の駅えんべつ富士見", municipality = "遠別町", lon = 141.7850, lat = 44.7170, x = 253.23f, y = 84.94f, description = "富士見ヶ丘公園の高台に位置し、利尻富士を一望できる道の駅。"),
        RouteAnchor(id = "kaginuma_park", rank = 2, name = "鏡沼海浜公園", municipality = "天塩町", lon = 141.7463, lat = 44.8745, x = 250.75f, y = 70.77f, description = "鏡沼のほとりに広がる原生花園とキャンプ場を備えた海浜公園。"),
        RouteAnchor(id = "teshio_michinoeki", rank = 1, name = "道の駅てしお", municipality = "天塩町", lon = 141.7453, lat = 44.8882, x = 250.69f, y = 69.54f, description = "天塩町の中心市街地にあり、特産しじみを使った料理が人気の道の駅。"),
        RouteAnchor(id = "kawaguchi_ruins_scenic_forest", rank = 3, name = "川口遺跡風景林", municipality = "天塩町", lon = 141.7394, lat = 44.8966, x = 250.31f, y = 68.78f, description = "川口遺跡風景林（天塩町）"),
        RouteAnchor(id = "teshio_river_mouth", rank = 2, name = "天塩川河口", municipality = "天塩町", lon = 141.7360, lat = 44.9235, x = 250.10f, y = 66.36f, description = "日本海に注ぐ北海道第二の大河・天塩川の河口。オロロンライン天塩河口大橋が架かる絶景。"),
        RouteAnchor(id = "otonrui_wind_farm", rank = 2, name = "オトンルイ風力発電所", municipality = "幌延町", lon = 141.6996, lat = 44.9850, x = 247.77f, y = 60.81f, description = "直線3.1kmにわたり28基の巨大風車が一列に並ぶ屈指の絶景。"),
        RouteAnchor(id = "latitude_45_monument", rank = 1, name = "北緯45度モニュメント", municipality = "幌延町", lon = 141.6813, lat = 45.0023, x = 246.60f, y = 59.25f, description = "オロロンライン沿いに立つ、地球の北極点と赤道のちょうど中間を示す記念碑。"),
        RouteAnchor(id = "sarobetsu_plain", rank = 1, name = "サロベツ原野", municipality = "豊富町", lon = 141.6700, lat = 45.1200, x = 245.88f, y = 48.61f, description = "見渡す限りの大湿原が広がる利尻礼文サロベツ国立公園の原生花園。"),
        RouteAnchor(id = "kohone_house", rank = 2, name = "こうほねの家", municipality = "稚内市", lon = 141.6050, lat = 45.2659, x = 241.73f, y = 35.39f, description = "サロベツ海岸沿いの花園休憩施設。利尻富士と日本海の展望地。"),
        RouteAnchor(id = "bakkai_port", rank = 1, name = "抜海港", municipality = "稚内市", lon = 141.6211, lat = 45.3107, x = 242.76f, y = 31.33f, description = "冬になると野生のゴマフアザラシが越冬のために群れ集まる日本最北の漁港。"),
        RouteAnchor(id = "wakkanai_onsen_dome", rank = 2, name = "稚内温泉 童夢", municipality = "稚内市", lon = 141.6375, lat = 45.4212, x = 243.81f, y = 21.29f, description = "利尻富士と夕日を望む日本最北の天然温泉施設。"),
        RouteAnchor(id = "noshappu_cape", rank = 1, name = "ノシャップ岬", municipality = "稚内市", lon = 141.6447, lat = 45.4490, x = 244.27f, y = 18.76f, description = "宗谷海峡を隔てて利尻富士と礼文島を望む野寒布岬。"),
        RouteAnchor(id = "wakkanai_century_tower", rank = 2, name = "開基百年記念塔", municipality = "稚内市", lon = 141.6606, lat = 45.4194, x = 245.28f, y = 21.45f, description = "稚内公園の高台にそびえ立ち、サハリンまで見渡すシンボルタワー。"),
        RouteAnchor(id = "wakkanai_station", rank = 1, name = "稚内駅", municipality = "稚内市", lon = 141.6770, lat = 45.4168, x = 246.33f, y = 21.69f, description = "JR日本最北端の駅。駅構内には最北端の線路終端標が残る。"),
        RouteAnchor(id = "wakkanai_onuma", rank = 2, name = "大沼白鳥飛来地", municipality = "稚内市", lon = 141.7671, lat = 45.3903, x = 252.08f, y = 24.10f, description = "春と秋に数万羽のオオハクチョウが飛来する稚内東郊の汽水湖。"),
        RouteAnchor(id = "soya_hills_white_path", rank = 1, name = "白い道", municipality = "稚内市", lon = 141.8967, lat = 45.4909, x = 260.36f, y = 14.94f, description = "宗谷丘陵に敷き詰められたホタテの貝殻が白く輝く絶景の小道。"),
        RouteAnchor(id = "soya_hill_wind_farm", rank = 2, name = "宗谷丘陵展望地", municipality = "稚内市", lon = 141.9344, lat = 45.5097, x = 262.76f, y = 13.23f, description = "周氷河地形のなだらかな丘陵に57基の風車が立ち並ぶ絶景スポット。"),
        RouteAnchor(id = "soya_cape_monument", rank = 1, name = "宗谷岬", municipality = "稚内市", lon = 141.9366, lat = 45.5228, x = 262.90f, y = 12.04f, description = "日本最北端の地の碑が立つ、日本の本土最北端の地。"),
        RouteAnchor(id = "michinoeki_sarufutsu_park", rank = 2, name = "道の駅さるふつ公園", municipality = "猿払村", lon = 142.1609, lat = 45.3365, x = 277.22f, y = 28.98f, description = "ホタテ水揚げ日本一を誇る猿払村の広大な道の駅。"),
        RouteAnchor(id = "esanuka_line", rank = 1, name = "エサヌカ線", municipality = "猿払村", lon = 142.2480, lat = 45.2450, x = 282.78f, y = 37.29f, description = "電柱もガードレールもない地平線へ続くストレートロード。"),
        RouteAnchor(id = "beniya_primeval_garden", rank = 2, name = "ベニヤ原生花園", municipality = "浜頓別町", lon = 142.3695, lat = 45.1471, x = 290.54f, y = 46.16f, description = "オホーツク海岸と湿原に約100種の花が咲く北限の原生花園。"),
        RouteAnchor(id = "kucharo_lake", rank = 1, name = "クッチャロ湖", municipality = "浜頓別町", lon = 142.3493, lat = 45.1271, x = 289.25f, y = 47.97f, description = "オオハクチョウが数千羽飛来するラムサール条約湿地。"),
        RouteAnchor(id = "kitami_kamui_cape_park", rank = 1, name = "北見神威岬", municipality = "枝幸町", lon = 142.5013, lat = 45.0589, x = 298.96f, y = 54.13f, description = "オホーツク海に鋭く落ち込む断崖。公園展望台から海を一望。"),
        RouteAnchor(id = "mikasayama_observatory", rank = 2, name = "三笠山展望閣", municipality = "枝幸町", lon = 142.5637, lat = 44.9380, x = 302.94f, y = 65.05f, description = "枝幸市街とオホーツク海を360度見渡す標高172mの展望台。"),
        RouteAnchor(id = "usutaibe_cape", rank = 1, name = "ウスタイベ千畳岩", municipality = "枝幸町", lon = 142.5823, lat = 44.9589, x = 304.13f, y = 63.16f, description = "畳を敷き詰めたような柱状節理の岩が広がるウスタイベ岬の名勝。"),
        RouteAnchor(id = "michinoeki_marine_island", rank = 2, name = "道の駅マリーンアイランド岡島", municipality = "枝幸町", lon = 142.6313, lat = 44.8782, x = 307.25f, y = 70.44f, description = "オホーツク海に浮かぶ豪華客船を模したユニークな道の駅。"),
        RouteAnchor(id = "oumu_michinoeki", rank = 1, name = "道の駅おうむ", municipality = "雄武町", lon = 142.9596, lat = 44.5831, x = 328.21f, y = 96.96f, description = "旧国鉄興浜南線雄武駅跡地に立つ道の駅。オホーツク海を望む展望塔がある。"),
        RouteAnchor(id = "hinode_cape", rank = 1, name = "日の出岬", municipality = "雄武町", lon = 143.0550, lat = 44.5280, x = 334.30f, y = 101.90f, description = "オホーツク海の水平線と総ガラス張りの展望台ラ・ルーナが名所。"),
        RouteAnchor(id = "okoppe_michinoeki", rank = 1, name = "道の駅おこっぺ", municipality = "興部町", lon = 143.1173, lat = 44.4694, x = 338.28f, y = 107.14f, description = "名峰・名産品と乳製品が人気の旧興部駅跡地の道の駅。"),
        RouteAnchor(id = "saruru_cape", rank = 2, name = "沙留岬", municipality = "興部町", lon = 143.2200, lat = 44.4350, x = 344.84f, y = 110.22f, description = "沙留漁港に隣接し、オホーツク海に突き出す穏やかな岬。"),
        RouteAnchor(id = "mombetsu_port_garinko", rank = 1, name = "ガリンコ号のりば", municipality = "紋別市", lon = 143.3630, lat = 44.3360, x = 353.97f, y = 119.06f, description = "流氷砕氷船ガリンコ号が発着する紋別海洋交流館。"),
        RouteAnchor(id = "crab_claw_sculpture", rank = 1, name = "カニの爪オブジェ", municipality = "紋別市", lon = 143.3774, lat = 44.3277, x = 354.89f, y = 119.80f, description = "高さ12m・幅6mを誇る紋別のシンボル的巨大モニュメント。"),
        RouteAnchor(id = "ryuhyo_park", rank = 2, name = "オホーツク流氷公園", municipality = "紋別市", lon = 143.3997, lat = 44.3139, x = 356.31f, y = 121.03f, description = "オホーツク海に面し、広大なラベンダー畑や交流館を備える道立公園。"),
        RouteAnchor(id = "kamiyubetsu_tulip_park", rank = 2, name = "チューリップ公園", municipality = "湧別町", lon = 143.5805, lat = 44.1586, x = 367.85f, y = 134.87f, description = "約200品種120万本のチューリップが咲き誇る湧別町屈指の名所。"),
        RouteAnchor(id = "yubetsu_michinoeki", rank = 1, name = "愛ランド湧別", municipality = "湧別町", lon = 143.7189, lat = 44.1311, x = 376.69f, y = 137.32f, description = "サロマ湖を望む遊園地ファミリー愛ランドが併設された道の駅。"),
        RouteAnchor(id = "kerochi_transport_park", rank = 2, name = "計呂地交通公園", municipality = "湧別町", lon = 143.7285, lat = 44.1039, x = 377.30f, y = 139.74f, description = "旧湧網線計呂地駅跡に蒸気機関車C58や客車が保存された歴史公園。"),
        RouteAnchor(id = "piraoro_observatory", rank = 1, name = "ピラオロ展望台", municipality = "佐呂間町", lon = 143.8027, lat = 44.0958, x = 382.04f, y = 140.46f, description = "サロマ湖を一望するアイヌ伝説の残る公設木造展望台。"),
        RouteAnchor(id = "saroma_lake_observatory", rank = 2, name = "サロマ湖展望台", municipality = "佐呂間町", lon = 143.8361, lat = 44.0974, x = 384.17f, y = 140.32f, description = "標高376mの幌岩山山頂からサロマ湖全体とオホーツク海を一望する絶景地。"),
        RouteAnchor(id = "kimuaneppu_cape", rank = 1, name = "キムアネップ岬", municipality = "佐呂間町", lon = 143.9069, lat = 44.1074, x = 388.69f, y = 139.43f, description = "サロマ湖に細長く突き出た岬。夕日とサンゴ草（アッケシソウ）の群生地。"),
        RouteAnchor(id = "wakka_primeval_garden", rank = 2, name = "ワッカ原生花園", municipality = "北見市", lon = 143.9633, lat = 44.1378, x = 392.29f, y = 136.72f, description = "サロマ湖とオホーツク海を分ける砂嘴に広がる国内最大の海岸草原湿地。"),
        RouteAnchor(id = "tokoro_curling_hall", rank = 1, name = "常呂カーリング", municipality = "北見市", lon = 144.0564, lat = 44.1215, x = 398.24f, y = 138.17f, description = "日本を代表するトップ選手を輩出した国内最大級のカーリング専用ホール。"),
        RouteAnchor(id = "notoro_lake_sangoso", rank = 2, name = "能取湖サンゴ草群落", municipality = "網走市", lon = 144.0884, lat = 44.0007, x = 400.28f, y = 148.90f, description = "秋になると湖畔が真っ赤な絨毯のように染まる日本一の群生地。"),
        RouteAnchor(id = "notoro_cape", rank = 1, name = "能取岬", municipality = "網走市", lon = 144.2432, lat = 44.1122, x = 410.16f, y = 139.00f, description = "オホーツク海に突き出す断崖と白黒ツートンの能取岬灯台。"),
        RouteAnchor(id = "futatsuiwa", rank = 2, name = "二ツ岩", municipality = "網走市", lon = 144.2509, lat = 44.0576, x = 410.65f, y = 143.85f, description = "網走の海岸沿いに並び立つ二つの巨大な奇岩（カモメの繁殖地）。"),
        RouteAnchor(id = "abashiri_michinoeki", rank = 1, name = "流氷街道網走", municipality = "網走市", lon = 144.2743, lat = 44.0219, x = 412.15f, y = 147.02f, description = "網走港に面し、流氷観光砕氷船おーろら号が発着する道の駅。"),
        RouteAnchor(id = "jr_kitahama_station", rank = 2, name = "JR北浜駅", municipality = "網走市", lon = 144.3527, lat = 43.9585, x = 417.15f, y = 152.65f, description = "オホーツク海に一番近い駅として有名な木造駅舎（流氷展望デッキ）。"),
        RouteAnchor(id = "koshimizu_primeval_garden", rank = 1, name = "小清水原生花園", municipality = "小清水町", lon = 144.4134, lat = 43.9419, x = 421.03f, y = 154.12f, description = "オホーツク海と濤沸湖に挟まれた砂丘に色とりどりの野花が咲き誇る名所。"),
        RouteAnchor(id = "michinoeki_shari", rank = 2, name = "道の駅しゃり", municipality = "斜里町", lon = 144.6643, lat = 43.9114, x = 437.05f, y = 156.82f, description = "世界自然遺産・知床の玄関口、知床斜里駅近くの道の駅。"),
        RouteAnchor(id = "road_to_heaven", rank = 1, name = "天に続く道", municipality = "斜里町", lon = 144.7987, lat = 43.9068, x = 445.63f, y = 157.23f, description = "約28kmにわたり直線道路が遥か彼方の天へと続くかのように見える絶景ロード。"),
        RouteAnchor(id = "oshinkoshin_waterfall", rank = 2, name = "オシンコシンの滝", municipality = "斜里町", lon = 144.9354, lat = 44.0381, x = 454.35f, y = 145.58f, description = "知床最大の瀑布で日本の滝百選。流れが二股に分かれる名瀑。"),
        RouteAnchor(id = "utoro_port", rank = 1, name = "ウトロ港", municipality = "斜里町", lon = 144.9926, lat = 44.0735, x = 458.00f, y = 142.44f, description = "知床観光船が発着する世界自然遺産・知床半島の西海岸の拠点港。"),
        RouteAnchor(id = "furepe_waterfall", rank = 2, name = "フレペの滝", municipality = "斜里町", lon = 145.0130, lat = 44.0960, x = 459.31f, y = 140.44f, description = "フレペの滝（斜里町）"),
        RouteAnchor(id = "shiretoko_five_lakes", rank = 1, name = "知床五湖", municipality = "斜里町", lon = 145.0806, lat = 44.1222, x = 463.62f, y = 138.11f, description = "知床連山と原生林に囲まれた幻想的な5つの湖。高架木道と地上遊歩道。"),
        RouteAnchor(id = "shiretoko_nature_center", rank = 2, name = "知床自然センター", municipality = "斜里町", lon = 145.0230, lat = 44.0915, x = 459.95f, y = 140.84f, description = "知床国立公園の情報発信拠点。大型スクリーンシアターやフレペの滝遊歩道入口を備える。"),
        RouteAnchor(id = "shiretoko_pass", rank = 1, name = "知床峠", municipality = "羅臼町", lon = 145.1100, lat = 44.0500, x = 465.50f, y = 144.53f, description = "標高738mから羅臼岳と北方領土・国後島を一望する知床横断道路の頂上。"),
        RouteAnchor(id = "kuma_no_yu", rank = 2, name = "熊の湯", municipality = "羅臼町", lon = 145.1552, lat = 44.0321, x = 468.39f, y = 146.12f, description = "羅臼川沿いの原生林に囲まれた歴史ある秘湯露天風呂。"),
        RouteAnchor(id = "rausu_michinoeki", rank = 1, name = "道の駅知床らうす", municipality = "羅臼町", lon = 145.1920, lat = 44.0172, x = 470.73f, y = 147.44f, description = "羅臼港の目の前にあり、羅臼昆布や新鮮な海産物が並ぶ知床東海岸の拠点。"),
        RouteAnchor(id = "rusa_field_house", rank = 2, name = "ルサフィールドハウス", municipality = "羅臼町", lon = 145.2638, lat = 44.1389, x = 475.32f, y = 136.63f, description = "知床岬先端部地区への情報拠点・海鳥観察スポット。"),
        RouteAnchor(id = "seseki_onsen", rank = 1, name = "セセキ温泉", municipality = "羅臼町", lon = 145.3082, lat = 44.1755, x = 478.15f, y = 133.37f, description = "ドラマ『北の国から 2002 遺言』のロケ地として有名な、干潮時のみ海中から姿を現す知床東海岸の秘湯。"),
        RouteAnchor(id = "aidomari_bridge", rank = 1, name = "相泊橋", municipality = "羅臼町", lon = 145.3174, lat = 44.1868, x = 478.74f, y = 132.36f, description = "道道87号知床公園羅臼線の車道終点。北海道一周すごろく第1部のゴール。"),
    )

    val totalNodes: Int get() = anchors.size
    val maxStepIndex: Int get() = anchors.size - 1
    val startNode: SugorokuNode get() = routeNodes.first()
    val goalNode: SugorokuNode get() = routeNodes.last()
    val startAnchor: RouteAnchor get() = anchors.first()
    val goalAnchor: RouteAnchor get() = anchors.last()

    val nodes: List<SugorokuNode> by lazy { routeNodes }
    val routeNodes: List<SugorokuNode> by lazy { initialNodes }

    fun getNodeByStepIndex(stepIndex: Int): SugorokuNode? =
        routeNodes.getOrNull(stepIndex)

    fun getNodeById(id: String): SugorokuNode? =
        routeNodes.firstOrNull { it.id == id }

    fun getAnchorById(id: String): RouteAnchor? =
        anchors.firstOrNull { it.id == id }

    fun getNextAnchor(currentId: String): RouteAnchor? {
        val index = anchors.indexOfFirst { it.id == currentId }
        return if (index != -1 && index + 1 < anchors.size) anchors[index + 1] else null
    }

    fun getPrevAnchor(currentId: String): RouteAnchor? {
        val index = anchors.indexOfFirst { it.id == currentId }
        return if (index > 0) anchors[index - 1] else null
    }

    val initialNodes: List<SugorokuNode> by lazy {
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
                    anchor.name.contains("河口") || anchor.name.contains("洞窟") || anchor.name.contains("滝") ||
                    anchor.name.contains("風力") || anchor.name.contains("牧場") || anchor.name.contains("林") ||
                    anchor.name.contains("水族館") || anchor.name.contains("マリーナ") || anchor.name.contains("関所") -> SugorokuNodeType.SCENIC_SPOT
                anchor.name.contains("駅") || anchor.name.contains("通") || anchor.name.contains("町") ||
                    anchor.name.contains("蒸溜所") || anchor.name.contains("酒造") || anchor.name.contains("ホール") ||
                    anchor.name.contains("カーリング") || anchor.name.contains("のりば") || anchor.name.contains("ハウス") ||
                    anchor.name.contains("港") -> SugorokuNodeType.TOWN_CENTER
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
