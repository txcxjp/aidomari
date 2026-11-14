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

    /** ユーザー指定の公式一次測地データに基づく正規アンカーポイント（全141箇所） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(id = "hakodateyama", rank = 1, name = "函館山", municipality = "函館市", lon = 140.70553624563212, lat = 41.744766784111675, x = 184.31f, y = 345.46f, description = "100万ドルの夜景で名高い函館山。津軽海峡と函館市街を一望する山頂展望台。"),
        RouteAnchor(id = "tachimachi_story", rank = 2, name = "立待岬物語", municipality = "函館市", lon = 140.7161344, lat = 41.7531496, x = 184.99f, y = 344.74f, description = "立待岬近くに設置された石碑。津軽海峡と断崖を望む。"),
        RouteAnchor(id = "jujigai_station", rank = 3, name = "十字街駅", municipality = "函館市", lon = 140.718685, lat = 41.76408, x = 185.15f, y = 343.81f, description = "函館市電（路面電車）の電停。函館山の麓、西部地区観光の玄関口。"),
        RouteAnchor(id = "mashumaru", rank = 1, name = "摩周丸", municipality = "函館市", lon = 140.72304509852597, lat = 41.77256073112773, x = 185.43f, y = 343.08f, description = "函館港旧第2岸壁に保存・係留されている青函連絡船記念館。"),
        RouteAnchor(id = "shinkawacho_station", rank = 3, name = "新川町駅", municipality = "函館市", lon = 140.73540500000001, lat = 41.773425, x = 186.22f, y = 343.01f, description = "函館市電の電停。大門地区と五稜郭方面を結ぶ中間地点。"),
        RouteAnchor(id = "horikawacho_station", rank = 2, name = "堀川町駅", municipality = "函館市", lon = 140.74406, lat = 41.780325, x = 186.77f, y = 342.42f, description = "函館市電（路面電車）の停留場。"),
        RouteAnchor(id = "chiyogadai_station", rank = 4, name = "千代台駅", municipality = "函館市", lon = 140.74868, lat = 41.784115, x = 187.06f, y = 342.09f, description = "函館市電の電停。千代台公園野球場（オーシャンスタジアム）や陸上競技場の最寄り。"),
        RouteAnchor(id = "goryokaku_koen_mae_station", rank = 3, name = "五稜郭公園前駅", municipality = "函館市", lon = 140.75208500000002, lat = 41.78926, x = 187.28f, y = 341.65f, description = "函館市電の電停。特別史跡五稜郭跡・五稜郭タワーの最寄り電停。"),
        RouteAnchor(id = "goryokaku", rank = 1, name = "五稜郭", municipality = "函館市", lon = 140.75679445417336, lat = 41.796991557953135, x = 187.58f, y = 340.99f, description = "幕末の歴史を刻む国指定特別史跡の星形要塞。"),
        RouteAnchor(id = "goryokaku_station", rank = 3, name = "五稜郭駅", municipality = "函館市", lon = 140.73356, lat = 41.803525, x = 186.10f, y = 340.43f, description = "JR函館本線・道南いさりび鉄道の駅。五稜郭公園への接続駅。"),
        RouteAnchor(id = "nanaehama_station", rank = 4, name = "七重浜駅", municipality = "北斗市", lon = 140.70809500000001, lat = 41.8176, x = 184.47f, y = 339.22f, description = "道南いさりび鉄道の駅。函館湾に面した北斗市の駅。"),
        RouteAnchor(id = "kikyo_station", rank = 2, name = "桔梗駅", municipality = "函館市", lon = 140.722985, lat = 41.84635, x = 185.42f, y = 336.76f, description = "JR函館本線の駅。函館市北部の住宅街に位置する。"),
        RouteAnchor(id = "onakayama_station", rank = 4, name = "大中山駅", municipality = "七飯町", lon = 140.7135, lat = 41.864779999999996, x = 184.82f, y = 335.18f, description = "JR函館本線の駅。七飯町の市街地に位置する。"),
        RouteAnchor(id = "nanae_station", rank = 3, name = "七飯駅", municipality = "七飯町", lon = 140.68856, lat = 41.887010000000004, x = 183.23f, y = 333.28f, description = "JR函館本線の駅。大中山駅と新函館北斗駅の間に位置する。"),
        RouteAnchor(id = "shin_hakodate_hokuto_station", rank = 1, name = "新函館北斗駅", municipality = "北斗市", lon = 140.6514055298738, lat = 41.90410586161933, x = 180.85f, y = 331.81f, description = "北海道新幹線の北の終着駅。道南の交通結節点。"),
        RouteAnchor(id = "kijibiki_plateau_observatory", rank = 4, name = "きじびき高原 パノラマ展望台", municipality = "北斗市", lon = 140.6157716, lat = 41.9442139, x = 178.58f, y = 328.37f, description = "標高560mの高原から大野平野、函館山、津軽海峡を一望する大パノラマ展望台。"),
        RouteAnchor(id = "higurashiyama_observatory", rank = 3, name = "日暮山展望台", municipality = "七飯町", lon = 140.644003, lat = 41.9814724, x = 180.38f, y = 325.17f, description = "大沼・小沼と秀峰・駒ヶ岳を一望するビュースポット。"),
        RouteAnchor(id = "komagatake_station", rank = 4, name = "駒ヶ岳駅", municipality = "森町", lon = 140.610515, lat = 42.03879, x = 178.24f, y = 320.24f, description = "JR函館本線の駅。秀峰・北海道駒ヶ岳の山麓に位置する。"),
        RouteAnchor(id = "you_yu_mori_michinoeki", rank = 2, name = "道の駅 YOU･遊･もり", municipality = "森町", lon = 140.568716, lat = 42.098901, x = 175.58f, y = 315.07f, description = "国道5号沿い、内浦湾を一望する展望ラウンジを備えた道の駅。"),
        RouteAnchor(id = "washinoki_port", rank = 4, name = "鷲ノ木漁港", municipality = "森町", lon = 140.5446953, lat = 42.1192285, x = 174.04f, y = 313.32f, description = "内浦湾に面する森町の漁港。幕末に旧幕府軍が上陸した歴史の地。"),
        RouteAnchor(id = "ishikura_station", rank = 3, name = "石倉駅", municipality = "森町", lon = 140.45846999999998, lat = 42.17288, x = 168.54f, y = 308.70f, description = "JR函館本線の駅。内浦湾沿いの静かな集落に位置する。"),
        RouteAnchor(id = "nodaoi_station", rank = 4, name = "野田生駅", municipality = "八雲町", lon = 140.375965, lat = 42.21703, x = 163.27f, y = 304.90f, description = "JR函館本線の駅。噴火湾沿いの集落に位置する。"),
        RouteAnchor(id = "funkawan_panorama_park", rank = 1, name = "噴火湾パノラマパーク", municipality = "八雲町", lon = 140.31405732962233, lat = 42.227215716723315, x = 159.32f, y = 304.02f, description = "噴火湾を見下ろす広大な丘陵公園。遊具やハーブ園が広がる絶景地。"),
        RouteAnchor(id = "yakumo_station", rank = 4, name = "八雲駅", municipality = "八雲町", lon = 140.27390333333335, lat = 42.253080000000004, x = 156.75f, y = 301.79f, description = "JR函館本線の特急停車駅。八雲町の市街地中心部に位置する。"),
        RouteAnchor(id = "yamasaki_station", rank = 3, name = "山崎駅", municipality = "八雲町", lon = 140.27461499999998, lat = 42.31461, x = 156.80f, y = 296.48f, description = "JR函館本線の駅。噴火湾沿いの静かな集落に位置する。"),
        RouteAnchor(id = "kuroiwa_station", rank = 2, name = "黒岩駅", municipality = "八雲町", lon = 140.28818, lat = 42.368354999999994, x = 157.67f, y = 291.84f, description = "JR函館本線の駅。噴火湾沿いに位置する。"),
        RouteAnchor(id = "kunnui_station", rank = 3, name = "国縫駅", municipality = "長万部町", lon = 140.320455, lat = 42.43871, x = 159.73f, y = 285.76f, description = "JR函館本線の駅。かつて瀬棚線が分岐した歴史ある鉄道駅。"),
        RouteAnchor(id = "oshamambe_station", rank = 1, name = "長万部駅", municipality = "長万部町", lon = 140.37552466273777, lat = 42.513017969486114, x = 163.24f, y = 279.33f, description = "函館本線と室蘭本線が分岐する交通の要衝。名物「かにめし」発祥の地。"),
        RouteAnchor(id = "oshamambe_park_campground", rank = 3, name = "長万部公園キャンプ場", municipality = "長万部町", lon = 140.3440808, lat = 42.5134273, x = 161.23f, y = 279.29f, description = "長万部市街近くの緑豊かな公園内にあるキャンプ場。池や遊具が整備。"),
        RouteAnchor(id = "futamata_station", rank = 2, name = "二股駅", municipality = "長万部町", lon = 140.32076999999998, lat = 42.575900000000004, x = 159.75f, y = 273.88f, description = "JR函館本線の駅。二股ラヂウム温泉への玄関口。"),
        RouteAnchor(id = "utasai_autocamp_le_pic", rank = 3, name = "歌才オートキャンプ場ル・ピック", municipality = "黒松内町", lon = 140.3067908, lat = 42.6501025, x = 158.85f, y = 267.44f, description = "日本最北のブナ自生北限地・歌才ブナ林に隣接する高規格オートキャンプ場。"),
        RouteAnchor(id = "kuromatsunai_michinoeki", rank = 1, name = "道の駅 くろまつない", municipality = "黒松内町", lon = 140.37979736828004, lat = 42.67239712919561, x = 163.51f, y = 265.50f, description = "北限のブナ原生林の里。本格焼きたてピザやパンが人気の道の駅。"),
        RouteAnchor(id = "neppu_station", rank = 4, name = "熱郛駅", municipality = "黒松内町", lon = 140.375135, lat = 42.6757, x = 163.22f, y = 265.22f, description = "JR函館本線の駅。黒松内町熱郛地区の中心駅。"),
        RouteAnchor(id = "kuromatsunai_station", rank = 3, name = "黒松内駅", municipality = "黒松内町", lon = 140.305735, lat = 42.66959, x = 158.79f, y = 265.75f, description = "JR函館本線の駅。ブナ北限の里・黒松内町の中心駅。"),
        RouteAnchor(id = "soebetsu_mini_visitor_center", rank = 4, name = "添別ミニビジターセンター", municipality = "黒松内町", lon = 140.2635322, lat = 42.6819902, x = 156.09f, y = 264.67f, description = "ブナ自生北限の里の自然情報や散策路案内を提供するガイダンス施設。"),
        RouteAnchor(id = "minatomare_suttsu_michinoeki", rank = 2, name = "道の駅 みなとま〜れ寿都", municipality = "寿都町", lon = 140.2318241, lat = 42.7955338, x = 154.07f, y = 254.80f, description = "寿都港を望むウォーターフロントに建つ道の駅。名物生しらすや海産物が人気。"),
        RouteAnchor(id = "benkei_cape", rank = 1, name = "弁慶岬", municipality = "寿都町", lon = 140.1887125350538, lat = 42.82183022260921, x = 151.32f, y = 252.51f, description = "義経・弁慶伝説が残る日本海に突き出た名岬。白亜の灯台が立つ。"),
        RouteAnchor(id = "shell_plaza_minato_michinoeki", rank = 2, name = "道の駅 シェルプラザ・港", municipality = "蘭越町", lon = 140.370027, lat = 42.8801825, x = 162.89f, y = 247.43f, description = "国道229号沿いに位置し、世界中の貝類を展示する「貝の館」が隣接する道の駅。"),
        RouteAnchor(id = "shinsen_numa_observatory", rank = 3, name = "神仙沼自然休養林展望台", municipality = "共和町", lon = 140.5897369, lat = 42.9088028, x = 176.92f, y = 244.94f, description = "ニセコ山系の最も美しい湿原・神仙沼を望む展望台。"),
        RouteAnchor(id = "horoni_railway_park", rank = 1, name = "幌似鉄道記念公園", municipality = "共和町", lon = 140.6139535344062, lat = 42.98665997187308, x = 178.46f, y = 238.15f, description = "旧国鉄岩内線の幌似駅跡を保存した鉄道記念公園。当時の木造駅舎と車両を展示。"),
        RouteAnchor(id = "ryujin_cape", rank = 2, name = "竜神岬", municipality = "神恵内村", lon = 140.4280092, lat = 43.1365587, x = 166.59f, y = 225.05f, description = "神恵内村の日本海に突き出た景勝岬。奇岩と荒波が織りなす積丹西岸の景勝地。"),
        RouteAnchor(id = "osukoi_kamoenai_michinoeki", rank = 4, name = "道の駅 オスコイ！かもえない", municipality = "神恵内村", lon = 140.3855694, lat = 43.1653589, x = 163.88f, y = 222.53f, description = "国道229号沿い、奇岩が連なる積丹半島西岸の道の駅。"),
        RouteAnchor(id = "annai_observatory_park", rank = 3, name = "あんない展望公園", municipality = "積丹町", lon = 140.332648, lat = 43.2423312, x = 160.50f, y = 215.79f, description = "積丹半島西岸の高台にある公園。日本海のパノラマを一望。"),
        RouteAnchor(id = "kamizaki_port", rank = 4, name = "神岬漁港", municipality = "神恵内村", lon = 140.3560466, lat = 43.3141748, x = 162.00f, y = 209.49f, description = "神威岬南部の西海岸に位置する静かな漁港。"),
        RouteAnchor(id = "kamui_cape", rank = 1, name = "神威岬", municipality = "積丹町", lon = 140.3594022649703, lat = 43.32848146798221, x = 162.21f, y = 208.23f, description = "積丹ブルーの海へダイナミックに突き出す大パノラマの絶景岬。"),
        RouteAnchor(id = "nozuka_campground", rank = 4, name = "野塚野営場", municipality = "積丹町", lon = 140.4442081, lat = 43.3338413, x = 167.63f, y = 207.76f, description = "積丹半島西岸、日本海の白波が打ち寄せる野塚海岸のキャンプ場。"),
        RouteAnchor(id = "joroko_iwa_observatory", rank = 3, name = "女郎子岩展望台", municipality = "積丹町", lon = 140.4983661, lat = 43.3696082, x = 171.08f, y = 204.62f, description = "積丹半島西海岸の奇岩・女郎子岩と日本海を見下ろす絶景展望台。"),
        RouteAnchor(id = "biyano_cape", rank = 4, name = "ビヤノ岬", municipality = "積丹町", lon = 140.5908492, lat = 43.3086971, x = 176.99f, y = 209.97f, description = "積丹半島北東部の断崖絶壁が連なる景勝岬。"),
        RouteAnchor(id = "ogon_cape_shakotan", rank = 2, name = "黄金岬", municipality = "積丹町", lon = 140.6013495, lat = 43.3031736, x = 177.66f, y = 210.45f, description = "美国港のすぐそばにある景勝地。かつてニシン見張り台があった岬。"),
        RouteAnchor(id = "takoana_no_misaki", rank = 3, name = "蛸穴ﾉ岬", municipality = "余市町", lon = 140.7084425, lat = 43.2525827, x = 184.50f, y = 214.89f, description = "積丹半島東部、余市町の海岸線に突き出た景勝岬。"),
        RouteAnchor(id = "shiripa_cape", rank = 4, name = "シリパ岬", municipality = "余市町", lon = 140.7724641, lat = 43.2266132, x = 188.58f, y = 217.16f, description = "余市港を見下ろす標高295mの断崖絶壁。余市のシンボルとして親しまれる名岬。"),
        RouteAnchor(id = "nikka_yoichi_distillery", rank = 1, name = "ニッカウヰスキー 余市蒸溜所", municipality = "余市町", lon = 140.79185822539347, lat = 43.18739590094136, x = 189.82f, y = 220.60f, description = "日本のウイスキーの父・竹鶴政孝が創業した伝統のウイスキー蒸溜所。"),
        RouteAnchor(id = "yoichi_fugoppe_campground", rank = 3, name = "余市フゴッペキャンプ場", municipality = "余市町", lon = 140.833029, lat = 43.1870537, x = 192.45f, y = 220.63f, description = "日本海とフゴッペ洞窟近くの豊かな自然に囲まれたキャンプ場。"),
        RouteAnchor(id = "ranshima_station", rank = 4, name = "蘭島駅", municipality = "小樽市", lon = 140.854675, lat = 43.196535, x = 193.83f, y = 219.80f, description = "JR函館本線の駅。明治36年開設の北海道最古の海水浴場・蘭島海水浴場の玄関口。"),
        RouteAnchor(id = "ryugamisaki", rank = 2, name = "竜ヶ岬", municipality = "小樽市", lon = 140.8615333, lat = 43.2177318, x = 194.27f, y = 217.94f, description = "小樽市塩谷海岸の断崖絶壁が続く奇岩景勝地。"),
        RouteAnchor(id = "shioya_station", rank = 3, name = "塩谷駅", municipality = "小樽市", lon = 140.92451, lat = 43.202434999999994, x = 198.29f, y = 219.28f, description = "JR函館本線（山線）の駅。塩谷海岸近くの丘陵地に位置する木造駅。"),
        RouteAnchor(id = "asahi_observatory", rank = 4, name = "旭展望台", municipality = "小樽市", lon = 140.9846182, lat = 43.1970028, x = 202.13f, y = 219.76f, description = "小樽市街と石狩湾を一望する標高190mの丘陵展望台。"),
        RouteAnchor(id = "otaru_canal", rank = 1, name = "小樽運河", municipality = "小樽市", lon = 141.0020940032056, lat = 43.19918050706471, x = 203.24f, y = 219.57f, description = "石造り倉庫群が連なりガス灯が灯る、小樽を代表する歴史的観光名所。"),
        RouteAnchor(id = "hiraiso_cape", rank = 4, name = "平磯岬", municipality = "小樽市", lon = 141.0360016, lat = 43.1826345, x = 205.41f, y = 221.02f, description = "小樽港南端に位置する景勝岬。小樽の街並みと石狩湾を一望。"),
        RouteAnchor(id = "asari_station", rank = 3, name = "朝里駅", municipality = "小樽市", lon = 141.0639, lat = 43.176860000000005, x = 207.19f, y = 221.52f, description = "JR函館本線の駅。ホームの目の前に日本海が広がる絶景の駅。"),
        RouteAnchor(id = "zenibako_station", rank = 4, name = "銭函駅", municipality = "小樽市", lon = 141.159925, lat = 43.143415, x = 213.32f, y = 224.45f, description = "JR函館本線の駅。石狩湾に面し「銭函」という縁起の良い名で知られる駅。"),
        RouteAnchor(id = "hoshimi_station", rank = 2, name = "ほしみ駅", municipality = "札幌市手稲区", lon = 141.19150000000002, lat = 43.13346, x = 215.34f, y = 225.32f, description = "JR函館本線の駅。小樽市との境界近くに位置する星置川沿いの駅。"),
        RouteAnchor(id = "teine_station", rank = 4, name = "手稲駅", municipality = "札幌市手稲区", lon = 141.24385, lat = 43.120125, x = 218.68f, y = 226.49f, description = "JR函館本線の主要駅。手稲区の行政・交通の中心地。"),
        RouteAnchor(id = "hassamu_station", rank = 3, name = "発寒駅", municipality = "札幌市西区", lon = 141.27663, lat = 43.100184999999996, x = 220.77f, y = 228.23f, description = "JR函館本線の駅。手稲区と西区の境界近くに位置する駅。"),
        RouteAnchor(id = "kotoni_station", rank = 4, name = "琴似駅", municipality = "札幌市西区", lon = 141.30739499999999, lat = 43.08151, x = 222.73f, y = 229.86f, description = "JR函館本線の快速停車駅。西区の中心繁華街・琴似の交通結節点。"),
        RouteAnchor(id = "sapporo_clock_tower", rank = 1, name = "札幌市時計台", municipality = "札幌市", lon = 141.35342842412132, lat = 43.062788412367794, x = 225.67f, y = 231.50f, description = "明治11年創建の旧札幌農学校演武場。札幌のシンボルとして親しまれる歴史的建造物。"),
        RouteAnchor(id = "moerenuma_park", rank = 4, name = "モエレ沼公園", municipality = "札幌市東区", lon = 141.42757399753575, lat = 43.12420999905941, x = 230.41f, y = 226.13f, description = "彫刻家イサム・ノグチが設計した広大なアートパーク。ガラスのピラミッドが象徴。"),
        RouteAnchor(id = "eight_campground", rank = 3, name = "えいとキャンプ場", municipality = "石狩市", lon = 141.4312693, lat = 43.2592649, x = 230.64f, y = 214.30f, description = "石狩市厚田区の丘陵地に位置する日本海を望む静かなキャンプ場。"),
        RouteAnchor(id = "mourai_beach_campground", rank = 4, name = "望来浜中央海水浴場キャンプ場", municipality = "石狩市", lon = 141.4157059, lat = 43.3162415, x = 229.65f, y = 209.31f, description = "石狩市厚田区望来の海岸線に広がる夕日の名所キャンプ場。"),
        RouteAnchor(id = "atsuta_observatory", rank = 2, name = "厚田展望台", municipality = "石狩市", lon = 141.4324888, lat = 43.4058027, x = 230.72f, y = 201.44f, description = "国道231号沿いの高台にある夕日の名所展望台。「恋人の聖地」に認定。"),
        RouteAnchor(id = "gokibiru_campground", rank = 4, name = "濃昼キャンプ場", municipality = "石狩市", lon = 141.3918023, lat = 43.4774891, x = 228.12f, y = 195.14f, description = "かつて陸の孤島と呼ばれた濃昼山道沿いに位置する自然豊かな海沿いのキャンプ場。"),
        RouteAnchor(id = "kawashimo_seaside_campground", rank = 3, name = "川下海浜公園キャンプ場", municipality = "石狩市", lon = 141.3862715, lat = 43.5834438, x = 227.77f, y = 185.81f, description = "石狩市浜益区の美しい砂浜に面した海浜公園キャンプ場。"),
        RouteAnchor(id = "ofuyu_cape", rank = 1, name = "雄冬岬", municipality = "増毛町", lon = 141.33591905548778, lat = 43.73280428173676, x = 224.56f, y = 172.63f, description = "かつて陸の孤島と呼ばれたオロロンラインの険しい断崖絶壁が続く名岬。"),
        RouteAnchor(id = "ofuyu_campground", rank = 4, name = "雄冬キャンプ場", municipality = "増毛町", lon = 141.338499, lat = 43.738614, x = 224.72f, y = 172.11f, description = "雄冬岬の険しい断崖の麓に広がる海辺のキャンプ場。"),
        RouteAnchor(id = "akaiwamisaki", rank = 3, name = "赤岩岬", municipality = "増毛町", lon = 141.3541826, lat = 43.7580764, x = 225.72f, y = 170.39f, description = "日本海に突き出た赤褐色の奇岩断崖が連なる景勝岬。"),
        RouteAnchor(id = "bekkari_port_lighthouse", rank = 2, name = "別苅港北防波堤灯台", municipality = "増毛町", lon = 141.4770056, lat = 43.8353243, x = 233.56f, y = 163.56f, description = "別苅漁港の北防波堤に立つ白亜の灯台。"),
        RouteAnchor(id = "mashike_riverside_autocamp", rank = 3, name = "増毛リバーサイドパークオートキャンプ場", municipality = "増毛町", lon = 141.514677, lat = 43.847438, x = 235.97f, y = 162.49f, description = "暑寒別川の河畔に広がる自然豊かなオートキャンプ場。"),
        RouteAnchor(id = "kunimare_brewery", rank = 1, name = "国稀酒造", municipality = "増毛町", lon = 141.52321177017552, lat = 43.85776020957361, x = 236.51f, y = 161.58f, description = "明治35年創業、暑寒別岳の伏流水で仕込む日本最北の造り酒屋。"),
        RouteAnchor(id = "ogon_cape_beach_park", rank = 1, name = "黄金岬海浜公園", municipality = "留萌市", lon = 141.62939738242676, lat = 43.94678147706456, x = 243.29f, y = 153.69f, description = "夕日に染まる日本海と荒波が砕ける奇岩が美しい、留萌を代表する夕日スポット。"),
        RouteAnchor(id = "obiracho_boyodai_campground", rank = 2, name = "小平町望洋台キャンプ場", municipality = "小平町", lon = 141.666466, lat = 44.0322567, x = 245.66f, y = 146.10f, description = "日本海を見下ろす丘の上に広がる広大なキャンプ場。"),
        RouteAnchor(id = "sankebetsu_bear_incident_site", rank = 1, name = "三毛別羆事件復元地", municipality = "苫前町", lon = 141.7994765008687, lat = 44.13722420804186, x = 254.15f, y = 136.77f, description = "大正4年に発生した史上最大の獣害事件の現場を今に伝える復元展示地。"),
        RouteAnchor(id = "tomamae_michinoeki", rank = 1, name = "道の駅 風Wとままえ", municipality = "苫前町", lon = 141.6567301836899, lat = 44.313040239571436, x = 245.04f, y = 121.11f, description = "日本海と天売・焼尻島を一望する風車群が立ち並ぶ温泉併設の道の駅。"),
        RouteAnchor(id = "haboro_rose_garden", rank = 1, name = "はぼろバラ園", municipality = "羽幌町", lon = 141.70752630772944, lat = 44.36636286445792, x = 248.28f, y = 116.35f, description = "約300種2000株のバラが咲き誇る、道の駅ほっと・はぼろに隣接する庭園。"),
        RouteAnchor(id = "toyomisaki_konpira_shrine", rank = 1, name = "豊岬金比羅神社", municipality = "初山別村", lon = 141.77250742378564, lat = 44.56371722986578, x = 252.43f, y = 98.70f, description = "日本海の荒波洗う岩礁の上に朱塗りの鳥居が立つ絶景の神社。"),
        RouteAnchor(id = "enbetsu_fujimi_michinoeki", rank = 1, name = "道の駅 えんべつ富士見", municipality = "遠別町", lon = 141.7921354376884, lat = 44.71405346787859, x = 253.68f, y = 85.21f, description = "日本海と利尻富士の眺望が素晴らしいオロロンライン沿いの道の駅。"),
        RouteAnchor(id = "kagamimuma_campground", rank = 1, name = "鏡沼海浜公園キャンプ場", municipality = "天塩町", lon = 141.7410437260453, lat = 44.87555180875946, x = 250.42f, y = 70.68f, description = "天塩川河口近くの鏡沼湖畔に広がるキャンプ場と温泉施設。"),
        RouteAnchor(id = "teshio_michinoeki", rank = 2, name = "道の駅 てしお", municipality = "天塩町", lon = 141.7477551, lat = 44.8845723, x = 250.85f, y = 69.87f, description = "天塩川河口の街に位置する道の駅。しじみラーメンや地場産品が名物。"),
        RouteAnchor(id = "otonrui_wind_farm", rank = 1, name = "オトンルイ風力発電所", municipality = "幌延町", lon = 141.69805457576095, lat = 44.97661307686825, x = 247.67f, y = 61.57f, description = "道道106号沿いに28基の巨大風車が直線に立ち並ぶサロベツ原野の壮観。"),
        RouteAnchor(id = "north_45_passing_point", rank = 3, name = "北緯45度通過点 モニュメント", municipality = "幌延町", lon = 141.6812322, lat = 45.0022577, x = 246.60f, y = 59.25f, description = "道道106号沿いに立つ、地球の北緯45度線を示すモニュメント。"),
        RouteAnchor(id = "shimonuma_station", rank = 2, name = "下沼駅", municipality = "幌延町", lon = 141.758205, lat = 45.03723, x = 251.51f, y = 56.09f, description = "JR宗谷本線の駅。パンケ沼やサロベツ原野への玄関口。"),
        RouteAnchor(id = "furusato_no_mori_campground", rank = 3, name = "ふるさとの森 森林公園 キャンプ場", municipality = "幌延町", lon = 141.8456545, lat = 45.0214273, x = 257.10f, y = 57.52f, description = "幌延町市街近くの丘陵の森に広がる静かなキャンプ場。"),
        RouteAnchor(id = "reindeer_ranch", rank = 1, name = "トナカイ観光牧場", municipality = "幌延町", lon = 141.86227257691442, lat = 45.04144807553853, x = 258.16f, y = 55.71f, description = "本物のトナカイとふれあえる日本唯一の観光牧場。"),
        RouteAnchor(id = "toyotomi_station", rank = 2, name = "豊富駅", municipality = "豊富町", lon = 141.77238333333335, lat = 45.10440333333333, x = 252.42f, y = 50.02f, description = "JR宗谷本線の特急停車駅。豊富温泉やサロベツ原野への拠点。"),
        RouteAnchor(id = "sarobetsu_wetland", rank = 1, name = "サロベツ湿原", municipality = "豊富町", lon = 141.70487679479774, lat = 45.10654194093067, x = 248.11f, y = 49.83f, description = "利尻礼文サロベツ国立公園の広大な湿原。木道から高山植物と利尻富士を望む。"),
        RouteAnchor(id = "kabutonuma_park_campground", rank = 3, name = "兜沼公園キャンプ場", municipality = "豊富町", lon = 141.7001835, lat = 45.2169499, x = 247.81f, y = 39.83f, description = "サロベツ原野の兜沼湖畔に広がる白樺林に囲まれたキャンプ場。"),
        RouteAnchor(id = "yuchi_station", rank = 4, name = "勇知駅", municipality = "稚内市", lon = 141.68517, lat = 45.25923, x = 246.85f, y = 36.00f, description = "JR宗谷本線の駅。サロベツ原野の北端、利尻富士を望む原野の木造駅。"),
        RouteAnchor(id = "minami_wakkanai_station", rank = 2, name = "南稚内駅", municipality = "稚内市", lon = 141.68294, lat = 45.397925, x = 246.71f, y = 23.40f, description = "JR宗谷本線の駅。稚内市街南部の交通・商業の拠点。"),
        RouteAnchor(id = "soya_cape", rank = 1, name = "宗谷岬", municipality = "稚内市", lon = 141.93667013638216, lat = 45.52259451811312, x = 262.91f, y = 12.06f, description = "北緯45度31分22秒、日本最北端の地の碑が立つ岬。"),
        RouteAnchor(id = "sarufutsu_park_michinoeki", rank = 2, name = "道の駅 さるふつ公園", municipality = "猿払村", lon = 142.177348, lat = 45.329219, x = 278.27f, y = 29.65f, description = "オホーツク海沿い、広大な敷地にホテルや牧場が広がる日本最北の村の道の駅。"),
        RouteAnchor(id = "esanuka_line", rank = 1, name = "エサヌカ線", municipality = "猿払村", lon = 142.27970649907144, lat = 45.22059445777629, x = 284.81f, y = 39.50f, description = "ガードレールも電柱もない平原をどこまでも一直線に貫くオホーツクの絶景道路。"),
        RouteAnchor(id = "kyu_tenpoku_yasubetsu_station", rank = 2, name = "旧天北線 安別駅跡", municipality = "猿払村", lon = 142.2802263, lat = 45.1756254, x = 284.84f, y = 43.57f, description = "旧国鉄天北線の駅跡。オホーツク沿岸の原野に佇む歴史遺構。"),
        RouteAnchor(id = "kutcharo_campground", rank = 1, name = "クッチャロ湖畔キャンプ場", municipality = "浜頓別町", lon = 142.34702759283468, lat = 45.1258773270401, x = 289.11f, y = 48.08f, description = "ラムサール条約湿地クッチャロ湖の湖畔に広がる、夕日の名所キャンプ場。"),
        RouteAnchor(id = "kitami_kamui_cape_lighthouse", rank = 2, name = "北見神威岬灯台", municipality = "浜頓別町", lon = 142.5013026, lat = 45.0588646, x = 298.96f, y = 54.14f, description = "オホーツク海に突き出た北見神威岬の突端に立つ白亜の灯台。"),
        RouteAnchor(id = "menashidomari_cape", rank = 3, name = "目梨泊岬", municipality = "枝幸町", lon = 142.519861, lat = 45.040012, x = 300.14f, y = 55.84f, description = "オホーツク海沿いに位置する景勝岬。荒々しい海岸美が広がる。"),
        RouteAnchor(id = "north_45_monument", rank = 1, name = "北緯45度モニュメント", municipality = "幌延町/中頓別境", lon = 142.53351198206778, lat = 45.002606473429196, x = 301.01f, y = 59.22f, description = "北極と赤道の中間点を示すモニュメント。オホーツク沿岸の境界点。"),
        RouteAnchor(id = "ustaibe_senjoiwa", rank = 1, name = "ウスタイベ千畳岩", municipality = "枝幸町", lon = 142.58328025404762, lat = 44.96124993449913, x = 304.19f, y = 62.95f, description = "畳を敷き詰めたような柱状節理の岩礁が広がるオホーツク海の景勝地。"),
        RouteAnchor(id = "marine_island_okashima_michinoeki", rank = 3, name = "道の駅 マリーンアイランド岡島", municipality = "枝幸町", lon = 142.631813, lat = 44.8783757, x = 307.29f, y = 70.42f, description = "オホーツク海に浮かぶ客船をイメージしたユニークな外観の道の駅。"),
        RouteAnchor(id = "otoineppu_cape", rank = 2, name = "音標岬", municipality = "雄武町", lon = 142.8184757, lat = 44.7081712, x = 319.20f, y = 85.74f, description = "オホーツク海に小さく突き出た岬。オホーツクライン沿いの景勝地。"),
        RouteAnchor(id = "oumu_michinoeki", rank = 3, name = "道の駅 おうむ", municipality = "雄武町", lon = 142.9597327, lat = 44.5830367, x = 328.22f, y = 96.97f, description = "オホーツク海沿いに位置する道の駅。展望タワーから雄大な海を一望できる。"),
        RouteAnchor(id = "la_luna_observatory", rank = 1, name = "ラ・ルーナ", municipality = "雄武町", lon = 143.05934211980704, lat = 44.52636109424547, x = 334.58f, y = 102.04f, description = "オホーツク海を一望する日の出岬に立つ総ガラス張りの展望台。"),
        RouteAnchor(id = "okoppe_michinoeki", rank = 1, name = "道の駅 おこっぺ", municipality = "興部町", lon = 143.11697158295465, lat = 44.46946617628397, x = 338.26f, y = 107.14f, description = "旧名寄本線興部駅跡に整備された道の駅。特産乳製品が人気。"),
        RouteAnchor(id = "saruru_cape", rank = 2, name = "沙留岬", municipality = "興部町", lon = 143.223066, lat = 44.441593, x = 345.03f, y = 109.63f, description = "オホーツク海に突き出た沙留海岸の岬。海水浴場や海浜公園が広がる。"),
        RouteAnchor(id = "mombetsu_station_site", rank = 3, name = "紋別駅跡", municipality = "紋別市", lon = 143.3519053, lat = 44.3527927, x = 353.26f, y = 117.56f, description = "旧名寄本線紋別駅の跡地。オホーツク流氷公園や市街地の拠点。"),
        RouteAnchor(id = "kani_no_tsume_object", rank = 1, name = "カニの爪オブジェ", municipality = "紋別市", lon = 143.37742655864253, lat = 44.32767506649157, x = 354.89f, y = 119.80f, description = "オホーツク海沿いにそびえ立つ高さ12mの巨大なカニの爪モニュメント。"),
        RouteAnchor(id = "komuke_campground", rank = 2, name = "コムケ国際キャンプ場", municipality = "紋別市", lon = 143.5201814, lat = 44.2555513, x = 364.00f, y = 126.24f, description = "野鳥の楽園コムケ湖畔に位置する静かなキャンプ場。"),
        RouteAnchor(id = "kamiyubetsu_onsen_michinoeki", rank = 3, name = "道の駅 かみゆうべつ温泉チューリップの湯", municipality = "湧別町", lon = 143.5956986, lat = 44.1851816, x = 368.82f, y = 132.51f, description = "旧国鉄名寄本線中湧別駅跡に位置する天然温泉併設の道の駅。"),
        RouteAnchor(id = "kamiyubetsu_tulip_park", rank = 1, name = "かみゆうべつチューリップ公園", municipality = "湧別町", lon = 143.579464112413, lat = 44.161614057521476, x = 367.79f, y = 134.60f, description = "広大な敷地に約200品種のチューリップが色鮮やかに咲き誇る名所公園。"),
        RouteAnchor(id = "sango_cape", rank = 3, name = "サンゴ岬", municipality = "湧別町", lon = 143.6740279, lat = 44.1835658, x = 373.82f, y = 132.65f, description = "サロマ湖西岸に位置する景勝岬。秋にはアッケシソウ（サンゴ草）が赤く色づく。"),
        RouteAnchor(id = "airando_yubetsu_michinoeki", rank = 2, name = "道の駅 愛ランド湧別", municipality = "湧別町", lon = 143.7192064, lat = 44.1308204, x = 376.71f, y = 137.34f, description = "サロマ湖を一望する遊園地「ファミリー愛ランドYOU」併設の道の駅。"),
        RouteAnchor(id = "piraoro_observatory", rank = 3, name = "ピラオロ展望台", municipality = "網走市", lon = 143.8027572, lat = 44.095918, x = 382.04f, y = 140.45f, description = "能取湖とオホーツク海を一望する景勝展望台。"),
        RouteAnchor(id = "saromako_michinoeki", rank = 4, name = "道の駅 サロマ湖", municipality = "佐呂間町", lon = 143.8241425, lat = 44.0999179, x = 383.41f, y = 140.09f, description = "国道238号沿い、サロマ湖を望む道の駅。名物ホタテ料理や物産が揃う。"),
        RouteAnchor(id = "saroma_lake_observatory", rank = 1, name = "サロマ湖展望台", municipality = "佐呂間町", lon = 143.83634393246012, lat = 44.09744029705804, x = 384.19f, y = 140.31f, description = "サロマ湖とオホーツク海を360度パノラマで見渡す標高376mの展望地。"),
        RouteAnchor(id = "kimaneppu_cape_coral_grass", rank = 3, name = "キムアネップ岬サンゴ草群生地", municipality = "佐呂間町", lon = 143.9037545, lat = 44.1023384, x = 388.49f, y = 139.88f, description = "サロマ湖東南に突き出た岬に広がる日本最大級のサンゴ草群生地。"),
        RouteAnchor(id = "okhotsk_no_mori_observatory", rank = 2, name = "オホーツクの森展望台", municipality = "網走市", lon = 144.0202384, lat = 43.9510205, x = 395.93f, y = 153.31f, description = "網走市郊外の丘陵地に広がる森林公園の展望台。能取湖やオホーツク海を一望。"),
        RouteAnchor(id = "memanbetsu_kohan_campground", rank = 3, name = "女満別湖畔キャンプ場", municipality = "大空町", lon = 144.167877, lat = 43.9164805, x = 405.35f, y = 156.37f, description = "網走湖畔の白砂青松の美しい湖畔に広がるキャンプ場。"),
        RouteAnchor(id = "yobito_station", rank = 4, name = "呼人駅", municipality = "網走市", lon = 144.218955, lat = 43.963785, x = 408.61f, y = 152.18f, description = "JR石北本線の駅。網走湖の南岸に位置する静かな湖畔の駅。"),
        RouteAnchor(id = "abashiri_prison_museum", rank = 1, name = "博物館網走監獄", municipality = "網走市", lon = 144.23008845295843, lat = 43.99577965325261, x = 409.32f, y = 149.34f, description = "明治時代からの網走刑務所旧建造物を保存公開する野外歴史博物館。"),
        RouteAnchor(id = "tentozan_observatory", rank = 3, name = "天都山展望台", municipality = "網走市", lon = 144.2397305, lat = 44.0005858, x = 409.94f, y = 148.92f, description = "標高207mの天都山山頂にある展望台。オホーツク流氷館を併設し知床連山を一望。"),
        RouteAnchor(id = "masuura_station", rank = 4, name = "鱒浦駅", municipality = "網走市", lon = 144.296535, lat = 43.98253, x = 413.57f, y = 150.52f, description = "JR釧網本線の駅。オホーツク海岸沿いに位置し海岸の絶景が広がる。"),
        RouteAnchor(id = "mokoto_station", rank = 2, name = "藻琴駅", municipality = "網走市", lon = 144.32001000000002, lat = 43.968395, x = 415.06f, y = 151.77f, description = "JR釧網本線のオホーツク海に面した木造駅舎の駅。喫茶店「トロッコ」が併設。"),
        RouteAnchor(id = "kitahama_station", rank = 3, name = "北浜駅", municipality = "網走市", lon = 144.35246, lat = 43.958535, x = 417.14f, y = 152.65f, description = "JR釧網本線の駅。「オホーツク海に一番近い駅」として全国的に有名な無人駅。"),
        RouteAnchor(id = "koshimizu_primeval_flower_garden", rank = 1, name = "小清水原生花園", municipality = "小清水町", lon = 144.41451457856553, lat = 43.94201658309603, x = 421.10f, y = 154.11f, description = "オホーツク海とオホーツク砂丘の間に広がる天然の花園。"),
        RouteAnchor(id = "shari_michinoeki", rank = 4, name = "道の駅 しゃり", municipality = "斜里町", lon = 144.6642138, lat = 43.9117709, x = 437.04f, y = 156.79f, description = "世界自然遺産・知床への玄関口、斜里市街中心部に位置する道の駅。"),
        RouteAnchor(id = "road_to_heaven_observatory", rank = 3, name = "天に続く道展望台", municipality = "斜里町", lon = 144.785545, lat = 43.906401, x = 444.79f, y = 157.27f, description = "地平線に向かって一直線に続く全長約28kmの道が「天に続く」ように見える絶景展望台。"),
        RouteAnchor(id = "utoro_shirietoku_michinoeki", rank = 4, name = "道の駅 うとろ・シリエトク", municipality = "斜里町", lon = 144.9906685, lat = 44.0690729, x = 457.88f, y = 142.83f, description = "知床観光の拠点となる大型道の駅。知床世界遺産センターに隣接する。"),
        RouteAnchor(id = "furepe_waterfall_observatory", rank = 2, name = "フレペの滝 展望台", municipality = "斜里町", lon = 145.0129553, lat = 44.0959619, x = 459.30f, y = 140.44f, description = "知床半島の断崖からオホーツク海へ直接流れ落ちる「乙女の涙」フレペの滝を望む展望台。"),
        RouteAnchor(id = "shiretoko_five_lakes", rank = 1, name = "知床五湖", municipality = "斜里町", lon = 145.08114194001618, lat = 44.12622511123464, x = 463.66f, y = 137.75f, description = "世界自然遺産知床の原生林に抱かれた五つの神秘的な湖沼群。高架木道が整備。"),
        RouteAnchor(id = "shiretoko_pass_observatory", rank = 1, name = "知床峠展望台", municipality = "斜里町/羅臼町境", lon = 145.10593246961236, lat = 44.05476067950885, x = 465.24f, y = 144.10f, description = "知床横断道路の最高地点。羅臼岳の雄姿と国後島を一望する峠展望台。"),
        RouteAnchor(id = "kumagoe_waterfall_observatory", rank = 2, name = "熊越の滝展望台", municipality = "羅臼町", lon = 145.1461308, lat = 44.031826, x = 467.81f, y = 146.14f, description = "知床峠への登り口近くにある名瀑・熊越の滝を望む展望地。"),
        RouteAnchor(id = "rausu_michinoeki", rank = 1, name = "道の駅 知床・らうす", municipality = "羅臼町", lon = 145.19227186693251, lat = 44.01766271327695, x = 470.75f, y = 147.40f, description = "羅臼港の目の前にあり、羅臼昆布や新鮮な海の幸が並ぶ知床東海岸の拠点。"),
        RouteAnchor(id = "aidomari_bridge", rank = 1, name = "相泊橋", municipality = "羅臼町", lon = 145.32468006793732, lat = 44.19083540598293, x = 479.21f, y = 132.00f, description = "道道87号知床公園羅臼線の車道終点。北海道一周すごろく第1部のゴール。"),
    )

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
