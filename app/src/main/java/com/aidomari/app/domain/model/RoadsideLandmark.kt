package com.aidomari.app.domain.model

import com.aidomari.app.R

/**
 * Living map animation personality assigned to each roadside landmark type.
 */
enum class LandmarkMotionStyle {
    NONE,
    DANCE_BOUNCE,
    WAVE_DANCE_2FRAME,
    BOAT_BOB,
    TREE_SWAY,
    TRAM_CHUG,
    ONSEN_STEAM,
    LIGHTHOUSE_BEAM,
    ROPEWAY_SPARKLE,
    RIVER_SHIMMER,
}

enum class RoadsideLandmarkType(
    val defaultDialogue: String,
    val drawableResId: Int,
    val baseDisplayWidthDp: Float,
    val baseDisplayHeightDp: Float,
    val motionStyle: LandmarkMotionStyle = LandmarkMotionStyle.NONE,
    val animFrame2DrawableResId: Int? = null,
) {
    CAPE_LIGHTHOUSE(
        defaultDialogue = "「青い海と白い灯台が見える！」",
        drawableResId = R.drawable.landmark_cape_lighthouse,
        baseDisplayWidthDp = 142f,
        baseDisplayHeightDp = 82f,
        motionStyle = LandmarkMotionStyle.LIGHTHOUSE_BEAM,
    ),
    RED_BRICK_WAREHOUSE(
        defaultDialogue = "「歴史ある赤レンガ倉庫だ！」",
        drawableResId = R.drawable.landmark_red_brick_warehouse,
        baseDisplayWidthDp = 164f,
        baseDisplayHeightDp = 84f,
        motionStyle = LandmarkMotionStyle.NONE,
    ),
    WESTERN_HOUSE(
        defaultDialogue = "「旧函館区公会堂とハリストス正教会が見える！」",
        drawableResId = R.drawable.landmark_western_house,
        baseDisplayWidthDp = 166f,
        baseDisplayHeightDp = 91f,
        motionStyle = LandmarkMotionStyle.NONE,
    ),
    CITY_TRAM(
        defaultDialogue = "「レトロな函館市電が走ってる！」",
        drawableResId = R.drawable.landmark_city_tram,
        baseDisplayWidthDp = 144f,
        baseDisplayHeightDp = 71f,
        motionStyle = LandmarkMotionStyle.TRAM_CHUG,
    ),
    GORYOKAKU_CHERRY(
        defaultDialogue = "「五稜郭の星形と五稜郭タワーだ！」",
        drawableResId = R.drawable.landmark_goryokaku_cherry,
        baseDisplayWidthDp = 164f,
        baseDisplayHeightDp = 100f,
        motionStyle = LandmarkMotionStyle.TREE_SWAY,
    ),
    ROTENBURO_ONSEN(
        defaultDialogue = "「湯けむり立つ露天風呂、気持ちよさそう〜！」",
        drawableResId = R.drawable.landmark_rotenburo_onsen,
        baseDisplayWidthDp = 148f,
        baseDisplayHeightDp = 70f,
        motionStyle = LandmarkMotionStyle.ONSEN_STEAM,
    ),
    MORNING_MARKET(
        defaultDialogue = "「朝市に新鮮なカニやお魚が並んでる！」",
        drawableResId = R.drawable.landmark_morning_market,
        baseDisplayWidthDp = 146f,
        baseDisplayHeightDp = 85f,
        motionStyle = LandmarkMotionStyle.DANCE_BOUNCE,
    ),
    TOWN_SHOPS(
        defaultDialogue = "「素敵なお店が並ぶ街並みだね！」",
        drawableResId = R.drawable.landmark_town_shops,
        baseDisplayWidthDp = 148f,
        baseDisplayHeightDp = 71f,
        motionStyle = LandmarkMotionStyle.NONE,
    ),
    RED_BRIDGE_RIVER(
        defaultDialogue = "「赤い橋の下にきれいな川が流れてる！」",
        drawableResId = R.drawable.landmark_red_bridge_river,
        baseDisplayWidthDp = 144f,
        baseDisplayHeightDp = 67f,
        motionStyle = LandmarkMotionStyle.RIVER_SHIMMER,
    ),
    FERRY_SHIP(
        defaultDialogue = "「青函連絡船・摩周丸が停まってる！」",
        drawableResId = R.drawable.landmark_ferry_ship,
        baseDisplayWidthDp = 156f,
        baseDisplayHeightDp = 89f,
        motionStyle = LandmarkMotionStyle.BOAT_BOB,
    ),
    MOUNTAIN_ROPEWAY(
        defaultDialogue = "「函館山山頂の展望台とロープウェイだ！」",
        drawableResId = R.drawable.landmark_mountain_ropeway,
        baseDisplayWidthDp = 162f,
        baseDisplayHeightDp = 97f,
        motionStyle = LandmarkMotionStyle.ROPEWAY_SPARKLE,
    ),
    CHERRY_ROW(
        defaultDialogue = "「公園の桜並木がきれいだね！」",
        drawableResId = R.drawable.landmark_cherry_row,
        baseDisplayWidthDp = 144f,
        baseDisplayHeightDp = 86f,
        motionStyle = LandmarkMotionStyle.TREE_SWAY,
    ),
    WAVING_SQUID(
        defaultDialogue = "「函館名物イカちゃんが手を振ってるよ！」",
        drawableResId = R.drawable.landmark_waving_squid_f1,
        baseDisplayWidthDp = 74f,
        baseDisplayHeightDp = 81f,
        motionStyle = LandmarkMotionStyle.WAVE_DANCE_2FRAME,
        animFrame2DrawableResId = R.drawable.landmark_waving_squid_f2,
    ),
    WAVING_FOX(
        defaultDialogue = "「キタキツネが楽しそうに踊ってる！」",
        drawableResId = R.drawable.landmark_waving_fox_f1,
        baseDisplayWidthDp = 76f,
        baseDisplayHeightDp = 83f,
        motionStyle = LandmarkMotionStyle.WAVE_DANCE_2FRAME,
        animFrame2DrawableResId = R.drawable.landmark_waving_fox_f2,
    ),
}

