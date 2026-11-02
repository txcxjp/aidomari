package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.RelatedItemsSection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RelatedItemsSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val card1 = Card(id = "c1", deckId = "d1", front = "Eloquent", back = "雄弁な")
    private val card2 = Card(id = "c2", deckId = "d1", front = "Eloquence", back = "雄弁さ")

    @Test
    fun relatedItemsSection_rendersNothingWhenListEmpty() {
        composeTestRule.setContent {
            RelatedItemsSection(
                relatedCards = emptyList(),
                onCardClick = {},
            )
        }

        composeTestRule.onNodeWithText("関連項目").assertDoesNotExist()
    }

    @Test
    fun relatedItemsSection_togglesExpansionAndInvokesCallback() {
        var clickedCard: Card? = null

        composeTestRule.setContent {
            RelatedItemsSection(
                relatedCards = listOf(card1, card2),
                onCardClick = { clickedCard = it },
            )
        }

        // Header shows count
        composeTestRule.onNodeWithText("関連項目 (2)").assertIsDisplayed()

        // Chips are hidden before expansion
        composeTestRule.onNodeWithText("Eloquent").assertDoesNotExist()

        // Click to expand
        composeTestRule.onNodeWithText("関連項目 (2)").performClick()

        // Chips appear
        composeTestRule.onNodeWithText("Eloquent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eloquence").assertIsDisplayed()

        // Tap chip
        composeTestRule.onNodeWithText("Eloquent").performClick()
        assertEquals("c1", clickedCard?.id)
    }
}
