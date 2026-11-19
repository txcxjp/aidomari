package com.aidomari.app.domain.model

import com.aidomari.app.R

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
        baseDisplayHeightDp = 98f,
    ),
    CAPE_LIGHTHOUSE(
        defaultDialogue = "「青い海と白い灯台が見える！」",
        drawableResId = R.drawable.landmark_cape_lighthouse,
        baseDisplayWidthDp = 128f,
        baseDisplayHeightDp = 74f,
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
        baseDisplayHeightDp = 98f,
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
        baseDisplayWidthDp = 166f,
        baseDisplayHeightDp = 89f,
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
        baseDisplayWidthDp = 132f,
        baseDisplayHeightDp = 77f,
    ),
    GORYOKAKU_CHERRY(
        defaultDialogue = "「星形の五稜郭に桜が満開だ！」",
        drawableResId = R.drawable.landmark_goryokaku_cherry,
        baseDisplayWidthDp = 168f,
        baseDisplayHeightDp = 87f,
    ),
    ROTENBURO_ONSEN(
        defaultDialogue = "「湯気の立つ露天風呂、気持ちよさそう〜！」",
        drawableResId = R.drawable.landmark_rotenburo_onsen,
        baseDisplayWidthDp = 134f,
        baseDisplayHeightDp = 82f,
    ),
    MORNING_MARKET(
        defaultDialogue = "「朝市にカニやイカがいっぱい並んでる！」",
        drawableResId = R.drawable.landmark_morning_market,
        baseDisplayWidthDp = 148f,
        baseDisplayHeightDp = 86f,
    ),
    APPLE_ORCHARD(
        defaultDialogue = "「真っ赤なりんごがたわわに実ってる！」",
        drawableResId = R.drawable.landmark_apple_orchard,
        baseDisplayWidthDp = 152f,
        baseDisplayHeightDp = 80f,
    ),
    TOWN_SHOPS(
        defaultDialogue = "「かわいいお店やポストが並ぶ街角だね！」",
        drawableResId = R.drawable.landmark_town_shops,
        baseDisplayWidthDp = 165f,
        baseDisplayHeightDp = 83f,
    ),
    RED_BRIDGE_RIVER(
        defaultDialogue = "「赤い橋の下をお魚が泳いでるよ！」",
        drawableResId = R.drawable.landmark_red_bridge_river,
        baseDisplayWidthDp = 148f,
        baseDisplayHeightDp = 79f,
    ),
    FERRY_SHIP(
        defaultDialogue = "「大きな連絡船が港に停まってる！」",
        drawableResId = R.drawable.landmark_ferry_ship,
        baseDisplayWidthDp = 148f,
        baseDisplayHeightDp = 87f,
    ),
    FOX_AND_HAY(
        defaultDialogue = "「牧草ロールの上でキタキツネがお昼寝してる！」",
        drawableResId = R.drawable.landmark_fox_and_hay,
        baseDisplayWidthDp = 142f,
        baseDisplayHeightDp = 82f,
    ),
    GRAZING_HORSES(
        defaultDialogue = "「お馬さんの親子が仲良く走ってる！」",
        drawableResId = R.drawable.landmark_grazing_horses,
        baseDisplayWidthDp = 152f,
        baseDisplayHeightDp = 82f,
    ),
    MOUNTAIN_ROPEWAY(
        defaultDialogue = "「ロープウェイで山頂展望台へ登れるんだね！」",
        drawableResId = R.drawable.landmark_mountain_ropeway,
        baseDisplayWidthDp = 152f,
        baseDisplayHeightDp = 80f,
    ),
    CHERRY_ROW(
        defaultDialogue = "「桜並木の花びらがひらひら舞ってる！」",
        drawableResId = R.drawable.landmark_cherry_row,
        baseDisplayWidthDp = 156f,
        baseDisplayHeightDp = 78f,
    ),
}

