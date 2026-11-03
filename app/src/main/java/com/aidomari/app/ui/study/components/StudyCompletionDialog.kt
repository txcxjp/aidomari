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
) {
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
                text = "学習完了！",
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
                    text = "$partName の全 $totalCards 単語の学習が完了しました。",
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
                if (hasNextPart) {
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

