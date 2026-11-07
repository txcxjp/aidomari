package com.aidomari.app.ui.lockscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.aidomari.app.core.lockscreen.LockScreenConfig
import com.aidomari.app.core.lockscreen.LockScreenTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LockScreenSettingsDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lockScreenSettingsDialog_displaysTitleAndOptions() {
        composeTestRule.setContent {
            LockScreenSettingsDialog(
                config = LockScreenConfig(isEnabled = true, target = LockScreenTarget.ALL),
                onConfigChanged = {},
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithText("ロック画面学習の設定").assertExists()
        composeTestRule.onNodeWithTag("TargetAllRadio").assertExists()
        composeTestRule.onNodeWithTag("TargetBookmarkRadio").assertExists()
        composeTestRule.onNodeWithTag("TargetDaysAgoRadio").assertExists()
    }

    @Test
    fun selectDaysAgoTarget_callsOnConfigChangedWithDaysAgo() {
        var currentConfig by mutableStateOf(
            LockScreenConfig(isEnabled = true, target = LockScreenTarget.ALL),
        )
        composeTestRule.setContent {
            LockScreenSettingsDialog(
                config = currentConfig,
                onConfigChanged = { currentConfig = it },
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithTag("TargetDaysAgoRadio").performScrollTo().performClick()
        assertEquals(LockScreenTarget.DAYS_AGO, currentConfig.target)
    }

    @Test
    fun daysAgoTargetSelected_displaysThresholdChips_andUpdatesThresholdOnSelect() {
        var currentConfig by mutableStateOf(
            LockScreenConfig(
                isEnabled = true,
                target = LockScreenTarget.DAYS_AGO,
                daysThreshold = 3,
            ),
        )
        composeTestRule.setContent {
            LockScreenSettingsDialog(
                config = currentConfig,
                onConfigChanged = { currentConfig = it },
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithTag("DaysThresholdChip_7").assertExists()
        composeTestRule.onNodeWithTag("DaysThresholdChip_7").performScrollTo().performClick()
        assertEquals(7, currentConfig.daysThreshold)
    }

    @Test
    fun dismissButton_callsOnDismissRequest() {
        var dismissed = false
        composeTestRule.setContent {
            LockScreenSettingsDialog(
                config = LockScreenConfig(isEnabled = true),
                onConfigChanged = {},
                onDismissRequest = { dismissed = true },
            )
        }

        composeTestRule.onNodeWithText("閉じる").performClick()
        assertTrue(dismissed)
    }
}
