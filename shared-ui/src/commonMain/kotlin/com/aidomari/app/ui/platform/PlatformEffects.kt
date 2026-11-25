package com.aidomari.app.ui.platform

import androidx.compose.runtime.Composable

/**
 * Handles system back navigation where applicable.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/**
 * Synchronizes platform system bars appearance with dark/light theme.
 */
@Composable
expect fun PlatformSystemBarsEffect(isDark: Boolean)

/**
 * Requests OS notification permission for card generation on platforms that require it.
 */
@Composable
expect fun RequestDigestNotificationPermissionEffect()

/**
 * Registers hardware volume-key listener on mobile platforms where available.
 */
@Composable
expect fun PlatformVolumeKeyEffect(key: Any?, onVolumeKey: (isUp: Boolean) -> Boolean)

/**
 * Platform-specific file open/save callbacks for backup JSON transfer.
 */
class PlatformJsonFileHandler(
    val launchImport: () -> Unit,
    val launchExport: (defaultFileName: String) -> Unit,
)

/**
 * Remembers platform file dialogs for importing and exporting backup JSON files.
 */
@Composable
expect fun rememberPlatformJsonFileHandler(
    onJsonLoaded: (String) -> Unit,
    onExportContent: suspend () -> String,
    onExportSaved: () -> Unit,
    onError: (String) -> Unit,
): PlatformJsonFileHandler
