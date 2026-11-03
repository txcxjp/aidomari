package com.aidomari.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aidomari.app.core.input.VolumeKeyHandler
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.ui.navigation.AidomariNavHost
import com.aidomari.app.ui.theme.AidomariTheme

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AidomariApp
        ttsManager = TtsManager(this)
        val themePreferences = app.themePreferences

        setContent {
            val themeConfig by themePreferences.themeConfig.collectAsState()
            AidomariTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AidomariNavHost(
                        repository = app.repository,
                        ttsManager = ttsManager,
                        themePreferences = themePreferences,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (VolumeKeyHandler.handleKeyEvent(event.keyCode)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
