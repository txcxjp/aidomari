package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.KeywordBottomSheet
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeywordBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val cardApple = Card(
        id = "c1",
        deckId = "deck-1",
        front = "Apple",
        back = "A delicious fruit",
        userSupplement = "Rich in vitamin C",
    )

    private val cardFruit = Card(
        id = "c2",
        deckId = "deck-1",
        front = "fruit",
        back = "Seed-bearing structure in flowering plants",
    )

    @Test
    fun keywordBottomSheet_displaysInitialCardContent_withoutPrematureDismissal() {
        var dismissed = false

        composeTestRule.setContent {
            KeywordBottomSheet(
                initialCard = cardApple,
                allSubjectCards = listOf(cardApple, cardFruit),
                onDismissRequest = { dismissed = true },
                onSpeak = {},
            )
        }

        // Verify initial card is rendered and onDismissRequest was NOT called
        assertFalse(dismissed)
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rich in vitamin C").assertIsDisplayed()
    }

    @Test
    fun keywordBottomSheet_recursiveKeywordClick_pushesToStackAndAllowsBackNavigation() {
        var dismissed = false

        composeTestRule.setContent {
            KeywordBottomSheet(
                initialCard = cardApple,
                allSubjectCards = listOf(cardApple, cardFruit),
                onDismissRequest = { dismissed = true },
                onSpeak = {},
            )
        }

        // Initial state: shows "Apple"
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()

        // Scroll to and tap recursive keyword chip "fruit" in Apple's meaning
        composeTestRule.onNode(hasText("fruit") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Stack updated: back button in header appears
        composeTestRule.onNodeWithText("戻る (1)").assertIsDisplayed()

        // Click "戻る (1)" button to pop back to Apple
        composeTestRule.onNodeWithText("戻る (1)").performClick()

        // Back on Apple
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        assertFalse(dismissed)
    }

    @Test
    fun keywordBottomSheet_closeButton_triggersDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            KeywordBottomSheet(
                initialCard = cardApple,
                allSubjectCards = listOf(cardApple, cardFruit),
                onDismissRequest = { dismissed = true },
                onSpeak = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Close").performClick()
        assertTrue(dismissed)
    }
}
