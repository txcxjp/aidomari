package com.aidomari.app.domain.model

import com.aidomari.app.R
import kotlin.math.abs
import kotlin.math.hypot

enum class RoadsideLandmarkType(
    val defaultDialogue: String,
    val drawableResId: Int,
    val baseDisplayWidthDp: Float,
    val baseDisplayHeightDp: Float,
) {
    DAIRY_COW(
        defaultDialogue = "「のんびり草を食べてる！」",
        drawableResId = R.drawable.landmark_dairy_cow,
        baseDisplayWidthDp = 185f,
        baseDisplayHeightDp = 75f,
    ),
    CAPE_LIGHTHOUSE(
        defaultDialogue = "「青い海と白い灯台が見える！」",
        drawableResId = R.drawable.landmark_cape_lighthouse,
        baseDisplayWidthDp = 76f,
        baseDisplayHeightDp = 106f,
    ),
    LOG_CABIN(
        defaultDialogue = "「煙突からあたたかい煙が…」",
        drawableResId = R.drawable.landmark_log_cabin,
        baseDisplayWidthDp = 135f,
        baseDisplayHeightDp = 98f,
    ),
    COASTAL_WINDMILL(
        defaultDialogue = "「風車がぐるぐる回ってる！」",
        drawableResId = R.drawable.landmark_coastal_windmill,
        baseDisplayWidthDp = 160f,
        baseDisplayHeightDp = 91f,
    ),
    BIRCH_TREES(
        defaultDialogue = "「白樺の木陰がすずしい！」",
        drawableResId = R.drawable.landmark_birch_trees,
        baseDisplayWidthDp = 110f,
        baseDisplayHeightDp = 76f,
    ),
    RED_BRICK_WAREHOUSE(
        defaultDialogue = "「歴史ある赤レンガ倉庫だ！」",
        drawableResId = R.drawable.landmark_red_brick_warehouse,
        baseDisplayWidthDp = 180f,
        baseDisplayHeightDp = 70f,
    ),
    RETRO_GAS_LAMP(
        defaultDialogue = "「レトロな灯りがやさしい…」",
        drawableResId = R.drawable.landmark_retro_gas_lamp,
        baseDisplayWidthDp = 24f,
        baseDisplayHeightDp = 32f,
    ),
    WESTERN_HOUSE(
        defaultDialogue = "「異国情緒のある洋館だね！」",
        drawableResId = R.drawable.landmark_western_house,
        baseDisplayWidthDp = 160f,
        baseDisplayHeightDp = 76f,
    ),
    FISHING_BOAT(
        defaultDialogue = "「波にゆられて気持ちよさそう！」",
        drawableResId = R.drawable.landmark_fishing_boat,
        baseDisplayWidthDp = 72f,
        baseDisplayHeightDp = 56f,
    ),
    FOREST_GROVE(
        defaultDialogue = "「深い森から鳥の声が聞こえる！」",
        drawableResId = R.drawable.landmark_forest_grove,
        baseDisplayWidthDp = 135f,
        baseDisplayHeightDp = 132f,
    ),
    CITY_TRAM(
        defaultDialogue = "「レトロな路面電車がゴトゴト走ってる！」",
        drawableResId = R.drawable.landmark_city_tram,
        baseDisplayWidthDp = 116f,
        baseDisplayHeightDp = 76f,
    ),
    GORYOKAKU_CHERRY(
        defaultDialogue = "「星形の五稜郭に桜が満開だ！」",
        drawableResId = R.drawable.landmark_goryokaku_cherry,
        baseDisplayWidthDp = 152f,
        baseDisplayHeightDp = 88f,
    ),
    ROTENBURO_ONSEN(
        defaultDialogue = "「湯気の立つ露天風呂、気持ちよさそう〜！」",
        drawableResId = R.drawable.landmark_rotenburo_onsen,
        baseDisplayWidthDp = 112f,
        baseDisplayHeightDp = 72f,
    ),
    MORNING_MARKET(
        defaultDialogue = "「朝市にカニやイカがいっぱい並んでる！」",
        drawableResId = R.drawable.landmark_morning_market,
        baseDisplayWidthDp = 136f,
        baseDisplayHeightDp = 74f,
    ),
    APPLE_ORCHARD(
        defaultDialogue = "「真っ赤なりんごがたわわに実ってる！」",
        drawableResId = R.drawable.landmark_apple_orchard,
        baseDisplayWidthDp = 128f,
        baseDisplayHeightDp = 77f,
    ),
    TOWN_SHOPS(
        defaultDialogue = "「かわいいお店やポストが並ぶ街角だね！」",
        drawableResId = R.drawable.landmark_town_shops,
        baseDisplayWidthDp = 155f,
        baseDisplayHeightDp = 60f,
    ),
    RED_BRIDGE_RIVER(
        defaultDialogue = "「赤い橋の下をお魚が泳いでるよ！」",
        drawableResId = R.drawable.landmark_red_bridge_river,
        baseDisplayWidthDp = 126f,
        baseDisplayHeightDp = 76f,
    ),
    FERRY_SHIP(
        defaultDialogue = "「大きな連絡船が港に停まってる！」",
        drawableResId = R.drawable.landmark_ferry_ship,
        baseDisplayWidthDp = 132f,
        baseDisplayHeightDp = 76f,
    ),
    FOX_AND_HAY(
        defaultDialogue = "「牧草ロールの上でキタキツネがお昼寝してる！」",
        drawableResId = R.drawable.landmark_fox_and_hay,
        baseDisplayWidthDp = 135f,
        baseDisplayHeightDp = 58f,
    ),
    GRAZING_HORSES(
        defaultDialogue = "「お馬さんの親子が仲良く走ってる！」",
        drawableResId = R.drawable.landmark_grazing_horses,
        baseDisplayWidthDp = 126f,
        baseDisplayHeightDp = 83f,
    ),
    MOUNTAIN_ROPEWAY(
        defaultDialogue = "「ロープウェイで山頂展望台へ登れるんだね！」",
        drawableResId = R.drawable.landmark_mountain_ropeway,
        baseDisplayWidthDp = 128f,
        baseDisplayHeightDp = 78f,
    ),
    CHERRY_ROW(
        defaultDialogue = "「桜並木の花びらがひらひら舞ってる！」",
        drawableResId = R.drawable.landmark_cherry_row,
        baseDisplayWidthDp = 150f,
        baseDisplayHeightDp = 66f,
    ),
}

