package com.aidomari.app.ui.share

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.core.share.FetchedArticle
import com.aidomari.app.core.share.ParsedShareContent
import com.aidomari.app.core.share.ShareArticleFetcher
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
class ShareReceiverActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyFetcher = object : ShareArticleFetcher {
        override suspend fun fetchArticle(url: String): Result<FetchedArticle> = Result.success(
            FetchedArticle(
                title = "知床の自然環境",
                content = "知床半島はオホーツク海に突き出た豊かな生態系を持つ世界遺産です。",
            ),
        )
    }

    @Test
    fun screen_rendersShareContent_andSavesForLater() {
        var savedUrl = ""
        var savedTitle = ""
        var savedContent = ""
        var navigateToDigest = true

        val parsed = ParsedShareContent(
            url = "https://example.com/hokkaido",
            title = "北海道の観光名所",
            content = "札幌、函館、旭川、小樽などの見どころを解説します。",
            isUrlOnly = false,
        )

        composeTestRule.setContent {
            ShareReceiverScreen(
                parsed = parsed,
                fetcher = dummyFetcher,
                onSave = { u, t, c, n ->
                    savedUrl = u
                    savedTitle = t
                    savedContent = c
                    navigateToDigest = n
                },
                onDismiss = {},
            )
        }

        // Verify title & card
        composeTestRule.onNodeWithTag("ShareReceiverCard").assertIsDisplayed()
        composeTestRule.onNodeWithText("愛泊に保存").assertIsDisplayed()
        composeTestRule.onNodeWithText("example.com").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ShareSaveButton").assertIsEnabled()

        // Click Save
        composeTestRule.onNodeWithTag("ShareSaveButton").performClick()

        assertEquals("https://example.com/hokkaido", savedUrl)
        assertEquals("北海道の観光名所", savedTitle)
        assertEquals("札幌、函館、旭川、小樽などの見どころを解説します。", savedContent)
        assertFalse(navigateToDigest)
    }

    @Test
    fun screen_clickDeckifyNow_callsOnSaveWithNavigateToDigestTrue() {
        var navigateToDigest = false

        val parsed = ParsedShareContent(
            url = "https://example.com/article",
            title = "技術メモ",
            content = "重要なアルゴリズムの解説です。",
            isUrlOnly = false,
        )

        composeTestRule.setContent {
            ShareReceiverScreen(
                parsed = parsed,
                fetcher = dummyFetcher,
                onSave = { _, _, _, n ->
                    navigateToDigest = n
                },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithTag("ShareDeckifyButton").performClick()
        assertTrue(navigateToDigest)
    }

    @Test
    fun screen_urlOnly_fetchesContentAndEnablesSave() {
        var savedTitle = ""
        var savedContent = ""

        val parsed = ParsedShareContent(
            url = "https://example.com/shiretoko",
            title = "共有記事",
            content = "",
            isUrlOnly = true,
        )

        composeTestRule.setContent {
            ShareReceiverScreen(
                parsed = parsed,
                fetcher = dummyFetcher,
                onSave = { _, t, c, _ ->
                    savedTitle = t
                    savedContent = c
                },
                onDismiss = {},
            )
        }

        composeTestRule.waitForIdle()

        // Fetched title & content should be populated
        composeTestRule.onNodeWithTag("ShareSaveButton").assertIsEnabled()
        composeTestRule.onNodeWithTag("ShareSaveButton").performClick()

        assertEquals("知床の自然環境", savedTitle)
        assertEquals("知床半島はオホーツク海に突き出た豊かな生態系を持つ世界遺産です。", savedContent)
    }

    @Test
    fun screen_excludedPrivacyUrl_displaysWarningAndDisablesSave() {
        val parsed = ParsedShareContent(
            url = "https://login.live.com/oauth",
            title = "ログイン画面",
            content = "パスワードを入力してください",
            isUrlOnly = false,
        )

        composeTestRule.setContent {
            ShareReceiverScreen(
                parsed = parsed,
                fetcher = dummyFetcher,
                onSave = { _, _, _, _ -> },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("プライバシー保護のため、除外対象のドメイン/ページです（保存されません）").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ShareSaveButton").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("ShareDeckifyButton").assertIsNotEnabled()
    }
}
