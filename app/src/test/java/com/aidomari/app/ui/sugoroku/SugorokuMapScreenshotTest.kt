package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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
    fun captureSugorokuMap_step10_goryokaku() {
        val step10Node = HokkaidoAct1Route.getNodeByStepIndex(10) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 10),
                    currentNode = step10Node,
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
    fun captureSugorokuMap_step250_suttsu_area() {
        val suttsuNode = HokkaidoAct1Route.getNodeByStepIndex(250) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 250),
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
    fun captureSugorokuMap_step1162_noshappu() {
        val noshappuNode = HokkaidoAct1Route.getNodeByStepIndex(1162) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 1162),
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
        val ferryNode = HokkaidoAct1Route.getNodeByStepIndex(1169) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 1169),
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
        val saruruNode = HokkaidoAct1Route.getNodeByStepIndex(1495) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 1495),
                    currentNode = saruruNode,
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
        val soyaNode = HokkaidoAct1Route.getNodeByStepIndex(1218) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 1218),
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
        val soyaNode = HokkaidoAct1Route.getNodeByStepIndex(1218) ?: HokkaidoAct1Route.startNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 1218),
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
    fun captureSugorokuMap_step1999_aidomari() {
        val aidomariNode = HokkaidoAct1Route.goalNode
        composeTestRule.setContent {
            SugorokuMapContent(
                uiState = SugorokuMapUiState(
                    progress = SugorokuProgress(currentStepIndex = 1999),
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
}
