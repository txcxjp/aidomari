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
        val part = DeckPart(partIndex = 0, partNumber = 1, startIndex = 0, endIndex = 30, count = 30)
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

        // Direct Edit and Delete buttons should not be exposed
        composeTestRule.onNodeWithContentDescription("Edit Deck").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Delete Deck").assertDoesNotExist()

        // Edit deck via 3-dot menu
        composeTestRule.onNodeWithContentDescription("Deck More Options").performClick()
        composeTestRule.onNodeWithText("デッキを編集").performClick()
        assertTrue(editDeckClicked)

        // Delete deck via 3-dot menu
        composeTestRule.onNodeWithContentDescription("Deck More Options").performClick()
        composeTestRule.onNodeWithText("デッキを削除").performClick()
        assertTrue(deleteDeckClicked)

        // Click part
        composeTestRule.onNodeWithText(part.displayName).performClick()
        assertEquals("deck-1", clickedDeckId)
        assertEquals(0, clickedPartIndex)
    }

    @Test
    fun deckItemCard_displaysReverseModeAndAutoSpeakBadges_correctly() {
        val deck = Deck(
            id = "deck-rev",
            subjectId = "sub-1",
            name = "テストブック (Part 1)",
            splitSize = 50,
            autoContinue = false,
            shuffleMode = true,
            reverseMode = true,
            autoSpeak = true,
            speechRate = 1.0f,
        )
        val parts = deck.calculateParts(totalCards = 0)
        val partWithSession = DeckPartWithSession(part = parts.first(), session = null)
        val item = DeckWithPartsUiModel(
            deck = deck,
            parts = listOf(partWithSession),
            totalCardCount = 0,
        )

        composeTestRule.setContent {
            DeckItemCard(
                item = item,
                onPartClick = { _, _ -> },
                onToggleShuffle = { },
                onToggleAutoSpeak = { },
                onManageCardsClick = { },
                onEditDeck = { },
                onDeleteDeck = { },
            )
        }

        composeTestRule.onNodeWithText("テストブック (Part 1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 単語").assertIsDisplayed()
        composeTestRule.onNodeWithText("シャッフル").assertIsDisplayed()
        composeTestRule.onNodeWithText("裏面スタート").assertIsDisplayed()
        composeTestRule.onNodeWithText("自動音声 (1.0x)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Part 1 (0枚)").assertIsDisplayed()
    }
}
