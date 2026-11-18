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
 * Roadside scenery prop placed along the Hokkaido Sugoroku route.
 * Rendered purely as storybook illustration without artificial text labels.
 *
 * @param id Unique identifier for debugging/tracking.
 * @param name Descriptive label (used for debugging/testing, never shown as on-canvas label).
 * @param x World SVG X coordinate (matches HokkaidoAct1Route coordinates).
 * @param y World SVG Y coordinate (matches HokkaidoAct1Route coordinates).
 * @param type Landmark category.
 * @param scale Individual scaling factor for natural environmental variety.
 * @param reactionDialogue Custom speech dialogue when player companion passes near.
 */
data class RoadsideLandmark(
    val id: String,
    val name: String = "",
    val x: Float,
    val y: Float,
    val type: RoadsideLandmarkType,
    val scale: Float = 1.0f,
    val reactionDialogue: String = type.defaultDialogue,
) {
    companion object {
        /**
         * Dense, continuous roadside scenery along the Act 1 Hokkaido walking route.
         * Modeled after real environmental compositions:
         * - Rows of historic warehouses along harbors
         * - Avenues of retro gas lamps lining town streets
         * - Continuous groves of birch trees flanking the highway
         * - Herds of grazing cows and rustic cabins across farmland
         * - Scenic lines of wind turbines along coastal highways
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = listOf(
            // =========================================================================
            // 1. Hakodate Bay & Western Historic District (Opening Steps 0 ~ 12)
            // =========================================================================
            // Streetlamps along Motomachi & Western Slopes
            RoadsideLandmark(id = "motomachi_lamp_1", name = "元町街灯1", x = 184.45f, y = 344.75f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),
            RoadsideLandmark(id = "motomachi_lamp_2", name = "元町街灯2", x = 184.68f, y = 344.30f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "motomachi_lamp_3", name = "十字街街灯", x = 184.95f, y = 343.85f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.95f),

            // Stepped waterfront warehouse row along Hakodate Bay (Kanemori Warehouses)
            RoadsideLandmark(id = "kanemori_warehouse_1", name = "金森倉庫1", x = 184.92f, y = 343.62f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.15f),
            RoadsideLandmark(id = "kanemori_warehouse_2", name = "金森倉庫2", x = 185.12f, y = 343.40f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.00f),
            RoadsideLandmark(id = "kanemori_warehouse_3", name = "金森倉庫3", x = 185.30f, y = 343.20f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 0.90f),

            // Lamps near Station & Waterfront Promenade
            RoadsideLandmark(id = "station_lamp_1", name = "駅前街灯1", x = 185.48f, y = 342.98f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "station_lamp_2", name = "駅前街灯2", x = 185.75f, y = 342.75f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),

            // =========================================================================
            // 2. Chiyogadai to Goryokaku Park Area (Steps 13 ~ 22)
            // =========================================================================
            // Trees leading into the park
            RoadsideLandmark(id = "chiyogadai_birch_1", name = "千代台並木1", x = 186.75f, y = 342.25f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "chiyogadai_birch_2", name = "千代台並木2", x = 187.00f, y = 341.95f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // Goryokaku Fortress Grove (dense cluster of birch and park trees framing the fort)
            RoadsideLandmark(id = "goryokaku_grove_1", name = "五稜郭樹林1", x = 187.25f, y = 341.40f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.20f),
            RoadsideLandmark(id = "goryokaku_grove_2", name = "五稜郭樹林2", x = 187.50f, y = 341.15f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "goryokaku_grove_3", name = "五稜郭樹林3", x = 187.80f, y = 340.75f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "goryokaku_grove_4", name = "五稜郭樹林4", x = 187.95f, y = 340.55f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.85f),

            // =========================================================================
            // 3. Ishikawa & Kikyo Countryside Avenue (Steps 23 ~ 28)
            // =========================================================================
            RoadsideLandmark(id = "ishikawa_birch_1", name = "石川並木1", x = 186.70f, y = 339.80f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "ishikawa_birch_2", name = "石川並木2", x = 186.35f, y = 338.80f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "kikyo_cabin", name = "桔梗の民家", x = 185.95f, y = 337.50f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "kikyo_lamp", name = "桔梗街灯", x = 185.60f, y = 336.85f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "kikyo_birch", name = "桔梗白樺", x = 185.15f, y = 336.35f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),

            // =========================================================================
            // 4. Nanae Farmland & Kameda Pastures (Steps 29 ~ 35)
            // =========================================================================
            // Tree-lined rural highway
            RoadsideLandmark(id = "nanae_avenue_1", name = "七飯街道並木1", x = 184.60f, y = 335.50f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "nanae_avenue_2", name = "七飯街道並木2", x = 184.15f, y = 334.80f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "nanae_avenue_3", name = "七飯街道並木3", x = 183.75f, y = 334.00f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),

            // Nanae Dairy Pasture: A herd of grazing cows (mother cow, calf, grazing cow)
            RoadsideLandmark(id = "nanae_cow_herd_1", name = "七飯放牧牛1", x = 182.45f, y = 331.60f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.10f),
            RoadsideLandmark(id = "nanae_cow_herd_2", name = "七飯放牧牛2(子牛)", x = 182.15f, y = 331.15f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.75f),
            RoadsideLandmark(id = "nanae_cow_herd_3", name = "七飯放牧牛3", x = 182.70f, y = 330.85f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.95f),
            RoadsideLandmark(id = "nanae_farm_cabin", name = "牧場の納屋", x = 181.85f, y = 331.45f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.05f),

            // =========================================================================
            // 5. Niyama Highlands & Onuma Lakeshore Forest (Steps 36 ~ 60)
            // =========================================================================
            RoadsideLandmark(id = "niyama_forest_1", name = "仁山樹林1", x = 179.35f, y = 329.80f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.20f),
            RoadsideLandmark(id = "niyama_cabin", name = "仁山の丸太小屋", x = 178.50f, y = 329.20f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.10f),
            RoadsideLandmark(id = "niyama_forest_2", name = "仁山樹林2", x = 178.10f, y = 328.90f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),

            // Deep woods framing Lake Onuma
            RoadsideLandmark(id = "onuma_woods_1", name = "大沼森林1", x = 180.35f, y = 325.40f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.25f),
            RoadsideLandmark(id = "onuma_woods_2", name = "大沼森林2", x = 180.85f, y = 324.70f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "onuma_lakeside_cabin", name = "大沼湖畔の山小屋", x = 181.25f, y = 324.10f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.00f),
            RoadsideLandmark(id = "onuma_woods_3", name = "大沼森林3", x = 180.50f, y = 323.50f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),

            // =========================================================================
            // 6. Shakotan & Otaru Canal Historical Waterfront
            // =========================================================================
            RoadsideLandmark(id = "kamui_cape_lighthouse", name = "神威岬灯台", x = 160.80f, y = 207.50f, type = RoadsideLandmarkType.CAPE_LIGHTHOUSE, scale = 1.25f),
            RoadsideLandmark(id = "kamui_cape_trees", name = "神威岬の木々", x = 161.40f, y = 208.20f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "yoichi_apple_cabin", name = "余市の果樹園小屋", x = 191.00f, y = 221.20f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.00f),

            // Otaru Canal Waterfront Warehouse Row & Streetlamps
            RoadsideLandmark(id = "otaru_warehouse_1", name = "小樽運河倉庫1", x = 203.60f, y = 221.70f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.15f),
            RoadsideLandmark(id = "otaru_warehouse_2", name = "小樽運河倉庫2", x = 204.05f, y = 221.40f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.00f),
            RoadsideLandmark(id = "otaru_canal_lamp_1", name = "小樽運河ガス灯1", x = 204.45f, y = 221.10f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "otaru_canal_lamp_2", name = "小樽運河ガス灯2", x = 204.75f, y = 220.80f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),

            // =========================================================================
            // 7. Ishikari & Ororon Coastal Wind Farm Highway
            // =========================================================================
            RoadsideLandmark(id = "ishikari_wind_1", name = "石狩風車1", x = 227.70f, y = 210.80f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.15f),
            RoadsideLandmark(id = "ishikari_wind_2", name = "石狩風車2", x = 228.30f, y = 209.60f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 0.95f),
            RoadsideLandmark(id = "mashike_coastal_trees", name = "増毛海岸樹林", x = 225.20f, y = 171.20f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // Tomamae coastal wind turbine avenue (three spinning turbines along the sea road)
            RoadsideLandmark(id = "tomamae_wind_1", name = "苫前風車1", x = 246.50f, y = 126.30f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.20f),
            RoadsideLandmark(id = "tomamae_wind_2", name = "苫前風車2", x = 247.10f, y = 125.10f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.00f),
            RoadsideLandmark(id = "tomamae_wind_3", name = "苫前風車3", x = 247.65f, y = 123.90f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 0.85f),

            // =========================================================================
            // 8. Soya Plains & The Northernmost Edge
            // =========================================================================
            RoadsideLandmark(id = "sarobetsu_birch_1", name = "サロベツ白樺1", x = 251.40f, y = 54.30f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "sarobetsu_birch_2", name = "サロベツ白樺2", x = 251.90f, y = 53.10f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "teshio_soya_cow_1", name = "宗谷放牧牛1", x = 250.90f, y = 70.80f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.10f),
            RoadsideLandmark(id = "teshio_soya_cow_2", name = "宗谷放牧牛2", x = 251.40f, y = 70.30f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.85f),
            RoadsideLandmark(id = "soya_cape_lighthouse", name = "宗谷岬灯台", x = 263.20f, y = 13.50f, type = RoadsideLandmarkType.CAPE_LIGHTHOUSE, scale = 1.30f),
        )
    }
}
