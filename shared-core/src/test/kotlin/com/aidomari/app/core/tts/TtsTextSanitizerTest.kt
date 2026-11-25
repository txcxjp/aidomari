package com.aidomari.app.core.tts

import org.junit.Assert.assertEquals
import org.junit.Test

class TtsTextSanitizerTest {

    @Test
    fun sanitize_replacesRubyWithReading() {
        val input = "[林檎|りんご] - A round red or green fruit."
        val expected = "りんご - A round red or green fruit."
        assertEquals(expected, TtsTextSanitizer.sanitize(input))
    }

    @Test
    fun sanitize_replacesMultipleRubiesInText() {
        val input = "アクティブ・リコール（[能動的想起|のうどうてきそうき]）と[分散学習|ぶんさんがくしゅう]"
        val expected = "アクティブ・リコール（のうどうてきそうき）とぶんさんがくしゅう"
        assertEquals(expected, TtsTextSanitizer.sanitize(input))
    }

    @Test
    fun sanitize_stripsMarkdownBoldAndItalic() {
        val input = "例文: **Consistency** is *the* key to mastery."
        val expected = "例文: Consistency is the key to mastery."
        assertEquals(expected, TtsTextSanitizer.sanitize(input))
    }

    @Test
    fun sanitize_stripsHashtags() {
        val input = "基本表現です。#英語 #挨拶 #重要"
        val expected = "基本表現です。"
        assertEquals(expected, TtsTextSanitizer.sanitize(input))
    }

    @Test
    fun sanitize_handlesCombinedFormattingAndWhitespace() {
        val input = """
            [林檎|りんご] - A **red** fruit.

            #くだもの #基本単語
        """.trimIndent()
        val expected = "りんご - A red fruit."
        assertEquals(expected, TtsTextSanitizer.sanitize(input))
    }

    @Test
    fun sanitize_handlesPlainTextAndBlankInput() {
        assertEquals("apple", TtsTextSanitizer.sanitize("apple"))
        assertEquals("", TtsTextSanitizer.sanitize(""))
        assertEquals("", TtsTextSanitizer.sanitize("   \n\t  "))
    }
}
