package com.aidomari.app.domain.model

import com.aidomari.app.R

enum class RoadsideLandmarkType(
    val defaultDialogue: String,
    val drawableResId: Int,
    val baseDisplayWidthDp: Float,
    val baseDisplayHeightDp: Float,
) {
    CAPE_LIGHTHOUSE(
        defaultDialogue = "「青い海と白い灯台が見える！」",
        drawableResId = R.drawable.landmark_cape_lighthouse,
        baseDisplayWidthDp = 152f,
        baseDisplayHeightDp = 89f,
    ),
    RED_BRICK_WAREHOUSE(
        defaultDialogue = "「歴史ある赤レンガ倉庫だ！」",
        drawableResId = R.drawable.landmark_red_brick_warehouse,
        baseDisplayWidthDp = 185f,
        baseDisplayHeightDp = 105f,
    ),
    WESTERN_HOUSE(
        defaultDialogue = "「異国情緒のある洋館だね！」",
        drawableResId = R.drawable.landmark_western_house,
        baseDisplayWidthDp = 180f,
        baseDisplayHeightDp = 100f,
    ),
    CITY_TRAM(
        defaultDialogue = "「レトロな路面電車がゴトゴト走ってる！」",
        drawableResId = R.drawable.landmark_city_tram,
        baseDisplayWidthDp = 160f,
        baseDisplayHeightDp = 84f,
    ),
    GORYOKAKU_CHERRY(
        defaultDialogue = "「星形の五稜郭に桜が満開だ！」",
        drawableResId = R.drawable.landmark_goryokaku_cherry,
        baseDisplayWidthDp = 188f,
        baseDisplayHeightDp = 106f,
    ),
    ROTENBURO_ONSEN(
        defaultDialogue = "「湯気の立つ露天風呂、気持ちよさそう〜！」",
        drawableResId = R.drawable.landmark_rotenburo_onsen,
        baseDisplayWidthDp = 156f,
        baseDisplayHeightDp = 83f,
    ),
    MORNING_MARKET(
        defaultDialogue = "「朝市にカニやイカがいっぱい並んでる！」",
        drawableResId = R.drawable.landmark_morning_market,
        baseDisplayWidthDp = 166f,
        baseDisplayHeightDp = 96f,
    ),
    TOWN_SHOPS(
        defaultDialogue = "「かわいいお店やメロンが並ぶ街角だね！」",
        drawableResId = R.drawable.landmark_town_shops,
        baseDisplayWidthDp = 176f,
        baseDisplayHeightDp = 97f,
    ),
    RED_BRIDGE_RIVER(
        defaultDialogue = "「赤い橋の下をお魚が泳いでるよ！」",
        drawableResId = R.drawable.landmark_red_bridge_river,
        baseDisplayWidthDp = 164f,
        baseDisplayHeightDp = 87f,
    ),
    FERRY_SHIP(
        defaultDialogue = "「大きな連絡船が港に停まってる！」",
        drawableResId = R.drawable.landmark_ferry_ship,
        baseDisplayWidthDp = 168f,
        baseDisplayHeightDp = 93f,
    ),
    MOUNTAIN_ROPEWAY(
        defaultDialogue = "「ロープウェイで山頂展望台へ登れるんだね！」",
        drawableResId = R.drawable.landmark_mountain_ropeway,
        baseDisplayWidthDp = 170f,
        baseDisplayHeightDp = 93f,
    ),
    CHERRY_ROW(
        defaultDialogue = "「カラフルなお花やハチさんが飛んでる！」",
        drawableResId = R.drawable.landmark_cherry_row,
        baseDisplayWidthDp = 172f,
        baseDisplayHeightDp = 95f,
    ),
}

