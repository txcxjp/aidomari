package com.aidomari.app.ui.deck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.aidomari.app.ui.deck.components.RandomQuestionsDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RandomQuestionsDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_rendersAllOptionsAndDefaultsToInitialCount() {
        var startedCount: Int? = null

        composeTestRule.setContent {
            RandomQuestionsDialog(
                totalCards = 100,
                initialCount = 10,
                onDismissRequest = {},
                onStartStudy = { startedCount = it },
            )
        }

        composeTestRule.onNodeWithText("ランダム出題の設定").assertIsDisplayed()
        composeTestRule.onNodeWithText("10問").assertIsDisplayed()
        composeTestRule.onNodeWithText("20問").assertIsDisplayed()
        composeTestRule.onNodeWithText("30問").assertIsDisplayed()
        composeTestRule.onNodeWithText("50問").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("全問 (100問)").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("学習開始").performClick()
        assertEquals(10, startedCount)
    }

    @Test
    fun dialog_selectCountAndStart_invokesCallbackWithSelectedCount() {
        var startedCount: Int? = null

        composeTestRule.setContent {
            RandomQuestionsDialog(
                totalCards = 100,
                initialCount = 10,
                onDismissRequest = {},
                onStartStudy = { startedCount = it },
            )
        }

        composeTestRule.onNodeWithText("30問").performClick()
        composeTestRule.onNodeWithText("学習開始").performClick()

        assertEquals(30, startedCount)
    }

    @Test
    fun dialog_selectAllQuestions_invokesCallbackWithNegativeOne() {
        var startedCount: Int? = null

        composeTestRule.setContent {
            RandomQuestionsDialog(
                totalCards = 42,
                initialCount = 10,
                onDismissRequest = {},
                onStartStudy = { startedCount = it },
            )
        }

        composeTestRule.onNodeWithText("全問 (42問)").performScrollTo().performClick()
        composeTestRule.onNodeWithText("学習開始").performClick()

        assertEquals(-1, startedCount)
    }

    @Test
    fun dialog_cancel_invokesDismissCallback() {
        var dismissed = false

        composeTestRule.setContent {
            RandomQuestionsDialog(
                totalCards = 50,
                initialCount = 10,
                onDismissRequest = { dismissed = true },
                onStartStudy = {},
            )
        }

        composeTestRule.onNodeWithText("キャンセル").performClick()
        assertTrue(dismissed)
    }
}
