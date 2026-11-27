package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.Act1TravelRoute
import com.aidomari.app.domain.model.Act1TravelRouteProgressCalculator
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.SugorokuProgress
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h851dp-xxhdpi")
class SugorokuMapScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureSugorokuMap_initialState() {
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 0),
                    currentNode = HokkaidoAct1Route.startNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_initial.png")
    }

    @Test
    fun captureSugorokuMap_continuousWalking_300m_onRoad() {
        val legs = Act1TravelRoute.getLegs()
        val routeProgress = Act1TravelRouteProgressCalculator.calculateProgress(300f, legs)

        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(totalDistanceMeters = 300f),
                    currentNode = HokkaidoAct1Route.startNode,
                    totalDistanceMeters = 300f,
                    playerWorldOffset = routeProgress.currentWorldOffset,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_walking_300m_road.png")
    }

    @Test
    fun captureSugorokuMap_zoomedIn() {
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 0),
                    currentNode = HokkaidoAct1Route.startNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        // Click zoom in button
        composeTestRule.onNodeWithContentDescription("拡大").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_zoomed_in.png")
    }

    @Test
    fun captureSugorokuMap_hakodateZoomedOut() {
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 0),
                    currentNode = HokkaidoAct1Route.startNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        repeat(5) {
            composeTestRule.onNodeWithContentDescription("縮小").performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_hakodate_zoomed_out.png")
    }

    @Test
    fun captureSugorokuMap_step10_goryokaku() {
        val goryokakuNode = HokkaidoAct1Route.getNodeById("goryokaku") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = goryokakuNode.stepIndex),
                    currentNode = goryokakuNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step10.png")
    }

    @Test
    fun captureSugorokuMap_step31_shin_hakodate_hokuto() {
        val shinkansenNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "shin_hakodate_hokuto_station" }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = shinkansenNode.stepIndex),
                    currentNode = shinkansenNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step31_shin_hakodate_hokuto.png")
    }

    @Test
    fun captureSugorokuMap_step36_onuma_komagatake() {
        val higureNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "mt_higure" }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = higureNode.stepIndex),
                    currentNode = higureNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step36_onuma_komagatake.png")
    }

    @Test
    fun captureSugorokuMap_step37_komagatake_onsen() {
        val akaigawaNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "akaigawa_station" }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = akaigawaNode.stepIndex),
                    currentNode = akaigawaNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step37_komagatake_onsen.png")
    }

    @Test
    fun captureSugorokuMap_step43_washinoki_coast() {
        val washinokiNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "anchor_797ebdbf" }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = washinokiNode.stepIndex),
                    currentNode = washinokiNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step43_washinoki_coast.png")
    }

    @Test
    fun captureSugorokuMap_step50_funkawan_yakumo() {
        val yakumoNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "funkawan_panorama_park" }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = yakumoNode.stepIndex),
                    currentNode = yakumoNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step50_funkawan_yakumo.png")
    }

    @Test
    fun captureSugorokuMap_roadsideLandmark_cow_and_reaction() {
        val startNode = HokkaidoAct1Route.startNode
        val warehouseLandmark = com.aidomari.app.domain.model.RoadsideLandmark.ACT1_LANDMARKS.first { it.id == "kanemori_warehouse_row" }
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = startNode.stepIndex),
                    currentNode = startNode,
                    playerWorldOffset = androidx.compose.ui.geometry.Offset(warehouseLandmark.x + 0.25f, warehouseLandmark.y + 0.14f),
                    nearbyLandmark = warehouseLandmark,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_cow_reaction.png")
    }

    @Test
    fun captureSugorokuMap_step250_suttsu_area() {
        val suttsuNode = HokkaidoAct1Route.getNodeById("minatomare_suttsu_michinoeki")
            ?: HokkaidoAct1Route.nodes.firstOrNull { it.name.contains("寿都") }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = suttsuNode.stepIndex),
                    currentNode = suttsuNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        // Unlock scroll restriction
        composeTestRule.onNodeWithTag("DebugScrollLockButton").performClick()
        composeTestRule.waitForIdle()

        // Zoom out 3 times to see highway network around Suttsu/Motsuta/Raiden
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("縮小").performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step250_suttsu_area.png")
    }

    @Test
    fun captureSugorokuMap_step377_kabuto_cape() {
        val kamuiNode = HokkaidoAct1Route.getNodeById("kamui_cape") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = kamuiNode.stepIndex),
                    currentNode = kamuiNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step377_kabuto_cape.png")
    }

    @Test
    fun captureSugorokuMap_step1162_noshappu() {
        val noshappuNode = HokkaidoAct1Route.getNodeById("minato_no_yu_onsen")
            ?: HokkaidoAct1Route.getNodeById("anchor_98c6e368")
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = noshappuNode.stepIndex),
                    currentNode = noshappuNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step1162_noshappu.png")
    }

    @Test
    fun captureSugorokuMap_step1169_ferry_terminal() {
        val ferryNode = HokkaidoAct1Route.getNodeById("haboro_rose_garden")
            ?: HokkaidoAct1Route.getNodeById("haboro_hashi")
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = ferryNode.stepIndex),
                    currentNode = ferryNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step1169_ferry_terminal.png")
    }

    @Test
    fun captureSugorokuMap_step1495_saruru_cape() {
        val hinodeNode = HokkaidoAct1Route.getNodeById("saruru_cape")
            ?: HokkaidoAct1Route.getNodeById("la_luna_observatory")
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = hinodeNode.stepIndex),
                    currentNode = hinodeNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step1495_saruru_cape.png")
    }

    @Test
    fun captureSugorokuMap_step1218_soya() {
        val soyaNode = HokkaidoAct1Route.getNodeById("soya_cape") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = soyaNode.stepIndex),
                    currentNode = soyaNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step1218_soya.png")
    }

    @Test
    fun captureSugorokuMap_step1218_soya_wide() {
        val soyaNode = HokkaidoAct1Route.getNodeById("soya_cape") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = soyaNode.stepIndex),
                    currentNode = soyaNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        // Unlock scroll restriction
        composeTestRule.onNodeWithTag("DebugScrollLockButton").performClick()
        composeTestRule.waitForIdle()

        // Zoom out to see Soya Cape coastline
        repeat(6) {
            composeTestRule.onNodeWithContentDescription("縮小").performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step1218_soya_wide.png")
    }

    @Test
    fun captureSugorokuMap_debugZoomedOut() {
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 0),
                    currentNode = HokkaidoAct1Route.startNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        // Unlock scroll restriction
        composeTestRule.onNodeWithTag("DebugScrollLockButton").performClick()
        composeTestRule.waitForIdle()

        // Click zoom out button multiple times to reach minScale and see full Hokkaido
        repeat(30) {
            composeTestRule.onNodeWithContentDescription("縮小").performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_debug_zoomed_out.png")
    }

    @Test
    fun captureSugorokuMap_step_teshio_area() {
        val teshioRiverNode = HokkaidoAct1Route.getNodeById("teshio_river_mouth_bridge") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = teshioRiverNode.stepIndex),
                    currentNode = teshioRiverNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step_teshio.png")
    }

    @Test
    fun captureSugorokuMap_step_mombetsu_area() {
        val garinkoNode = HokkaidoAct1Route.getNodeById("kani_no_tsume_object")
            ?: HokkaidoAct1Route.getNodeById("port_of_monbetsu")
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = garinkoNode.stepIndex),
                    currentNode = garinkoNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step_mombetsu.png")
    }

    @Test
    fun captureSugorokuMap_step311_otonrui_wind_farm() {
        val otonruiNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "otonrui_wind_farm" }
            ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = otonruiNode.stepIndex),
                    currentNode = otonruiNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step311_otonrui_wind_farm.png")
    }

    @Test
    fun captureSugorokuMap_step444_rausu_whale() {
        val whaleNode = HokkaidoAct1Route.nodes.firstOrNull { it.id == "whale_watching_hill_park" }
            ?: HokkaidoAct1Route.goalNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = whaleNode.stepIndex),
                    currentNode = whaleNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step444_rausu_whale.png")
    }

    @Test
    fun captureSugorokuMap_step1999_aidomari() {
        val aidomariNode = HokkaidoAct1Route.goalNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = aidomariNode.stepIndex),
                    currentNode = aidomariNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step1999_aidomari.png")
    }

    @Test
    fun captureSugorokuMap_step161_otaru_canal() {
        val otaruNode = HokkaidoAct1Route.getNodeById("otaru_canal") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = otaruNode.stepIndex),
                    currentNode = otaruNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step161_otaru_canal.png")
    }

    @Test
    fun captureSugorokuMap_step213_sapporo_clock_tower() {
        val sapporoNode = HokkaidoAct1Route.getNodeById("sapporo_clock_tower") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = sapporoNode.stepIndex),
                    currentNode = sapporoNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step213_sapporo_clock_tower.png")
    }

    @Test
    fun captureSugorokuMap_step398_abashiri_prison() {
        val abashiriNode = HokkaidoAct1Route.getNodeById("abashiri_prison_museum") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = abashiriNode.stepIndex),
                    currentNode = abashiriNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step398_abashiri_prison.png")
    }

    @Test
    fun captureSugorokuMap_step432_shiretoko_five_lakes() {
        val shiretokoNode = HokkaidoAct1Route.getNodeById("shiretoko_five_lakes") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = shiretokoNode.stepIndex),
                    currentNode = shiretokoNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step432_shiretoko_five_lakes.png")
    }

    @Test
    fun captureSugorokuMap_step434_shiretoko_pass() {
        val passNode = HokkaidoAct1Route.getNodeById("shiretoko_pass_observatory") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = passNode.stepIndex),
                    currentNode = passNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_step434_shiretoko_pass.png")
    }

    @Test
    fun captureSugorokuMap_anchorArrivalCelebration() {
        val goryokakuNode = HokkaidoAct1Route.getNodeById("goryokaku") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = goryokakuNode.stepIndex, totalDistanceMeters = 8400f),
                    currentNode = goryokakuNode,
                    arrivalCelebrationNode = goryokakuNode,
                    totalDistanceMeters = 8400f,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
                onDismissCelebration = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_anchor_arrival_celebration.png")
    }

    @Test
    fun captureSugorokuMap_wakkanai_dome() {
        val wakkanaiNode = HokkaidoAct1Route.getNodeById("anchor_98c6e368") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = wakkanaiNode.stepIndex),
                    currentNode = wakkanaiNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_wakkanai_dome.png")
    }

    @Test
    fun captureSugorokuMap_otoppu_cape() {
        val otoppuNode = HokkaidoAct1Route.getNodeById("otoineppu_cape") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = otoppuNode.stepIndex),
                    currentNode = otoppuNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_otoppu_cape.png")
    }

    @Test
    fun captureSugorokuMap_oshinkoshin_falls() {
        val oshinkoshinNode = HokkaidoAct1Route.getNodeById("oshinkoshinno_falls") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = oshinkoshinNode.stepIndex),
                    currentNode = oshinkoshinNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_oshinkoshin_falls.png")
    }

    @Test
    fun captureSugorokuMap_sapporo_ramen_shop_card() {
        val hassamuNode = HokkaidoAct1Route.getNodeById("hassamu_chuo_station") ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = hassamuNode.stepIndex),
                    currentNode = hassamuNode,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("SugorokuLandmarkAnimCanvas").performTouchInput {
            click(Offset(centerX, centerY - 86.dp.toPx()))
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_sapporo_ramen_card.png")
    }

    private fun captureBatch1aSpot(anchorId: String, fileName: String) {
        val node = HokkaidoAct1Route.getNodeById(anchorId) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = node.stepIndex),
                    currentNode = node,
                ),
                onBack = {},
                onSelectNode = {},
                onDismissDialog = {},
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/$fileName")
    }

    @Test
    fun captureSugorokuMap_batch1a_onakayama() =
        captureBatch1aSpot("onakayama_station", "sugoroku_batch1a_01_onakayama.png")

    @Test
    fun captureSugorokuMap_batch1a_mochizuki() =
        captureBatch1aSpot("anchor_24fb4a9f", "sugoroku_batch1a_02_mochizuki.png")

    @Test
    fun captureSugorokuMap_batch1a_mori() =
        captureBatch1aSpot("you_yu_mori", "sugoroku_batch1a_05_mori.png")

    @Test
    fun captureSugorokuMap_batch1a_otoshibe() =
        captureBatch1aSpot("otoshibe_station", "sugoroku_batch1a_03_otoshibe.png")

    @Test
    fun captureSugorokuMap_batch1a_yakumo() =
        captureBatch1aSpot("yakumo_station", "sugoroku_batch1a_04_yakumo.png")

    @Test
    fun captureSugorokuMap_batch1b1_yawaragi() =
        captureBatch1aSpot("anchor_8090bd6c", "sugoroku_batch1b1_01_yawaragi.png")

    @Test
    fun captureSugorokuMap_batch1b1_kuroiwa() =
        captureBatch1aSpot("kuroiwa_station", "sugoroku_batch1b1_02_kuroiwa.png")

    @Test
    fun captureSugorokuMap_batch1b1_kunnui() =
        captureBatch1aSpot("kunnui_station", "sugoroku_batch1b1_03_kunnui.png")

    @Test
    fun captureSugorokuMap_batch1b1_oshamambe() =
        captureBatch1aSpot("oshamambe_station", "sugoroku_batch1b1_04_oshamambe.png")

    @Test
    fun captureSugorokuMap_batch1b1_shambedake() =
        captureBatch1aSpot("shambedake", "sugoroku_batch1b1_05_shambedake.png")

    @Test
    fun captureSugorokuMap_batch1b1_kuromatsunai() =
        captureBatch1aSpot("kuromatsunai_michinoeki", "sugoroku_batch1b1_06_kuromatsunai.png")

    @Test
    fun captureSugorokuMap_batch1b1_neppu() =
        captureBatch1aSpot("neppu_station", "sugoroku_batch1b1_07_neppu.png")

    @Test
    fun captureSugorokuMap_batch1b2_yubetsu() =
        captureBatch1aSpot("yubetsu_no_yu_onsen", "sugoroku_batch1b2_01_yubetsu.png")

    @Test
    fun captureSugorokuMap_batch1b2_suttsu() =
        captureBatch1aSpot("minatomare_suttsu_michinoeki", "sugoroku_batch1b2_02_suttsu.png")

    @Test
    fun captureSugorokuMap_batch1b2_rankoshi() =
        captureBatch1aSpot("anchor_1c34fc36", "sugoroku_batch1b2_03_rankoshi.png")

    @Test
    fun captureSugorokuMap_batch1b2_kannonyama() =
        captureBatch1aSpot("anchor_144e98d1", "sugoroku_batch1b2_04_kannonyama.png")

    @Test
    fun captureSugorokuMap_batch1b2_benkei() =
        captureBatch1aSpot("benkei_cape", "sugoroku_batch1b2_05_benkei.png")

    @Test
    fun captureSugorokuMap_batch1b2_kumanoyama() =
        captureBatch1aSpot("kumanoyama", "sugoroku_batch1b2_06_kumanoyama.png")

    @Test
    fun captureSugorokuMap_batch1b2_iwanai() =
        captureBatch1aSpot("anchor_5b0b7417", "sugoroku_batch1b2_07_iwanai.png")

    @Test
    fun captureSugorokuMap_batch1b3_iwanai_michinoeki() =
        captureBatch1aSpot("anchor_50650bcf", "sugoroku_batch1b3_01_iwanai_kyowa.png")

    @Test
    fun captureSugorokuMap_batch1b3_tomari_sakazuki() =
        captureBatch1aSpot("sakazuki_onsen_chokaso", "sugoroku_batch1b3_02_tomari.png")

    @Test
    fun captureSugorokuMap_batch1b3_kamoenai() =
        captureBatch1aSpot("anchor_cb97a679", "sugoroku_batch1b3_03_kamoenai.png")

    @Test
    fun captureSugorokuMap_batch1b3_shakotan_ogon() =
        captureBatch1aSpot("ogon_cape_shakotan", "sugoroku_batch1b3_04_shakotan_bikuni.png")

    @Test
    fun captureSugorokuMap_batch1b3_furubira() =
        captureBatch1aSpot("anchor_549550dc", "sugoroku_batch1b3_05_furubira.png")
}
