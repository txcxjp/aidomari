package com.aidomari.app.core.share

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

interface ShareArticleFetcher {
    suspend fun fetchArticle(url: String): Result<FetchedArticle>
}

data class FetchedArticle(
    val title: String?,
    val content: String,
)

class DefaultShareArticleFetcher(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000,
    private val maxBytes: Int = 512 * 1024,
) : ShareArticleFetcher {

    override suspend fun fetchArticle(url: String): Result<FetchedArticle> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = URL(url)
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 Aidomari/1.0",
                )
                setRequestProperty("Accept", "text/html,application/xhtml+xml,text/plain;q=0.9,*/*;q=0.8")
                setRequestProperty("Accept-Language", "ja,en-US;q=0.9,en;q=0.8")
            }

            try {
                val statusCode = conn.responseCode
                require(statusCode in 200..299) { "HTTP $statusCode" }

                val stream = conn.inputStream
                val rawHtml = stream.use { s ->
                    val reader = BufferedReader(InputStreamReader(s, Charsets.UTF_8))
                    val sb = StringBuilder()
                    val buffer = CharArray(4096)
                    var totalRead = 0
                    var read: Int
                    while (reader.read(buffer).also { read = it } != -1) {
                        sb.append(buffer, 0, read)
                        totalRead += read
                        if (totalRead >= maxBytes) break
                    }
                    sb.toString()
                }

                val title = extractTitleFromHtml(rawHtml)
                val text = cleanHtmlToText(rawHtml)

                FetchedArticle(
                    title = title,
                    content = text.ifBlank { "(URL: $url)" },
                )
            } finally {
                conn.disconnect()
            }
        }
    }

    private fun extractTitleFromHtml(html: String): String? {
        val titleRegex = Regex("""<title[^>]*>(.*?)</title>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val match = titleRegex.find(html) ?: return null
        val rawTitle = match.groupValues[1]
        return decodeHtmlEntities(rawTitle.trim())
    }

    fun cleanHtmlToText(html: String): String {
        var text = html
        val stripTags = listOf("script", "style", "noscript", "svg", "nav", "footer", "header")
        for (tag in stripTags) {
            text = text.replace(Regex("""<$tag[^>]*>.*?</$tag>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        }

        val blockTags = listOf("p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "li", "tr", "br")
        for (tag in blockTags) {
            text = text.replace(Regex("""</?$tag[^>]*>""", RegexOption.IGNORE_CASE), "\n")
        }

        text = text.replace(Regex("""<[^>]+>"""), " ")
        text = decodeHtmlEntities(text)

        val lines = text.lines()
            .map { it.trim().replace(Regex("""[ \t]+"""), " ") }
            .filter { it.isNotBlank() }

        return lines.joinToString("\n\n")
    }

    fun decodeHtmlEntities(input: String): String = input
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&copy;", "©")
        .replace("&trade;", "™")
        .replace(Regex("""&#(\d+);""")) { match ->
            val code = match.groupValues[1].toIntOrNull()
            if (code != null) code.toChar().toString() else match.value
        }
}
