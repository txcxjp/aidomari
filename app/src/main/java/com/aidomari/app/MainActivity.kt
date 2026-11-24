package com.aidomari.app

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.aidomari.app.core.input.VolumeKeyHandler
import com.aidomari.app.core.lockscreen.LockScreenStudyService
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.ui.navigation.AidomariNavHost
import com.aidomari.app.ui.theme.AidomariTheme

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AidomariApp
        if (app.lockScreenPreferences.config.value.isEnabled) {
            LockScreenStudyService.start(this)
        }
        ttsManager = TtsManager(this)
        val themePreferences = app.themePreferences
        pendingRoute.value = intent?.getStringExtra("navigate_to")

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
                        sugorokuDebugPreferences = app.sugorokuDebugPreferences,
                        sourceArticleRepository = app.sourceArticleRepository,
                        initialRoute = pendingRoute.value,
                        onInitialRouteHandled = { pendingRoute.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("navigate_to")?.let { route ->
            pendingRoute.value = route
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
