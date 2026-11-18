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
        baseDisplayWidthDp = 34f,
        baseDisplayHeightDp = 21f,
    ),
    CAPE_LIGHTHOUSE(
        defaultDialogue = "「青い海と白い灯台が見える！」",
        drawableResId = R.drawable.landmark_cape_lighthouse,
        baseDisplayWidthDp = 28f,
        baseDisplayHeightDp = 25f,
    ),
    LOG_CABIN(
        defaultDialogue = "「煙突からあたたかい煙が…」",
        drawableResId = R.drawable.landmark_log_cabin,
        baseDisplayWidthDp = 30f,
        baseDisplayHeightDp = 26f,
    ),
    COASTAL_WINDMILL(
        defaultDialogue = "「風車がぐるぐる回ってる！」",
        drawableResId = R.drawable.landmark_coastal_windmill,
        baseDisplayWidthDp = 26f,
        baseDisplayHeightDp = 25f,
    ),
    BIRCH_TREES(
        defaultDialogue = "「白樺の木陰がすずしい！」",
        drawableResId = R.drawable.landmark_birch_trees,
        baseDisplayWidthDp = 22f,
        baseDisplayHeightDp = 27f,
    ),
    RED_BRICK_WAREHOUSE(
        defaultDialogue = "「歴史ある赤レンガ倉庫だ！」",
        drawableResId = R.drawable.landmark_red_brick_warehouse,
        baseDisplayWidthDp = 34f,
        baseDisplayHeightDp = 24f,
    ),
    RETRO_GAS_LAMP(
        defaultDialogue = "「レトロな灯りがやさしい…」",
        drawableResId = R.drawable.landmark_retro_gas_lamp,
        baseDisplayWidthDp = 18f,
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
 * @param reactionDialogue Custom speech dialogue when player passes near.
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
         * Curated roadside scenery landmarks along the Act 1 Hokkaido walking route.
         * Densely placed starting right from the Hakodate starting line so the road
         * always feels alive with diverse, charming roadside world elements.
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = listOf(
            // --- 1. Hakodate Port & Historic District (Opening Steps 0 ~ 20) ---
            RoadsideLandmark(
                id = "motomachi_gas_lamp",
                name = "元町のガス灯",
                x = 184.6f,
                y = 344.2f,
                type = RoadsideLandmarkType.RETRO_GAS_LAMP,
                reactionDialogue = "「坂道のガス灯がきれい！」",
            ),
            RoadsideLandmark(
                id = "kanemori_red_brick_warehouse",
                name = "金森赤レンガ倉庫",
                x = 185.1f,
                y = 343.3f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                reactionDialogue = "「港の赤レンガ倉庫だ！」",
            ),
            RoadsideLandmark(
                id = "goryokaku_birch_park",
                name = "五稜郭の白樺並木",
                x = 187.2f,
                y = 341.3f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                reactionDialogue = "「緑の並木が気持ちいい！」",
            ),
            RoadsideLandmark(
                id = "kikyo_gas_lamp",
                name = "桔梗の街灯",
                x = 185.8f,
                y = 336.4f,
                type = RoadsideLandmarkType.RETRO_GAS_LAMP,
                reactionDialogue = "「街道の灯りがぽっと光ってる」",
            ),

            // --- 2. Nanae & Oshima Farmland & Forest (Steps 20 ~ 60) ---
            RoadsideLandmark(
                id = "nanae_birch_woods",
                name = "七飯の白樺林",
                x = 183.9f,
                y = 334.8f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                reactionDialogue = "「白樺の木陰がすずしい！」",
            ),
            RoadsideLandmark(
                id = "nanae_ranch_cow",
                name = "七飯の放牧牛",
                x = 182.2f,
                y = 331.0f,
                type = RoadsideLandmarkType.DAIRY_COW,
                reactionDialogue = "「のんびり草を食べてる！」",
            ),
            RoadsideLandmark(
                id = "niyama_forest_cabin",
                name = "仁山の丸太小屋",
                x = 178.5f,
                y = 329.2f,
                type = RoadsideLandmarkType.LOG_CABIN,
                reactionDialogue = "「薪のいいかおりがする！」",
            ),
            RoadsideLandmark(
                id = "onuma_lakeside_birch",
                name = "大沼湖畔の白樺",
                x = 180.8f,
                y = 324.2f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                reactionDialogue = "「湖の風が通り抜ける！」",
            ),

            // --- 3. Shiribeshi / Shakotan & Yoichi (Japan Sea Coast) ---
            RoadsideLandmark(
                id = "kamui_cape_lighthouse",
                name = "神威岬灯台",
                x = 160.8f,
                y = 207.5f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                reactionDialogue = "「青い海と白い灯台が見える！」",
            ),
            RoadsideLandmark(
                id = "yoichi_apple_cabin",
                name = "余市のかわいい小屋",
                x = 191.0f,
                y = 221.2f,
                type = RoadsideLandmarkType.LOG_CABIN,
                reactionDialogue = "「りんごの里についた！」",
            ),
            RoadsideLandmark(
                id = "otaru_canal_warehouse",
                name = "小樽運河の石造倉庫",
                x = 203.8f,
                y = 221.5f,
                type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE,
                reactionDialogue = "「小樽の歴史ある運河だ！」",
            ),
            RoadsideLandmark(
                id = "otaru_canal_gas_lamp",
                name = "小樽運河のガス灯",
                x = 204.6f,
                y = 221.0f,
                type = RoadsideLandmarkType.RETRO_GAS_LAMP,
                reactionDialogue = "「ガス灯にあかりが灯ってる！」",
            ),

            // --- 4. Ishikari & Rumoi Coast (Ororon Line) ---
            RoadsideLandmark(
                id = "ishikari_coastal_windmill",
                name = "石狩の風車",
                x = 228.2f,
                y = 210.0f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                reactionDialogue = "「日本海の風がふいてる！」",
            ),
            RoadsideLandmark(
                id = "mashike_birch_grove",
                name = "増毛の海岸樹林",
                x = 225.2f,
                y = 171.2f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                reactionDialogue = "「断崖に木々が立ってる！」",
            ),
            RoadsideLandmark(
                id = "tomamae_ororon_windmill",
                name = "苫前夕陽ケ丘の風車",
                x = 247.0f,
                y = 125.2f,
                type = RoadsideLandmarkType.COASTAL_WINDMILL,
                reactionDialogue = "「風車がずっと続いてる！」",
            ),

            // --- 5. Soya / Northern Plains & Capes ---
            RoadsideLandmark(
                id = "sarobetsu_birch_woods",
                name = "サロベツ原野の白樺",
                x = 251.8f,
                y = 53.5f,
                type = RoadsideLandmarkType.BIRCH_TREES,
                reactionDialogue = "「広大な湿原の向こうに利尻富士が！」",
            ),
            RoadsideLandmark(
                id = "teshio_soya_cow",
                name = "宗谷の放牧牛",
                x = 251.2f,
                y = 70.5f,
                type = RoadsideLandmarkType.DAIRY_COW,
                reactionDialogue = "「見渡すかぎり広い原野だ！」",
            ),
            RoadsideLandmark(
                id = "soya_cape_lighthouse",
                name = "宗谷岬灯台",
                x = 263.2f,
                y = 13.5f,
                type = RoadsideLandmarkType.CAPE_LIGHTHOUSE,
                reactionDialogue = "「日本の最北端が見えた！」",
            ),
        )
    }
}
