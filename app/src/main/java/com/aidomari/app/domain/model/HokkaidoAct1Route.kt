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
 * ノードメタデータ（座標を持たず、名称や説明文等の属性のみ保持）。
 */
data class SugorokuNodeMeta(
    val id: String,
    val stepIndex: Int,
    val name: String,
    val municipality: String,
    val type: SugorokuNodeType,
    val description: String = "",
    val roadName: String = "",
)

/**
 * 北海道一周すごろく（第1部：函館駅前 〜 相泊終点）全2,000マスのルートデータ。
 * 全ての岬・灯台・港湾・主要ランドマークのみを厳選した公的測地アンカーを保持し、
 * 全2,000マスの座標はアンカーとアンカーを結ぶ直線上で均等に線形補間（Lerp）して実行時に動的算出します。
 */
object HokkaidoAct1Route {

    val totalNodes: Int = 2000
    val maxStepIndex: Int = totalNodes - 1

    /** 公式一次測地データに基づくアンカーポイント（全ての岬・灯台・港湾網羅） */
    val anchors: List<RouteAnchor> = listOf(
        RouteAnchor(0, 140.7264, 41.7738, 185.64f, 342.97f, "函館駅前"),
        RouteAnchor(18, 140.7337, 41.8035, 186.11f, 340.43f, "五稜郭駅前"),
        RouteAnchor(44, 140.6723, 41.9814, 182.19f, 325.17f, "大沼国定公園"),
        RouteAnchor(60, 140.5919, 42.1111, 177.06f, 314.02f, "森港"),
        RouteAnchor(61, 140.575, 42.109, 175.98f, 314.2f, "森駅前"),
        RouteAnchor(75, 140.4721, 42.1595, 169.41f, 309.86f, "石倉港"),
        RouteAnchor(87, 140.4256, 42.1895, 166.44f, 307.27f, "落部港"),
        RouteAnchor(93, 140.3792, 42.2179, 163.48f, 304.83f, "野田生港"),
        RouteAnchor(98, 140.2678, 42.2514, 156.36f, 301.94f, "JR八雲駅前"),
        RouteAnchor(108, 140.2703, 42.3121, 156.52f, 296.70f, "山崎港"),
        RouteAnchor(132, 140.33, 42.4389, 160.34f, 285.74f, "国縫漁港"),
        RouteAnchor(148, 140.375, 42.5125, 163.21f, 279.37f, "長万部観光案内所"),
        RouteAnchor(225, 140.245, 42.791, 154.91f, 255.2f, "樽岸港"),
        RouteAnchor(229, 140.2361, 42.7944, 154.34f, 254.9f, "寿都港"),
        RouteAnchor(230, 140.2285, 42.7938, 153.86f, 254.95f, "道の駅みなとま〜れ寿都"),
        RouteAnchor(233, 140.1832, 42.8174, 150.96f, 252.9f, "弁慶岬"),
        RouteAnchor(235, 140.1897, 42.8247, 151.38f, 252.26f, "弁慶岬灯台"),
        RouteAnchor(247, 140.0683, 42.7158, 143.63f, 261.73f, "道の駅よってけ！島牧"),
        RouteAnchor(249, 140.1124, 42.7397, 146.44f, 259.66f, "本目港"),
        RouteAnchor(254, 140.0615, 42.7005, 143.19f, 263.06f, "泊港（島牧）"),
        RouteAnchor(259, 140.005, 42.668, 139.59f, 265.89f, "栄浜港（島牧）"),
        RouteAnchor(270, 139.8322, 42.6045, 128.55f, 271.4f, "茂津多岬"),
        RouteAnchor(271, 139.8278, 42.6144, 128.27f, 270.54f, "茂津多岬灯台"),
        RouteAnchor(274, 139.851, 42.628, 129.75f, 269.36f, "茂津多岬入口"),
        RouteAnchor(315, 140.2361, 42.7944, 154.34f, 254.9f, "寿都港"),
        RouteAnchor(328, 140.4037, 42.9276, 165.04f, 243.3f, "カスペの岬"),
        RouteAnchor(329, 140.4028, 42.9242, 164.98f, 243.6f, "雷電温泉郷"),
        RouteAnchor(348, 140.5119, 42.9964, 171.95f, 237.3f, "岩内港"),
        RouteAnchor(357, 140.512, 43.001, 171.95f, 236.9f, "岩内港北防波堤"),
        RouteAnchor(362, 140.505, 42.992, 171.51f, 237.68f, "岩内西港"),
        RouteAnchor(377, 140.4653, 43.0841, 168.97f, 229.64f, "兜岬"),
        RouteAnchor(382, 140.4, 43.1073, 164.8f, 227.61f, "盃漁港"),
        RouteAnchor(389, 140.4989, 43.063, 171.12f, 231.48f, "泊港（泊村）"),
        RouteAnchor(394, 140.472, 43.118, 169.4f, 226.67f, "滝ノ澗ノ岬"),
        RouteAnchor(399, 140.432, 43.1435, 166.85f, 224.44f, "神恵内港"),
        RouteAnchor(414, 140.3269, 43.2206, 160.14f, 217.69f, "川白港"),
        RouteAnchor(426, 140.3306, 43.2603, 160.37f, 214.21f, "ジュウボウ岬"),
        RouteAnchor(428, 140.354, 43.322, 161.87f, 208.8f, "神威岬アプローチ"),
        RouteAnchor(429, 140.428, 43.144, 166.59f, 224.4f, "神恵内灯台前"),
        RouteAnchor(431, 140.354, 43.328, 161.87f, 208.27f, "神威岬入口"),
        RouteAnchor(432, 140.3477, 43.3335, 161.47f, 207.79f, "神威岬駐車場"),
        RouteAnchor(435, 140.3495, 43.33, 161.58f, 208.1f, "神威岬尾根道"),
        RouteAnchor(436, 140.3486, 43.3304, 161.52f, 208.06f, "神威岬灯台"),
        RouteAnchor(438, 140.3462, 43.3338, 161.37f, 207.76f, "神威岬展望広場"),
        RouteAnchor(439, 140.3505, 43.331, 161.64f, 208.01f, "神威岬自然遊歩道"),
        RouteAnchor(444, 140.3793, 43.3275, 163.48f, 208.32f, "余別港"),
        RouteAnchor(451, 140.4614, 43.3459, 168.72f, 206.7f, "日司港"),
        RouteAnchor(452, 140.4814, 43.3731, 170.0f, 204.31f, "積丹出岬灯台"),
        RouteAnchor(455, 140.4766, 43.3731, 169.69f, 204.31f, "積丹岬自然歩道"),
        RouteAnchor(458, 140.4752, 43.3672, 169.61f, 204.83f, "入舸港"),
        RouteAnchor(460, 140.418, 43.3577, 165.95f, 205.67f, "幌武意港"),
        RouteAnchor(461, 140.527, 43.3594, 172.91f, 205.52f, "マッカ岬"),
        RouteAnchor(463, 140.5994, 43.2968, 177.53f, 211.01f, "美国港"),
        RouteAnchor(465, 140.6019, 43.3027, 177.69f, 210.49f, "黄金岬"),
        RouteAnchor(471, 140.6377, 43.2772, 179.98f, 212.73f, "古平港"),
        RouteAnchor(486, 140.7356, 43.2421, 186.23f, 215.81f, "ワッカケ岬"),
        RouteAnchor(495, 140.771, 43.187, 188.49f, 220.63f, "JR余市駅前"),
        RouteAnchor(498, 140.7883, 43.1997, 189.59f, 219.52f, "モイレ岬"),
        RouteAnchor(499, 140.7775, 43.2114, 188.9f, 218.5f, "余市港"),
        RouteAnchor(510, 140.8646, 43.2079, 194.47f, 218.8f, "忍路港"),
        RouteAnchor(511, 140.8592, 43.2118, 194.12f, 218.46f, "兜岬（小樽）"),
        RouteAnchor(526, 141.0161, 43.2383, 204.14f, 216.14f, "日和山灯台"),
        RouteAnchor(528, 141.0121, 43.2299, 203.88f, 216.88f, "祝津港"),
        RouteAnchor(541, 140.9937, 43.1977, 202.71f, 219.7f, "JR小樽駅前"),
        RouteAnchor(570, 141.28, 43.2, 220.99f, 219.5f, "石狩湾新港西埠頭"),
        RouteAnchor(571, 141.286, 43.204, 221.37f, 219.15f, "石狩湾新港中央埠頭"),
        RouteAnchor(574, 141.31, 43.215, 222.9f, 218.18f, "石狩湾新港東埠頭"),
        RouteAnchor(584, 141.3606, 43.2569, 226.13f, 214.51f, "石狩灯台"),
        RouteAnchor(613, 141.432, 43.399, 230.69f, 202.04f, "厚田港"),
        RouteAnchor(652, 141.388, 43.595, 227.88f, 184.79f, "川下港"),
        RouteAnchor(654, 141.3926, 43.6068, 228.17f, 183.75f, "浜益海浜公園"),
        RouteAnchor(659, 141.384, 43.602, 227.62f, 184.17f, "浜益港"),
        RouteAnchor(672, 141.365, 43.68, 226.41f, 177.29f, "幌港"),
        RouteAnchor(675, 141.342, 43.742, 224.94f, 171.82f, "雄冬港"),
        RouteAnchor(684, 141.353, 43.745, 225.65f, 171.55f, "雄冬岬展望台"),
        RouteAnchor(687, 141.351, 43.746, 225.52f, 171.46f, "雄冬岬灯台跡"),
        RouteAnchor(690, 141.3494, 43.7474, 225.42f, 171.34f, "雄冬岬（おふゆみさき）"),
        RouteAnchor(693, 141.476, 43.805, 233.5f, 166.25f, "岩尾港"),
        RouteAnchor(705, 141.512, 43.842, 235.8f, 162.97f, "別苅港"),
        RouteAnchor(708, 141.5249, 43.8561, 236.62f, 161.72f, "旧JR増毛駅前"),
        RouteAnchor(712, 141.534, 43.854, 237.2f, 161.91f, "増毛港"),
        RouteAnchor(716, 141.5275, 43.855, 236.79f, 161.82f, "増毛灯台"),
        RouteAnchor(744, 141.6296, 43.9461, 243.3f, 153.75f, "黄金岬（おうごんみさき）"),
        RouteAnchor(745, 141.631, 43.947, 243.39f, 153.67f, "黄金岬キャンプ場"),
        RouteAnchor(748, 141.638, 43.956, 243.84f, 152.87f, "留萌港"),
        RouteAnchor(751, 141.6369, 43.941, 243.77f, 154.2f, "JR留萌駅跡"),
        RouteAnchor(774, 141.635, 43.962, 243.65f, 152.34f, "留萌港北防波堤"),
        RouteAnchor(779, 141.63, 43.945, 243.33f, 153.85f, "黄金岬遊歩道"),
        RouteAnchor(781, 141.639, 43.958, 243.9f, 152.69f, "留萌港フェリーターミナル跡"),
        RouteAnchor(794, 141.642, 43.953, 244.1f, 153.14f, "留萌港展望の丘"),
        RouteAnchor(796, 141.6628, 44.0154, 245.42f, 147.6f, "小平港"),
        RouteAnchor(801, 141.6582, 44.1537, 245.13f, 135.31f, "鬼鹿港"),
        RouteAnchor(837, 141.6545, 43.9979, 244.89f, 149.15f, "臼谷港"),
        RouteAnchor(842, 141.6629, 44.2264, 245.43f, 128.84f, "力昼港"),
        RouteAnchor(846, 141.66, 44.24, 245.24f, 127.62f, "栄浜港（苫前）"),
        RouteAnchor(851, 141.6508, 44.3092, 244.66f, 121.45f, "苫前岬"),
        RouteAnchor(866, 141.8436, 44.145, 256.97f, 136.08f, "三毛別羆事件復元地"),
        RouteAnchor(873, 141.6525, 44.3167, 244.77f, 120.78f, "苫前港"),
        RouteAnchor(901, 141.698, 44.375, 247.67f, 115.58f, "羽幌港フェリー待合所"),
        RouteAnchor(902, 141.6983, 44.3756, 247.69f, 115.53f, "羽幌港"),
        RouteAnchor(917, 141.7377, 44.4128, 250.21f, 112.2f, "築別港"),
        RouteAnchor(943, 141.7676, 44.5332, 252.11f, 101.43f, "初山別港"),
        RouteAnchor(952, 141.776, 44.558, 252.65f, 99.21f, "豊岬（とよさき）"),
        RouteAnchor(953, 141.771, 44.559, 252.33f, 99.12f, "豊岬港"),
        RouteAnchor(954, 141.773, 44.561, 252.46f, 98.94f, "豊岬海岸"),
        RouteAnchor(955, 141.777, 44.562, 252.71f, 98.85f, "豊岬高台"),
        RouteAnchor(956, 141.778, 44.563, 252.78f, 98.76f, "豊岬神社"),
        RouteAnchor(959, 141.7739, 44.5631, 252.52f, 98.75f, "金比羅岬"),
        RouteAnchor(961, 141.7739, 44.5631, 252.52f, 98.75f, "初山別灯台"),
        RouteAnchor(962, 141.78, 44.564, 252.91f, 98.67f, "初山別温泉岬の湯"),
        RouteAnchor(966, 141.7761, 44.5639, 252.66f, 98.68f, "みさき台公園"),
        RouteAnchor(982, 141.7922, 44.7222, 253.68f, 84.48f, "遠別港"),
        RouteAnchor(1029, 141.7364, 44.8703, 250.12f, 71.15f, "天塩港"),
        RouteAnchor(1030, 141.732, 44.872, 249.84f, 71.0f, "天塩港防波堤"),
        RouteAnchor(1065, 141.65, 45.0, 244.61f, 59.46f, "北緯45度モニュメント"),
        RouteAnchor(1078, 141.6402, 45.0838, 243.98f, 51.88f, "稚咲内漁港"),
        RouteAnchor(1090, 141.64, 45.15, 243.97f, 45.89f, "サロベツ原野"),
        RouteAnchor(1101, 141.6205, 45.3082, 242.72f, 31.55f, "抜海岩"),
        RouteAnchor(1104, 141.6211, 45.3107, 242.76f, 31.33f, "抜海港"),
        RouteAnchor(1110, 141.65, 45.42, 244.61f, 21.4f, "ルエラン岬"),
        RouteAnchor(1116, 141.6528, 45.3875, 244.79f, 24.35f, "坂の下港"),
        RouteAnchor(1134, 141.6583, 45.3833, 245.14f, 24.73f, "西浜港"),
        RouteAnchor(1141, 141.646, 45.445, 244.35f, 19.12f, "ノシャップ岬手前海岸"),
        RouteAnchor(1143, 141.6433, 45.452, 244.18f, 18.48f, "ノシャップ漁港"),
        RouteAnchor(1152, 141.6445, 45.4528, 244.26f, 18.41f, "ノシャップ寒流水族館"),
        RouteAnchor(1155, 141.6452, 45.4495, 244.3f, 18.71f, "稚内灯台"),
        RouteAnchor(1159, 141.6433, 45.4542, 244.18f, 18.28f, "ノシャップ岬先端"),
        RouteAnchor(1160, 141.6435, 45.454, 244.19f, 18.3f, "ノシャップ岬碑"),
        RouteAnchor(1161, 141.6433, 45.4542, 244.18f, 18.28f, "ノシャップ岬（野寒布岬）"),
        RouteAnchor(1162, 141.645, 45.452, 244.29f, 18.48f, "ノシャップ岬東アプローチ"),
        RouteAnchor(1165, 141.6708, 45.4028, 245.93f, 22.96f, "恵比須港"),
        RouteAnchor(1168, 141.6785, 45.419, 246.43f, 21.49f, "稚内港北防波堤ドーム"),
        RouteAnchor(1169, 141.679, 45.418, 246.46f, 21.58f, "稚内港フェリーターミナル"),
        RouteAnchor(1170, 141.6806, 45.4119, 246.56f, 22.13f, "稚内港中央埠頭"),
        RouteAnchor(1171, 141.677, 45.4168, 246.33f, 21.69f, "JR日本最北端の駅・稚内駅"),
        RouteAnchor(1196, 141.802, 45.405, 254.31f, 22.76f, "稚内空港"),
        RouteAnchor(1200, 141.874, 45.452, 258.91f, 18.48f, "富磯漁港"),
        RouteAnchor(1201, 141.8748, 45.4528, 258.96f, 18.41f, "富磯港"),
        RouteAnchor(1206, 141.878, 45.4634, 259.16f, 17.45f, "宗谷岬入口"),
        RouteAnchor(1207, 141.9486, 45.52, 263.67f, 12.29f, "宗谷港"),
        RouteAnchor(1214, 141.934, 45.519, 262.74f, 12.38f, "宗谷岬郵便局"),
        RouteAnchor(1215, 141.9355, 45.521, 262.83f, 12.2f, "宗谷岬展望台"),
        RouteAnchor(1216, 141.936, 45.522, 262.87f, 12.11f, "宗谷岬音楽碑"),
        RouteAnchor(1217, 141.9364, 45.5214, 262.89f, 12.16f, "宗谷岬灯台"),
        RouteAnchor(1218, 141.9366, 45.5228, 262.9f, 12.04f, "日本最北端の地の碑（宗谷岬）"),
        RouteAnchor(1222, 142.12, 45.36, 274.61f, 26.85f, "知来港"),
        RouteAnchor(1227, 142.1603, 45.3427, 277.19f, 28.42f, "浜鬼志別港"),
        RouteAnchor(1247, 142.2292, 45.276, 281.58f, 34.48f, "浜猿払港"),
        RouteAnchor(1253, 142.185, 45.312, 278.76f, 31.21f, "エサヌカ線入口"),
        RouteAnchor(1260, 142.248, 45.245, 282.78f, 37.29f, "エサヌカ線中央"),
        RouteAnchor(1267, 142.3061, 45.1954, 286.49f, 41.78f, "エサヌカ線出口"),
        RouteAnchor(1294, 142.146, 45.334, 276.27f, 29.21f, "クッチャロ湖入口"),
        RouteAnchor(1306, 142.394, 45.1305, 292.1f, 47.66f, "浜頓別港"),
        RouteAnchor(1325, 142.4523, 45.0742, 295.83f, 52.75f, "斜内港"),
        RouteAnchor(1330, 142.495, 45.056, 298.55f, 54.4f, "神威岬旧道跡"),
        RouteAnchor(1331, 142.497, 45.057, 298.68f, 54.31f, "神威岬覆道"),
        RouteAnchor(1332, 142.499, 45.058, 298.81f, 54.22f, "神威岬公園"),
        RouteAnchor(1333, 142.5014, 45.0589, 298.96f, 54.13f, "北見神威岬"),
        RouteAnchor(1334, 142.5014, 45.0589, 298.96f, 54.13f, "神威岬灯台"),
        RouteAnchor(1335, 142.502, 45.0595, 299.0f, 54.08f, "神威岬灯台下遊歩道"),
        RouteAnchor(1336, 142.498, 45.0575, 298.74f, 54.26f, "神威岬展望パーキング"),
        RouteAnchor(1347, 142.54, 45.0, 301.43f, 59.46f, "山臼港"),
        RouteAnchor(1350, 142.56, 44.98, 302.7f, 61.26f, "目梨泊港"),
        RouteAnchor(1355, 142.5823, 44.9589, 304.13f, 63.16f, "ウスタイベ岬"),
        RouteAnchor(1358, 142.585, 44.937, 304.3f, 65.14f, "枝幸灯台"),
        RouteAnchor(1364, 142.5837, 44.9368, 304.22f, 65.16f, "枝幸港"),
        RouteAnchor(1393, 142.7211, 44.7788, 312.99f, 79.39f, "乙忠部港"),
        RouteAnchor(1399, 142.78, 44.72, 316.75f, 84.67f, "音標港"),
        RouteAnchor(1406, 142.8181, 44.7081, 319.18f, 85.74f, "音標岬"),
        RouteAnchor(1411, 142.88, 44.65, 323.13f, 90.96f, "幌内港"),
        RouteAnchor(1434, 142.963, 44.5832, 328.43f, 96.95f, "雄武港（おうむこう）"),
        RouteAnchor(1452, 143.04, 44.54, 333.35f, 100.82f, "沢木港"),
        RouteAnchor(1459, 143.062, 44.5321, 334.75f, 101.53f, "日の出岬"),
        RouteAnchor(1461, 143.06, 44.531, 334.62f, 101.63f, "ホテル日の出岬"),
        RouteAnchor(1462, 143.061, 44.5315, 334.69f, 101.58f, "日の出岬キャンプ場"),
        RouteAnchor(1463, 143.0618, 44.532, 334.74f, 101.54f, "日の出岬遊歩道"),
        RouteAnchor(1464, 143.0622, 44.5322, 334.76f, 101.52f, "日の出岬モニュメント"),
        RouteAnchor(1465, 143.0625, 44.5325, 334.78f, 101.49f, "日の出岬遠望台"),
        RouteAnchor(1483, 143.14, 44.47, 339.73f, 107.09f, "興部港"),
        RouteAnchor(1494, 143.2261, 44.4324, 345.23f, 110.45f, "沙留港"),
        RouteAnchor(1499, 143.223, 44.4413, 345.03f, 109.65f, "沙留岬"),
        RouteAnchor(1515, 143.355, 44.354, 353.46f, 117.46f, "オホーツク流氷公園"),
        RouteAnchor(1526, 143.3656, 44.3291, 354.13f, 119.68f, "紋別港第3埠頭"),
        RouteAnchor(1527, 143.3687, 44.3508, 354.33f, 117.74f, "紋別港赤灯台プロムナード"),
        RouteAnchor(1529, 143.3687, 44.3508, 354.33f, 117.74f, "紋別本港"),
        RouteAnchor(1553, 143.404, 44.304, 356.59f, 121.92f, "オホーツク紋別空港"),
        RouteAnchor(1586, 143.7374, 44.1887, 377.87f, 132.19f, "登栄床港"),
        RouteAnchor(1597, 143.6189, 44.2296, 370.3f, 128.55f, "湧別港"),
        RouteAnchor(1628, 143.6918, 44.1507, 374.96f, 135.58f, "芭露港"),
        RouteAnchor(1641, 143.8068, 44.091, 382.3f, 140.88f, "富武士港"),
        RouteAnchor(1648, 143.9118, 44.1083, 389.0f, 139.35f, "キムアネップ岬"),
        RouteAnchor(1656, 143.85, 44.08, 385.06f, 141.86f, "浜佐呂間港"),
        RouteAnchor(1669, 144.0707, 44.1211, 399.15f, 138.21f, "常呂港防波堤"),
        RouteAnchor(1670, 144.0707, 44.1211, 399.15f, 138.21f, "常呂港"),
        RouteAnchor(1694, 144.1187, 44.0939, 402.21f, 140.63f, "能取漁港"),
        RouteAnchor(1695, 144.24, 44.095, 409.96f, 140.53f, "美岬（みさき）アプローチ"),
        RouteAnchor(1696, 144.242, 44.105, 410.08f, 139.64f, "美岬牧場"),
        RouteAnchor(1697, 144.2431, 44.1122, 410.15f, 139.0f, "能取岬（のとろみさき）"),
        RouteAnchor(1698, 144.2431, 44.1122, 410.15f, 139.0f, "能取岬灯台"),
        RouteAnchor(1716, 144.2588, 44.0202, 411.16f, 147.17f, "JR網走駅前"),
        RouteAnchor(1727, 144.275, 44.024, 412.19f, 146.84f, "網走港本港地区"),
        RouteAnchor(1730, 144.295, 44.025, 413.47f, 146.75f, "網走港赤灯台"),
        RouteAnchor(1765, 144.5022, 43.9135, 426.7f, 156.64f, "小清水原生花園"),
        RouteAnchor(1816, 144.6628, 43.9122, 436.95f, 156.75f, "斜里港"),
        RouteAnchor(1854, 144.9833, 44.0625, 457.41f, 143.42f, "チャシコツ岬"),
        RouteAnchor(1860, 144.996, 44.068, 458.22f, 142.93f, "ウトロ港（知床観光船乗り場）"),
        RouteAnchor(1865, 144.985, 44.08, 457.52f, 141.86f, "ウトロ灯台"),
        RouteAnchor(1867, 145.005, 44.09, 458.8f, 140.97f, "プユニ岬アプローチ"),
        RouteAnchor(1868, 145.0093, 44.0951, 459.07f, 140.52f, "プユニ岬"),
        RouteAnchor(1875, 145.0805, 44.1222, 463.62f, 138.11f, "知床五湖フィールドハウス"),
        RouteAnchor(1909, 145.11, 44.05, 465.5f, 144.53f, "知床峠（標高738m）"),
        RouteAnchor(1941, 145.191, 44.019, 470.67f, 147.28f, "羅臼港本港地区"),
        RouteAnchor(1943, 145.195, 44.015, 470.93f, 147.64f, "羅臼港南防波堤"),
        RouteAnchor(1948, 145.2, 44.03, 471.25f, 146.3f, "知床岬遠望コース"),
        RouteAnchor(1950, 145.1623, 43.9897, 468.84f, 149.88f, "松法漁港"),
        RouteAnchor(1981, 145.3174, 44.1868, 478.74f, 132.36f, "相泊漁港"),
        RouteAnchor(1982, 145.3174, 44.1868, 478.74f, 132.36f, "相泊港船揚場"),
        RouteAnchor(1985, 145.2871, 44.1525, 476.81f, 135.42f, "相泊温泉（日本最北東端の温泉）"),
        RouteAnchor(1987, 145.29, 44.16, 476.99f, 134.75f, "知床岬航路案内所"),
        RouteAnchor(1990, 145.305, 44.175, 477.95f, 133.41f, "知床岬遠望岩礁"),
        RouteAnchor(1999, 145.3174, 44.1868, 478.74f, 132.36f, "相泊（最果ての終着点・相泊温泉）"),
    )

