package com.aidomari.app.ui.deck.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.domain.model.PastDayDeckType
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.ui.deck.DeckPartWithSession
import com.aidomari.app.ui.deck.PastDayDeckUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PastDayDecksCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pastDayDecksCard_emptyList_doesNotDisplaySection() {
        composeTestRule.setContent {
            PastDayDecksCard(
                decks = emptyList(),
                onPartClick = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithTag("PastDayDecksSection").assertDoesNotExist()
    }

    @Test
    fun pastDayDecksCard_displaysDecks_andHandlesPartClick() {
        val deckYesterday = Deck(id = "past_yesterday_sub1", subjectId = "sub1", name = "昨日のカード")
        val partYesterday = DeckPart(partIndex = 0, partNumber = 1, startIndex = 0, endIndex = 3, count = 3)
        val modelYesterday = PastDayDeckUiModel(
            type = PastDayDeckType.YESTERDAY,
            deck = deckYesterday,
            parts = listOf(DeckPartWithSession(part = partYesterday, session = null)),
            totalCount = 3,
            targetDateDescription = "1日前",
        )

        val deckWeek = Deck(id = "past_week_sub1", subjectId = "sub1", name = "先週のカード")
        val partWeek = DeckPart(partIndex = 0, partNumber = 1, startIndex = 0, endIndex = 5, count = 5)
        val sessionWeek = StudySession(deckId = "past_week_sub1", partIndex = 0, isCompleted = true, completedPartIndices = setOf(0))
        val modelWeek = PastDayDeckUiModel(
            type = PastDayDeckType.WEEK,
            deck = deckWeek,
            parts = listOf(DeckPartWithSession(part = partWeek, session = sessionWeek)),
            totalCount = 5,
            targetDateDescription = "8日前",
        )

        var clickedDeckId: String? = null
        var clickedPartIndex: Int? = null

        composeTestRule.setContent {
            PastDayDecksCard(
                decks = listOf(modelYesterday, modelWeek),
                onPartClick = { dId, pIdx ->
                    clickedDeckId = dId
                    clickedPartIndex = pIdx
                },
            )
        }

        // Section header
        composeTestRule.onNodeWithText("あの日のカード").assertIsDisplayed()

        // Yesterday deck
        composeTestRule.onNodeWithText("昨日のカード").assertIsDisplayed()
        composeTestRule.onNodeWithText("1日前").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 枚").assertIsDisplayed()

        // Week deck
        composeTestRule.onNodeWithText("先週のカード").assertIsDisplayed()
        composeTestRule.onNodeWithText("8日前").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 枚").assertIsDisplayed()
        composeTestRule.onNodeWithText("完了").assertIsDisplayed()

        // Click yesterday part
        composeTestRule.onNodeWithTag("PastDayPart_YESTERDAY_0").performClick()
        assertEquals("past_yesterday_sub1", clickedDeckId)
        assertEquals(0, clickedPartIndex)
    }
}
