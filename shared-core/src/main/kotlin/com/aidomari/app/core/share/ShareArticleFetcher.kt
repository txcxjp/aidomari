package com.aidomari.app.core.share

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

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
    private val maxBytes: Int = 10 * 1024 * 1024, // 10 MB buffer
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

                val rawBytes = conn.inputStream.use { input ->
                    val baos = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var total = 0
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        val toWrite = minOf(read, maxBytes - total)
                        baos.write(buffer, 0, toWrite)
                        total += toWrite
                        if (total >= maxBytes) break
                    }
                    baos.toByteArray()
                }

                val contentType = conn.contentType.orEmpty()
                val rawHtml = decodeHtml(rawBytes, contentType)

                val title = extractTitleFromHtml(rawHtml)
                val text = cleanHtmlToText(rawHtml)

                FetchedArticle(
                    title = title,
                    content = text,
                )
            } finally {
                conn.disconnect()
            }
        }.recoverCatching { throwable ->
            throw mapFetchException(throwable, url)
        }
    }

    internal fun decodeHtml(bytes: ByteArray, contentTypeHeader: String): String {
        if (bytes.isEmpty()) return ""

        val metaCharset = extractCharsetFromMeta(bytes)
        val headerCharset = extractCharsetFromContentType(contentTypeHeader)

        // Meta tag in HTML is often more accurate than default web server header (e.g. Apache default ISO-8859-1)
        val primaryCharset = when {
            metaCharset != null && (headerCharset == null || headerCharset.name().equals("ISO-8859-1", ignoreCase = true) || headerCharset.name().equals("US-ASCII", ignoreCase = true)) -> metaCharset
            headerCharset != null -> headerCharset
            else -> metaCharset
        }

        if (primaryCharset != null) {
            val decoded = tryDecodeStrict(bytes, primaryCharset)
            if (decoded != null) return decoded
        }

        // Try strict UTF-8
        val utf8 = tryDecodeStrict(bytes, StandardCharsets.UTF_8)
        if (utf8 != null) return utf8

        // Try Windows-31J / Shift_JIS (standard for Japanese legacy web)
        val sjis = findCharset("Windows-31J") ?: findCharset("Shift_JIS")
        if (sjis != null) {
            val sjisDecoded = tryDecodeStrict(bytes, sjis)
            if (sjisDecoded != null) return sjisDecoded
        }

        // Try EUC-JP
        val eucJp = findCharset("EUC-JP")
        if (eucJp != null) {
            val eucDecoded = tryDecodeStrict(bytes, eucJp)
            if (eucDecoded != null) return eucDecoded
        }

        // Final fallback: Lenient UTF-8
        return String(bytes, StandardCharsets.UTF_8)
    }

    private fun tryDecodeStrict(bytes: ByteArray, charset: Charset): String? = runCatching {
        val decoder = charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        decoder.decode(ByteBuffer.wrap(bytes)).toString()
    }.getOrNull()

    private fun findCharset(name: String): Charset? = runCatching {
        Charset.forName(name)
    }.getOrNull()

    internal fun extractCharsetFromContentType(contentType: String): Charset? {
        val match = Regex("""charset\s*=\s*["']?([a-zA-Z0-9_\-]+)["']?""", RegexOption.IGNORE_CASE)
            .find(contentType) ?: return null
        return findCharset(match.groupValues[1].trim())
    }

    internal fun extractCharsetFromMeta(bytes: ByteArray): Charset? {
        val headLen = minOf(bytes.size, 4096)
        val headStr = String(bytes, 0, headLen, Charsets.ISO_8859_1)

        val meta1 = Regex("""<meta\s+[^>]*charset=["']?([a-zA-Z0-9_\-]+)["']?""", RegexOption.IGNORE_CASE).find(headStr)
        if (meta1 != null) {
            val cs = findCharset(meta1.groupValues[1].trim())
            if (cs != null) return cs
        }

        val meta2 = Regex("""content=["'][^"']*charset=([a-zA-Z0-9_\-]+)["']""", RegexOption.IGNORE_CASE).find(headStr)
        if (meta2 != null) {
            val cs = findCharset(meta2.groupValues[1].trim())
            if (cs != null) return cs
        }

        return null
    }

    internal fun mapFetchException(t: Throwable, url: String): Throwable {
        val msg = t.message.orEmpty()
        val localizedMessage = when {
            msg.contains("Cleartext HTTP traffic", ignoreCase = true) ||
                msg.contains("cleartext", ignoreCase = true) ||
                (url.startsWith("http://", ignoreCase = true) && msg.contains("not permitted", ignoreCase = true)) ->
                "非暗号化通信(HTTP)のサイトのため、セキュリティ保護により自動取得できません"
            t is UnknownHostException ->
                "サーバーが見つかりませんでした（オフラインか無効なURLです）"
            t is SocketTimeoutException ->
                "通信がタイムアウトしました"
            msg.startsWith("HTTP ") ->
                "記事を取得できませんでした ($msg)"
            else ->
                t.message ?: "記事の取得に失敗しました"
        }
        return IOException(localizedMessage, t)
    }

    fun extractTitleFromHtml(html: String): String? {
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
