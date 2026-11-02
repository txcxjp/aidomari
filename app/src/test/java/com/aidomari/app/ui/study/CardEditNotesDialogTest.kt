package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.CardEditNotesDialog
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
class CardEditNotesDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val card = Card(
        id = "c1",
        deckId = "d1",
        front = "Eloquent",
        back = "雄弁な、説得力のある",
        userQuestion = "How to say in German?",
        userSupplement = "Origin: Latin eloquens",
    )

    @Test
    fun dialog_rendersInitialNotesAndSavesModifiedValues() {
        var savedQuestion = ""
        var savedSupplement = ""
        var dismissed = false

        composeTestRule.setContent {
            CardEditNotesDialog(
                card = card,
                onDismissRequest = { dismissed = true },
                onSave = { q, s ->
                    savedQuestion = q
                    savedSupplement = s
                },
            )
        }

        composeTestRule.onNodeWithText("カードの質問・メモを編集").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eloquent").assertIsDisplayed()
        composeTestRule.onNodeWithText("雄弁な、説得力のある").assertIsDisplayed()

        // Modify question
        composeTestRule.onNodeWithText("How to say in German?").performTextClearance()
        composeTestRule.onNodeWithText("表面の質問（自分用クイズ）").performTextInput("What is a synonym?")

        // Modify supplement
        composeTestRule.onNodeWithText("Origin: Latin eloquens").performTextClearance()
        composeTestRule.onNodeWithText("裏面の補足メモ（解説・例文など）").performTextInput("Synonym: articulate")

        // Click save
        composeTestRule.onNodeWithText("保存").performClick()

        assertEquals("What is a synonym?", savedQuestion)
        assertEquals("Synonym: articulate", savedSupplement)
        assertFalse(dismissed)
    }

    @Test
    fun dialog_cancelButton_invokesOnDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            CardEditNotesDialog(
                card = card,
                onDismissRequest = { dismissed = true },
                onSave = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("キャンセル").performClick()
        assertTrue(dismissed)
    }
}
