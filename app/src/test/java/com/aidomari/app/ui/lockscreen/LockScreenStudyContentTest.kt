package com.aidomari.app.ui.lockscreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.StudyDirection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LockScreenStudyContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCard = Card(
        id = "card-1",
        deckId = "deck-1",
        term = "apple",
        description = "りんご",
    )

    @Test
    fun lockScreenContent_displaysTerm_whenTermToDescription() {
        composeTestRule.setContent {
            LockScreenStudyContent(
                uiState = LockScreenUiState(
                    isLoading = false,
                    card = sampleCard,
                    isFlipped = false,
                    studyDirection = StudyDirection.TERM_TO_DESCRIPTION,
                ),
                onFlip = {},
                onDone = {},
                onRetry = {},
                onSpeak = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("apple").assertIsDisplayed()
    }

    @Test
    fun lockScreenContent_displaysDescription_whenDescriptionToTerm() {
        composeTestRule.setContent {
            LockScreenStudyContent(
                uiState = LockScreenUiState(
                    isLoading = false,
                    card = sampleCard,
                    isFlipped = false,
                    studyDirection = StudyDirection.DESCRIPTION_TO_TERM,
                ),
                onFlip = {},
                onDone = {},
                onRetry = {},
                onSpeak = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("りんご").assertIsDisplayed()
    }

    @Test
    fun lockScreenContent_dismissButton_callsOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            LockScreenStudyContent(
                uiState = LockScreenUiState(
                    isLoading = false,
                    card = sampleCard,
                    isFlipped = false,
                ),
                onFlip = {},
                onDone = {},
                onRetry = {},
                onSpeak = {},
                onDismiss = { dismissed = true },
            )
        }

        composeTestRule.onNodeWithTag("LockScreenDismissButton").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun lockScreenContent_upwardSwipe_animatesAndCallsOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            LockScreenStudyContent(
                uiState = LockScreenUiState(
                    isLoading = false,
                    card = sampleCard,
                    isFlipped = false,
                ),
                onFlip = {},
                onDone = {},
                onRetry = {},
                onSpeak = {},
                onDismiss = { dismissed = true },
            )
        }

        composeTestRule.onNodeWithTag("LockScreenStudyRoot").performTouchInput {
            swipeUp()
        }
        composeTestRule.waitForIdle()
        assertTrue(dismissed)
    }
}
