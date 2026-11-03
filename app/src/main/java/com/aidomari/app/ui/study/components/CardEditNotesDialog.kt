package com.aidomari.app.ui.study.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews

@Composable
fun CardEditNotesDialog(
    card: Card,
    onDismissRequest: () -> Unit,
    onSave: (question: String, supplement: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var questionText by remember(card) { mutableStateOf(card.userQuestion) }
    var supplementText by remember(card) { mutableStateOf(card.userSupplement) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = "カードの質問・メモを編集",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Card word indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = card.front,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = card.back,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Question on front
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("表面の質問（自分用クイズ）") },
                    placeholder = { Text("例: 派生語は？ / 類義語は？") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                )

                // Supplement note on back
                OutlinedTextField(
                    value = supplementText,
                    onValueChange = { supplementText = it },
                    label = { Text("裏面の補足メモ（解説・例文など）") },
                    placeholder = { Text("例: 例文: He is honest.\n注意: 前置詞はtoを使う") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(questionText, supplementText)
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("キャンセル")
            }
        },
    )
}

@ThemePreviews
@Composable
private fun CardEditNotesDialogPreview() {
    AidomariTheme {
        CardEditNotesDialog(
            card = PreviewSampleData.sampleCard,
            onDismissRequest = {},
            onSave = { _, _ -> },
        )
    }
}

