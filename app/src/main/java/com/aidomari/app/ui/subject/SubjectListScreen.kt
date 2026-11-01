package com.aidomari.app.ui.subject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.Subject
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectViewModel,
    onSubjectClick: (subjectId: String, subjectName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subjects by viewModel.subjects.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aidomari（相泊）",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    TextButton(onClick = { showImportDialog = true }) {
                        Text(text = "インポート")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject",
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (subjects.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "教科が登録されていません",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "右下の ＋ ボタンから新規作成するか、\nインポートボタンから過去の単語帳データを読み込んでください。",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { showImportDialog = true }) {
                        Text(text = "データをインポートする")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubjectClick(subject.id, subject.name) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                        ),
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                    ) {
                                        Text(
                                            text = "TTS音声: ${subject.ttsLanguage}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { subjectToDelete = subject }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Subject",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddDialog) {
        var subjectName by remember { mutableStateOf("") }
        var selectedLanguage by remember { mutableStateOf("en-US") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "新規教科の作成") },
            text = {
                Column {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text(text = "教科名（例: 英単語、世界史）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "読み上げ言語（TTS）:",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("en-US" to "英語", "ja-JP" to "日本語", "auto" to "自動").forEach { (code, label) ->
                            OutlinedButton(
                                onClick = { selectedLanguage = code },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedLanguage == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.createSubject(subjectName, selectedLanguage)
                            showAddDialog = false
                        }
                    },
                ) {
                    Text(text = "作成")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var jsonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(text = "データインポート") },
            text = {
                Column {
                    Text(
                        text = "Elm形式または新形式のJSONを貼り付けてください：",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        placeholder = { Text(text = "[ { \"title\": \"デッキ名\", \"content\": [...] } ]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonText.isNotBlank()) {
                            viewModel.importData(jsonText) { result ->
                                coroutineScope.launch {
                                    result.onSuccess { summary ->
                                        snackbarHostState.showSnackbar(
                                            "インポート完了: ${summary.decksCount} デッキ / ${summary.cardsCount} 単語",
                                        )
                                        showImportDialog = false
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar("インポート失敗: ${err.message}")
                                    }
                                }
                            }
                        }
                    },
                ) {
                    Text(text = "インポート実行")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }

    // Delete confirmation dialog
    if (subjectToDelete != null) {
        val target = subjectToDelete!!
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text(text = "教科の削除") },
            text = {
                Text(text = "「${target.name}」および所属するすべてのデッキ・単語カードを削除しますか？この操作は取り消せません。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(target.id)
                        subjectToDelete = null
                    },
                ) {
                    Text(text = "削除する")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }
}
