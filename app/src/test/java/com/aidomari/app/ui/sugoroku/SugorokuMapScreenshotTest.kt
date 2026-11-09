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

        // Click zoom out button multiple times to verify unlimited zoom out in debug mode
        repeat(7) {
            composeTestRule.onNodeWithContentDescription("縮小").performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.onRoot().captureRoboImage("build/outputs/roborazzi/sugoroku_map_debug_zoomed_out.png")
    }
}