    val routeNodes: List<SugorokuNode>
        get() = nodes

    val startNode: SugorokuNode by lazy { nodes[0] }
    val goalNode: SugorokuNode by lazy { nodes[1999] }

    fun getNodeByIndex(index: Int): SugorokuNode? = if (index in 0 until totalNodes) nodes[index] else null
    fun getNodeByStepIndex(stepIndex: Int): SugorokuNode? = if (stepIndex in 0 until totalNodes) nodes[stepIndex] else null
    fun getNodeById(id: String): SugorokuNode? = nodes.firstOrNull { it.id == id }

    /**
     * 実行時にアンカー間を直線補間して全2,000ノードの座標を動的算出。
     */
    val nodes: List<SugorokuNode> by lazy {
        computeInterpolatedNodes()
    }

    private fun computeInterpolatedNodes(): List<SugorokuNode> {
        val result = ArrayList<SugorokuNode>(totalNodes)
        val metas = rawNodeMetas()
        for (i in 0 until anchors.size - 1) {
            val a1 = anchors[i]
            val a2 = anchors[i + 1]
            val sStart = a1.stepIndex
            val sEnd = a2.stepIndex
            val count = sEnd - sStart
            for (stepOffset in 0 until count) {
                val step = sStart + stepOffset
                val t = stepOffset.toFloat() / count.toFloat()
                val x = a1.x + t * (a2.x - a1.x)
                val y = a1.y + t * (a2.y - a1.y)
                val meta = metas[step]
                result.add(
                    SugorokuNode(
                        id = meta.id,
                        stepIndex = step,
                        name = meta.name,
                        municipality = meta.municipality,
                        type = meta.type,
                        x = x,
                        y = y,
                        description = meta.description,
                        roadName = meta.roadName,
                        isAnchor = (stepOffset == 0),
                    )
                )
            }
        }
        val lastAnchor = anchors.last()
        val lastMeta = metas[lastAnchor.stepIndex]
        result.add(
            SugorokuNode(
                id = lastMeta.id,
                stepIndex = lastAnchor.stepIndex,
                name = lastMeta.name,
                municipality = lastMeta.municipality,
                type = lastMeta.type,
                x = lastAnchor.x,
                y = lastAnchor.y,
                description = lastMeta.description,
                roadName = lastMeta.roadName,
                isAnchor = true,
            )
        )
        return result
    }

