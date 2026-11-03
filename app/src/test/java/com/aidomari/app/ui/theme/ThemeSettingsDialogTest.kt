package com.aidomari.app.ui.theme

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeSettingsDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_rendersAllOptionsAndTriggersCallback() {
        var latestConfig = ThemeConfig(ThemeMode.SYSTEM, AppColorTheme.INDIGO)
        var dismissed = false

        composeTestRule.setContent {
            ThemeSettingsDialog(
                currentConfig = latestConfig,
                onConfigChanged = { latestConfig = it },
                onDismissRequest = { dismissed = true },
            )
        }

        // Title and sections
        composeTestRule.onNodeWithText("テーマ・外観設定").assertIsDisplayed()
        composeTestRule.onNodeWithText("表示モード").assertIsDisplayed()
        composeTestRule.onNodeWithText("カラーテーマ").assertIsDisplayed()

        // Theme modes
        composeTestRule.onNodeWithTag("ThemeMode_SYSTEM").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ThemeMode_LIGHT").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ThemeMode_DARK").assertIsDisplayed()

        // Color themes
        composeTestRule.onNodeWithTag("ColorTheme_INDIGO").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ColorTheme_FOREST").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ColorTheme_SAKURA").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ColorTheme_AMBER").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ColorTheme_SLATE").assertIsDisplayed()

        // Change mode to DARK
        composeTestRule.onNodeWithTag("ThemeMode_DARK").performClick()
        assertEquals(ThemeMode.DARK, latestConfig.mode)

        // Change color to SAKURA
        composeTestRule.onNodeWithTag("ColorTheme_SAKURA").performClick()
        assertEquals(AppColorTheme.SAKURA, latestConfig.colorTheme)

        // Dismiss
        composeTestRule.onNodeWithTag("CloseThemeSettingsButton").performClick()
        assertTrue(dismissed)
    }
}
