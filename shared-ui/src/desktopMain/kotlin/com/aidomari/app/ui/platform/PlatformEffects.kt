package com.aidomari.app.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import javax.swing.SwingUtilities

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop screens use top-bar back buttons and standard window navigation.
}

@Composable
actual fun PlatformSystemBarsEffect(isDark: Boolean) {
    // No mobile system status/navigation bars on desktop.
}

@Composable
actual fun RequestDigestNotificationPermissionEffect() {
    // Desktop does not require Android runtime notification permission.
}

@Composable
actual fun PlatformVolumeKeyEffect(key: Any?, onVolumeKey: (isUp: Boolean) -> Boolean) {
    // Desktop uses keyboard shortcuts instead of mobile volume keys.
}

@Composable
actual fun rememberPlatformJsonFileHandler(
    onJsonLoaded: (String) -> Unit,
    onExportContent: suspend () -> String,
    onExportSaved: () -> Unit,
    onError: (String) -> Unit,
): PlatformJsonFileHandler {
    val scope = rememberCoroutineScope()
    val latestLoaded = rememberUpdatedState(onJsonLoaded)
    val latestExportContent = rememberUpdatedState(onExportContent)
    val latestExportSaved = rememberUpdatedState(onExportSaved)
    val latestError = rememberUpdatedState(onError)

    return remember(scope) {
        PlatformJsonFileHandler(
            launchImport = {
                scope.launch {
                    try {
                        val selected = chooseFileWithAwt(
                            title = "インポートするファイルを選択",
                            mode = FileDialog.LOAD,
                            defaultFile = null,
                        )
                        if (selected != null) {
                            val json = withContext(Dispatchers.IO) {
                                selected.readText(Charsets.UTF_8)
                            }
                            if (json.isNotBlank()) {
                                latestLoaded.value(json)
                            }
                        }
                    } catch (e: Exception) {
                        latestError.value("ファイルの読み込みに失敗しました: ${e.message}")
                    }
                }
            },
            launchExport = { defaultFileName ->
                scope.launch {
                    try {
                        val selected = chooseFileWithAwt(
                            title = "バックアップを保存",
                            mode = FileDialog.SAVE,
                            defaultFile = defaultFileName,
                        )
                        if (selected != null) {
                            val json = latestExportContent.value()
                            withContext(Dispatchers.IO) {
                                selected.writeText(json, Charsets.UTF_8)
                            }
                            latestExportSaved.value()
                        }
                    } catch (e: Exception) {
                        latestError.value("エクスポートに失敗しました: ${e.message}")
                    }
                }
            },
        )
    }
}

private suspend fun chooseFileWithAwt(
    title: String,
    mode: Int,
    defaultFile: String?,
): File? = withContext(Dispatchers.IO) {
    var result: File? = null
    SwingUtilities.invokeAndWait {
        val dialog = FileDialog(null as Frame?, title, mode).apply {
            if (!defaultFile.isNullOrBlank()) {
                file = defaultFile
            }
            filenameFilter = FilenameFilter { _, name ->
                name.endsWith(".json", ignoreCase = true) || name.endsWith(".txt", ignoreCase = true)
            }
            isVisible = true
        }
        val dir = dialog.directory
        val fileName = dialog.file
        dialog.dispose()
        if (dir != null && fileName != null) {
            result = File(dir, fileName)
        }
    }
    result
}
