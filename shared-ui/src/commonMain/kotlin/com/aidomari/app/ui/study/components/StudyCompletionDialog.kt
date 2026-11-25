package com.aidomari.app.ui.study.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews

@Composable
fun StudyCompletionDialog(
    partName: String,
    totalCards: Int,
    hasNextPart: Boolean,
    onNextPart: () -> Unit,
    onRestartPart: () -> Unit,
    onBackToDecks: () -> Unit,
    isRandomMode: Boolean = false,
    onContinueRandomStudy: (() -> Unit)? = null,
) {
    val titleText = if (isRandomMode) "ランダム出題完了！" else "学習完了！"
    val messageText = if (isRandomMode) {
        "全 $totalCards 単語のランダム出題が完了しました。"
    } else {
        "$partName の全 $totalCards 単語の学習が完了しました。"
    }

    AlertDialog(
        onDismissRequest = { /* Modal dialog */ },
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
            )
        },
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isRandomMode && onContinueRandomStudy != null) {
                    Button(
                        onClick = onContinueRandomStudy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "続けて出題する")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (hasNextPart) {
                    Button(
                        onClick = onNextPart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "次のパートへ進む")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onRestartPart,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "このパートをもう一度学習")
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onBackToDecks,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "デッキ一覧に戻る")
                }
            }
        },
        dismissButton = null,
    )
}

@ThemePreviews
@Composable
private fun StudyCompletionDialogPreview() {
    AidomariTheme {
        StudyCompletionDialog(
            partName = "Part 1",
            totalCards = 50,
            hasNextPart = true,
            onNextPart = {},
            onRestartPart = {},
            onBackToDecks = {},
        )
    }
}

@ThemePreviews
@Composable
private fun StudyCompletionDialog_Random_Preview() {
    AidomariTheme {
        StudyCompletionDialog(
            partName = "Part 1",
            totalCards = 10,
            hasNextPart = false,
            onNextPart = {},
            onRestartPart = {},
            onBackToDecks = {},
            isRandomMode = true,
            onContinueRandomStudy = {},
        )
    }
}
