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

    private val cardCider = Card(
        id = "c3",
        deckId = "deck-1",
        front = "Cider",
        back = "Juice made from crushed apples",
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
    fun keywordBottomSheet_displaysSupplement_whenPresent() {
        val cardWithSupplement = cardApple.copy(userSupplement = "ビタミンCが豊富に含まれる")

        composeTestRule.setContent {
            KeywordBottomSheet(
                initialCard = cardWithSupplement,
                allSubjectCards = listOf(cardWithSupplement),
                onDismissRequest = {},
                onSpeak = {},
            )
        }

        composeTestRule.onNodeWithText("補足メモ").assertIsDisplayed()
        composeTestRule.onNodeWithText("ビタミンCが豊富に含まれる").assertIsDisplayed()
    }

    @Test
    fun keywordSheetContent_moreOptions_hiddenWhenCallbacksNull() {
        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple, cardFruit),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = {},
                onSpeak = {},
                onEditNotesClick = null,
                onDeleteClick = null,
            )
        }

        composeTestRule.onNodeWithContentDescription("More Options").assertDoesNotExist()
    }

    @Test
    fun keywordSheetContent_editNotesClick_invokesCallbackFromMoreMenu() {
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

        composeTestRule.onNodeWithContentDescription("More Options").performClick()
        composeTestRule.onNodeWithText("メモ・疑問点を編集").performClick()
        assertTrue(editClicked)
    }

    @Test
    fun keywordSheetContent_recursiveKeywordClick_invokesCallback() {
        var clickedCard: Card? = null

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple, cardFruit, cardCider),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = { card -> clickedCard = card },
                onSpeak = {},
            )
        }

        // Verify redundant "関連キーワード" label is not rendered
        composeTestRule.onNodeWithText("関連キーワード").assertDoesNotExist()

        // Expand "関連項目 (1)" (which references Apple from Cider) and tap "Cider" to navigate
        composeTestRule.onNodeWithText("関連項目 (1)")
            .performScrollTo()
            .performClick()

        composeTestRule.onNode(hasText("Cider") and hasClickAction())
            .performScrollTo()
            .performClick()

        assertEquals("c3", clickedCard?.id)
        assertEquals("Cider", clickedCard?.front)
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

    @Test
    fun keywordSheetContent_showsRelatedItems_andInvokesKeywordClick() {
        val cardPie = Card(id = "c3", deckId = "deck-1", front = "apple pie", back = "Pastry with apples")
        var clickedCard: Card? = null

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple, cardFruit, cardPie),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = { clickedCard = it },
                onSpeak = {},
            )
        }

        composeTestRule.onNodeWithText("関連項目 (1)")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("apple pie")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals("c3", clickedCard?.id)
    }

    @Test
    fun keywordSheetContent_deleteClick_triggersCallbackFromMoreMenu() {
        var deleteClicked = false

        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = {},
                onSpeak = {},
                onDeleteClick = { deleteClicked = true },
            )
        }

        composeTestRule.onNodeWithContentDescription("More Options").performClick()
        composeTestRule.onNodeWithText("カードを削除").performClick()
        assertTrue(deleteClicked)
    }

    @Test
    fun keywordSheetContent_deleteOptionHidden_whenDeleteCallbackNull() {
        composeTestRule.setContent {
            KeywordSheetContent(
                currentCard = cardApple,
                cardStackSize = 1,
                allSubjectCards = listOf(cardApple),
                onBackClick = {},
                onCloseClick = {},
                onKeywordClick = {},
                onSpeak = {},
                onEditNotesClick = {},
                onDeleteClick = null,
            )
        }

        composeTestRule.onNodeWithContentDescription("More Options").performClick()
        composeTestRule.onNodeWithText("カードを削除").assertDoesNotExist()
        composeTestRule.onNodeWithText("メモ・疑問点を編集").assertIsDisplayed()
    }
}
