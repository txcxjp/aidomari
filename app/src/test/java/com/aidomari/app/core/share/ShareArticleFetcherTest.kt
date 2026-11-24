package com.aidomari.app.core.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.Charset

class ShareArticleFetcherTest {

    private val fetcher = DefaultShareArticleFetcher()

    @Test
    fun cleanHtmlToText_removesScriptsAndStyles() {
        val html = """
            <html>
            <head>
                <script>console.log("tracking code");</script>
                <style>body { font-size: 14px; }</style>
            </head>
            <body>
                <h1>北海道の地理</h1>
                <p>北海道は日本の北部に位置する島です。</p>
                <script>analytics.send();</script>
            </body>
            </html>
        """.trimIndent()

        val text = fetcher.cleanHtmlToText(html)

        assertFalse(text.contains("tracking code"))
        assertFalse(text.contains("font-size"))
        assertFalse(text.contains("analytics"))
        assertTrue(text.contains("北海道の地理"))
        assertTrue(text.contains("北海道は日本の北部に位置する島です。"))
    }

    @Test
    fun cleanHtmlToText_removesNavAndFooter() {
        val html = """
            <nav><a href="/">Home</a> | <a href="/about">About</a></nav>
            <main>
                <p>本文コンテンツです。</p>
            </main>
            <footer>Copyright 2026 Aidomari. All rights reserved.</footer>
        """.trimIndent()

        val text = fetcher.cleanHtmlToText(html)

        assertFalse(text.contains("Home"))
        assertFalse(text.contains("Copyright 2026"))
        assertTrue(text.contains("本文コンテンツです。"))
    }

    @Test
    fun decodeHtmlEntities_replacesEntitiesProperly() {
        val encoded = "Tom &amp; Jerry &lt;friends&gt; &#39;quotes&#39; &quot;say&quot; &copy; 2026 &#12354;"
        val decoded = fetcher.decodeHtmlEntities(encoded)

        assertEquals("Tom & Jerry <friends> 'quotes' \"say\" © 2026 あ", decoded)
    }

    @Test
    fun cleanHtmlToText_convertsBlockElementsToNewlines() {
        val html = "<h1>タイトル</h1><p>段落1</p><p>段落2</p><ul><li>項目A</li><li>項目B</li></ul>"
        val text = fetcher.cleanHtmlToText(html)

        val lines = text.lines().filter { it.isNotBlank() }
        assertEquals(5, lines.size)
        assertEquals("タイトル", lines[0])
        assertEquals("段落1", lines[1])
        assertEquals("段落2", lines[2])
        assertEquals("項目A", lines[3])
        assertEquals("項目B", lines[4])
    }

    @Test
    fun decodeHtml_detectsShiftJisFromMeta() {
        val rawHtmlText = """
            <html>
            <head><meta http-equiv="Content-Type" content="text/html; charset=Shift_JIS"><title>度胸だめし</title></head>
            <body><p>怪談と度胸だめしの記録です。</p></body>
            </html>
        """.trimIndent()

        val sjisCharset = Charset.forName("Windows-31J")
        val bytes = rawHtmlText.toByteArray(sjisCharset)

        val decoded = fetcher.decodeHtml(bytes, "text/html")
        assertTrue(decoded.contains("度胸だめし"))
        assertTrue(decoded.contains("怪談と度胸だめしの記録です。"))
    }

    @Test
    fun decodeHtml_detectsShiftJisWithoutMeta_fallback() {
        // Legacy site with Microsoft FrontPage 4.0: No meta charset, encoded in Shift_JIS / Windows-31J
        val rawHtmlText = """
            <html>
            <head><title>度胸だめし</title></head>
            <body><p>真夜中の知床峠での度胸だめし。</p></body>
            </html>
        """.trimIndent()

        val sjisCharset = Charset.forName("Windows-31J")
        val bytes = rawHtmlText.toByteArray(sjisCharset)

        val decoded = fetcher.decodeHtml(bytes, "text/html")
        assertTrue(decoded.contains("度胸だめし"))
        assertTrue(decoded.contains("真夜中の知床峠での度胸だめし。"))
    }

    @Test
    fun decodeHtml_detectsEucJpFromMeta() {
        val rawHtmlText = """
            <html>
            <head><meta charset="EUC-JP"><title>道東の歴史</title></head>
            <body><p>知床と根室の歴史的資料。</p></body>
            </html>
        """.trimIndent()

        val eucCharset = Charset.forName("EUC-JP")
        val bytes = rawHtmlText.toByteArray(eucCharset)

        val decoded = fetcher.decodeHtml(bytes, "text/html")
        assertTrue(decoded.contains("道東の歴史"))
        assertTrue(decoded.contains("知床と根室の歴史的資料。"))
    }

    @Test
    fun mapFetchException_cleartextHttpError_returnsFriendlyMessage() {
        val cleartextException = IOException("Cleartext HTTP traffic to www.110kz.com not permitted")
        val mapped = fetcher.mapFetchException(cleartextException, "http://www.110kz.com/033/")

        assertEquals("非暗号化通信(HTTP)のサイトのため、セキュリティ保護により自動取得できません", mapped.message)
    }

    @Test
    fun mapFetchException_unknownHost_returnsFriendlyMessage() {
        val unknownHost = UnknownHostException("nonexistent.domain")
        val mapped = fetcher.mapFetchException(unknownHost, "https://nonexistent.domain")

        assertEquals("サーバーが見つかりませんでした（オフラインか無効なURLです）", mapped.message)
    }

    @Test
    fun mapFetchException_socketTimeout_returnsFriendlyMessage() {
        val timeout = SocketTimeoutException("connect timed out")
        val mapped = fetcher.mapFetchException(timeout, "https://example.com")

        assertEquals("通信がタイムアウトしました", mapped.message)
    }
}
