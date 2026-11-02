package com.aidomari.app.ui.deck.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aidomari.app.domain.model.Deck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeckDialogsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addDeckDialog_createsDeck_onConfirm() {
        var createdName: String? = null
        var createdSplitSize: Int? = null

        composeTestRule.setContent {
            AddDeckDialog(
                onDismissRequest = {},
                onConfirm = { name, splitSize, _, _, _, _, _ ->
                    createdName = name
                    createdSplitSize = splitSize
                },
            )
        }

        composeTestRule.onNodeWithText("新規デッキの作成").assertIsDisplayed()
        composeTestRule.onNodeWithText("デッキ名").performTextInput("TOEIC頻出")
        composeTestRule.onNodeWithText("作成").performClick()

        assertEquals("TOEIC頻出", createdName)
        assertEquals(50, createdSplitSize)
    }

    @Test
    fun editDeckDialog_updatesDeck_onConfirm() {
        val deck = Deck(id = "deck-1", subjectId = "sub-1", name = "旧タイトル", splitSize = 30)
        var updatedDeck: Deck? = null

        composeTestRule.setContent {
            EditDeckDialog(
                deck = deck,
                onDismissRequest = {},
                onConfirm = { updatedDeck = it },
            )
        }

        composeTestRule.onNodeWithText("デッキ設定の変更").assertIsDisplayed()
        composeTestRule.onNodeWithText("保存").performClick()

        assertEquals("旧タイトル", updatedDeck?.name)
        assertEquals(30, updatedDeck?.splitSize)
    }

    @Test
    fun deleteDeckConfirmDialog_confirms_onDelete() {
        val deck = Deck(id = "deck-1", subjectId = "sub-1", name = "削除対象デッキ")
        var confirmed = false

        composeTestRule.setContent {
            DeleteDeckConfirmDialog(
                deck = deck,
                onDismissRequest = {},
                onConfirm = { confirmed = true },
            )
        }

        composeTestRule.onNodeWithText("デッキの削除").assertIsDisplayed()
        composeTestRule.onNodeWithText("削除する").performClick()
        assertTrue(confirmed)
    }
}
