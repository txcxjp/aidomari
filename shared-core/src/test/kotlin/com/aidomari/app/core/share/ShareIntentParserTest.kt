package com.aidomari.app.core.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareIntentParserTest {

    @Test
    fun parse_plainUrlOnly_extractsUrlAndDomainTitle() {
        val input = "https://ja.wikipedia.org/wiki/%E5%8C%97%E6%B5%B7%E9%81%93"
        val result = ShareIntentParser.parse(input)

        assertEquals("https://ja.wikipedia.org/wiki/%E5%8C%97%E6%B5%B7%E9%81%93", result.url)
        assertEquals("北海道", result.title)
        assertEquals("", result.content)
        assertTrue(result.isUrlOnly)
    }

    @Test
    fun parse_chromeFormat_subjectAndUrl() {
        val subject = "知床半島 - Wikipedia"
        val text = "https://ja.wikipedia.org/wiki/%E7%9F%A5%E5%BA%8A%E5%8D%8A%E5%B3%B6"

        val result = ShareIntentParser.parse(text, subject)

        assertEquals("https://ja.wikipedia.org/wiki/%E7%9F%A5%E5%BA%8A%E5%8D%8A%E5%B3%B6", result.url)
        assertEquals("知床半島 - Wikipedia", result.title)
        assertEquals("", result.content)
        assertTrue(result.isUrlOnly)
    }

    @Test
    fun parse_textWithAppendedUrl_extractsContentAndUrl() {
        val text = """
            知床半島（しれとこはんとう）は、北海道東部にあるオホーツク海に面した半島。
            2005年に世界自然遺産に登録されました。

            https://example.com/shiretoko
        """.trimIndent()

        val result = ShareIntentParser.parse(text)

        assertEquals("https://example.com/shiretoko", result.url)
        assertFalse(result.isUrlOnly)
        assertTrue(result.content.contains("世界自然遺産に登録されました"))
        assertEquals("知床半島（しれとこはんとう）は、北海道東部にあるオホーツク海に面した半島。", result.title)
    }

    @Test
    fun parse_pureTextWithoutUrl_extractsContentWithNoUrl() {
        val text = "日本の最東端は納沙布岬、最北端は宗谷岬です。"
        val result = ShareIntentParser.parse(text)

        assertNull(result.url)
        assertFalse(result.isUrlOnly)
        assertEquals("日本の最東端は納沙布岬、最北端は宗谷岬です。", result.title)
        assertEquals("日本の最東端は納沙布岬、最北端は宗谷岬です。", result.content)
    }

    @Test
    fun parse_nullAndEmpty_returnsFallback() {
        val result = ShareIntentParser.parse(null, null)

        assertNull(result.url)
        assertFalse(result.isUrlOnly)
        assertEquals("共有テキスト", result.title)
        assertEquals("", result.content)
    }

    @Test
    fun parse_urlWithTrailingPunctuation_cleansPunctuation() {
        val text = "記事をチェックしてね: https://example.com/news/123."
        val result = ShareIntentParser.parse(text)

        assertEquals("https://example.com/news/123", result.url)
        assertTrue(result.content.contains("記事をチェックしてね:"))
    }
}
