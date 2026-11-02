package com.aidomari.app.ui.study.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyTtsSettingsDialog(
    autoSpeak: Boolean,
    speechRate: Float,
    onDismissRequest: () -> Unit,
    onSave: (autoSpeak: Boolean, speechRate: Float) -> Unit,
    modifier: Modifier = Modifier,
    onTestSpeak: ((rate: Float) -> Unit)? = null,
) {
    var currentAutoSpeak by remember(autoSpeak) { mutableStateOf(autoSpeak) }
    var currentSpeechRate by remember(speechRate) { mutableFloatStateOf(speechRate) }

    val rateOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = "音声・読み上げ設定",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自動読み上げ",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "カード表示時に自動で発音",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = currentAutoSpeak,
                        onCheckedChange = { currentAutoSpeak = it },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "読み上げ速度",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rateOptions.forEach { rate ->
                        val isSelected = (currentSpeechRate - rate).let { it >= -0.01f && it <= 0.01f }
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentSpeechRate = rate },
                            label = { Text(text = "${rate}x") },
                        )
                    }
                }

                if (onTestSpeak != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onTestSpeak(currentSpeechRate) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "発音テスト (${currentSpeechRate}x)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentAutoSpeak, currentSpeechRate)
                },
            ) {
                Text(text = "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "キャンセル")
            }
        },
    )
}