/**
 * Roadside scenery panorama or vignette placed along the Hokkaido Sugoroku route.
 * Rendered purely as hand-drawn brushwork scenery without artificial text labels or token shadows.
 * Every illustration asset appears at most once across the route (zero image reuse).
 *
 * @param id Unique identifier for debugging/tracking.
 * @param name Descriptive label (used for debugging/testing, never shown as on-canvas label).
 * @param x World SVG X coordinate (matches HokkaidoAct1Route coordinates).
 * @param y World SVG Y coordinate (matches HokkaidoAct1Route coordinates).
 * @param type Landmark category (each sized to natural panoramic or vignette proportions).
 * @param scale Individual scaling factor for natural environmental variety.
 * @param flipX Mirrors the panorama horizontally for directional variety.
 * @param reactionDialogue Custom speech dialogue when player companion passes near.
 * @param isMajorRegional Prioritizes key regional centerpieces when zoomed out to prevent crowding.
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
                scale = 0.95f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "motomachi_slope_town",
                name = "元町歴史的洋館・教会・坂道街並み",
                x = 184.52f,
                y = 344.08f,
                type = RoadsideLandmarkType.WESTERN_HOUSE,
                scale = 1.05f,
                reactionDialogue = "「洋館と教会が並ぶすてきな坂道だね！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hakodate_city_tram",
                name = "函館市電",
                x = 184.88f,
                y = 344.45f,
                type = RoadsideLandmarkType.CITY_TRAM,
                scale = 0.96f,
                reactionDialogue = "「レトロな函館市電がゴトゴト走ってる！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "kanemori_warehouse_row",
                name = "金森赤レンガ倉庫と運河",
                x = 184.28f,
                y = 343.42f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 1.05f,
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
                reactionDialogue = "「カニにイカに朝市は活気があるなぁ！」",
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "hako_bay_ferry_mashumaru",
                name = "青函連絡船摩周丸",
                x = 184.85f,
                y = 342.38f,
                type = RoadsideLandmarkType.FERRY_SHIP,
                scale = 1.02f,
                reactionDialogue = "「大きな連絡船、摩周丸が見える！」",
                isMajorRegional = true,
            ),

            // =========================================================================
            // 2. GORYOKAKU & YUNOKAWA (五稜郭・湯の川・商店街・桜並木) - 5 unique assets
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
                name = "昭和橋と清流",
                x = 187.15f,
                y = 342.55f,
                type = RoadsideLandmarkType.RED_BRIDGE_RIVER,
                scale = 1.02f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_east_shops",
                name = "五稜郭商店街",
                x = 187.65f,
                y = 342.02f,
                type = RoadsideLandmarkType.TOWN_SHOPS,
                scale = 1.04f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "goryokaku_star_moat_cherry",
                name = "五稜郭の堀と満開の桜",
                x = 187.08f,
                y = 341.12f,
                type = RoadsideLandmarkType.GORYOKAKU_CHERRY,
                scale = 1.18f,
                reactionDialogue = "「五稜郭の石垣とお堀に桜がいっぱい咲いてる！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_west_cherry_row",
                name = "五稜郭外周桜並木",
                x = 186.22f,
                y = 341.22f,
                type = RoadsideLandmarkType.CHERRY_ROW,
                scale = 1.04f,
                isMajorRegional = false,
            ),

            // =========================================================================
            // 3. NANAE RANCH & KIKYO (桔梗・七飯大牧場・キタキツネ・りんご園・白樺とお馬さん) - 5 unique assets
            // =========================================================================
            RoadsideLandmark(
                id = "kikyo_country_hamlet",
                name = "桔梗の田園集落",
                x = 185.75f,
                y = 337.20f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.08f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "nanae_cow_herd_1",
                name = "七飯大牧場",
                x = 182.18f,
                y = 331.38f,
                type = RoadsideLandmarkType.DAIRY_COW,
                scale = 1.15f,
                reactionDialogue = "「のんびり草を食べてる！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "nanae_fox_hay_bales",
                name = "七飯牧草ロールとキタキツネ",
                x = 182.42f,
                y = 331.95f,
                type = RoadsideLandmarkType.FOX_AND_HAY,
                scale = 1.02f,
                isMajorRegional = false,
            ),
            RoadsideLandmark(
                id = "nanae_orchard",
                name = "ななえりんご園",
                x = 183.48f,
                y = 331.30f,
                type = RoadsideLandmarkType.APPLE_ORCHARD,
                scale = 1.05f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "nanae_grazing_horses",
                name = "七飯のお馬さん親子",
                x = 182.12f,
                y = 330.76f,
                type = RoadsideLandmarkType.GRAZING_HORSES,
                scale = 1.04f,
                isMajorRegional = false,
            ),

            // =========================================================================
            // 4. ONUMA, SUTTSU, ORORON & SAROBETSU (大沼・寿都・苫前・サロベツ) - 4 remaining unique assets
            // =========================================================================
            RoadsideLandmark(
                id = "onuma_woods_1",
                name = "大沼湖畔の深い森",
                x = 180.35f,
                y = 325.30f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.20f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "suttsu_fishing_boat",
                name = "寿都・長万部沖の漁船",
                x = 155.80f,
                y = 255.80f,
                type = RoadsideLandmarkType.FISHING_BOAT,
                scale = 1.05f,
                reactionDialogue = "「海の上を漁船が進んでる！」",
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "tomamae_wind_1",
                name = "苫前オトンルイ風力発電群",
                x = 247.00f,
                y = 125.10f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                scale = 1.20f,
                isMajorRegional = true,
            ),
            RoadsideLandmark(
                id = "sarobetsu_birch_1",
                name = "サロベツ原野の白樺",
                x = 251.60f,
                y = 69.50f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 1.15f,
                isMajorRegional = true,
            ),
        )

        /**
         * Complete Act 1 scenery list: every illustration appears at most once across Hokkaido.
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = CURATED_LANDMARKS
    }
}
