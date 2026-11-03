package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.ui.study.components.StudyTopBar
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudyTopBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun studyTopBar_displaysTitleAndSubtitle() {
        composeTestRule.setContent {
            StudyTopBar(
                title = "基本情報技術者試験対策",
                subtitle = "Part 1 (1〜50)",
                isBookmarked = false,
                onBack = {},
                onToggleBookmark = {},
                onRestart = {},
            )
        }

        composeTestRule.onNodeWithText("基本情報技術者試験対策").assertIsDisplayed()
        composeTestRule.onNodeWithText("Part 1 (1〜50)").assertIsDisplayed()
    }

    @Test
    fun studyTopBar_compactActions_displaysOnlyDirectSearchBookmarkAndMore() {
        composeTestRule.setContent {
            StudyTopBar(
                title = "テストデッキ",
                subtitle = "Part 1",
                isBookmarked = true,
                onBack = {},
                onToggleBookmark = {},
                onRestart = {},
                onSearchClick = {},
                onOpenTtsSettings = {},
                onEditNotes = {},
                onDeleteCard = {},
            )
        }

        // Direct actions present in top bar
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search Words").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Bookmark").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("More Options").assertIsDisplayed()

        // Secondary actions should NOT be directly visible before opening menu
        composeTestRule.onNodeWithText("音声読み上げ設定").assertDoesNotExist()
        composeTestRule.onNodeWithText("メモ・疑問点を編集").assertDoesNotExist()
        composeTestRule.onNodeWithText("最初からやり直す").assertDoesNotExist()
        composeTestRule.onNodeWithText("カードを削除").assertDoesNotExist()
    }

    @Test
    fun studyTopBar_moreMenu_opensAndInvokesCallbacks() {
        var ttsClicked = false
        var editClicked = false
        var restartClicked = false
        var deleteClicked = false

        composeTestRule.setContent {
            StudyTopBar(
                title = "テストデッキ",
                subtitle = "Part 1",
                isBookmarked = false,
                onBack = {},
                onToggleBookmark = {},
                onRestart = { restartClicked = true },
                onSearchClick = {},
                onOpenTtsSettings = { ttsClicked = true },
                onEditNotes = { editClicked = true },
                onDeleteCard = { deleteClicked = true },
            )
        }

        // Open More Options dropdown menu
        composeTestRule.onNodeWithContentDescription("More Options").performClick()

        // Menu items appear
        composeTestRule.onNodeWithText("音声読み上げ設定").assertIsDisplayed()
        composeTestRule.onNodeWithText("メモ・疑問点を編集").assertIsDisplayed()
        composeTestRule.onNodeWithText("最初からやり直す").assertIsDisplayed()
        composeTestRule.onNodeWithText("カードを削除").assertIsDisplayed()

        // Click a menu item
        composeTestRule.onNodeWithText("最初からやり直す").performClick()
        assertTrue(restartClicked)
    }
}
