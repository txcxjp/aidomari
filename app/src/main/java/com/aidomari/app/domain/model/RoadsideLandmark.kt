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
    WESTERN_HOUSE(
        defaultDialogue = "「異国情緒のある洋館だね！」",
        drawableResId = R.drawable.landmark_western_house,
        baseDisplayWidthDp = 28f,
        baseDisplayHeightDp = 32f,
    ),
    FISHING_BOAT(
        defaultDialogue = "「波にゆられて気持ちよさそう！」",
        drawableResId = R.drawable.landmark_fishing_boat,
        baseDisplayWidthDp = 30f,
        baseDisplayHeightDp = 24f,
    ),
    FOREST_GROVE(
        defaultDialogue = "「深い森から鳥の声が聞こえる！」",
        drawableResId = R.drawable.landmark_forest_grove,
        baseDisplayWidthDp = 32f,
        baseDisplayHeightDp = 28f,
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
         * - Dense mountain forests covering Mount Hakodate slopes and winding roads
         * - City blocks filled with western houses, cabins, and street trees
         * - Rows of historic red brick warehouses lining harbors and bays
         * - Fishing boats floating on the bay waters
         * - Continuous avenues of trees flanking roads on both sides
         * - Large herds of grazing cows and calves across pastures
         * - Scenic lines of wind turbines along coastal highways
         */
        val ACT1_LANDMARKS: List<RoadsideLandmark> = listOf(
            // =========================================================================
            // 1. MOUNT HAKODATE (函館山・実在の登山道675号・山林樹林帯)
            // =========================================================================
            RoadsideLandmark(id = "hako_summit_forest_1", name = "函館山山頂森林1", x = 183.86f, y = 343.96f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "hako_summit_forest_2", name = "函館山山頂森林2", x = 184.03f, y = 343.90f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),
            RoadsideLandmark(id = "hako_summit_cabin", name = "山頂見晴らし小屋", x = 183.95f, y = 344.05f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "hako_winding_birch_1", name = "登山道カーブ白樺1", x = 184.08f, y = 344.03f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "hako_winding_forest_2", name = "登山道樹林2", x = 184.25f, y = 344.08f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "hako_winding_birch_3", name = "登山道白樺3", x = 184.11f, y = 344.20f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "hako_winding_forest_4", name = "九十九折樹林4", x = 184.34f, y = 343.92f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "hako_winding_forest_5", name = "九十九折樹林5", x = 184.56f, y = 344.20f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.10f),
            RoadsideLandmark(id = "hako_winding_birch_6", name = "登山道白樺6", x = 184.58f, y = 344.29f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "hako_mountain_base_forest", name = "山麓森林", x = 184.67f, y = 344.33f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),
            RoadsideLandmark(id = "hako_mountain_base_birch", name = "山麓白樺", x = 184.76f, y = 344.31f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // Western Peninsula & Ridge Forests
            RoadsideLandmark(id = "hako_west_coast_forest_1", name = "西海岸森林1", x = 183.35f, y = 344.25f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.30f),
            RoadsideLandmark(id = "hako_west_coast_forest_2", name = "西海岸森林2", x = 183.55f, y = 344.05f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),
            RoadsideLandmark(id = "hako_west_coast_forest_3", name = "西海岸森林3", x = 183.45f, y = 344.50f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "hako_west_coast_birch_4", name = "西海岸白樺4", x = 183.65f, y = 344.35f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "hako_south_slope_forest_1", name = "南斜面森林1", x = 183.75f, y = 344.75f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),
            RoadsideLandmark(id = "hako_south_slope_forest_2", name = "南斜面森林2", x = 184.05f, y = 344.80f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "hako_south_slope_birch_3", name = "南斜面白樺3", x = 184.25f, y = 344.90f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // =========================================================================
            // 2. MOTOMACHI & HISTORIC CITY BLOCKS (元町・市街地・洋館街)
            // =========================================================================
            // Western Houses nestled in the city grid blocks
            RoadsideLandmark(id = "motomachi_western_1", name = "元町洋館1", x = 184.58f, y = 344.60f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 1.10f, reactionDialogue = "「かわいいミント色の洋館！」"),
            RoadsideLandmark(id = "motomachi_western_2", name = "元町洋館2", x = 184.72f, y = 344.42f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 0.95f),
            RoadsideLandmark(id = "motomachi_western_3", name = "元町洋館3", x = 184.88f, y = 344.25f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 1.05f),
            RoadsideLandmark(id = "motomachi_western_4", name = "元町洋館4", x = 184.65f, y = 344.15f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 0.90f),
            RoadsideLandmark(id = "motomachi_western_5", name = "元町洋館5", x = 184.82f, y = 344.00f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 1.00f),
            RoadsideLandmark(id = "motomachi_western_6", name = "元町洋館6", x = 184.45f, y = 343.70f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 1.05f),
            RoadsideLandmark(id = "motomachi_western_7", name = "元町洋館7", x = 184.60f, y = 343.55f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 0.95f),

            // City Block Residential Cabins
            RoadsideLandmark(id = "motomachi_cabin_1", name = "元町民家1", x = 184.95f, y = 344.12f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "motomachi_cabin_2", name = "元町民家2", x = 185.08f, y = 344.28f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.90f),
            RoadsideLandmark(id = "motomachi_cabin_3", name = "元町民家3", x = 184.35f, y = 343.50f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "motomachi_cabin_4", name = "元町民家4", x = 184.50f, y = 343.35f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.90f),

            // Streetlamps along Motomachi slopes
            RoadsideLandmark(id = "slope_lamp_1", name = "坂道街灯1", x = 184.48f, y = 344.70f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "slope_lamp_2", name = "坂道街灯2", x = 184.60f, y = 344.52f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.95f),
            RoadsideLandmark(id = "slope_lamp_3", name = "坂道街灯3", x = 184.75f, y = 344.35f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "slope_lamp_4", name = "坂道街灯4", x = 184.90f, y = 344.18f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.95f),
            RoadsideLandmark(id = "slope_lamp_5", name = "坂道街灯5", x = 185.05f, y = 344.02f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "slope_lamp_6", name = "坂道街灯6", x = 184.38f, y = 343.65f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),
            RoadsideLandmark(id = "slope_lamp_7", name = "坂道街灯7", x = 184.52f, y = 343.48f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),

            // Street Trees throughout town blocks
            RoadsideLandmark(id = "town_tree_1", name = "街路樹1", x = 184.50f, y = 344.40f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "town_tree_2", name = "街路樹2", x = 184.68f, y = 344.28f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "town_tree_3", name = "街路樹3", x = 184.82f, y = 344.12f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "town_tree_4", name = "街路樹4", x = 184.98f, y = 344.45f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "town_tree_5", name = "街路樹5", x = 185.12f, y = 344.55f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "town_tree_6", name = "街路樹6", x = 184.28f, y = 343.58f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "town_tree_7", name = "街路樹7", x = 184.42f, y = 343.40f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "town_tree_8", name = "街路樹8", x = 184.58f, y = 343.25f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // =========================================================================
            // 3. HAKODATE HARBOR & WATERFRONT (函館湾・赤レンガ倉庫街・海上)
            // =========================================================================
            // Stepped warehouse row along the bay curve
            RoadsideLandmark(id = "kanemori_warehouse_1", name = "金森倉庫1", x = 184.85f, y = 343.68f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.20f, reactionDialogue = "「海沿いの赤レンガ倉庫街だ！」"),
            RoadsideLandmark(id = "kanemori_warehouse_2", name = "金森倉庫2", x = 184.98f, y = 343.54f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.05f),
            RoadsideLandmark(id = "kanemori_warehouse_3", name = "金森倉庫3", x = 185.12f, y = 343.41f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.15f),
            RoadsideLandmark(id = "kanemori_warehouse_4", name = "金森倉庫4", x = 185.25f, y = 343.28f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.00f),
            RoadsideLandmark(id = "kanemori_warehouse_5", name = "金森倉庫5", x = 185.38f, y = 343.16f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.10f),
            RoadsideLandmark(id = "kanemori_warehouse_6", name = "金森倉庫6", x = 185.52f, y = 343.04f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 0.95f),

            // Quayside boardwalk gas lamps
            RoadsideLandmark(id = "quay_lamp_1", name = "波止場街灯1", x = 184.80f, y = 343.74f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "quay_lamp_2", name = "波止場街灯2", x = 184.92f, y = 343.60f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.95f),
            RoadsideLandmark(id = "quay_lamp_3", name = "波止場街灯3", x = 185.06f, y = 343.47f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "quay_lamp_4", name = "波止場街灯4", x = 185.20f, y = 343.34f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.95f),
            RoadsideLandmark(id = "quay_lamp_5", name = "波止場街灯5", x = 185.33f, y = 343.22f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "quay_lamp_6", name = "波止場街灯6", x = 185.46f, y = 343.10f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.95f),
            RoadsideLandmark(id = "quay_lamp_7", name = "波止場街灯7", x = 185.58f, y = 342.98f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),

            // Fishing boats & lighthouse in Hakodate Bay
            RoadsideLandmark(id = "hako_bay_boat_1", name = "函館湾漁船1", x = 184.65f, y = 343.40f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 1.05f, reactionDialogue = "「港に船が浮かんでる！」"),
            RoadsideLandmark(id = "hako_bay_boat_2", name = "函館湾漁船2", x = 184.82f, y = 343.10f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 0.90f),
            RoadsideLandmark(id = "hako_bay_boat_3", name = "函館湾漁船3", x = 185.10f, y = 342.75f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 1.15f),
            RoadsideLandmark(id = "hako_harbor_lighthouse", name = "函館港防波堤灯台", x = 184.45f, y = 343.20f, type = RoadsideLandmarkType.CAPE_LIGHTHOUSE, scale = 1.20f),

            // =========================================================================
            // 4. HAKODATE STATION TO GORYOKAKU (並木道・駅前通り)
            // =========================================================================
            // Tree-lined avenue flanking both sides of the boulevard
            RoadsideLandmark(id = "avenue_left_1", name = "並木道左1", x = 185.80f, y = 342.80f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "avenue_right_1", name = "並木道右1", x = 185.95f, y = 342.95f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "avenue_left_2", name = "並木道左2", x = 186.10f, y = 342.60f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "avenue_right_2", name = "並木道右2", x = 186.25f, y = 342.75f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "avenue_left_3", name = "並木道左3", x = 186.40f, y = 342.50f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "avenue_right_3", name = "並木道右3", x = 186.55f, y = 342.65f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "avenue_left_4", name = "並木道左4", x = 186.70f, y = 342.30f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "avenue_right_4", name = "並木道右4", x = 186.85f, y = 342.45f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "avenue_left_5", name = "並木道左5", x = 187.00f, y = 342.00f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "avenue_right_5", name = "並木道右5", x = 187.15f, y = 342.15f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),

            // Gas lamps along boulevard
            RoadsideLandmark(id = "daimon_lamp_1", name = "大門街灯1", x = 185.70f, y = 342.85f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "daimon_lamp_2", name = "大門街灯2", x = 186.00f, y = 342.68f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),
            RoadsideLandmark(id = "daimon_lamp_3", name = "千代台街灯1", x = 186.30f, y = 342.58f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "daimon_lamp_4", name = "千代台街灯2", x = 186.60f, y = 342.38f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),

            // Goryokaku Star Fortress park groves
            RoadsideLandmark(id = "goryokaku_forest_1", name = "五稜郭樹林1", x = 187.35f, y = 341.35f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "goryokaku_birch_1", name = "五稜郭白樺1", x = 187.55f, y = 341.10f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "goryokaku_forest_2", name = "五稜郭樹林2", x = 187.75f, y = 340.85f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.30f),
            RoadsideLandmark(id = "goryokaku_birch_2", name = "五稜郭白樺2", x = 187.90f, y = 340.65f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "goryokaku_forest_3", name = "五稜郭樹林3", x = 187.65f, y = 340.75f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "goryokaku_birch_3", name = "五稜郭白樺3", x = 187.45f, y = 340.95f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "goryokaku_pavilion", name = "公園東屋", x = 187.60f, y = 341.00f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.05f),

            // =========================================================================
            // 5. KIKYO & ISHIKAWA COUNTRYSIDE (桔梗・石川の田園・白樺街道)
            // =========================================================================
            RoadsideLandmark(id = "ishikawa_forest", name = "石川樹林", x = 186.70f, y = 339.80f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "ishikawa_birch_1", name = "石川並木1", x = 186.35f, y = 338.80f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "ishikawa_cabin", name = "石川の民家", x = 186.15f, y = 338.20f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "kikyo_cabin", name = "桔梗の民家", x = 185.95f, y = 337.50f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "kikyo_lamp", name = "桔梗街灯", x = 185.60f, y = 336.85f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "kikyo_birch", name = "桔梗白樺", x = 185.15f, y = 336.35f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "kikyo_forest", name = "桔梗山林", x = 184.85f, y = 335.95f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),

            // =========================================================================
            // 6. NANAE DAIRY PASTURE & FARMLAND (七飯放牧地・大牧場・国道並木)
            // =========================================================================
            // The Herd of Dairy Cows (8 cows of varied sizes & postures)
            RoadsideLandmark(id = "nanae_cow_herd_1", name = "七飯放牧牛1(親牛)", x = 182.45f, y = 331.60f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.15f, reactionDialogue = "「のんびり草を食べてる！」"),
            RoadsideLandmark(id = "nanae_cow_herd_2", name = "七飯放牧牛2(子牛)", x = 182.25f, y = 331.40f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.70f),
            RoadsideLandmark(id = "nanae_cow_herd_3", name = "七飯放牧牛3(採食)", x = 182.68f, y = 331.25f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.00f),
            RoadsideLandmark(id = "nanae_cow_herd_4", name = "七飯放牧牛4(休憩)", x = 182.05f, y = 331.70f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.95f),
            RoadsideLandmark(id = "nanae_cow_herd_5", name = "七飯放牧牛5(大)", x = 182.80f, y = 331.50f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.05f),
            RoadsideLandmark(id = "nanae_cow_herd_6", name = "七飯放牧牛6(子牛)", x = 182.50f, y = 331.05f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.65f),
            RoadsideLandmark(id = "nanae_cow_herd_7", name = "七飯放牧牛7", x = 182.15f, y = 330.95f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.90f),
            RoadsideLandmark(id = "nanae_cow_herd_8", name = "七飯放牧牛8", x = 181.75f, y = 331.30f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.00f),

            // Ranch Barns & Cabins
            RoadsideLandmark(id = "nanae_farm_barn", name = "牧場の赤い納屋", x = 181.85f, y = 331.45f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.15f),
            RoadsideLandmark(id = "nanae_hay_shed", name = "牧草小屋", x = 182.10f, y = 330.70f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.95f),
            RoadsideLandmark(id = "nanae_farmhouse", name = "牧場主の家", x = 181.50f, y = 331.65f, type = RoadsideLandmarkType.WESTERN_HOUSE, scale = 1.05f),

            // Meadow mid-field elements
            RoadsideLandmark(id = "nanae_pasture_cabin_mid", name = "放牧地小屋", x = 182.35f, y = 331.85f, type = RoadsideLandmarkType.LOG_CABIN, scale = 0.90f),
            RoadsideLandmark(id = "nanae_pasture_birch_mid", name = "放牧地の一本白樺", x = 182.10f, y = 331.25f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // Roadside Tree Avenue (Route 5 Highway flanking both sides)
            RoadsideLandmark(id = "nanae_road_left_1", name = "国道並木左1", x = 182.90f, y = 331.85f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "nanae_road_right_1", name = "国道並木右1", x = 183.15f, y = 331.75f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "nanae_road_left_2", name = "国道並木左2", x = 182.70f, y = 331.45f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "nanae_road_right_2", name = "国道並木右2", x = 182.95f, y = 331.35f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "nanae_road_left_3", name = "国道並木左3", x = 182.50f, y = 331.05f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "nanae_road_right_3", name = "国道並木右3", x = 182.75f, y = 330.95f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.90f),
            RoadsideLandmark(id = "nanae_road_left_4", name = "国道並木左4", x = 182.30f, y = 330.65f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.00f),
            RoadsideLandmark(id = "nanae_road_right_4", name = "国道並木右4", x = 182.55f, y = 330.55f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.20f),
            RoadsideLandmark(id = "nanae_road_left_5", name = "国道並木左5", x = 182.10f, y = 330.25f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "nanae_road_right_5", name = "国道並木右5", x = 182.35f, y = 330.15f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),

            // Windbreak Forest Groves framing the pasture
            RoadsideLandmark(id = "nanae_windbreak_1", name = "防風林1", x = 181.60f, y = 332.05f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "nanae_windbreak_2", name = "防風林2", x = 181.90f, y = 332.20f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "nanae_windbreak_3", name = "防風林3", x = 182.25f, y = 332.35f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "nanae_windbreak_4", name = "防風林4", x = 182.55f, y = 332.40f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.30f),
            RoadsideLandmark(id = "nanae_windbreak_5", name = "防風林5", x = 181.40f, y = 331.20f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),
            RoadsideLandmark(id = "nanae_windbreak_6", name = "防風林6", x = 181.35f, y = 330.80f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.10f),
            RoadsideLandmark(id = "nanae_windbreak_7", name = "防風林7", x = 181.45f, y = 330.40f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "nanae_windbreak_8", name = "防風林8", x = 181.70f, y = 330.30f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "nanae_windbreak_9", name = "防風林9", x = 182.00f, y = 330.15f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),

            // =========================================================================
            // 7. NIYAMA & ONUMA LAKESIDE (仁山高原・大沼国定公園)
            // =========================================================================
            RoadsideLandmark(id = "niyama_forest_1", name = "仁山樹林1", x = 179.35f, y = 329.80f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.25f),
            RoadsideLandmark(id = "niyama_birch_1", name = "仁山白樺1", x = 178.90f, y = 329.50f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "niyama_cabin", name = "仁山の丸太小屋", x = 178.50f, y = 329.20f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.10f),
            RoadsideLandmark(id = "niyama_forest_2", name = "仁山樹林2", x = 178.10f, y = 328.90f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "onuma_woods_1", name = "大沼森林1", x = 180.35f, y = 325.40f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.30f),
            RoadsideLandmark(id = "onuma_woods_2", name = "大沼白樺2", x = 180.85f, y = 324.70f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.05f),
            RoadsideLandmark(id = "onuma_lakeside_cabin", name = "大沼湖畔の山小屋", x = 181.25f, y = 324.10f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.05f),
            RoadsideLandmark(id = "onuma_boat", name = "大沼遊覧ボート", x = 180.95f, y = 324.35f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 0.95f),
            RoadsideLandmark(id = "onuma_woods_3", name = "大沼森林3", x = 180.50f, y = 323.50f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),

            // =========================================================================
            // 8. SHAKOTAN PENINSULA & OTARU (積丹半島・余市・小樽運河)
            // =========================================================================
            RoadsideLandmark(id = "kamui_cape_lighthouse", name = "神威岬灯台", x = 160.80f, y = 207.50f, type = RoadsideLandmarkType.CAPE_LIGHTHOUSE, scale = 1.30f),
            RoadsideLandmark(id = "kamui_cape_boat", name = "神威岬沖の漁船", x = 160.20f, y = 207.10f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 1.00f),
            RoadsideLandmark(id = "kamui_cape_forest", name = "神威岬樹林", x = 161.40f, y = 208.20f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.15f),
            RoadsideLandmark(id = "yoichi_apple_cabin", name = "余市の果樹園小屋", x = 191.00f, y = 221.20f, type = RoadsideLandmarkType.LOG_CABIN, scale = 1.00f),
            RoadsideLandmark(id = "yoichi_birch_grove", name = "余市白樺林", x = 191.60f, y = 221.50f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.10f),
            RoadsideLandmark(id = "otaru_warehouse_1", name = "小樽運河倉庫1", x = 203.60f, y = 221.70f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.15f),
            RoadsideLandmark(id = "otaru_warehouse_2", name = "小樽運河倉庫2", x = 204.05f, y = 221.40f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 1.05f),
            RoadsideLandmark(id = "otaru_warehouse_3", name = "小樽運河倉庫3", x = 204.40f, y = 221.15f, type = RoadsideLandmarkType.RED_BRICK_WAREHOUSE, scale = 0.95f),
            RoadsideLandmark(id = "otaru_canal_boat", name = "小樽運河ボート", x = 203.85f, y = 221.25f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 0.90f),
            RoadsideLandmark(id = "otaru_canal_lamp_1", name = "小樽運河ガス灯1", x = 204.25f, y = 221.30f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.90f),
            RoadsideLandmark(id = "otaru_canal_lamp_2", name = "小樽運河ガス灯2", x = 204.65f, y = 220.95f, type = RoadsideLandmarkType.RETRO_GAS_LAMP, scale = 0.85f),

            // =========================================================================
            // 9. ISHIKARI & ORORON COASTAL HIGHWAY (オロロンライン風車群・日本海)
            // =========================================================================
            RoadsideLandmark(id = "ishikari_wind_1", name = "石狩風車1", x = 227.70f, y = 210.80f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.15f),
            RoadsideLandmark(id = "ishikari_wind_2", name = "石狩風車2", x = 228.30f, y = 209.60f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.00f),
            RoadsideLandmark(id = "ishikari_boat", name = "石狩湾漁船", x = 226.80f, y = 210.20f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 1.05f),
            RoadsideLandmark(id = "mashike_coastal_trees", name = "増毛海岸樹林", x = 225.20f, y = 171.20f, type = RoadsideLandmarkType.FOREST_GROVE, scale = 1.20f),
            RoadsideLandmark(id = "tomamae_wind_1", name = "苫前風車1", x = 246.50f, y = 126.30f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.20f),
            RoadsideLandmark(id = "tomamae_wind_2", name = "苫前風車2", x = 247.10f, y = 125.10f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.05f),
            RoadsideLandmark(id = "tomamae_wind_3", name = "苫前風車3", x = 247.65f, y = 123.90f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 0.90f),

            // =========================================================================
            // 10. SAROBETSU & CAPE SOYA (サロベツ原野・宗谷岬)
            // =========================================================================
            RoadsideLandmark(id = "sarobetsu_birch_1", name = "サロベツ白樺1", x = 251.40f, y = 54.30f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 1.15f),
            RoadsideLandmark(id = "sarobetsu_birch_2", name = "サロベツ白樺2", x = 251.90f, y = 53.10f, type = RoadsideLandmarkType.BIRCH_TREES, scale = 0.95f),
            RoadsideLandmark(id = "teshio_soya_cow_1", name = "宗谷放牧牛1", x = 250.90f, y = 70.80f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.15f),
            RoadsideLandmark(id = "teshio_soya_cow_2", name = "宗谷放牧牛2(子牛)", x = 251.25f, y = 70.45f, type = RoadsideLandmarkType.DAIRY_COW, scale = 0.75f),
            RoadsideLandmark(id = "teshio_soya_cow_3", name = "宗谷放牧牛3", x = 251.60f, y = 70.10f, type = RoadsideLandmarkType.DAIRY_COW, scale = 1.00f),
            RoadsideLandmark(id = "soya_cape_lighthouse", name = "宗谷岬灯台", x = 263.20f, y = 13.50f, type = RoadsideLandmarkType.CAPE_LIGHTHOUSE, scale = 1.35f),
            RoadsideLandmark(id = "soya_fishing_boat", name = "宗谷岬沖合漁船", x = 263.90f, y = 12.80f, type = RoadsideLandmarkType.FISHING_BOAT, scale = 1.10f),
            RoadsideLandmark(id = "soya_wind_turbine", name = "宗谷丘陵風車", x = 264.40f, y = 14.50f, type = RoadsideLandmarkType.COASTAL_WINDMILL, scale = 1.15f),
        )
    }
}
