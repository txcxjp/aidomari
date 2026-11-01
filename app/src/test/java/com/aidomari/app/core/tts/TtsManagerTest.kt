package com.aidomari.app.core.tts

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TtsManagerTest {

    @Test
    fun resolveLocale_mapsStandardTags() {
        assertEquals(Locale.US, TtsManager.resolveLocale("en-US"))
        assertEquals(Locale.US, TtsManager.resolveLocale("en"))
        assertEquals(Locale.JAPAN, TtsManager.resolveLocale("ja-JP"))
        assertEquals(Locale.JAPAN, TtsManager.resolveLocale("ja"))
        assertEquals(Locale.SIMPLIFIED_CHINESE, TtsManager.resolveLocale("zh-CN"))
        assertEquals(Locale.KOREA, TtsManager.resolveLocale("ko-KR"))
    }

    @Test
    fun resolveLocale_autoReturnsDefaultLocale() {
        assertEquals(Locale.getDefault(), TtsManager.resolveLocale("auto"))
        assertEquals(Locale.getDefault(), TtsManager.resolveLocale(""))
    }
}
