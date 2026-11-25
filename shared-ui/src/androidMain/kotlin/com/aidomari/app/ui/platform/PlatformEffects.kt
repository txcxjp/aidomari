package com.aidomari.app.ui.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.aidomari.app.core.input.VolumeKeyHandler
import kotlinx.coroutines.launch

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun PlatformSystemBarsEffect(isDark: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            runCatching {
                val window = view.context.findActivity()?.window ?: return@SideEffect
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }
}

@Composable
actual fun RequestDigestNotificationPermissionEffect() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            Build.FINGERPRINT != "robolectric" &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
actual fun PlatformVolumeKeyEffect(key: Any?, onVolumeKey: (isUp: Boolean) -> Boolean) {
    val latestHandler = rememberUpdatedState(onVolumeKey)
    DisposableEffect(key) {
        VolumeKeyHandler.listener = { isUp -> latestHandler.value(isUp) }
        onDispose {
            VolumeKeyHandler.listener = null
        }
    }
}

@Composable
actual fun rememberPlatformJsonFileHandler(
    onJsonLoaded: (String) -> Unit,
    onExportContent: suspend () -> String,
    onExportSaved: () -> Unit,
    onError: (String) -> Unit,
): PlatformJsonFileHandler {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestLoaded = rememberUpdatedState(onJsonLoaded)
    val latestExportContent = rememberUpdatedState(onExportContent)
    val latestExportSaved = rememberUpdatedState(onExportSaved)
    val latestError = rememberUpdatedState(onError)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    latestLoaded.value(jsonString)
                }
            } catch (e: Exception) {
                latestError.value("ファイルの読み込みに失敗しました: ${e.message}")
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val exportJson = latestExportContent.value()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(exportJson.toByteArray())
                    }
                    latestExportSaved.value()
                } catch (e: Exception) {
                    latestError.value("エクスポートに失敗しました: ${e.message}")
                }
            }
        }
    }

    return remember(importLauncher, exportLauncher) {
        PlatformJsonFileHandler(
            launchImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
            launchExport = { fileName -> exportLauncher.launch(fileName) },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
