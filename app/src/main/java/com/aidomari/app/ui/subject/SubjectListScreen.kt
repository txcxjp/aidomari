package com.aidomari.app.ui.subject

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreferences
import com.aidomari.app.ui.theme.ThemePreviews
import com.aidomari.app.ui.theme.ThemeSettingsDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectViewModel,
    onSubjectClick: (subjectId: String, subjectName: String) -> Unit,
    modifier: Modifier = Modifier,
    themePreferences: ThemePreferences? = null,
) {
    val context = LocalContext.current
    val subjects by viewModel.subjects.collectAsState()
    val archivedSubjects by viewModel.archivedSubjects.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isArchivedView by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    BackHandler(enabled = isArchivedView) {
        isArchivedView = false
    }

    // File picker launcher for JSON import (Storage Access Framework)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    }
                    if (!jsonString.isNullOrBlank()) {
                        viewModel.importData(jsonString) { result ->
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
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("ファイルの読み込みに失敗しました: ${e.message}")
                }
            }
        }
    }

    // Export launcher to save JSON backup
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val exportJson = viewModel.exportData()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(exportJson.toByteArray())
                    }
                    snackbarHostState.showSnackbar("エクスポート完了: バックアップを保存しました")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("エクスポートに失敗しました: ${e.message}")
                }
            }
        }
    }

    SubjectListContent(
        subjects = subjects,
        archivedSubjects = archivedSubjects,
        isArchivedView = isArchivedView,
        onSubjectClick = onSubjectClick,
        onBackFromArchived = { isArchivedView = false },
        onSwitchToArchived = { isArchivedView = true },
        onAddSubjectClick = { showAddDialog = true },
        onImportClick = { showImportDialog = true },
        onExportClick = { exportFileLauncher.launch("aidomari_backup.json") },
        onThemeClick = { showThemeDialog = true },
        onArchiveSubject = { subject ->
            viewModel.archiveSubject(subject.id)
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "「${subject.name}」をアーカイブしました",
                    actionLabel = "元に戻す",
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.unarchiveSubject(subject.id)
                }
            }
        },
        onUnarchiveSubject = { subject ->
            viewModel.unarchiveSubject(subject.id)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("「${subject.name}」を元に戻しました")
            }
        },
        onDeleteSubject = { subject ->
            subjectToDelete = subject
        },
        onFilePickerClick = {
            filePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )

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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "現行アプリ（Elm）のエクスポートファイルや単語帳JSONを取り込みます。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent File Selection Button
                    Button(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "ファイルを選択してインポート",
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "または JSON テキストを直接貼り付け",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        label = { Text(text = "JSON テキスト") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 8,
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
                    enabled = jsonText.isNotBlank(),
                ) {
                    Text(text = "テキストから取り込み")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(text = "閉じる")
                }
            },
        )
    }

    // Delete confirmation dialog (only accessible from Archived view)
    if (subjectToDelete != null) {
        val target = subjectToDelete!!
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text(text = "教科を完全に削除") },
            text = {
                Text(text = "「${target.name}」および所属するすべてのデッキ・単語カードを完全に削除しますか？\nこの操作は取り消せません。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(target.id)
                        subjectToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("「${target.name}」を完全に削除しました")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(text = "完全に削除する")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }

    // Theme Settings Dialog
    if (showThemeDialog) {
        val actualPrefs = themePreferences ?: remember { ThemePreferences(context) }
        val currentConfig by actualPrefs.themeConfig.collectAsState()
        ThemeSettingsDialog(
            currentConfig = currentConfig,
            onConfigChanged = { actualPrefs.setThemeConfig(it) },
            onDismissRequest = { showThemeDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListContent(
    subjects: List<Subject>,
    archivedSubjects: List<Subject>,
    isArchivedView: Boolean,
    onSubjectClick: (subjectId: String, subjectName: String) -> Unit,
    onBackFromArchived: () -> Unit,
    onSwitchToArchived: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onThemeClick: () -> Unit,
    onArchiveSubject: (Subject) -> Unit,
    onUnarchiveSubject: (Subject) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onFilePickerClick: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isArchivedView) {
                TopAppBar(
                    title = {
                        Text(
                            text = "アーカイブ済み教科",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackFromArchived) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "相泊",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "v${com.aidomari.app.BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        if (archivedSubjects.isNotEmpty()) {
                            TextButton(
                                onClick = onSwitchToArchived,
                                modifier = Modifier.testTag("TopBarArchivedButton"),
                            ) {
                                Text(text = "アーカイブ (${archivedSubjects.size})")
                            }
                        }
                        TextButton(onClick = onImportClick) {
                            Text(text = "インポート")
                        }
                        if (subjects.isNotEmpty() || archivedSubjects.isNotEmpty()) {
                            TextButton(onClick = onExportClick) {
                                Text(text = "エクスポート")
                            }
                        }
                        IconButton(
                            onClick = onThemeClick,
                            modifier = Modifier.testTag("ThemeSettingsButton"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "テーマ設定",
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!isArchivedView) {
                FloatingActionButton(
                    onClick = onAddSubjectClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Subject",
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (isArchivedView) {
                // Archived Subjects List
                if (archivedSubjects.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "アーカイブされた教科はありません",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(archivedSubjects, key = { it.id }) { subject ->
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
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                            ) {
                                                Text(
                                                    text = "アーカイブ済み",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                )
                                            }
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
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        var showMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showMenu = true },
                                                modifier = Modifier
                                                    .testTag("ArchivedSubjectMoreOptionsButton_${subject.id}")
                                                    .semantics { contentDescription = "Archived Subject More Options" },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Archived Subject More Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false },
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("元に戻す") },
                                                    modifier = Modifier.testTag("UnarchiveMenuItem_${subject.id}"),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Unarchive,
                                                            contentDescription = null,
                                                        )
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onUnarchiveSubject(subject)
                                                    },
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "教科を完全に削除",
                                                            color = MaterialTheme.colorScheme.error,
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("DeleteSubjectMenuItem_${subject.id}"),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error,
                                                        )
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onDeleteSubject(subject)
                                                    },
                                                )
                                            }
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
            } else {
                // Active Subjects List
                if (subjects.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (archivedSubjects.isNotEmpty()) {
                            Text(
                                text = "アクティブな教科がありません",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSwitchToArchived,
                                modifier = Modifier.testTag("ArchivedEmptyViewButton"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "アーカイブされた教科を見る (${archivedSubjects.size}件)")
                            }
                        } else {
                            Text(
                                text = "教科が登録されていません",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "端末内の単語帳ファイル（.json / .txt）を選択するか、\n右下の ＋ ボタンから教科を作成してください。",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            if (onFilePickerClick != null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onFilePickerClick) {
                                        Text(text = "ファイルを選択してインポート")
                                    }
                                }
                            }
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
                                        var showMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showMenu = true },
                                                modifier = Modifier
                                                    .testTag("SubjectMoreOptionsButton_${subject.id}")
                                                    .semantics { contentDescription = "Subject More Options" },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Subject More Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false },
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("教科をアーカイブ") },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Archive,
                                                            contentDescription = null,
                                                        )
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onArchiveSubject(subject)
                                                    },
                                                )
                                            }
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

                        if (archivedSubjects.isNotEmpty()) {
                            item(key = "archived_section_link") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onSwitchToArchived)
                                        .testTag("ArchivedSubjectsLink"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Archive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                text = "アーカイブされた教科 (${archivedSubjects.size}件)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
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
    }
}

