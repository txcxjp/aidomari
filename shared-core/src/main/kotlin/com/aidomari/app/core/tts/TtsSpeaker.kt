package com.aidomari.app.core.tts

/**
 * Cross-platform text-to-speech playback contract.
 */
interface TtsSpeaker {
    fun speak(text: String, languageTag: String = "auto", speechRate: Float? = null)
    fun setSpeechRate(rate: Float)
    fun stop()
    fun shutdown()
}
