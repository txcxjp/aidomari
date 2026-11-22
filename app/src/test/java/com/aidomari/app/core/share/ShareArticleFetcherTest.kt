package com.aidomari.app.core.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