@ThemePreviews
@Composable
private fun SubjectListContentPreview() {
    AidomariTheme {
        SubjectListContent(
            subjects = PreviewSampleData.sampleSubjects,
            archivedSubjects = emptyList(),
            isArchivedView = false,
            onSubjectClick = { _, _ -> },
            onBackFromArchived = {},
            onSwitchToArchived = {},
            onAddSubjectClick = {},
            onImportClick = {},
            onExportClick = {},
            onThemeClick = {},
            onArchiveSubject = {},
            onUnarchiveSubject = {},
            onDeleteSubject = {},
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun SubjectListContentEmptyPreview() {
    AidomariTheme {
        SubjectListContent(
            subjects = emptyList(),
            archivedSubjects = emptyList(),
            isArchivedView = false,
            onSubjectClick = { _, _ -> },
            onBackFromArchived = {},
            onSwitchToArchived = {},
            onAddSubjectClick = {},
            onImportClick = {},
            onExportClick = {},
            onThemeClick = {},
            onArchiveSubject = {},
            onUnarchiveSubject = {},
            onDeleteSubject = {},
        )
    }
}

@Preview(name = "Archived View", showBackground = true)
@Composable
private fun SubjectListContentArchivedPreview() {
    AidomariTheme {
        SubjectListContent(
            subjects = emptyList(),
            archivedSubjects = listOf(PreviewSampleData.sampleSubjects.first().copy(isArchived = true)),
            isArchivedView = true,
            onSubjectClick = { _, _ -> },
            onBackFromArchived = {},
            onSwitchToArchived = {},
            onAddSubjectClick = {},
            onImportClick = {},
            onExportClick = {},
            onThemeClick = {},
            onArchiveSubject = {},
            onUnarchiveSubject = {},
            onDeleteSubject = {},
        )
    }
}
