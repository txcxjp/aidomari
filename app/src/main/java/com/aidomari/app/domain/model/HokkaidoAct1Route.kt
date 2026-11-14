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

    /** ユーザー指定の公式一次測地データに基づく正規アンカーポイント（全78箇所） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(id = "hakodateyama", rank = 1, name = "函館山", municipality = "函館市", lon = 140.70553624563212, lat = 41.744766784111675, x = 184.31f, y = 345.46f, description = "100万ドルの夜景で名高い函館山。津軽海峡と函館市街を一望する山頂展望台。"),
        RouteAnchor(id = "tachimachi_story", rank = 2, name = "立待岬物語", municipality = "函館市", lon = 140.7161344, lat = 41.7531496, x = 184.99f, y = 344.74f, description = "立待岬近くに設置された石碑。津軽海峡と断崖を望む。"),
        RouteAnchor(id = "mashumaru", rank = 1, name = "摩周丸", municipality = "函館市", lon = 140.72304509852597, lat = 41.77256073112773, x = 185.43f, y = 343.08f, description = "函館港旧第2岸壁に保存・係留されている青函連絡船記念館。"),
        RouteAnchor(id = "onari_bath_center", rank = 2, name = "桜成浴場センター", municipality = "函館市", lon = 140.7363224, lat = 41.7799329, x = 186.28f, y = 342.45f, description = "函館市街に位置する公衆浴場。"),
        RouteAnchor(id = "goryokaku", rank = 1, name = "五稜郭", municipality = "函館市", lon = 140.75679445417336, lat = 41.796991557953135, x = 187.58f, y = 340.99f, description = "幕末の歴史を刻む国指定特別史跡の星形要塞。"),
        RouteAnchor(id = "apple_onsen", rank = 2, name = "七飯町健康センター アップル温泉", municipality = "七飯町", lon = 140.7082658, lat = 41.8599512, x = 184.48f, y = 335.59f, description = "七飯町にある天然温泉健康センター。りんごの里の名湯。"),
        RouteAnchor(id = "shin_hakodate_hokuto_station", rank = 1, name = "新函館北斗駅", municipality = "北斗市", lon = 140.6514055298738, lat = 41.90410586161933, x = 180.85f, y = 331.81f, description = "北海道新幹線の北の終着駅。道南の交通結節点。"),
        RouteAnchor(id = "kenashiyama", rank = 2, name = "毛無山", municipality = "北斗市", lon = 140.4704672, lat = 42.0876673, x = 169.30f, y = 316.04f, description = "北斗市と木古内町境に位置する山峰。"),
        RouteAnchor(id = "funkawan_panorama_park", rank = 1, name = "噴火湾パノラマパーク", municipality = "八雲町", lon = 140.31405732962233, lat = 42.227215716723315, x = 159.32f, y = 304.02f, description = "噴火湾を見下ろす広大な丘陵公園。遊具やハーブ園が広がる絶景地。"),
        RouteAnchor(id = "kuroiwa_station", rank = 2, name = "黒岩駅", municipality = "八雲町", lon = 140.28818, lat = 42.368354999999994, x = 157.67f, y = 291.84f, description = "JR函館本線の駅。噴火湾沿いに位置する。"),
        RouteAnchor(id = "oshamambe_station", rank = 1, name = "長万部駅", municipality = "長万部町", lon = 140.37552466273777, lat = 42.513017969486114, x = 163.24f, y = 279.33f, description = "函館本線と室蘭本線が分岐する交通の要衝。名物「かにめし」発祥の地。"),
        RouteAnchor(id = "shambedake", rank = 2, name = "写万部山", municipality = "長万部町", lon = 140.3856694, lat = 42.5907112, x = 163.89f, y = 272.59f, description = "長万部町北部にそびえる山峰。"),
        RouteAnchor(id = "kuromatsunai_michinoeki", rank = 1, name = "道の駅 くろまつない", municipality = "黒松内町", lon = 140.37979736828004, lat = 42.67239712919561, x = 163.51f, y = 265.50f, description = "北限のブナ原生林の里。本格焼きたてピザやパンが人気の道の駅。"),
        RouteAnchor(id = "yubetsu_no_yu_onsen", rank = 2, name = "寿都温泉 ゆべつのゆ", municipality = "寿都町", lon = 140.2746812, lat = 42.7517391, x = 156.80f, y = 258.61f, description = "寿都湾南部に位置する天然温泉施設。塩化物泉の名湯。"),
        RouteAnchor(id = "benkei_cape", rank = 1, name = "弁慶岬", municipality = "寿都町", lon = 140.1887125350538, lat = 42.82183022260921, x = 151.32f, y = 252.51f, description = "義経・弁慶伝説が残る日本海に突き出た名岬。白亜の灯台が立つ。"),
        RouteAnchor(id = "kumanoyama", rank = 2, name = "熊野山", municipality = "寿都町", lon = 140.4367436, lat = 42.9369803, x = 167.15f, y = 242.48f, description = "寿都町南部の日本海を望む山峰。"),
        RouteAnchor(id = "horoni_railway_park", rank = 1, name = "幌似鉄道記念公園", municipality = "共和町", lon = 140.6139535344062, lat = 42.98665997187308, x = 178.46f, y = 238.15f, description = "旧国鉄岩内線の幌似駅跡を保存した鉄道記念公園。当時の木造駅舎と車両を展示。"),
        RouteAnchor(id = "ryujin_cape", rank = 2, name = "竜神岬", municipality = "神恵内村", lon = 140.4280092, lat = 43.1365587, x = 166.59f, y = 225.05f, description = "神恵内村の日本海に突き出た景勝岬。奇岩と荒波が織りなす積丹西岸の景勝地。"),
        RouteAnchor(id = "kamui_cape", rank = 1, name = "神威岬", municipality = "積丹町", lon = 140.3594022649703, lat = 43.32848146798221, x = 162.21f, y = 208.23f, description = "積丹ブルーの海へダイナミックに突き出す大パノラマの絶景岬。"),
        RouteAnchor(id = "ogon_cape_shakotan", rank = 2, name = "黄金岬", municipality = "積丹町", lon = 140.6013495, lat = 43.3031736, x = 177.66f, y = 210.45f, description = "美国港のすぐそばにある景勝地。かつてニシン見張り台があった岬。"),
        RouteAnchor(id = "nikka_yoichi_distillery", rank = 1, name = "ニッカウヰスキー 余市蒸溜所", municipality = "余市町", lon = 140.79185822539347, lat = 43.18739590094136, x = 189.82f, y = 220.60f, description = "日本のウイスキーの父・竹鶴政孝が創業した伝統のウイスキー蒸溜所。"),
        RouteAnchor(id = "funatoriyama", rank = 2, name = "船取山", municipality = "余市町", lon = 140.8729112, lat = 43.1896818, x = 195.00f, y = 220.40f, description = "余市町と小樽市境に位置する山峰。"),
        RouteAnchor(id = "otaru_canal", rank = 1, name = "小樽運河", municipality = "小樽市", lon = 141.0020940032056, lat = 43.19918050706471, x = 203.24f, y = 219.57f, description = "石造り倉庫群が連なりガス灯が灯る、小樽を代表する歴史的観光名所。"),
        RouteAnchor(id = "wao_no_mori_observatory", rank = 2, name = "ワオーの森　展望台", municipality = "小樽市", lon = 141.1413313, lat = 43.1307911, x = 212.13f, y = 225.55f, description = "小樽市張碓の丘陵に広がるワオーの森の展望地。"),
        RouteAnchor(id = "sapporo_clock_tower", rank = 1, name = "札幌市時計台", municipality = "札幌市", lon = 141.35342842412132, lat = 43.062788412367794, x = 225.67f, y = 231.50f, description = "明治11年創建の旧札幌農学校演武場。札幌のシンボルとして親しまれる歴史的建造物。"),
        RouteAnchor(id = "atsuta_observatory", rank = 2, name = "厚田展望台", municipality = "石狩市", lon = 141.4324888, lat = 43.4058027, x = 230.72f, y = 201.44f, description = "国道231号沿いの高台にある夕日の名所展望台。「恋人の聖地」に認定。"),
        RouteAnchor(id = "ofuyu_cape", rank = 1, name = "雄冬岬", municipality = "増毛町", lon = 141.33591905548778, lat = 43.73280428173676, x = 224.56f, y = 172.63f, description = "かつて陸の孤島と呼ばれたオロロンラインの険しい断崖絶壁が続く名岬。"),
        RouteAnchor(id = "akaiwamisaki", rank = 2, name = "赤岩岬", municipality = "増毛町", lon = 141.3541826, lat = 43.7580764, x = 225.72f, y = 170.39f, description = "日本海に突き出た赤褐色の奇岩断崖が連なる景勝岬。"),
        RouteAnchor(id = "kunimare_brewery", rank = 1, name = "国稀酒造", municipality = "増毛町", lon = 141.52321177017552, lat = 43.85776020957361, x = 236.51f, y = 161.58f, description = "明治35年創業、暑寒別岳の伏流水で仕込む日本最北の造り酒屋。"),
        RouteAnchor(id = "anchor_855e4c59", rank = 2, name = "夕陽ヶ丘公園", municipality = "増毛町", lon = 141.6373647, lat = 43.9337364, x = 243.80f, y = 154.84f, description = "夕陽ヶ丘公園。"),
        RouteAnchor(id = "ogon_cape_beach_park", rank = 1, name = "黄金岬海浜公園", municipality = "留萌市", lon = 141.62939738242676, lat = 43.94678147706456, x = 243.29f, y = 153.69f, description = "夕日に染まる日本海と荒波が砕ける奇岩が美しい、留萌を代表する夕日スポット。"),
        RouteAnchor(id = "boyodai_observatory", rank = 2, name = "望洋台展望台", municipality = "留萌市", lon = 141.662725, lat = 44.0353934, x = 245.42f, y = 145.82f, description = "留萌市街と日本海を一望する高台の展望台。"),
        RouteAnchor(id = "sankebetsu_bear_incident_site", rank = 1, name = "三毛別羆事件復元地", municipality = "苫前町", lon = 141.7994765008687, lat = 44.13722420804186, x = 254.15f, y = 136.77f, description = "大正4年に発生した史上最大の獣害事件の現場を今に伝える復元展示地。"),
        RouteAnchor(id = "anchor_ba92a4c7", rank = 2, name = "緑ヶ丘公園", municipality = "苫前町", lon = 141.7154669, lat = 44.2592681, x = 248.79f, y = 125.91f, description = "緑ヶ丘公園。"),
        RouteAnchor(id = "tomamae_michinoeki", rank = 1, name = "道の駅 風Wとままえ", municipality = "苫前町", lon = 141.6567301836899, lat = 44.313040239571436, x = 245.04f, y = 121.11f, description = "日本海と天売・焼尻島を一望する風車群が立ち並ぶ温泉併設の道の駅。"),
        RouteAnchor(id = "anchor_dcf9ba92", rank = 2, name = "栄町公園", municipality = "羽幌町", lon = 141.6977548, lat = 44.3542909, x = 247.66f, y = 117.43f, description = "栄町公園。"),
        RouteAnchor(id = "haboro_rose_garden", rank = 1, name = "はぼろバラ園", municipality = "羽幌町", lon = 141.70752630772944, lat = 44.36636286445792, x = 248.28f, y = 116.35f, description = "約300種2000株のバラが咲き誇る、道の駅ほっと・はぼろに隣接する庭園。"),
        RouteAnchor(id = "shosanbetsu_sports_park", rank = 2, name = "初山別村スポーツ公園", municipality = "初山別村", lon = 141.7720804, lat = 44.5306585, x = 252.40f, y = 101.66f, description = "初山別村の日本海を望むスポーツ公園。"),
        RouteAnchor(id = "toyomisaki_konpira_shrine", rank = 1, name = "豊岬金比羅神社", municipality = "初山別村", lon = 141.77250742378564, lat = 44.56371722986578, x = 252.43f, y = 98.70f, description = "日本海の荒波洗う岩礁の上に朱塗りの鳥居が立つ絶景の神社。"),
        RouteAnchor(id = "konpu_primeval_flower_garden", rank = 2, name = "金浦原生花園", municipality = "遠別町", lon = 141.7922968, lat = 44.6815561, x = 253.69f, y = 88.13f, description = "遠別町南部の海岸沿いに広がる天然原生花園。"),
        RouteAnchor(id = "enbetsu_fujimi_michinoeki", rank = 1, name = "道の駅 えんべつ富士見", municipality = "遠別町", lon = 141.7921354376884, lat = 44.71405346787859, x = 253.68f, y = 85.21f, description = "日本海と利尻富士の眺望が素晴らしいオロロンライン沿いの道の駅。"),
        RouteAnchor(id = "soya_cape_ranch_teshio", rank = 2, name = "宗谷岬牧場天塩分場", municipality = "天塩町", lon = 141.7692925, lat = 44.8703441, x = 252.22f, y = 71.15f, description = "天塩町の丘陵に広がる広大な放牧場。"),
        RouteAnchor(id = "kagamimuma_campground", rank = 1, name = "鏡沼海浜公園キャンプ場", municipality = "天塩町", lon = 141.7410437260453, lat = 44.87555180875946, x = 250.42f, y = 70.68f, description = "天塩川河口近くの鏡沼湖畔に広がるキャンプ場と温泉施設。"),
        RouteAnchor(id = "teshio_itsukushima_park", rank = 2, name = "天塩いつくしま公園", municipality = "天塩町", lon = 141.740845, lat = 44.8924378, x = 250.41f, y = 69.16f, description = "天塩川河口近くに広がる水と緑の憩い公園。"),
        RouteAnchor(id = "otonrui_wind_farm", rank = 1, name = "オトンルイ風力発電所", municipality = "幌延町", lon = 141.69805457576095, lat = 44.97661307686825, x = 247.67f, y = 61.57f, description = "道道106号沿いに28基の巨大風車が直線に立ち並ぶサロベツ原野の壮観。"),
        RouteAnchor(id = "anchor_03c73480", rank = 2, name = "北緯45度の碑", municipality = "幌延町", lon = 141.807524, lat = 45.0022523, x = 254.66f, y = 59.25f, description = "北緯45度の碑。"),
        RouteAnchor(id = "reindeer_ranch", rank = 1, name = "トナカイ観光牧場", municipality = "幌延町", lon = 141.86227257691442, lat = 45.04144807553853, x = 258.16f, y = 55.71f, description = "本物のトナカイとふれあえる日本唯一の観光牧場。"),
        RouteAnchor(id = "toyotomi_nature_park", rank = 2, name = "豊富町自然公園", municipality = "豊富町", lon = 141.7897588, lat = 45.100936, x = 253.53f, y = 50.33f, description = "豊富町の緑豊かな自然公園。"),
        RouteAnchor(id = "sarobetsu_wetland", rank = 1, name = "サロベツ湿原", municipality = "豊富町", lon = 141.70487679479774, lat = 45.10654194093067, x = 248.11f, y = 49.83f, description = "利尻礼文サロベツ国立公園の広大な湿原。木道から高山植物と利尻富士を望む。"),
        RouteAnchor(id = "minato_no_yu_onsen", rank = 2, name = "稚内天然温泉 港のゆ", municipality = "稚内市", lon = 141.6766775, lat = 45.4080491, x = 246.31f, y = 22.48f, description = "稚内港を望む天然温泉施設。"),
        RouteAnchor(id = "soya_cape", rank = 1, name = "宗谷岬", municipality = "稚内市", lon = 141.93667013638216, lat = 45.52259451811312, x = 262.91f, y = 12.06f, description = "北緯45度31分22秒、日本最北端の地の碑が立つ岬。"),
        RouteAnchor(id = "sarufutsu_park_michinoeki", rank = 2, name = "道の駅 さるふつ公園", municipality = "猿払村", lon = 142.177348, lat = 45.329219, x = 278.27f, y = 29.65f, description = "オホーツク海沿い、広大な敷地にホテルや牧場が広がる日本最北の村の道の駅。"),
        RouteAnchor(id = "esanuka_line", rank = 1, name = "エサヌカ線", municipality = "猿払村", lon = 142.27970649907144, lat = 45.22059445777629, x = 284.81f, y = 39.50f, description = "ガードレールも電柱もない平原をどこまでも一直線に貫くオホーツクの絶景道路。"),
        RouteAnchor(id = "kyu_tenpoku_yasubetsu_station", rank = 2, name = "旧天北線 安別駅跡", municipality = "猿払村", lon = 142.2802263, lat = 45.1756254, x = 284.84f, y = 43.57f, description = "旧国鉄天北線の駅跡。オホーツク沿岸の原野に佇む歴史遺構。"),
        RouteAnchor(id = "kutcharo_campground", rank = 1, name = "クッチャロ湖畔キャンプ場", municipality = "浜頓別町", lon = 142.34702759283468, lat = 45.1258773270401, x = 289.11f, y = 48.08f, description = "ラムサール条約湿地クッチャロ湖の湖畔に広がる、夕日の名所キャンプ場。"),
        RouteAnchor(id = "mt_shanai", rank = 2, name = "斜内山", municipality = "浜頓別町", lon = 142.4858844, lat = 45.0535904, x = 297.97f, y = 54.61f, description = "斜内山。"),
        RouteAnchor(id = "north_45_monument", rank = 1, name = "北緯45度モニュメント", municipality = "幌延町/中頓別境", lon = 142.53351198206778, lat = 45.002606473429196, x = 301.01f, y = 59.22f, description = "北極と赤道の中間点を示すモニュメント。オホーツク沿岸の境界点。"),
        RouteAnchor(id = "ustaibe_senjoiwa", rank = 1, name = "ウスタイベ千畳岩", municipality = "枝幸町", lon = 142.58328025404762, lat = 44.96124993449913, x = 304.19f, y = 62.95f, description = "畳を敷き詰めたような柱状節理の岩礁が広がるオホーツク海の景勝地。"),
        RouteAnchor(id = "otoineppu_cape", rank = 2, name = "音標岬", municipality = "雄武町", lon = 142.8184757, lat = 44.7081712, x = 319.20f, y = 85.74f, description = "オホーツク海に小さく突き出た岬。オホーツクライン沿いの景勝地。"),
        RouteAnchor(id = "la_luna_observatory", rank = 1, name = "ラ・ルーナ", municipality = "雄武町", lon = 143.05934211980704, lat = 44.52636109424547, x = 334.58f, y = 102.04f, description = "オホーツク海を一望する日の出岬に立つ総ガラス張りの展望台。"),
        RouteAnchor(id = "okoppe_michinoeki", rank = 1, name = "道の駅 おこっぺ", municipality = "興部町", lon = 143.11697158295465, lat = 44.46946617628397, x = 338.26f, y = 107.14f, description = "旧名寄本線興部駅跡に整備された道の駅。特産乳製品が人気。"),
        RouteAnchor(id = "saruru_cape", rank = 2, name = "沙留岬", municipality = "興部町", lon = 143.223066, lat = 44.441593, x = 345.03f, y = 109.63f, description = "オホーツク海に突き出た沙留海岸の岬。海水浴場や海浜公園が広がる。"),
        RouteAnchor(id = "kani_no_tsume_object", rank = 1, name = "カニの爪オブジェ", municipality = "紋別市", lon = 143.37742655864253, lat = 44.32767506649157, x = 354.89f, y = 119.80f, description = "オホーツク海沿いにそびえ立つ高さ12mの巨大なカニの爪モニュメント。"),
        RouteAnchor(id = "komuke_campground", rank = 2, name = "コムケ国際キャンプ場", municipality = "紋別市", lon = 143.5201814, lat = 44.2555513, x = 364.00f, y = 126.24f, description = "野鳥の楽園コムケ湖畔に位置する静かなキャンプ場。"),
        RouteAnchor(id = "kamiyubetsu_tulip_park", rank = 1, name = "かみゆうべつチューリップ公園", municipality = "湧別町", lon = 143.579464112413, lat = 44.161614057521476, x = 367.79f, y = 134.60f, description = "広大な敷地に約200品種のチューリップが色鮮やかに咲き誇る名所公園。"),
        RouteAnchor(id = "airando_yubetsu_michinoeki", rank = 2, name = "道の駅 愛ランド湧別", municipality = "湧別町", lon = 143.7192064, lat = 44.1308204, x = 376.71f, y = 137.34f, description = "サロマ湖を一望する遊園地「ファミリー愛ランドYOU」併設の道の駅。"),
        RouteAnchor(id = "saroma_lake_observatory", rank = 1, name = "サロマ湖展望台", municipality = "佐呂間町", lon = 143.83634393246012, lat = 44.09744029705804, x = 384.19f, y = 140.31f, description = "サロマ湖とオホーツク海を360度パノラマで見渡す標高376mの展望地。"),
        RouteAnchor(id = "anchor_1442b988", rank = 2, name = "イワケシ山", municipality = "佐呂間町", lon = 143.9777372, lat = 44.0451333, x = 393.21f, y = 144.96f, description = "イワケシ山。"),
        RouteAnchor(id = "abashiri_prison_museum", rank = 1, name = "博物館網走監獄", municipality = "網走市", lon = 144.23008845295843, lat = 43.99577965325261, x = 409.32f, y = 149.34f, description = "明治時代からの網走刑務所旧建造物を保存公開する野外歴史博物館。"),
        RouteAnchor(id = "mokoto_station", rank = 2, name = "藻琴駅", municipality = "網走市", lon = 144.32001000000002, lat = 43.968395, x = 415.06f, y = 151.77f, description = "JR釧網本線のオホーツク海に面した木造駅舎の駅。喫茶店「トロッコ」が併設。"),
        RouteAnchor(id = "koshimizu_primeval_flower_garden", rank = 1, name = "小清水原生花園", municipality = "小清水町", lon = 144.41451457856553, lat = 43.94201658309603, x = 421.10f, y = 154.11f, description = "オホーツク海とオホーツク砂丘の間に広がる天然の花園。"),
        RouteAnchor(id = "furepe_waterfall_observatory", rank = 2, name = "フレペの滝 展望台", municipality = "斜里町", lon = 145.0129553, lat = 44.0959619, x = 459.30f, y = 140.44f, description = "知床半島の断崖からオホーツク海へ直接流れ落ちる「乙女の涙」フレペの滝を望む展望台。"),
        RouteAnchor(id = "shiretoko_five_lakes", rank = 1, name = "知床五湖", municipality = "斜里町", lon = 145.08114194001618, lat = 44.12622511123464, x = 463.66f, y = 137.75f, description = "世界自然遺産知床の原生林に抱かれた五つの神秘的な湖沼群。高架木道が整備。"),
        RouteAnchor(id = "shiretoko_pass_observatory", rank = 1, name = "知床峠展望台", municipality = "斜里町/羅臼町境", lon = 145.10593246961236, lat = 44.05476067950885, x = 465.24f, y = 144.10f, description = "知床横断道路の最高地点。羅臼岳の雄姿と国後島を一望する峠展望台。"),
        RouteAnchor(id = "kumagoe_waterfall_observatory", rank = 2, name = "熊越の滝展望台", municipality = "羅臼町", lon = 145.1461308, lat = 44.031826, x = 467.81f, y = 146.14f, description = "知床峠への登り口近くにある名瀑・熊越の滝を望む展望地。"),
        RouteAnchor(id = "rausu_michinoeki", rank = 1, name = "道の駅 知床・らうす", municipality = "羅臼町", lon = 145.19227186693251, lat = 44.01766271327695, x = 470.75f, y = 147.40f, description = "羅臼港の目の前にあり、羅臼昆布や新鮮な海の幸が並ぶ知床東海岸の拠点。"),
        RouteAnchor(id = "whale_watching_hill_park", rank = 2, name = "クジラの見える丘公園", municipality = "羅臼町", lon = 145.2195039, lat = 44.0372356, x = 472.49f, y = 145.66f, description = "根室海峡を見下ろしクジラやシャチを観察できる展望公園。"),
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
