package com.aidomari.app.core.privacy

object PrivacyFilter {

    // 検索エンジンのホスト
    val SEARCH_ENGINE_HOSTS = setOf(
        "google.com",
        "google.co.jp",
        "bing.com",
        "search.yahoo.co.jp",
        "search.yahoo.com",
        "duckduckgo.com",
        "baidu.com",
        "ecosia.org",
        "yandex.com",
        "ask.com",
    )

    // 認証・アカウント系サブドメインプレフィックス
    val EXCLUDED_HOST_PREFIXES = listOf(
        "login.",
        "auth.",
        "signin.",
        "accounts.",
        "account.",
        "id.",
        "admin.",
        "sso.",
    )

    // ローカル・社内ネットワークホスト
    val LOCAL_OR_INTERNAL_DOMAINS = setOf(
        "localhost",
        "127.0.0.1",
        "0.0.0.0",
        "::1",
    )

    /**
     * 指定されたURLまたはドメインがプライバシー保護のための除外対象かを判定します。
     * @return true: 除外対象（取り込み不可）, false: 通常Web記事（取り込み可能）
     */
    fun isExcluded(url: String, explicitDomain: String = ""): Boolean {
        if (url.isBlank()) return true
        val cleanUrl = url.trim().lowercase()

        // スキーム以降を分離
        val withoutScheme = if (cleanUrl.contains("://")) cleanUrl.substringAfter("://") else cleanUrl
        val hostPart = withoutScheme.substringBefore("/").substringBefore("?").substringBefore("#")
        val extractedHost = hostPart.substringBefore(":").removePrefix("www.")

        val domain = if (explicitDomain.isNotBlank()) {
            explicitDomain.trim().lowercase().removePrefix("www.")
        } else {
            extractedHost
        }

        if (domain.isBlank()) return true

        // 1. ローカル・内部IP・LANドメイン (192.168.*, 10.*, 127.*, localhost, .local, etc.)
        if (LOCAL_OR_INTERNAL_DOMAINS.contains(domain) ||
            domain.startsWith("192.168.") ||
            domain.startsWith("10.") ||
            domain.startsWith("127.") ||
            domain.endsWith(".local") ||
            domain.endsWith(".internal") ||
            domain.endsWith(".lan") ||
            domain.endsWith(".test")
        ) {
            return true
        }

        // 2. 検索エンジンホスト (Google各ドメイン、Bing、Yahoo検索、DuckDuckGo等)
        if (SEARCH_ENGINE_HOSTS.contains(domain) ||
            domain == "google.com" ||
            domain.endsWith(".google.com") ||
            domain == "google.co.jp" ||
            domain.endsWith(".google.co.jp") ||
            domain == "bing.com" ||
            domain.endsWith(".bing.com") ||
            domain == "search.yahoo.co.jp" ||
            domain == "search.yahoo.com"
        ) {
            return true
        }

        // 3. 認証・ログイン系サブドメイン
        if (EXCLUDED_HOST_PREFIXES.any { domain.startsWith(it) }) {
            return true
        }

        // 4. URLパスに含まれるログイン・認証エンドポイント
        val path = if (withoutScheme.contains("/")) "/" + withoutScheme.substringAfter("/").substringBefore("?").substringBefore("#") else ""
        if (path.contains("/login") ||
            path.contains("/signin") ||
            path.contains("/auth/") ||
            path.contains("/oauth") ||
            path.contains("/logout")
        ) {
            return true
        }

        return false
    }
}
