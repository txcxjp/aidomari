package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.CardFaceBack
import com.aidomari.app.ui.study.components.CardFaceFront
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CardFaceReverseTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCard = Card(
        id = "c1",
        deckId = "d1",
        front = "Perspective",
        back = "視点・観点",
        userQuestion = "How do you use it?",
        userSupplement = "Point of view",
    )

    @Test
    fun cardFaceFront_normalMode_displaysFrontWordAndSpeaker() {
        var spoken = false
        composeTestRule.setContent {
            CardFaceFront(
                card = testCard,
                onSpeak = { spoken = true },
                isReverse = false,
            )
        }

        composeTestRule.onNodeWithText("Perspective").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do you use it?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertIsDisplayed().performClick()
        assertTrue(spoken)
    }

    @Test
    fun cardFaceFront_reverseMode_displaysMeaningWithoutFrontSpeaker() {
        composeTestRule.setContent {
            CardFaceFront(
                card = testCard,
                onSpeak = {},
                isReverse = true,
            )
        }

        composeTestRule.onNodeWithText("視点・観点").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do you use it?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertDoesNotExist()
    }

    @Test
    fun cardFaceBack_reverseMode_displaysFrontWordWithSpeakerAndMeaning() {
        var spoken = false
        var doneClicked = false
        var retryClicked = false

        composeTestRule.setContent {
            CardFaceBack(
                card = testCard,
                onDone = { doneClicked = true },
                onRetry = { retryClicked = true },
                isReverse = true,
                onSpeak = { spoken = true },
            )
        }

        composeTestRule.onNodeWithText("Perspective").assertIsDisplayed()
        composeTestRule.onNodeWithText("視点・観点").assertIsDisplayed()
        composeTestRule.onNodeWithText("Point of view").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertIsDisplayed().performClick()
        assertTrue(spoken)

        composeTestRule.onNodeWithText("覚えた").assertIsDisplayed().performClick()
        assertTrue(doneClicked)

        composeTestRule.onNodeWithText("もう一度").assertIsDisplayed().performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun cardFaceBack_withRelatedCards_displaysRelatedSectionAndNavigates() {
        val relatedCard = Card(
            id = "c2",
            deckId = "d1",
            front = "Prospect",
            back = "見通し・展望 (shares root with perspective)",
        )
        var clickedCard: Card? = null

        composeTestRule.setContent {
            CardFaceBack(
                card = testCard,
                onDone = {},
                onRetry = {},
                candidateCards = listOf(testCard, relatedCard),
                onKeywordClick = { clickedCard = it },
            )
        }

        composeTestRule.onNodeWithText("関連項目 (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("関連項目 (1)").performClick()
        composeTestRule.onNodeWithText("Prospect").assertIsDisplayed().performClick()
        assertEquals("c2", clickedCard?.id)
    }
}
