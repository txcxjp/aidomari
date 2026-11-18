package com.aidomari.app.domain.model

import com.aidomari.app.R

enum class RoadsideLandmarkType(
    val defaultDialogue: String,
    val drawableResId: Int,
    val baseDisplayWidthDp: Float,
    val baseDisplayHeightDp: Float,
) {
    DAIRY_COW(
        defaultDialogue = "🐮「モ〜」",
        drawableResId = R.drawable.landmark_dairy_cow,
        baseDisplayWidthDp = 34f,
        baseDisplayHeightDp = 21f,
    ),
    CAPE_LIGHTHOUSE(
        defaultDialogue = "🗼「海がきれい！」",
        drawableResId = R.drawable.landmark_cape_lighthouse,
        baseDisplayWidthDp = 28f,
        baseDisplayHeightDp = 25f,
    ),
    LOG_CABIN(
        defaultDialogue = "🏠「煙突からいいにおい…」",
        drawableResId = R.drawable.landmark_log_cabin,
        baseDisplayWidthDp = 30f,
        baseDisplayHeightDp = 26f,
    ),
    COASTAL_WINDMILL(
        defaultDialogue = "🍃「風車がぐるぐる！」",
        drawableResId = R.drawable.landmark_coastal_windmill,
        baseDisplayWidthDp = 26f,
        baseDisplayHeightDp = 25f,
    ),
}

/**
 * Roadside landmark vignette placed along the Hokkaido Sugoroku route.
 *
 * @param id Unique identifier.
 * @param name Display label for the landmark.
 * @param x World SVG X coordinate (matches HokkaidoAct1Route coordinates).
 * @param y World SVG Y coordinate (matches HokkaidoAct1Route coordinates).
 * @param type Landmark category.
 * @param reactionDialogue Optional custom speech dialogue when player passes near.
 */
data class RoadsideLandmark(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val type: RoadsideLandmarkType,
    val reactionDialogue: String = type.defaultDialogue,
) {
    companion object {
        /**
         * Curated roadside landmarks along the Act 1 Hokkaido walking route.
         * Placed beside authentic geographical landmarks (pastures, capes, camps, wind farms).
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = listOf(
            // --- Oshima Peninsula / Hakodate - Nanae ---
            RoadsideLandmark(
                id = "nanae_ranch_cow",
                name = "七飯の放牧牛",
                x = 182.2f,
                y = 331.0f,
                type = RoadsideLandmarkType.DAIRY_COW,
                reactionDialogue = "🐮「モ〜（草がおいしい）」",
            ),
            RoadsideLandmark(
                id = "niyama_forest_cabin",
                name = "仁山の丸太小屋",
                x = 178.5f,
                y = 329.2f,
                type = RoadsideLandmarkType.LOG_CABIN,
                reactionDialogue = "🏠「薪のいいかおり」",
            ),

            // --- Shiribeshi / Shakotan - Yoichi ---
            RoadsideLandmark(
                id = "kamui_cape_lighthouse",
                name = "神威岬灯台",
                x = 160.8f,
                y = 207.5f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                reactionDialogue = "🗼「青い海と白い灯台！」",
            ),
            RoadsideLandmark(
                id = "yoichi_apple_cabin",
                name = "余市のかわいい小屋",
                x = 191.0f,
                y = 221.2f,
                type = RoadsideLandmarkType.LOG_CABIN,
                reactionDialogue = "🍎「りんごの里だ！」",
            ),

            // --- Ishikari & Rumoi Coast (Ororon Line) ---
            RoadsideLandmark(
                id = "ishikari_coastal_windmill",
                name = "石狩の風車",
                x = 228.2f,
                y = 210.0f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                reactionDialogue = "🍃「日本海の風がふいてる」",
            ),
            RoadsideLandmark(
                id = "tomamae_ororon_windmill",
                name = "苫前夕陽ケ丘の風車",
                x = 247.0f,
                y = 125.2f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                reactionDialogue = "🍃「風車がずっと続いてる！」",
            ),

            // --- Soya / Northern Pastures ---
            RoadsideLandmark(
                id = "teshio_soya_cow",
                name = "宗谷の放牧牛",
                x = 251.2f,
                y = 70.5f,
                type = RoadsideLandmarkType.DAIRY_COW,
                reactionDialogue = "🐮「モ〜（広い原野だ）」",
            ),
            RoadsideLandmark(
                id = "soya_cape_lighthouse",
                name = "宗谷岬灯台",
                x = 263.2f,
                y = 13.5f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                reactionDialogue = "🗼「日本の最北端が見えた！」",
            ),
        )
    }
}