/**
 * Roadside pop-collage illustration cluster placed along the Hokkaido Sugoroku route.
 * Rendered cleanly without text labels, oval shadows, or zoom-out culling so overlapping
 * transparent marker illustrations layer joyfully together.
 *
 * @param id Unique identifier for debugging/tracking.
 * @param name Descriptive label (used for debugging/testing, never shown as on-canvas label).
 * @param x World SVG X coordinate (matches HokkaidoAct1Route coordinates).
 * @param y World SVG Y coordinate (matches HokkaidoAct1Route coordinates).
 * @param type Landmark category (each asset appears at most once).
 * @param scale Individual scaling factor for natural variety.
 * @param flipX Mirrors horizontally for directional variety.
 * @param reactionDialogue Custom speech dialogue when player companion passes near.
 * @param isMajorRegional Marks key regional landmarks.
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
                x = 183.95f,
                y = 344.22f,
                type = RoadsideLandmarkType.MOUNTAIN_ROPEWAY,
                scale = 1.05f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "hako_cape_lighthouse",
                name = "立待・穴澗岬灯台",
                x = 183.72f,
                y = 343.65f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                scale = 0.96f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "motomachi_slope_town",
                name = "元町歴史的洋館・教会・坂道街並み",
                x = 184.52f,
                y = 344.08f,
                type = RoadsideLandmarkType.WESTERN_HOUSE,
                scale = 1.04f,
                reactionDialogue = "「洋館と時計塔が並ぶすてきな坂道だね！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hakodate_city_tram",
                name = "函館市電",
                x = 184.88f,
                y = 344.45f,
                type = RoadsideLandmarkType.CITY_TRAM,
                scale = 0.96f,
                reactionDialogue = "「楽しい函館市電がゴトゴト走ってる！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "kanemori_warehouse_row",
                name = "金森赤レンガ倉庫と運河",
                x = 184.28f,
                y = 343.42f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 1.04f,
                reactionDialogue = "「運河沿いに赤レンガ倉庫が並んでる！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "hakodate_morning_market",
                name = "函館朝市の屋台",
                x = 184.82f,
                y = 342.96f,
                type = RoadsideLandmarkType.MORNING_MARKET,
                scale = 0.98f,
                reactionDialogue = "「カニにお寿司にラーメン！朝市は賑やかだなあ！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hako_bay_ferry_mashumaru",
                name = "青函連絡船摩周丸",
                x = 184.85f,
                y = 342.38f,
                type = RoadsideLandmarkType.FERRY_SHIP,
                scale = 1.02f,
                reactionDialogue = "「大きな連絡船とタコさんが見える！」",
                isMajorRegional = true,
            ),

            // =========================================================================
            // 2. GORYOKAKU & YUNOKAWA (五稜郭・湯の川・商店街・お花畑) - 5 unique assets
            // =========================================================================
            RoadsideLandmark(
                id = "onari_rotenburo_onsen",
                name = "湯けむり露天風呂",
                x = 186.12f,
                y = 342.20f,
                type = RoadsideLandmarkType.ROTENBURO_ONSEN,
                scale = 1.02f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "showabashi_red_bridge",
                name = "昭和橋とペンギン・お魚",
                x = 187.15f,
                y = 342.55f,
                type = RoadsideLandmarkType.RED_BRIDGE_RIVER,
                scale = 1.02f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_east_shops",
                name = "五稜郭のにぎわいスイーツ通り",
                x = 187.65f,
                y = 342.02f,
                type = RoadsideLandmarkType.TOWN_SHOPS,
                scale = 1.02f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_star_moat_cherry",
                name = "五稜郭タワーと星形の堀",
                x = 187.08f,
                y = 341.12f,
                type = RoadsideLandmarkType.GORYOKAKU_CHERRY,
                scale = 1.12f,
                reactionDialogue = "「星形の五稜郭に桜とお侍さんがいる！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_west_cherry_row",
                name = "花畑と虹の並木道",
                x = 186.22f,
                y = 341.22f,
                type = RoadsideLandmarkType.CHERRY_ROW,
                scale = 1.02f,
                isMajorRegional = false,
            ),
        )

        /**
         * Complete Act 1 scenery list: 12 unique pop-collage illustrations (zero duplicates, zero old assets).
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = CURATED_LANDMARKS
    }
}
