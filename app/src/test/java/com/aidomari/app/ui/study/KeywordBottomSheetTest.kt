package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
    fun keywordBottomSheet_displaysQuestion_whenPresent() {
        val cardWithQuestion = cardApple.copy(userQuestion = "どんな果物？")

        composeTestRule.setContent {
            KeywordBottomSheet(
                initialCard = cardWithQuestion,
                allSubjectCards = listOf(cardWithQuestion),
                onDismissRequest = {},
                onSpeak = {},
            )
        }

        composeTestRule.onNodeWithText("疑問点・質問").assertIsDisplayed()
        composeTestRule.onNodeWithText("どんな果物？").assertIsDisplayed()
    }

    @Test
    fun keywordBottomSheet_editNotes_opensDialog_andSavesNotes() {
        var savedCardId: String? = null
        var savedQuestion: String? = null
        var savedSupplement: String? = null

        composeTestRule.setContent {
            KeywordBottomSheet(
                initialCard = cardFruit,
                allSubjectCards = listOf(cardFruit),
                onDismissRequest = {},
                onSpeak = {},
                onSaveNotes = { id, q, s ->
                    savedCardId = id
                    savedQuestion = q
                    savedSupplement = s
                },
            )
        }

        // Tap Edit Notes icon button
        composeTestRule.onNodeWithContentDescription("Edit Notes").performClick()

        // CardEditNotesDialog is displayed
        composeTestRule.onNodeWithText("カードの質問・メモを編集").assertExists()

        // Input Question and Supplement
        composeTestRule.onNodeWithText("表面の質問（自分用クイズ）").performTextInput("果物の定義は？")
        composeTestRule.onNodeWithText("裏面の補足メモ（解説・例文など）").performTextInput("種子を含む器官")

        // Click Save
        composeTestRule.onNodeWithText("保存").performClick()

        // Verify callback was invoked with updated values
        assertEquals("c2", savedCardId)
        assertEquals("果物の定義は？", savedQuestion)
        assertEquals("種子を含む器官", savedSupplement)

        // Verify updated notes appear in the sheet immediately
        composeTestRule.onNodeWithText("疑問点・質問").assertIsDisplayed()
        composeTestRule.onNodeWithText("果物の定義は？").assertIsDisplayed()
        composeTestRule.onNodeWithText("補足メモ").assertIsDisplayed()
        composeTestRule.onNodeWithText("種子を含む器官").assertIsDisplayed()
    }

    @Test
    fun keywordSheetContent_editNotesClick_invokesCallback() {
        var editClicked = false

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple, cardFruit),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = {},
                onSpeak = {},
                onEditNotesClick = { editClicked = true },
            )
        }

        composeTestRule.onNodeWithContentDescription("Edit Notes").performClick()
        assertTrue(editClicked)
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
