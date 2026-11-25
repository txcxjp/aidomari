package com.aidomari.app.desktop

import com.aidomari.app.core.tts.TtsSpeaker
import com.aidomari.app.core.tts.TtsTextSanitizer
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.roundToInt

/**
 * Desktop [TtsSpeaker] using Windows SAPI SpeechSynthesizer via PowerShell on Windows,
 * and standard speech commands (`spd-say` / `say`) where available on Unix systems.
 */
class DesktopTtsSpeaker(
    private val osName: String = System.getProperty("os.name").orEmpty(),
) : TtsSpeaker {

    private var currentSpeechRate: Float = 1.0f
    private val activeProcess = AtomicReference<Process?>(null)

    override fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 2.0f)
    }

    override fun speak(text: String, languageTag: String, speechRate: Float?) {
        if (speechRate != null) {
            setSpeechRate(speechRate)
        }
        val cleanText = TtsTextSanitizer.sanitize(text)
        if (cleanText.isBlank()) return

        stop()
        val resolvedLang = resolveLanguage(languageTag, cleanText)
        val rateToUse = currentSpeechRate

        thread(isDaemon = true, name = "aidomari-desktop-tts") {
            runCatching {
                val process = buildTtsProcess(cleanText, resolvedLang, rateToUse) ?: return@runCatching
                activeProcess.set(process)
                process.waitFor()
                activeProcess.compareAndSet(process, null)
            }
        }
    }

    override fun stop() {
        activeProcess.getAndSet(null)?.let { proc ->
            runCatching { proc.destroyForcibly() }
        }
    }

    override fun shutdown() {
        stop()
    }

    private fun buildTtsProcess(text: String, langTag: String, rate: Float): Process? = when {
        osName.contains("win", ignoreCase = true) -> {
            // SAPI rate ranges from -10 to 10 (0 is 1.0x)
            val sapiRate = ((rate - 1.0f) * 5.0f).roundToInt().coerceIn(-5, 8)
            val langPrefix = langTag.substringBefore('-').lowercase()
            val textB64 = Base64.getEncoder().encodeToString(text.toByteArray(Charsets.UTF_8))
            val psScript = """
                    Add-Type -AssemblyName System.Speech
                    ${'$'}synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
                    ${'$'}synth.Rate = $sapiRate
                    ${'$'}voice = ${'$'}synth.GetInstalledVoices() | Where-Object { ${'$'}_.VoiceInfo.Culture.Name -like '$langPrefix*' } | Select-Object -First 1
                    if (${'$'}voice -ne ${'$'}null) { ${'$'}synth.SelectVoice(${'$'}voice.VoiceInfo.Name) }
                    ${'$'}bytes = [System.Convert]::FromBase64String('$textB64')
                    ${'$'}msg = [System.Text.Encoding]::UTF8.GetString(${'$'}bytes)
                    ${'$'}synth.Speak(${'$'}msg)
            """.trimIndent()
            val encodedCmd = Base64.getEncoder().encodeToString(psScript.toByteArray(Charsets.UTF_16LE))
            ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encodedCmd)
                .redirectErrorStream(true)
                .start()
        }

        osName.contains("mac", ignoreCase = true) -> {
            val wordsPerMin = (180 * rate).roundToInt().coerceIn(90, 360)
            ProcessBuilder("say", "-r", wordsPerMin.toString(), text).start()
        }

        else -> {
            // Optional Linux speech-dispatcher CLI if installed; silent no-op otherwise.
            val ratePercent = ((rate - 1.0f) * 50.0f).roundToInt().coerceIn(-50, 80)
            ProcessBuilder("spd-say", "-r", ratePercent.toString(), "-l", langTag.substringBefore('-'), text)
                .start()
        }
    }

    companion object {
        internal fun resolveLanguage(languageTag: String, sampleText: String): String {
            if (!languageTag.equals("auto", ignoreCase = true) && languageTag.isNotBlank()) {
                return languageTag
            }
            if (sampleText.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FFF' }) {
                return "ja-JP"
            }
            if (sampleText.any { it in '\uAC00'..'\uD7AF' || it in '\u1100'..'\u11FF' }) {
                return "ko-KR"
            }
            return "en-US"
        }
    }
}
