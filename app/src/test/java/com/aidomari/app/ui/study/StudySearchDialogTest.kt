package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.StudySearchContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudySearchDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val cardApple = Card(
        id = "c1",
        deckId = "deck-1",
        front = "Apple",
        back = "りんご",
        userSupplement = "甘くて赤い果物",
    )

    private val cardBanana = Card(
        id = "c2",
        deckId = "deck-1",
        front = "Banana",
        back = "バナナ",
    )

    @Test
    fun searchContent_rendersEmptyState_whenQueryBlank() {
        composeTestRule.setContent {
            StudySearchContent(
                searchQuery = "",
                searchResults = emptyList(),
                onQueryChange = {},
                onCardClick = {},
                onClose = {},
            )
        }

        composeTestRule.onNodeWithText("単語検索").assertIsDisplayed()
        composeTestRule.onNodeWithText("教科内の単語を検索").assertIsDisplayed()
        composeTestRule.onNodeWithText("見出し語、意味、メモから検索できます").assertIsDisplayed()
    }

    @Test
    fun searchContent_rendersNoResults_whenNoMatches() {
        composeTestRule.setContent {
            StudySearchContent(
                searchQuery = "UnknownWord",
                searchResults = emptyList(),
                onQueryChange = {},
                onCardClick = {},
                onClose = {},
            )
        }

        composeTestRule.onNodeWithText("「UnknownWord」に一致する単語はありません").assertIsDisplayed()
    }

    @Test
    fun searchContent_rendersResults_andInvokesClick() {
        var clickedCard: Card? = null

        composeTestRule.setContent {
            StudySearchContent(
                searchQuery = "App",
                searchResults = listOf(cardApple, cardBanana),
                onQueryChange = {},
                onCardClick = { clickedCard = it },
                onClose = {},
            )
        }

        composeTestRule.onNodeWithText("2件の単語が見つかりました").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        composeTestRule.onNodeWithText("りんご").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banana").assertIsDisplayed()

        composeTestRule.onNodeWithText("Apple").performClick()
        assertEquals("c1", clickedCard?.id)
    }

    @Test
    fun searchContent_clearQuery_invokesCallback() {
        var newQuery: String? = null

        composeTestRule.setContent {
            StudySearchContent(
                searchQuery = "Query",
                searchResults = emptyList(),
                onQueryChange = { newQuery = it },
                onCardClick = {},
                onClose = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear Search").performClick()
        assertEquals("", newQuery)
    }

    @Test
    fun searchContent_closeButton_invokesCallback() {
        var closed = false

        composeTestRule.setContent {
            StudySearchContent(
                searchQuery = "",
                searchResults = emptyList(),
                onQueryChange = {},
                onCardClick = {},
                onClose = { closed = true },
            )
        }

        composeTestRule.onNodeWithContentDescription("Close Search").performClick()
        assertTrue(closed)
    }
}
