package com.aidomari.app.ui.deck.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.ui.deck.DeckPartWithSession
import com.aidomari.app.ui.deck.DeckWithPartsUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeckItemCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun deckItemCard_displaysDeckInfo_andInteractionsWork() {
        val deck = Deck(
            id = "deck-1",
            subjectId = "sub-1",
            name = "日常英会話",
            splitSize = 50,
            autoContinue = true,
            shuffleMode = true,
        )
        val part = DeckPart(deckId = "deck-1", partIndex = 0, startIndex = 0, endIndex = 30, totalCards = 30)
        val partWithSession = DeckPartWithSession(part = part, session = null)
        val item = DeckWithPartsUiModel(
            deck = deck,
            parts = listOf(partWithSession),
            totalCardCount = 30,
        )

        var clickedDeckId: String? = null
        var clickedPartIndex: Int? = null
        var shuffleToggled = false
        var autoSpeakToggled = false
        var manageCardsClicked = false
        var editDeckClicked = false
        var deleteDeckClicked = false

        composeTestRule.setContent {
            DeckItemCard(
                item = item,
                onPartClick = { dId, pIdx ->
                    clickedDeckId = dId
                    clickedPartIndex = pIdx
                },
                onToggleShuffle = { shuffleToggled = true },
                onToggleAutoSpeak = { autoSpeakToggled = true },
                onManageCardsClick = { manageCardsClicked = true },
                onEditDeck = { editDeckClicked = true },
                onDeleteDeck = { deleteDeckClicked = true },
            )
        }

        composeTestRule.onNodeWithText("日常英会話").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 単語").assertIsDisplayed()
        composeTestRule.onNodeWithText("自動継続").assertIsDisplayed()
        composeTestRule.onNodeWithText("シャッフル").assertIsDisplayed()

        // Toggle shuffle
        composeTestRule.onNodeWithText("シャッフル").performClick()
        assertTrue(shuffleToggled)

        // Manage cards button
        composeTestRule.onNodeWithTag("ManageCardsButton_deck-1").performClick()
        assertTrue(manageCardsClicked)

        // Edit deck button
        composeTestRule.onNodeWithContentDescription("Edit Deck").performClick()
        assertTrue(editDeckClicked)

        // Delete deck button
        composeTestRule.onNodeWithContentDescription("Delete Deck").performClick()
        assertTrue(deleteDeckClicked)

        // Click part
        composeTestRule.onNodeWithText(part.displayName).performClick()
        assertEquals("deck-1", clickedDeckId)
        assertEquals(0, clickedPartIndex)
    }
}
