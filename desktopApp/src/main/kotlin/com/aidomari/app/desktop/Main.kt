package com.aidomari.app.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.aidomari.app.AidomariVersion
import com.aidomari.app.core.ai.GeminiApiClientImpl
import com.aidomari.app.core.server.LocalIngestServerManager
import com.aidomari.app.ui.digest.BrowsingDigestViewModel
import com.aidomari.app.ui.navigation.SharedAidomariNavHost
import com.aidomari.app.ui.theme.AidomariTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Desktop entry point hosting the shared Aidomari Compose UI, local SQLite storage,
 * and localhost article ingestion server (`127.0.0.1:8877`).
 */
fun main() {
    val database = DesktopDatabase()
    val wordsRepository = DesktopWordsRepository(database)
    val sourceArticleRepository = DesktopSourceArticleRepository(database)
    val themeConfigStore = DesktopThemeConfigStore()
    val geminiConfigStore = DesktopGeminiConfigStore()
    val ttsSpeaker = DesktopTtsSpeaker()
    val geminiApiClient = GeminiApiClientImpl()
    val localIngestServerManager = LocalIngestServerManager(sourceArticleRepository)
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    localIngestServerManager.startServer()
    appScope.launch {
        runCatching { sourceArticleRepository.cleanupOldArticles() }
    }

    application {
        val windowState = rememberWindowState(width = 1100.dp, height = 780.dp)
        val themeConfig by themeConfigStore.themeConfig.collectAsState()
        val digestViewModel = remember {
            BrowsingDigestViewModel(
                sourceArticleRepository = sourceArticleRepository,
                wordsRepository = wordsRepository,
                geminiApiClient = geminiApiClient,
                geminiPreferences = geminiConfigStore,
            )
        }

        Window(
            onCloseRequest = {
                localIngestServerManager.stopServer()
                ttsSpeaker.shutdown()
                appScope.cancel()
                database.close()
                exitApplication()
            },
            title = "Aidomari (相泊)",
            state = windowState,
        ) {
            AidomariTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SharedAidomariNavHost(
                        repository = wordsRepository,
                        ttsSpeaker = ttsSpeaker,
                        themeConfigStore = themeConfigStore,
                        geminiConfigStore = geminiConfigStore,
                        sourceArticleRepository = sourceArticleRepository,
                        browsingDigestViewModel = digestViewModel,
                        geminiApiClient = geminiApiClient,
                        appVersion = AidomariVersion.DEFAULT_NAME,
                    )
                }
            }
        }
    }
}
