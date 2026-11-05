package com.aidomari.app.ui.lockscreen

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aidomari.app.AidomariApp
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.ui.theme.AidomariTheme

class LockScreenStudyActivity : ComponentActivity() {

    private var ttsManager: TtsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockScreenFlags()

        val app = application as AidomariApp
        ttsManager = TtsManager(this)
        val viewModel = LockScreenStudyViewModel(
            repository = app.repository,
            preferences = app.lockScreenPreferences,
            ttsManager = ttsManager,
        )

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            BackHandler {
                finish()
            }

            AidomariTheme {
                LockScreenStudyContent(
                    uiState = uiState,
                    onFlip = { viewModel.flipCard() },
                    onDone = { viewModel.markDone { finish() } },
                    onRetry = { viewModel.markRetry { finish() } },
                    onSpeak = { viewModel.speakText(it) },
                    onDismiss = { finish() },
                )
            }
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
    }

    override fun onDestroy() {
        ttsManager?.shutdown()
        ttsManager = null
        super.onDestroy()
    }
}
