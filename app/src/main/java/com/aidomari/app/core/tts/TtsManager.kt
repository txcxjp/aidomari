package com.aidomari.app.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

open class TtsManager(
    context: Context? = null,
    private val initTts: Boolean = true,
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = if (context != null && initTts) {
        TextToSpeech(context.applicationContext, this)
    } else {
        null
    }
    private var isInitialized = false
    private var currentSpeechRate: Float = 1.0f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.getDefault()
            tts?.setSpeechRate(currentSpeechRate)
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setSpeechRate(currentSpeechRate)
        }
    }

    fun getSpeechRate(): Float = currentSpeechRate

    fun isReady(): Boolean = isInitialized

    fun speak(text: String, languageTag: String = "auto", speechRate: Float? = null) {
        if (speechRate != null) {
            setSpeechRate(speechRate)
        }
        if (!isInitialized || text.isBlank()) return

        val targetLocale = resolveLocale(languageTag)
        tts?.language = targetLocale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        fun resolveLocale(languageTag: String): Locale =
            when (languageTag.lowercase()) {
                "en", "en-us", "us" -> Locale.US
                "en-gb", "uk" -> Locale.UK
                "ja", "ja-jp" -> Locale.JAPAN
                "zh", "zh-cn" -> Locale.SIMPLIFIED_CHINESE
                "zh-tw" -> Locale.TRADITIONAL_CHINESE
                "ko", "ko-kr" -> Locale.KOREA
                "de", "de-de" -> Locale.GERMANY
                "fr", "fr-fr" -> Locale.FRANCE
                "auto", "" -> Locale.getDefault()
                else -> {
                    try {
                        Locale.forLanguageTag(languageTag)
                    } catch (_: Exception) {
                        Locale.getDefault()
                    }
                }
            }
    }
}
