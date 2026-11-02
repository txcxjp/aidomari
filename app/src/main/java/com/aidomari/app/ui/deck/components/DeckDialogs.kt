package com.aidomari.app.ui.deck.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.aidomari.app.domain.model.Deck

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDeckDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (
        name: String,
        splitSize: Int,
        autoContinue: Boolean,
        shuffleMode: Boolean,
        reverseMode: Boolean,
        autoSpeak: Boolean,
        speechRate: Float,
    ) -> Unit,
) {
    var deckName by remember { mutableStateOf("") }
    var splitSizeText by remember { mutableStateOf("50") }
    var autoContinue by remember { mutableStateOf(false) }
    var shuffleMode by remember { mutableStateOf(false) }
    var reverseMode by remember { mutableStateOf(false) }
    var autoSpeak by remember { mutableStateOf(false) }
    var speechRate by remember { mutableFloatStateOf(1.0f) }
    val rateOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "新規デッキの作成") },
        text = {
            Column {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text(text = "デッキ名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = splitSizeText,
                    onValueChange = { splitSizeText = it },
                    label = { Text(text = "分割枚数（デフォルト: 50枚）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "自動継続（終了時に次のPartへ）")
                    Switch(checked = autoContinue, onCheckedChange = { autoContinue = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "シャッフル出題")
                    Switch(checked = shuffleMode, onCheckedChange = { shuffleMode = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "裏面から出題（和英・意味先出し）")
                    Switch(checked = reverseMode, onCheckedChange = { reverseMode = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "自動読み上げ（カード表示時に発音）")
                    Switch(checked = autoSpeak, onCheckedChange = { autoSpeak = it })
                }
                if (autoSpeak) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "読み上げ速度: ${speechRate}x",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rateOptions.forEach { rate ->
                            val isSelected = (speechRate - rate).let { it >= -0.01f && it <= 0.01f }
                            FilterChip(
                                selected = isSelected,
                                onClick = { speechRate = rate },
                                label = { Text(text = "${rate}x") },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deckName.isNotBlank()) {
                        val splitSize = splitSizeText.toIntOrNull() ?: 50
                        onConfirm(
                            deckName.trim(),
                            splitSize,
                            autoContinue,
                            shuffleMode,
                            reverseMode,
                            autoSpeak,
                            speechRate,
                        )
                    }
                },
            ) {
                Text(text = "作成")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "キャンセル")
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditDeckDialog(
    deck: Deck,
    onDismissRequest: () -> Unit,
    onConfirm: (updated: Deck) -> Unit,
) {
    var deckName by remember(deck) { mutableStateOf(deck.name) }
    var splitSizeText by remember(deck) { mutableStateOf(deck.splitSize.toString()) }
    var autoContinue by remember(deck) { mutableStateOf(deck.autoContinue) }
    var shuffleMode by remember(deck) { mutableStateOf(deck.shuffleMode) }
    var reverseMode by remember(deck) { mutableStateOf(deck.reverseMode) }
    var autoSpeak by remember(deck) { mutableStateOf(deck.autoSpeak) }
    var speechRate by remember(deck) { mutableFloatStateOf(deck.speechRate) }
    val rateOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "デッキ設定の変更") },
        text = {
            Column {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text(text = "デッキ名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = splitSizeText,
                    onValueChange = { splitSizeText = it },
                    label = { Text(text = "分割枚数") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "自動継続（終了時に次のPartへ）")
                    Switch(checked = autoContinue, onCheckedChange = { autoContinue = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "シャッフル出題")
                    Switch(checked = shuffleMode, onCheckedChange = { shuffleMode = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "裏面から出題（和英・意味先出し）")
                    Switch(checked = reverseMode, onCheckedChange = { reverseMode = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "自動読み上げ（カード表示時に発音）")
                    Switch(checked = autoSpeak, onCheckedChange = { autoSpeak = it })
                }
                if (autoSpeak) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "読み上げ速度: ${speechRate}x",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rateOptions.forEach { rate ->
                            val isSelected = (speechRate - rate).let { it >= -0.01f && it <= 0.01f }
                            FilterChip(
                                selected = isSelected,
                                onClick = { speechRate = rate },
                                label = { Text(text = "${rate}x") },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deckName.isNotBlank()) {
                        val splitSize = splitSizeText.toIntOrNull()?.coerceAtLeast(1) ?: deck.splitSize
                        val updated = deck.copy(
                            name = deckName.trim(),
                            splitSize = splitSize,
                            autoContinue = autoContinue,
                            shuffleMode = shuffleMode,
                            reverseMode = reverseMode,
                            autoSpeak = autoSpeak,
                            speechRate = speechRate,
                        )
                        onConfirm(updated)
                    }
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

@Composable
fun DeleteDeckConfirmDialog(
    deck: Deck,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "デッキの削除") },
        text = {
            Text(text = "「${deck.name}」および所属するすべての単語カードを削除しますか？この操作は取り消せません。")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = "削除する")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "キャンセル")
            }
        },
    )
}
