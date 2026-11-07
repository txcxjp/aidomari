package com.aidomari.app.ui.deck.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews

@Composable
fun RandomQuestionsDialog(
    totalCards: Int,
    initialCount: Int,
    onDismissRequest: () -> Unit,
    onStartStudy: (count: Int, onlyLearned: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    learnedCardsCount: Int = 0,
    initialOnlyLearned: Boolean = false,
    title: String = "ランダム出題の設定",
    description: String = "教科全体のカード（${totalCards}枚）からランダムに出題します。出題する問題数を選択してください。",
) {
    var onlyLearned by remember(initialOnlyLearned) { mutableStateOf(initialOnlyLearned) }
    var selectedCount by remember(initialCount) { mutableIntStateOf(initialCount) }
    val isLearnedAvailable = learnedCardsCount > 0
    val isFilteringLearned = onlyLearned && isLearnedAvailable
    val effectiveTotalCards = if (isFilteringLearned) learnedCardsCount else totalCards

    val options = listOf(10, 20, 30, 50, -1)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle for only learned cards
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isLearnedAvailable) {
                            onlyLearned = !onlyLearned
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "学習済みの単語のみ出題",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLearnedAvailable) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                            )
                            Text(
                                text = if (isLearnedAvailable) {
                                    "学習済み: $learnedCardsCount / ${totalCards}問"
                                } else {
                                    "学習済みの単語がありません"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = isFilteringLearned,
                            onCheckedChange = { onlyLearned = it },
                            enabled = isLearnedAvailable,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    options.forEach { count ->
                        val isAvailable = effectiveTotalCards > 0 && (count <= 0 || effectiveTotalCards >= count)
                        val isSelected = if (selectedCount <= 0 || selectedCount > effectiveTotalCards) {
                            count == -1
                        } else {
                            selectedCount == count
                        }
                        val label = if (count <= 0) "全問 (${effectiveTotalCards}問)" else "${count}問"

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isAvailable) {
                                    selectedCount = count
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    enabled = isAvailable,
                                    onClick = { if (isAvailable) selectedCount = count },
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAvailable) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = effectiveTotalCards > 0,
                onClick = {
                    val finalCount = if (selectedCount <= 0 || selectedCount > effectiveTotalCards) {
                        -1
                    } else {
                        selectedCount
                    }
                    onStartStudy(finalCount, isFilteringLearned)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "学習開始")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "キャンセル")
            }
        },
    )
}

@ThemePreviews
@Composable
private fun RandomQuestionsDialogPreview() {
    AidomariTheme {
        RandomQuestionsDialog(
            totalCards = 100,
            learnedCardsCount = 42,
            initialCount = 20,
            onDismissRequest = {},
            onStartStudy = { _, _ -> },
        )
    }
}
