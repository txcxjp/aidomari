package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.ui.study.components.StudyTtsSettingsDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudyTtsSettingsDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_rendersTtsSettingsAndSavesUpdatedValues() {
        var savedAutoSpeak = false
        var savedSpeechRate = 1.0f
        var testSpokenRate = 0f

        composeTestRule.setContent {
            StudyTtsSettingsDialog(
                autoSpeak = false,
                speechRate = 1.0f,
                onDismissRequest = {},
                onSave = { auto, rate ->
                    savedAutoSpeak = auto
                    savedSpeechRate = rate
                },
                onTestSpeak = { rate ->
                    testSpokenRate = rate
                },
            )
        }

        composeTestRule.onNodeWithText("音声・読み上げ設定").assertIsDisplayed()
        composeTestRule.onNodeWithText("自動読み上げ").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.0x").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.25x").assertIsDisplayed()

        // Toggle auto-speak
        composeTestRule.onNodeWithText("自動読み上げ").performClick()

        // Select 1.25x
        composeTestRule.onNodeWithText("1.25x").performClick()

        // Click test speech button
        composeTestRule.onNodeWithText("発音テスト (1.25x)").performClick()
        assertEquals(1.25f, testSpokenRate, 0.001f)

        // Save
        composeTestRule.onNodeWithText("保存").performClick()
        assertTrue(savedAutoSpeak)
        assertEquals(1.25f, savedSpeechRate, 0.001f)
    }

    @Test
    fun dialog_cancelInvokesDismissRequest() {
        var dismissed = false

        composeTestRule.setContent {
            StudyTtsSettingsDialog(
                autoSpeak = true,
                speechRate = 1.0f,
                onDismissRequest = { dismissed = true },
                onSave = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("キャンセル").performClick()
        assertTrue(dismissed)
    }
}
