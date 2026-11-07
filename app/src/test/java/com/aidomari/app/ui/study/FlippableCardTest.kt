package com.aidomari.app.ui.study

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.aidomari.app.ui.study.components.FlippableCard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FlippableCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun flippableCard_tapOnFront_doesNotFlip() {
        var flipped = false
        composeTestRule.setContent {
            FlippableCard(
                isFlipped = false,
                onFlip = { flipped = true },
                onDone = {},
                onRetry = {},
                modifier = Modifier
                    .size(300.dp)
                    .testTag("FlippableCardNode"),
                frontContent = { Text("Front Content") },
                backContent = { Text("Back Content") },
            )
        }

        composeTestRule.onNodeWithTag("FlippableCardNode").performClick()
        composeTestRule.waitForIdle()

        assertFalse(flipped)
    }

    @Test
    fun flippableCard_tapOnBack_doesNotFlipBackToFront() {
        var flipped = false
        composeTestRule.setContent {
            FlippableCard(
                isFlipped = true,
                onFlip = { flipped = true },
                onDone = {},
                onRetry = {},
                modifier = Modifier
                    .size(300.dp)
                    .testTag("FlippableCardNode"),
                frontContent = { Text("Front Content") },
                backContent = { Text("Back Content") },
            )
        }

        composeTestRule.onNodeWithTag("FlippableCardNode").performClick()
        composeTestRule.waitForIdle()

        assertFalse(flipped)
    }

    @Test
    fun flippableCard_horizontalSwipeOnFront_flipsCard() {
        var flipped = false
        composeTestRule.setContent {
            FlippableCard(
                isFlipped = false,
                onFlip = { flipped = true },
                onDone = {},
                onRetry = {},
                modifier = Modifier
                    .size(300.dp)
                    .testTag("FlippableCardNode"),
                frontContent = { Text("Front Content") },
                backContent = { Text("Back Content") },
            )
        }

        composeTestRule.onNodeWithTag("FlippableCardNode").performTouchInput {
            swipeRight()
        }
        composeTestRule.waitForIdle()

        assertTrue(flipped)
    }
}
