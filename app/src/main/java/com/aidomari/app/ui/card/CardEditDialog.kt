package com.aidomari.app.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.Card

@Composable
fun CardEditDialog(
    card: Card?,
    onDismissRequest: () -> Unit,
    onSave: (front: String, back: String, userQuestion: String, userSupplement: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditMode = card != null
    var frontText by remember(card) { mutableStateOf(card?.front ?: "") }
    var backText by remember(card) { mutableStateOf(card?.back ?: "") }
    var questionText by remember(card) { mutableStateOf(card?.userQuestion ?: "") }
    var supplementText by remember(card) { mutableStateOf(card?.userSupplement ?: "") }

    val canSave = frontText.isNotBlank() && backText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("CardEditDialog"),
        title = {
            Text(
                text = if (isEditMode) "単語カードを編集" else "新規単語を追加",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = frontText,
                    onValueChange = { frontText = it },
                    label = { Text("表面（単語・見出し語）*") },
                    placeholder = { Text("例: serendipity") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("CardEditFrontTextField"),
                )

                OutlinedTextField(
                    value = backText,
                    onValueChange = { backText = it },
                    label = { Text("裏面（意味・訳・解答）*") },
                    placeholder = { Text("例: 思いがけない発見、幸運な偶然") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("CardEditBackTextField"),
                )

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("表面の質問（自分用クイズ）") },
                    placeholder = { Text("例: 派生語は？ / 類義語は？") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("CardEditQuestionTextField"),
                )

                OutlinedTextField(
                    value = supplementText,
                    onValueChange = { supplementText = it },
                    label = { Text("裏面の補足メモ（解説・例文など）") },
                    placeholder = { Text("例: 語源: セレンディップの3人の王子") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("CardEditSupplementTextField"),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        onSave(frontText, backText, questionText, supplementText)
                    }
                },
                enabled = canSave,
                modifier = Modifier.testTag("CardEditSaveButton"),
            ) {
                Text(text = "保存")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("CardEditCancelButton"),
            ) {
                Text(text = "キャンセル")
            }
        },
    )
}
