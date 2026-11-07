package com.aidomari.app.ui.lockscreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeTestRule.onNodeWithText("ロック画面学習の設定").assertIsDisplayed()
        composeTestRule.onNodeWithTag("TargetAllRadio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("TargetBookmarkRadio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("TargetDaysAgoRadio").assertIsDisplayed()
    }

    @Test
    fun selectDaysAgoTarget_callsOnConfigChangedWithDaysAgo() {
        var updatedConfig: LockScreenConfig? = null
        composeTestRule.setContent {
            LockScreenSettingsDialog(
                config = LockScreenConfig(isEnabled = true, target = LockScreenTarget.ALL),
                onConfigChanged = { updatedConfig = it },
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithTag("TargetDaysAgoRadio").performClick()
        assertEquals(LockScreenTarget.DAYS_AGO, updatedConfig?.target)
    }

    @Test
    fun daysAgoTargetSelected_displaysThresholdChips_andUpdatesThresholdOnSelect() {
        var updatedConfig: LockScreenConfig? = null
        composeTestRule.setContent {
            LockScreenSettingsDialog(
                config = LockScreenConfig(
                    isEnabled = true,
                    target = LockScreenTarget.DAYS_AGO,
                    daysThreshold = 3,
                ),
                onConfigChanged = { updatedConfig = it },
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithTag("DaysThresholdChip_7").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DaysThresholdChip_7").performClick()
        assertEquals(7, updatedConfig?.daysThreshold)
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
