package com.aidomari.app.ui.deck.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeckSearchContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun deckSearchContent_emptyQuery_showsPrompt() {
        composeTestRule.setContent {
            DeckSearchContent(
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                onCardClick = {},
                onToggleBookmark = {},
                onSpeakText = {},
            )
        }

        composeTestRule.onNodeWithText("教科内の単語を検索").assertIsDisplayed()
        composeTestRule.onNodeWithText("見出し語、意味、メモから検索できます").assertIsDisplayed()
    }

    @Test
    fun deckSearchContent_noResults_showsEmptyMessage() {
        composeTestRule.setContent {
            DeckSearchContent(
                searchQuery = "該当なし",
                searchResults = emptyList(),
                isSearching = false,
                onCardClick = {},
                onToggleBookmark = {},
                onSpeakText = {},
            )
        }

        composeTestRule.onNodeWithText("「該当なし」に一致する単語はありません").assertIsDisplayed()
    }

    @Test
    fun deckSearchContent_displaysResults_andHandlesClicks() {
        val cards = listOf(
            Card(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご"),
        )
        var clickedCard: Card? = null
        var bookmarkedCard: Card? = null

        composeTestRule.setContent {
            DeckSearchContent(
                searchQuery = "app",
                searchResults = cards,
                isSearching = false,
                onCardClick = { clickedCard = it },
                onToggleBookmark = { bookmarkedCard = it },
                onSpeakText = {},
            )
        }

        composeTestRule.onNodeWithText("1件の単語が見つかりました").assertIsDisplayed()
        composeTestRule.onNodeWithText("apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("りんご").assertIsDisplayed()

        composeTestRule.onNodeWithTag("SearchBookmarkButton_card-1").performClick()
        assertEquals("card-1", bookmarkedCard?.id)

        composeTestRule.onNodeWithText("apple").performClick()
        assertEquals("card-1", clickedCard?.id)
    }
}
