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
import com.aidomari.app.ui.study.components.KeywordSheetContent
import org.junit.Assert.assertEquals
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
    fun keywordSheetContent_recursiveKeywordClick_invokesCallback() {
        var clickedCard: Card? = null

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple, cardFruit),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = { card -> clickedCard = card },
                onSpeak = {},
            )
        }

        // Scroll to and tap recursive keyword chip "fruit" in Apple's meaning
        composeTestRule.onNode(hasText("fruit") and hasClickAction())
            .performScrollTo()
            .performClick()

        assertEquals("c2", clickedCard?.id)
        assertEquals("fruit", clickedCard?.front)
    }

    @Test
    fun keywordSheetContent_backButton_and_closeButton_invokeCallbacks() {
        var backClicked = false
        var closeClicked = false

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardFruit,
                cardStackSize = 2,
                allSubjectCards = listOf(cardApple, cardFruit),
                onBackClick = { backClicked = true },
                onCloseClick = { closeClicked = true },
                onKeywordClick = {},
                onSpeak = {},
            )
        }

        // Stack size > 1 shows "戻る (1)" button
        composeTestRule.onNodeWithText("戻る (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("戻る (1)").performClick()
        assertTrue(backClicked)

        // Close button calls onCloseClick
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        assertTrue(closeClicked)
    }

    @Test
    fun keywordSheetContent_toggleBookmark_invokesCallback() {
        var toggledCard: Card? = null

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple, cardFruit),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = {},
                onSpeak = {},
                onToggleBookmark = { toggledCard = it },
            )
        }

        composeTestRule.onNodeWithContentDescription("Toggle Bookmark").performClick()
        assertEquals("c1", toggledCard?.id)
    }
}
