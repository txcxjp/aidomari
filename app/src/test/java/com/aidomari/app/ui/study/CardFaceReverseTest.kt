package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.StudyDirection
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
        term = "Perspective",
        description = "視点・観点",
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
                studyDirection = StudyDirection.TERM_TO_DESCRIPTION,
            )
        }

        composeTestRule.onNodeWithText("Perspective").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do you use it?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertIsDisplayed().performClick()
        assertTrue(spoken)
    }

    @Test
    fun cardFaceFront_reverseMode_displaysMeaningWithFrontSpeaker() {
        var spoken = false
        composeTestRule.setContent {
            CardFaceFront(
                card = testCard,
                onSpeak = { spoken = true },
                studyDirection = StudyDirection.DESCRIPTION_TO_TERM,
            )
        }

        composeTestRule.onNodeWithText("視点・観点").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do you use it?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertIsDisplayed().performClick()
        assertTrue(spoken)
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
                studyDirection = StudyDirection.DESCRIPTION_TO_TERM,
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
    fun cardFaceBack_normalMode_displaysSpeaker() {
        var spoken = false
        composeTestRule.setContent {
            CardFaceBack(
                card = testCard,
                onDone = {},
                onRetry = {},
                studyDirection = StudyDirection.TERM_TO_DESCRIPTION,
                onSpeak = { spoken = true },
            )
        }

        composeTestRule.onNodeWithText("視点・観点").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertIsDisplayed().performClick()
        assertTrue(spoken)
    }

    @Test
    fun cardFaceBack_withRelatedCards_displaysRelatedSectionAndNavigates() {
        val relatedCard = Card(
            id = "c2",
            deckId = "d1",
            term = "Prospect",
            description = "見通し・展望 (shares root with perspective)",
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

    @Test
    fun cardFace_longDescriptionInDescriptionFirstDirection_displaysCleanlyOnBothFaces() {
        val longDescription =
            "ある選択を行ったことで断念せざるを得なかった他の代替選択肢のうち、最も価値が高かったものから得られたはずの最大利益。"
        val longCard = Card(
            id = "c-long",
            deckId = "d1",
            term = "機会費用",
            description = longDescription,
            userSupplement = "意思決定の比較評価において重要な概念。",
        )

        composeTestRule.setContent {
            CardFaceFront(
                card = longCard,
                onSpeak = {},
                studyDirection = StudyDirection.DESCRIPTION_TO_TERM,
            )
        }
        composeTestRule.onNodeWithText(longDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Speak").assertIsDisplayed()
    }

    @Test
    fun cardFaceBack_longDescriptionInDescriptionFirstDirection_displaysAnswerAndActions() {
        val longDescription =
            "ある選択を行ったことで断念せざるを得なかった他の代替選択肢のうち、最も価値が高かったものから得られたはずの最大利益。"
        val longCard = Card(
            id = "c-long-back",
            deckId = "d1",
            term = "機会費用",
            description = longDescription,
            userSupplement = "意思決定の比較評価において重要な概念。",
        )

        composeTestRule.setContent {
            CardFaceBack(
                card = longCard,
                onDone = {},
                onRetry = {},
                studyDirection = StudyDirection.DESCRIPTION_TO_TERM,
                onSpeak = {},
            )
        }
        composeTestRule.onNodeWithText("機会費用").assertIsDisplayed()
        composeTestRule.onNodeWithText(longDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText("覚えた").assertIsDisplayed()
        composeTestRule.onNodeWithText("もう一度").assertIsDisplayed()
    }
}