    private fun rawNodeMetas(): List<SugorokuNodeMeta> {
        val anchorMap = anchors.associateBy { it.stepIndex }
        return List(totalNodes) { step ->
            val anchor = anchorMap[step]
            if (anchor != null) {
                val nodeType = when {
                    step == 0 -> SugorokuNodeType.START_HAKODATE
                    step == maxStepIndex -> SugorokuNodeType.GOAL_AIDOMARI
                    anchor.name.contains("岬") || anchor.name.contains("灯台") || anchor.name.contains("碑") || anchor.name.contains("公園") || anchor.name.contains("展望") -> SugorokuNodeType.SCENIC_SPOT
                    anchor.name.contains("駅") || anchor.name.contains("通") || anchor.name.contains("町") -> SugorokuNodeType.TOWN_CENTER
                    else -> SugorokuNodeType.NORMAL
                }
                SugorokuNodeMeta(
                    id = String.format("node_%04d", step),
                    stepIndex = step,
                    name = anchor.name,
                    municipality = "",
                    type = nodeType,
                    description = "",
                    roadName = "",
                )
            } else {
                SugorokuNodeMeta(
                    id = String.format("node_%04d", step),
                    stepIndex = step,
                    name = "",
                    municipality = "",
                    type = SugorokuNodeType.NORMAL,
                    description = "",
                    roadName = "",
                )
            }
        }
    }
}
