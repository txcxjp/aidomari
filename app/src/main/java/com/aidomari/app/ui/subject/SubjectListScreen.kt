package com.aidomari.app.ui.subject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.aidomari.app.AidomariApp
import com.aidomari.app.BuildConfig
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.ui.lockscreen.LockScreenSettingsDialog
import com.aidomari.app.ui.theme.ThemeConfigStore
import com.aidomari.app.ui.theme.ThemePreferences

@Composable
fun SubjectListScreen(
    viewModel: SubjectViewModel,
    onSubjectClick: (subjectId: String, subjectName: String) -> Unit,
    modifier: Modifier = Modifier,
    onCalendarClick: () -> Unit = {},
    onSugorokuMapClick: () -> Unit = {},
    onBrowsingDigestClick: () -> Unit = {},
    themePreferences: ThemeConfigStore? = null,
    lockScreenPreferences: LockScreenPreferences? = null,
) {
    val context = LocalContext.current
    val resolvedThemeStore = themePreferences ?: remember { ThemePreferences(context) }
    val resolvedGeminiStore = remember { GeminiPreferences(context) }

    SharedSubjectListScreen(
        viewModel = viewModel,
        onSubjectClick = onSubjectClick,
        themeConfigStore = resolvedThemeStore,
        geminiConfigStore = resolvedGeminiStore,
        modifier = modifier,
        onCalendarClick = onCalendarClick,
        onSugorokuMapClick = onSugorokuMapClick,
        onBrowsingDigestClick = onBrowsingDigestClick,
        appVersion = BuildConfig.VERSION_NAME,
        lockScreenDialogContent = { onDismiss ->
            val actualLockPrefs = lockScreenPreferences ?: remember {
                (context.applicationContext as? AidomariApp)?.lockScreenPreferences
                    ?: LockScreenPreferences(context)
            }
            val currentLockConfig by actualLockPrefs.config.collectAsState()
            LockScreenSettingsDialog(
                config = currentLockConfig,
                onConfigChanged = { actualLockPrefs.updateConfig(it) },
                onDismissRequest = onDismiss,
            )
        },
    )
}
