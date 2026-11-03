package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.KeywordText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeywordTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val cardApple = Card(
        id = "c1",
        deckId = "d1",
        front = "apple",
        back = "A sweet red fruit",
    )

    private val cardFruit = Card(
        id = "c2",
        deckId = "d1",
        front = "fruit",
        back = "Plant product containing seeds",
    )

    @Test
    fun keywordText_rendersAnnotatedText_withoutRedundantRelatedKeywordsChips() {
        var clickedCard: Card? = null

        composeTestRule.setContent {
            KeywordText(
                text = "A sweet red fruit",
                candidateCards = listOf(cardApple, cardFruit),
                currentCardId = cardApple.id,
                onKeywordClick = { clickedCard = it },
            )
        }

        // Entire text is displayed
        composeTestRule.onNodeWithText("A sweet red fruit").assertIsDisplayed()

        // Redundant "関連キーワード" header must NOT exist
        composeTestRule.onNodeWithText("関連キーワード").assertDoesNotExist()
    }

    @Test
    fun keywordText_rendersPlainText_whenNoKeywordMatches() {
        composeTestRule.setContent {
            KeywordText(
                text = "Simple plain text without matches",
                candidateCards = emptyList(),
                onKeywordClick = {},
            )
        }

        composeTestRule.onNodeWithText("Simple plain text without matches").assertIsDisplayed()
        composeTestRule.onNodeWithText("関連キーワード").assertDoesNotExist()
    }
}
