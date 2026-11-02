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

    @Test
    fun setSpeechRate_clampsValueWithinRange() {
        val manager = TtsManager(context = null, initTts = false)
        assertEquals(1.0f, manager.getSpeechRate(), 0.001f)

        manager.setSpeechRate(1.25f)
        assertEquals(1.25f, manager.getSpeechRate(), 0.001f)

        manager.setSpeechRate(0.2f)
        assertEquals(0.5f, manager.getSpeechRate(), 0.001f)

        manager.setSpeechRate(2.8f)
        assertEquals(2.0f, manager.getSpeechRate(), 0.001f)
    }

    @Test
    fun speak_withSpeechRateParameter_updatesCurrentRate() {
        val manager = TtsManager(context = null, initTts = false)
        manager.speak("hello", languageTag = "en", speechRate = 1.5f)
        assertEquals(1.5f, manager.getSpeechRate(), 0.001f)
    }
}
