package com.aidomari.app.core.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyFilterTest {

    @Test
    fun isExcluded_returnsTrue_forSearchEngines() {
        val searchUrls = listOf(
            "https://www.google.com/search?q=kotlin+coroutines",
            "https://google.co.jp/search?q=aidomari",
            "https://www.bing.com/search?q=test",
            "https://search.yahoo.co.jp/search?p=hokkaido",
            "https://duckduckgo.com/?q=android+jetpack+compose",
            "https://www.ecosia.org/search?q=privacy",
        )

        for (url in searchUrls) {
            assertTrue("Expected $url to be excluded", PrivacyFilter.isExcluded(url))
        }
    }

    @Test
    fun isExcluded_returnsTrue_forAuthAndAccountDomains() {
        val authUrls = listOf(
            "https://accounts.google.com/signin/v2/identifier",
            "https://login.microsoftonline.com/common/oauth2",
            "https://auth.github.com/login",
            "https://signin.aws.amazon.com/",
            "https://id.apple.com/",
            "https://admin.example.com/dashboard",
        )

        for (url in authUrls) {
            assertTrue("Expected $url to be excluded", PrivacyFilter.isExcluded(url))
        }
    }

    @Test
    fun isExcluded_returnsTrue_forAuthPaths() {
        val loginPathUrls = listOf(
            "https://example.com/login",
            "https://example.com/user/signin",
            "https://example.com/api/auth/callback",
            "https://example.com/oauth/authorize",
        )

        for (url in loginPathUrls) {
            assertTrue("Expected $url to be excluded", PrivacyFilter.isExcluded(url))
        }
    }

    @Test
    fun isExcluded_returnsTrue_forLocalAndInternalDomains() {
        val localUrls = listOf(
            "http://localhost:8080/my-notes",
            "http://127.0.0.1:8877/api/status",
            "http://192.168.1.50/router.local",
            "http://mycompany.internal/wiki",
            "http://dev.test/article",
        )

        for (url in localUrls) {
            assertTrue("Expected $url to be excluded", PrivacyFilter.isExcluded(url))
        }
    }

    @Test
    fun isExcluded_returnsFalse_forGeneralArticles() {
        val articleUrls = listOf(
            "https://ja.wikipedia.org/wiki/北海道",
            "https://zenn.dev/user/articles/android-room-database",
            "https://qiita.com/developer/items/1234567890abcdef",
            "https://b.hatena.ne.jp/entry/s/example.com/guide",
            "https://developer.android.com/guide/topics/ui/compose",
            "https://medium.com/@author/learning-kotlin-flows",
            "https://news.yahoo.co.jp/articles/1234567890",
        )

        for (url in articleUrls) {
            assertFalse("Expected $url to be allowed", PrivacyFilter.isExcluded(url))
        }
    }
}
