package com.aidomari.app.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aidomari.app.core.ai.GeminiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiSettingsDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_rendersFields_andSavesUpdatedConfig() {
        var latestConfig = GeminiConfig(apiKey = "", modelName = "gemini-1.5-flash")
        var dismissed = false

        composeTestRule.setContent {
            GeminiSettingsDialog(
                config = latestConfig,
                onConfigChanged = { latestConfig = it },
                onTestConnection = { _, _ -> Result.success(true) },
                onDismissRequest = { dismissed = true },
            )
        }

        // Verify title & info
        composeTestRule.onNodeWithText("AIデッキ化設定 (Gemini)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("GeminiApiKeyInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("GeminiTestConnectionButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("GeminiSaveButton").assertIsDisplayed()

        // Input API Key
        composeTestRule.onNodeWithTag("GeminiApiKeyInput").performTextInput("AIzaSyTest123")

        // Save
        composeTestRule.onNodeWithTag("GeminiSaveButton").performClick()

        assertEquals("AIzaSyTest123", latestConfig.apiKey)
        assertTrue(dismissed)
    }

    @Test
    fun dialog_testConnection_displaysSuccessBadge() {
        val config = GeminiConfig(apiKey = "valid_key", modelName = "gemini-1.5-flash")
        var connectionTested = false

        composeTestRule.setContent {
            GeminiSettingsDialog(
                config = config,
                onConfigChanged = {},
                onTestConnection = { key, model ->
                    connectionTested = true
                    assertEquals("valid_key", key)
                    assertEquals("gemini-1.5-flash", model)
                    Result.success(true)
                },
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithTag("GeminiTestConnectionButton").performClick()
        composeTestRule.waitForIdle()

        assertTrue(connectionTested)
        composeTestRule.onNodeWithText("接続成功").assertIsDisplayed()
    }

    @Test
    fun dialog_testConnection_displaysErrorBadgeOnFailure() {
        val config = GeminiConfig(apiKey = "invalid_key", modelName = "gemini-1.5-flash")

        composeTestRule.setContent {
            GeminiSettingsDialog(
                config = config,
                onConfigChanged = {},
                onTestConnection = { _, _ ->
                    Result.failure(IllegalArgumentException("無効なAPIキーです"))
                },
                onDismissRequest = {},
            )
        }

        composeTestRule.onNodeWithTag("GeminiTestConnectionButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("接続失敗").assertExists()
        composeTestRule.onNodeWithText("無効なAPIキーです").assertExists()
    }
}
