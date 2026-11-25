package com.aidomari.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aidomari.app.AidomariApp
import com.aidomari.app.BuildConfig
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.core.debug.SugorokuDebugPreferences
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.domain.repository.SourceArticleRepository
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.ui.lockscreen.LockScreenSettingsDialog
import com.aidomari.app.ui.sugoroku.SugorokuMapScreen
import com.aidomari.app.ui.sugoroku.SugorokuMapViewModel
import com.aidomari.app.ui.theme.ThemePreferences

@Composable
fun AidomariNavHost(
    repository: WordsRepository,
    ttsManager: TtsManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    themePreferences: ThemePreferences? = null,
    sugorokuDebugPreferences: SugorokuDebugPreferences? = null,
    sourceArticleRepository: SourceArticleRepository? = null,
    initialRoute: String? = null,
    onInitialRouteHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val app = context.applicationContext as? AidomariApp
    val resolvedThemeStore = themePreferences ?: remember { ThemePreferences(context) }
    val resolvedGeminiStore = remember { GeminiPreferences(context) }

    SharedAidomariNavHost(
        repository = repository,
        ttsSpeaker = ttsManager,
        themeConfigStore = resolvedThemeStore,
        geminiConfigStore = resolvedGeminiStore,
        modifier = modifier,
        navController = navController,
        sugorokuSpeedProvider = sugorokuDebugPreferences,
        sourceArticleRepository = sourceArticleRepository,
        browsingDigestViewModel = app?.browsingDigestViewModel,
        initialRoute = initialRoute,
        onInitialRouteHandled = onInitialRouteHandled,
        appVersion = BuildConfig.VERSION_NAME,
        onSugorokuMapClick = { navController.navigate("sugoroku_map") },
        lockScreenDialogContent = { onDismiss ->
            val lockPrefs = remember { app?.lockScreenPreferences ?: LockScreenPreferences(context) }
            val currentLockConfig by lockPrefs.config.collectAsState()
            LockScreenSettingsDialog(
                config = currentLockConfig,
                onConfigChanged = { lockPrefs.updateConfig(it) },
                onDismissRequest = onDismiss,
            )
        },
        extraRoutes = { controller ->
            composable("sugoroku_map") {
                val mapViewModel = remember { SugorokuMapViewModel(repository, sugorokuDebugPreferences) }
                SugorokuMapScreen(
                    viewModel = mapViewModel,
                    onBack = { controller.popBackStack() },
                )
            }
        },
    )
}
