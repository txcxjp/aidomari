package com.aidomari.app.ui.lockscreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidomari.app.core.lockscreen.LockScreenConfig
import com.aidomari.app.core.lockscreen.LockScreenStudyService
import com.aidomari.app.core.lockscreen.LockScreenTarget

@Composable
fun LockScreenSettingsDialog(
    config: LockScreenConfig,
    onConfigChanged: (LockScreenConfig) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showPermissionAlert by remember { mutableStateOf(false) }

    fun checkAndToggle(newEnabled: Boolean) {
        if (newEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                showPermissionAlert = true
                return
            }
            LockScreenStudyService.start(context)
        } else {
            LockScreenStudyService.stop(context)
        }
        onConfigChanged(config.copy(isEnabled = newEnabled))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "ロック画面学習の設定",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "スマホの画面点灯時に、ロック画面の上に1問演習カードを表示して習慣化します。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ロック画面学習を有効にする",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (config.isEnabled) "有効（画面点灯時に表示）" else "無効",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (config.isEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }

                    Switch(
                        checked = config.isEnabled,
                        onCheckedChange = { checkAndToggle(it) },
                        modifier = Modifier.testTag("LockScreenStudySwitch"),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Selection
                Text(
                    text = "出題対象:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = config.isEnabled) {
                            onConfigChanged(config.copy(target = LockScreenTarget.ALL))
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = config.target == LockScreenTarget.ALL,
                        onClick = { onConfigChanged(config.copy(target = LockScreenTarget.ALL)) },
                        enabled = config.isEnabled,
                        modifier = Modifier.testTag("TargetAllRadio"),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "すべての単語（全教科からランダム）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (config.isEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = config.isEnabled) {
                            onConfigChanged(config.copy(target = LockScreenTarget.BOOKMARK_ONLY))
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = config.target == LockScreenTarget.BOOKMARK_ONLY,
                        onClick = { onConfigChanged(config.copy(target = LockScreenTarget.BOOKMARK_ONLY)) },
                        enabled = config.isEnabled,
                        modifier = Modifier.testTag("TargetBookmarkRadio"),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ブックマークした単語のみ（苦手対策）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (config.isEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = config.isEnabled) {
                            onConfigChanged(config.copy(target = LockScreenTarget.DAYS_AGO))
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = config.target == LockScreenTarget.DAYS_AGO,
                        onClick = { onConfigChanged(config.copy(target = LockScreenTarget.DAYS_AGO)) },
                        enabled = config.isEnabled,
                        modifier = Modifier.testTag("TargetDaysAgoRadio"),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${config.daysThreshold}日以上前に出題した単語（未学習含む）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (config.isEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                }

                if (config.target == LockScreenTarget.DAYS_AGO) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp, top = 2.dp, bottom = 4.dp)
                            .testTag("DaysThresholdRow")
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(1, 3, 7, 14, 30).forEach { days ->
                            FilterChip(
                                selected = config.daysThreshold == days,
                                onClick = {
                                    if (config.isEnabled) {
                                        onConfigChanged(config.copy(daysThreshold = days))
                                    }
                                },
                                label = { Text("${days}日") },
                                enabled = config.isEnabled,
                                modifier = Modifier.testTag("DaysThresholdChip_$days"),
                            )
                        }
                    }
                }

                if (config.isEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "常駐通知からサービスが稼働します。上にスワイプするといつでも即座に閉じてスマホを使えます。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text(text = "閉じる")
            }
        },
    )

    // Permission Alert Dialog
    if (showPermissionAlert) {
        AlertDialog(
            onDismissRequest = { showPermissionAlert = false },
            title = { Text(text = "権限の許可が必要です") },
            text = {
                Text(
                    text = "ロック画面上に演習画面を表示するためには、「他のアプリの上に重ねて表示」権限を許可してください。",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionAlert = false
                        openOverlayPermissionSettings(context)
                    },
                ) {
                    Text(text = "設定を開く")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionAlert = false }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }
}

private fun openOverlayPermissionSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val generalIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(generalIntent)
        }
    }
}