/**
 * Warm hand-drawn travel-journal scenery landmark placed along the Hokkaido Sugoroku route.
 * Positioned close to each corresponding anchor without text labels or oval shadows.
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
    val isMajorRegional: Boolean = false,
) {
    companion object {
        private val CURATED_LANDMARKS: List<RoadsideLandmark> = listOf(
            // =========================================================================
            // 1. HAKODATE (函館山・元町・ベイエリア・函館朝市・青函連絡船) - 7 unique assets
            // =========================================================================
            RoadsideLandmark(
                id = "hako_mountain_ropeway",
                name = "函館山山頂展望台とロープウェイ",
                x = 183.92f,
                y = 345.16f,
                type = RoadsideLandmarkType.MOUNTAIN_ROPEWAY,
                scale = 1.05f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "hako_cape_lighthouse",
                name = "立待・穴澗岬灯台",
                x = 183.68f,
                y = 343.62f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                scale = 0.96f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "motomachi_slope_town",
                name = "元町（旧函館区公会堂・函館ハリストス正教会）",
                x = 184.42f,
                y = 343.92f,
                type = RoadsideLandmarkType.WESTERN_HOUSE,
                scale = 1.04f,
                reactionDialogue = "「旧函館区公会堂とハリストス正教会だ！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hakodate_city_tram",
                name = "函館市電",
                x = 184.62f,
                y = 344.40f,
                type = RoadsideLandmarkType.CITY_TRAM,
                scale = 0.96f,
                reactionDialogue = "「レトロな函館市電がゴトゴト走ってる！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hakodate_waving_squid",
                name = "手を振る函館イカちゃん",
                x = 183.98f,
                y = 344.52f,
                type = RoadsideLandmarkType.WAVING_SQUID,
                scale = 1.0f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "kanemori_warehouse_row",
                name = "金森赤レンガ倉庫",
                x = 184.45f,
                y = 343.45f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 1.04f,
                reactionDialogue = "「運河沿いに赤レンガ倉庫が並んでる！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "hakodate_morning_market",
                name = "函館朝市",
                x = 184.78f,
                y = 342.94f,
                type = RoadsideLandmarkType.MORNING_MARKET,
                scale = 0.96f,
                reactionDialogue = "「カニにお魚にメロン！朝市は賑やかだなあ！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hako_bay_ferry_mashumaru",
                name = "青函連絡船摩周丸",
                x = 184.86f,
                y = 342.42f,
                type = RoadsideLandmarkType.FERRY_SHIP,
                scale = 1.02f,
                reactionDialogue = "「青函連絡船・摩周丸が見える！」",
                isMajorRegional = true,
            ),

            // =========================================================================
            // 2. GORYOKAKU & YUNOKAWA (五稜郭・桜並木・湯の川・昭和橋・商店街) - 5 unique assets
            // =========================================================================
            RoadsideLandmark(
                id = "onari_rotenburo_onsen",
                name = "湯けむり露天風呂",
                x = 185.95f,
                y = 342.02f,
                type = RoadsideLandmarkType.ROTENBURO_ONSEN,
                scale = 1.02f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "showabashi_red_bridge",
                name = "昭和橋と清流",
                x = 187.12f,
                y = 342.58f,
                type = RoadsideLandmarkType.RED_BRIDGE_RIVER,
                scale = 1.02f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_east_shops",
                name = "五稜郭の街並み",
                x = 187.72f,
                y = 341.98f,
                type = RoadsideLandmarkType.TOWN_SHOPS,
                scale = 1.02f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_star_moat_cherry",
                name = "五稜郭タワーと星形の堀",
                x = 186.88f,
                y = 341.05f,
                type = RoadsideLandmarkType.GORYOKAKU_CHERRY,
                scale = 1.12f,
                reactionDialogue = "「五稜郭の星形と五稜郭タワーが見える！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_west_cherry_row",
                name = "五稜郭公園の桜並木",
                x = 186.02f,
                y = 341.22f,
                type = RoadsideLandmarkType.CHERRY_ROW,
                scale = 1.00f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_waving_fox",
                name = "踊るキタキツネ",
                x = 186.52f,
                y = 341.58f,
                type = RoadsideLandmarkType.WAVING_FOX,
                scale = 1.0f,
                isMajorRegional = false,
            ),
        )

        /**
         * Complete Act 1 scenery list: 12 scenic landmarks plus 2 waving animated companions.
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = CURATED_LANDMARKS
    }
}
