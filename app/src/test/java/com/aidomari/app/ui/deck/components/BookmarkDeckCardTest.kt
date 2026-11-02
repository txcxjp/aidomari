package com.aidomari.app.ui.deck.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.ui.deck.BookmarkDeckUiModel
import com.aidomari.app.ui.deck.DeckPartWithSession
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BookmarkDeckCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookmarkDeckCard_displaysInfo_andHandlesPartClick() {
        val deck = Deck(id = "bookmark-deck", subjectId = "sub1", name = "苦手単語")
        val part = DeckPart(deckId = "bookmark-deck", partIndex = 0, startIndex = 0, endIndex = 5, totalCards = 5)
        val partWithSession = DeckPartWithSession(part = part, session = null)
        val model = BookmarkDeckUiModel(
            deck = deck,
            parts = listOf(partWithSession),
            totalCount = 5,
        )

        var clickedDeckId: String? = null
        var clickedPartIndex: Int? = null

        composeTestRule.setContent {
            BookmarkDeckCard(
                model = model,
                onPartClick = { dId, pIdx ->
                    clickedDeckId = dId
                    clickedPartIndex = pIdx
                },
            )
        }

        composeTestRule.onNodeWithText("苦手単語（ブックマーク）").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 枚").assertIsDisplayed()
        composeTestRule.onNodeWithText("集中学習をはじめる").assertIsDisplayed()

        composeTestRule.onNodeWithText("集中学習をはじめる").performClick()
        assertEquals("bookmark-deck", clickedDeckId)
        assertEquals(0, clickedPartIndex)
    }

    @Test
    fun bookmarkDeckCard_displaysCompletionBadge_whenCompleted() {
        val deck = Deck(id = "bookmark-deck", subjectId = "sub1", name = "苦手単語")
        val part = DeckPart(deckId = "bookmark-deck", partIndex = 0, startIndex = 0, endIndex = 3, totalCards = 3)
        val session = StudySession(deckId = "bookmark-deck", partIndex = 0, isCompleted = true, completedPartIndices = setOf(0))
        val partWithSession = DeckPartWithSession(part = part, session = session)
        val model = BookmarkDeckUiModel(
            deck = deck,
            parts = listOf(partWithSession),
            totalCount = 3,
        )

        composeTestRule.setContent {
            BookmarkDeckCard(
                model = model,
                onPartClick = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("完了").assertIsDisplayed()
        composeTestRule.onNodeWithText("進捗率: 100%").assertIsDisplayed()
    }
}
