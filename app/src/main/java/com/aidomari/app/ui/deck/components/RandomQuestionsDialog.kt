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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    onStartStudy: (count: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCount by remember(initialCount) { mutableIntStateOf(initialCount) }
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
                text = "ランダム出題の設定",
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
                    text = "教科全体のカード（${totalCards}枚）からランダムに出題します。出題する問題数を選択してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    options.forEach { count ->
                        val isSelected = selectedCount == count
                        val label = if (count <= 0) "全問 (${totalCards}問)" else "${count}問"
                        val isAvailable = count <= 0 || totalCards >= count

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCount = count },
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
                                    onClick = { selectedCount = count },
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAvailable) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
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
                onClick = {
                    val finalCount = if (selectedCount <= 0) -1 else selectedCount
                    onStartStudy(finalCount)
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
            initialCount = 20,
            onDismissRequest = {},
            onStartStudy = {},
        )
    }
}

