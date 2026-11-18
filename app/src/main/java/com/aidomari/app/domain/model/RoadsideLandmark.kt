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
        /**
         * Cohesive panoramic storybook scenes along the Act 1 Hokkaido walking route.
         * Avoids repetitive single-icon stamps and overlapping elements:
         * - Mt. Hakodate deep evergreen & birch forest slopes
         * - Motomachi historic cobblestone slope neighborhood
         * - Hakodate Bay canal with connected red-brick warehouse row & harbor boats
         * - Goryokaku park woods & birch avenue
         * - Nanae dairy pasture panorama with barn, silo, split-rail fence & varied cow herd
         * - Country hamlets, Onuma lakeside woods, Otaru canal, coastal wind turbine bluffs & capes
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = listOf(
            // =========================================================================
            // 1. HAKODATE & MT. HAKODATE (函館山・元町坂道・ベイエリア赤レンガ倉庫街)
            // =========================================================================
            RoadsideLandmark(
                id = "hako_mountain_woods_main",
                name = "函館山深い森",
                x = 183.92f,
                y = 344.35f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.22f,
                flipX = false,
            ),
            RoadsideLandmark(
                id = "hako_mountain_woods_west",
                name = "函館山西斜面森林",
                x = 183.48f,
                y = 344.62f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 0.98f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "hako_mountain_birch",
                name = "函館山南斜面白樺",
                x = 184.22f,
                y = 344.78f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 0.95f,
            ),
            RoadsideLandmark(
                id = "hako_cape_lighthouse",
                name = "立待・穴澗岬灯台",
                x = 183.76f,
                y = 343.75f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                scale = 0.85f,
            ),
            RoadsideLandmark(
                id = "motomachi_slope_town",
                name = "元町歴史的洋館・教会・坂道街並み",
                x = 184.45f,
                y = 344.04f,
                type = RoadsideLandmarkType.WESTERN_HOUSE,
                scale = 1.08f,
                reactionDialogue = "「洋館と教会が並ぶすてきな坂道だね！」",
            ),
            RoadsideLandmark(
                id = "kanemori_warehouse_row",
                name = "金森赤レンガ倉庫と運河",
                x = 184.72f,
                y = 343.38f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 1.08f,
                reactionDialogue = "「運河沿いに赤レンガ倉庫が並んでる！」",
            ),
            RoadsideLandmark(
                id = "hako_bay_boat_1",
                name = "函館湾の漁船",
                x = 184.85f,
                y = 342.88f,
                type = RoadsideLandmarkType.FISHING_BOAT,
                scale = 1.05f,
                reactionDialogue = "「港に漁船が浮かんでる！」",
            ),

            // =========================================================================
            // 2. GORYOKAKU & KIKYO (五稜郭公園・桔梗田園)
            // =========================================================================
            RoadsideLandmark(
                id = "chiyogadai_birch",
                name = "千代台白樺",
                x = 186.65f,
                y = 342.20f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 1.05f,
            ),
            RoadsideLandmark(
                id = "goryokaku_birch_avenue",
                name = "五稜郭公園白樺並木",
                x = 187.15f,
                y = 341.45f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 1.00f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "goryokaku_park_woods",
                name = "五稜郭公園の森",
                x = 187.55f,
                y = 340.95f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.20f,
            ),
            RoadsideLandmark(
                id = "kikyo_country_hamlet",
                name = "桔梗の田園集落",
                x = 185.75f,
                y = 337.20f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.10f,
            ),

            // =========================================================================
            // 3. NANAE DAIRY RANCH & FARMLAND (七飯大牧場・放牧牛の群れ)
            // =========================================================================
            RoadsideLandmark(
                id = "nanae_cow_herd_1",
                name = "七飯大牧場",
                x = 182.22f,
                y = 331.38f,
                type = RoadsideLandmarkType.DAIRY_COW,
                scale = 1.18f,
                reactionDialogue = "「のんびり草を食べてる！」",
            ),
            RoadsideLandmark(
                id = "nanae_country_hamlet",
                name = "七飯街道の集落",
                x = 182.88f,
                y = 330.88f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.05f,
            ),
            RoadsideLandmark(
                id = "nanae_birch_left",
                name = "国道5号白樺並木（西側）",
                x = 182.38f,
                y = 330.72f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 0.95f,
                flipX = true,
            ),
            RoadsideLandmark(
                id = "nanae_birch_grove",
                name = "七飯の白樺林",
                x = 183.02f,
                y = 331.62f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                scale = 1.00f,
            ),
            RoadsideLandmark(
                id = "nanae_windbreak_woods",
                name = "牧場の防風林",
                x = 181.75f,
                y = 332.02f,
                type = RoadsideLandmarkType.FOREST_GROVE,
                scale = 1.05f,
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
                name = "余市の果樹園集落",
                x = 191.05f,
                y = 221.20f,
                type = RoadsideLandmarkType.LOG_CABIN,
                scale = 1.05f,
            ),
            RoadsideLandmark(
                id = "otaru_warehouse_1",
                name = "小樽運河と赤レンガ倉庫街",
                x = 203.90f,
                y = 221.50f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                scale = 1.15f,
                reactionDialogue = "「小樽運河沿いの石造り・レンガ倉庫が見えた！」",
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
    }
}