/**
 * Roadside scenery panorama or vignette placed along the Hokkaido Sugoroku route.
 * Rendered purely as storybook illustration without artificial text labels or token shadows.
 *
 * @param id Unique identifier for debugging/tracking.
 * @param name Descriptive label (used for debugging/testing, never shown as on-canvas label).
 * @param x World SVG X coordinate (matches HokkaidoAct1Route coordinates).
 * @param y World SVG Y coordinate (matches HokkaidoAct1Route coordinates).
 * @param type Landmark category (each sized to natural panoramic or vignette proportions).
 * @param scale Individual scaling factor for natural environmental variety.
 * @param flipX Mirrors the panorama horizontally so nearby hills/woodlands look distinct.
 * @param reactionDialogue Custom speech dialogue when player companion passes near.
 */
data class RoadsideLandmark(
    val id: String,
    val name: String = "",
    val x: Float,
    val y: Float,
    val type: RoadsideLandmarkType,
    val scale: Float = 1.0f,
    val flipX: Boolean = false,
    val reactionDialogue: String = type.defaultDialogue,
) {
    companion object {
        private val CURATED_LANDMARKS: List<RoadsideLandmark> = listOf(
            // =========================================================================
            // 1. HAKODATE & MT. HAKODATE (函館山・元町・ベイエリア・函館駅)
            // =========================================================================
            RoadsideLandmark(
                id = "hako_mountain_ropeway",
                name = "函館山山頂展望台とロープウェイ",
                x = 183.92f,
                y = 344.12f,
                type = RoadsideLandmarkType.MOUNTAIN_ROPEWAY,
                scale = 0.96f,
            ),
            RoadsideLandmark(
                id = "hako_mountain_woods_main",
                name = "函館山深い森",
                x = 183.82f,
                y = 344.48f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.15f,
            ),
            RoadsideLandmark(
                id = "hako_mountain_woods_west",
                name = "函館山西斜面森林",
                x = 183.45f,
                y = 344.72f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 0.96f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "hako_mountain_birch",
                name = "函館山南斜面白樺",
                x = 184.16f,
                y = 344.86f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 0.92f,
            ),
            RoadsideLandmark(
                id = "hako_cape_lighthouse",
                name = "立待・穴澗岬灯台",
                x = 183.75f,
                y = 343.72f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                scale = 0.88f,
            ),
            RoadsideLandmark(
                id = "yachigashira_onsen_bath",
                name = "谷地頭露天風呂",
                x = 184.58f,
                y = 345.05f,
                type = RoadsideLandmarkType.ROTENBURO_ONSEN,
                scale = 0.90f,
                reactionDialogue = "「湯気の立つ谷地頭温泉、いい気持ち〜！」",
            ),
            RoadsideLandmark(
                id = "motomachi_slope_town",
                name = "元町歴史的洋館・教会・坂道街並み",
                x = 184.56f,
                y = 344.04f,
                type = RoadsideLandmarkType.WESTERN_HOUSE,
                scale = 0.98f,
                reactionDialogue = "「洋館と教会が並ぶすてきな坂道だね！」",
            ),
            RoadsideLandmark(
                id = "hakodate_city_tram",
                name = "函館市電",
                x = 184.90f,
                y = 344.36f,
                type = RoadsideLandmarkType.CITY_TRAM,
                scale = 0.85f,
                reactionDialogue = "「レトロな函館市電がゴトゴト走ってる！」",
            ),
            RoadsideLandmark(
                id = "hakodate_town_shops",
                name = "西部地区の商店街",
                x = 184.12f,
                y = 343.32f,
                type = RoadsideLandmarkType.TOWN_SHOPS,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "kanemori_warehouse_row",
                name = "金森赤レンガ倉庫と運河",
                x = 184.92f,
                y = 343.38f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 0.96f,
                reactionDialogue = "「運河沿いに赤レンガ倉庫が並んでる！」",
            ),
            RoadsideLandmark(
                id = "hakodate_morning_market",
                name = "函館朝市の屋台",
                x = 185.34f,
                y = 343.24f,
                type = RoadsideLandmarkType.MORNING_MARKET,
                scale = 0.90f,
                reactionDialogue = "「カニにイカにメロン！朝市は活気があるなぁ！」",
            ),
            RoadsideLandmark(
                id = "hako_bay_ferry_mashumaru",
                name = "青函連絡船摩周丸",
                x = 185.14f,
                y = 342.74f,
                type = RoadsideLandmarkType.FERRY_SHIP,
                scale = 0.95f,
                reactionDialogue = "「大きな連絡船、摩周丸が見える！」",
            ),
            RoadsideLandmark(
                id = "hako_bay_boat_1",
                name = "函館湾の漁船",
                x = 184.74f,
                y = 342.84f,
                type = RoadsideLandmarkType.FISHING_BOAT,
                scale = 0.95f,
                reactionDialogue = "「港に漁船が浮かんでる！」",
            ),

            // =========================================================================
            // 2. GORYOKAKU & KIKYO (五稜郭公園・湯の川/富岡・桜並木・桔梗)
            // =========================================================================
            RoadsideLandmark(
                id = "onari_rotenburo_onsen",
                name = "桜成湯けむり露天風呂",
                x = 186.02f,
                y = 342.22f,
                type = RoadsideLandmarkType.ROTENBURO_ONSEN,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "chiyogadai_birch",
                name = "千代台白樺",
                x = 186.58f,
                y = 342.32f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "showabashi_red_bridge",
                name = "昭和橋と清流",
                x = 187.15f,
                y = 342.55f,
                type = RoadsideLandmarkType.RED_BRIDGE_RIVER,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "hakodate_east_western",
                name = "千代台通りの洋館街",
                x = 187.58f,
                y = 342.82f,
                type = RoadsideLandmarkType.WESTERN_HOUSE,
                scale = 0.95f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "horikawacho_town_shops",
                name = "五稜郭商店街",
                x = 186.08f,
                y = 341.62f,
                type = RoadsideLandmarkType.TOWN_SHOPS,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "goryokaku_city_tram",
                name = "五稜郭公園前の市電",
                x = 186.72f,
                y = 341.88f,
                type = RoadsideLandmarkType.CITY_TRAM,
                scale = 0.90f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_east_shops",
                name = "中央通りの商店街",
                x = 187.74f,
                y = 342.15f,
                type = RoadsideLandmarkType.TOWN_SHOPS,
                scale = 0.95f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_east_cherry_row",
                name = "五稜郭外周桜並木",
                x = 187.86f,
                y = 341.58f,
                type = RoadsideLandmarkType.CHERRY_ROW,
                scale = 0.95f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_star_moat_cherry",
                name = "五稜郭の堀と満開の桜",
                x = 187.10f,
                y = 341.12f,
                type = RoadsideLandmarkType.GORYOKAKU_CHERRY,
                scale = 1.15f,
                reactionDialogue = "「五稜郭の石垣とお堀に桜がいっぱい咲いてる！」",
            ),
            RoadsideLandmark(
                id = "goryokaku_park_woods",
                name = "五稜郭公園の森",
                x = 187.82f,
                y = 340.76f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.08f,
            ),
            RoadsideLandmark(
                id = "goryokaku_west_cherry_row",
                name = "函館公園桜通り",
                x = 186.25f,
                y = 341.05f,
                type = RoadsideLandmarkType.CHERRY_ROW,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "tomioka_apple_orchard",
                name = "富岡果樹園",
                x = 186.08f,
                y = 340.45f,
                type = RoadsideLandmarkType.APPLE_ORCHARD,
                scale = 0.92f,
            ),
            RoadsideLandmark(
                id = "tomioka_rotenburo",
                name = "富岡・昭和温泉",
                x = 186.25f,
                y = 339.92f,
                type = RoadsideLandmarkType.ROTENBURO_ONSEN,
                scale = 0.92f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "ishikawa_red_bridge",
                name = "石川3号橋",
                x = 186.78f,
                y = 337.90f,
                type = RoadsideLandmarkType.RED_BRIDGE_RIVER,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "kikyo_country_hamlet",
                name = "桔梗の田園集落",
                x = 185.75f,
                y = 337.20f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.05f,
            ),
            RoadsideLandmark(
                id = "nanae_apple_onsen_orchard",
                name = "七飯りんご果樹園",
                x = 184.10f,
                y = 335.60f,
                type = RoadsideLandmarkType.APPLE_ORCHARD,
                scale = 1.05f,
            ),

            // =========================================================================
            // 3. NANAE DAIRY RANCH & FARMLAND (七飯大牧場・キツネ・お馬・りんご園・温泉)
            // =========================================================================
            RoadsideLandmark(
                id = "nanae_cow_herd_1",
                name = "七飯大牧場",
                x = 182.18f,
                y = 331.38f,
                type = RoadsideLandmarkType.DAIRY_COW,
                scale = 1.15f,
                reactionDialogue = "「のんびり草を食べてる！」",
            ),
            RoadsideLandmark(
                id = "nanae_fox_hay_bales",
                name = "七飯牧草ロールとキタキツネ",
                x = 182.38f,
                y = 331.88f,
                type = RoadsideLandmarkType.FOX_AND_HAY,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "nanae_grazing_horses",
                name = "七飯のお馬さん親子",
                x = 182.05f,
                y = 330.80f,
                type = RoadsideLandmarkType.GRAZING_HORSES,
                scale = 0.98f,
            ),
            RoadsideLandmark(
                id = "nanae_country_hamlet",
                name = "七飯街道の集落",
                x = 182.88f,
                y = 330.88f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.00f,
            ),
            RoadsideLandmark(
                id = "nanae_orchard",
                name = "ななえりんご園",
                x = 183.06f,
                y = 331.50f,
                type = RoadsideLandmarkType.APPLE_ORCHARD,
                scale = 1.00f,
            ),
            RoadsideLandmark(
                id = "nanae_yuhi_onsen",
                name = "ななえ天然温泉露天風呂",
                x = 182.92f,
                y = 332.08f,
                type = RoadsideLandmarkType.ROTENBURO_ONSEN,
                scale = 0.92f,
            ),
            RoadsideLandmark(
                id = "nanae_birch_left",
                name = "国道5号白樺並木（西側）",
                x = 182.45f,
                y = 330.58f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 0.88f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "nanae_windbreak_woods",
                name = "牧場の防風林",
                x = 181.72f,
                y = 332.08f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.02f,
            ),

            // =========================================================================
            // 4. NIYAMA, ONUMA, SHAKOTAN & OTARU (大沼・積丹・小樽運河)
            // =========================================================================
            RoadsideLandmark(
                id = "niyama_cabin",
                name = "仁山高原の山小屋集落",
                x = 178.55f,
                y = 329.20f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.10f,
            ),
            RoadsideLandmark(
                id = "onuma_woods_1",
                name = "大沼湖畔の深い森",
                x = 180.35f,
                y = 325.30f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.25f,
            ),
            RoadsideLandmark(
                id = "onuma_boat",
                name = "大沼の遊覧ボート",
                x = 180.95f,
                y = 324.35f,
                type = RoadsideLandmarkType.FISHING_BOAT,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "kamui_cape_lighthouse",
                name = "神威岬灯台",
                x = 160.80f,
                y = 207.50f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                scale = 1.15f,
            ),
            RoadsideLandmark(
                id = "yoichi_apple_cabin",
                name = "余市のりんご果樹園",
                x = 191.05f,
                y = 221.20f,
                type = RoadsideLandmarkType.APPLE_ORCHARD,
                scale = 1.08f,
            ),
            RoadsideLandmark(
                id = "otaru_warehouse_1",
                name = "小樽運河と赤レンガ倉庫街",
                x = 203.90f,
                y = 221.50f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 1.12f,
                reactionDialogue = "「小樽運河沿いの倉庫群が見えた！」",
            ),
            RoadsideLandmark(
                id = "otaru_canal_boat",
                name = "小樽港の漁船",
                x = 204.10f,
                y = 220.85f,
                type = RoadsideLandmarkType.FISHING_BOAT,
                scale = 0.95f,
            ),

            // =========================================================================
            // 5. ORORON LINE, SAROBETSU & CAPE SOYA (オロロンライン・サロベツ・宗谷岬)
            // =========================================================================
            RoadsideLandmark(
                id = "ishikari_wind_1",
                name = "石狩海岸の風車群",
                x = 227.95f,
                y = 210.20f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                scale = 1.10f,
            ),
            RoadsideLandmark(
                id = "tomamae_wind_1",
                name = "苫前オトンルイ風力発電群",
                x = 247.00f,
                y = 125.10f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                scale = 1.20f,
            ),
            RoadsideLandmark(
                id = "sarobetsu_birch_1",
                name = "サロベツ原野の白樺",
                x = 251.50f,
                y = 54.00f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 1.15f,
            ),
            RoadsideLandmark(
                id = "teshio_soya_cow_1",
                name = "宗谷の放牧牧場",
                x = 251.20f,
                y = 70.40f,
                type = RoadsideLandmarkType.DAIRY_COW,
                scale = 1.15f,
            ),
            RoadsideLandmark(
                id = "soya_cape_lighthouse",
                name = "宗谷岬灯台",
                x = 263.20f,
                y = 13.50f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                scale = 1.20f,
            ),
            RoadsideLandmark(
                id = "soya_fishing_boat",
                name = "宗谷岬沖の漁船",
                x = 263.90f,
                y = 12.80f,
                type = RoadsideLandmarkType.FISHING_BOAT,
                scale = 1.05f,
            ),
        )

        /**
         * Complete Act 1 scenery list combining curated scenes around key areas and deterministic
         * anchor-linked contextual scenes along Steps 35..lastIndex so all 700km remain lively.
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> by lazy {
            buildAct1Landmarks()
        }

        private fun buildAct1Landmarks(): List<RoadsideLandmark> {
            val result = CURATED_LANDMARKS.toMutableList()
            val nodes = HokkaidoAct1Route.nodes
            if (nodes.size <= 35) return result

            val fallbackTypes = arrayOf(
                RoadsideLandmarkType.BIRCH_TREES,
                RoadsideLandmarkType.FOREST_GROVE,
                RoadsideLandmarkType.FOX_AND_HAY,
                RoadsideLandmarkType.GRAZING_HORSES,
            )
            val townTypes = arrayOf(
                RoadsideLandmarkType.TOWN_SHOPS,
                RoadsideLandmarkType.LOG_CABIN,
                RoadsideLandmarkType.APPLE_ORCHARD,
            )
            val ranchTypes = arrayOf(
                RoadsideLandmarkType.DAIRY_COW,
                RoadsideLandmarkType.GRAZING_HORSES,
                RoadsideLandmarkType.FOX_AND_HAY,
            )
            val parkTypes = arrayOf(
                RoadsideLandmarkType.CHERRY_ROW,
                RoadsideLandmarkType.GORYOKAKU_CHERRY,
            )

            // Center of Hokkaido island in SVG space used to point roadside offsets safely inland
            val inlandCenterX = 295f
            val inlandCenterY = 200f
            val minSpacingSq = 0.52f * 0.52f

            for (i in 35 until nodes.size) {
                val node = nodes[i]
                val prev = nodes[i - 1]
                val next = nodes[(i + 1).coerceAtMost(nodes.lastIndex)]
                val dx = next.x - prev.x
                val dy = next.y - prev.y
                val len = hypot(dx, dy).coerceAtLeast(0.001f)
                val tx = dx / len
                val ty = dy / len

                // Candidate perpendicular normal
                var nx = -ty
                var ny = tx
                val toCenterX = inlandCenterX - node.x
                val toCenterY = inlandCenterY - node.y
                val dotInward = nx * toCenterX + ny * toCenterY

                val nName = node.name
                val type = when {
                    nName.contains("温泉") || nName.contains("湯") -> RoadsideLandmarkType.ROTENBURO_ONSEN
                    nName.contains("橋") || nName.contains("川") -> RoadsideLandmarkType.RED_BRIDGE_RIVER
                    nName.contains("牧場") -> ranchTypes[i % ranchTypes.size]
                    nName.contains("港") || nName.contains("漁") -> {
                        if (i % 2 == 0) RoadsideLandmarkType.MORNING_MARKET else RoadsideLandmarkType.FISHING_BOAT
                    }
                    nName.contains("灯台") || nName.contains("岬") -> RoadsideLandmarkType.CAPE_LIGHTHOUSE
                    nName.contains("風") -> RoadsideLandmarkType.COASTAL_WINDMILL
                    nName.contains("桜") || nName.contains("公園") || nName.contains("城") -> parkTypes[i % parkTypes.size]
                    nName.contains("山") || nName.contains("展望") -> RoadsideLandmarkType.MOUNTAIN_ROPEWAY
                    nName.contains("駅") || nName.contains("町") -> townTypes[i % townTypes.size]
                    else -> fallbackTypes[i % fallbackTypes.size]
                }

                // Point harbor boats seaward; keep inland scenes pointed toward interior land
                val wantsSeaward = type == RoadsideLandmarkType.FISHING_BOAT || type == RoadsideLandmarkType.FERRY_SHIP
                if (wantsSeaward) {
                    if (dotInward > 0f) {
                        nx = -nx
                        ny = -ny
                    }
                } else {
                    if (dotInward < 0f) {
                        nx = -nx
                        ny = -ny
                    }
                }

                val offsetDist = if (wantsSeaward) 0.42f else 0.34f
                val cx = node.x + nx * offsetDist
                val cy = node.y + ny * offsetDist

                val tooClose = result.any { existing ->
                    val ex = cx - existing.x
                    val ey = cy - existing.y
                    ex * ex + ey * ey < minSpacingSq
                }
                if (tooClose) continue

                val hash = abs(node.id.hashCode())
                val scale = 0.90f + (hash % 26) * 0.01f
                result.add(
                    RoadsideLandmark(
                        id = "anchor_scene_${node.id}",
                        name = node.name,
                        x = cx,
                        y = cy,
                        type = type,
                        scale = scale,
                        flipX = (i % 2 == 1),
                    ),
                )
            }
            return result
        }
    }
}
