package com.aidomari.app.ui.subject

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.ui.lockscreen.LockScreenSettingsDialog
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.AppColorTheme
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
    onCalendarClick: () -> Unit = {},
    themePreferences: ThemePreferences? = null,
    lockScreenPreferences: LockScreenPreferences? = null,
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
    var showLockScreenDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }
    var importTargetSubjectId by remember { mutableStateOf<String?>(null) }

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
                        viewModel.importData(jsonString, targetSubjectId = importTargetSubjectId) { result ->
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
        onCalendarClick = onCalendarClick,
        onLockScreenClick = { showLockScreenDialog = true },
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
        onEditSubject = { subject ->
            subjectToEdit = subject
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
        var selectedColorTheme by remember { mutableStateOf<String?>(null) }

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
                    Spacer(modifier = Modifier.height(14.dp))
                    SubjectColorThemePicker(
                        selectedColorTheme = selectedColorTheme,
                        onColorThemeSelected = { selectedColorTheme = it },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.createSubject(subjectName, selectedLanguage, selectedColorTheme)
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

    // Edit Subject Dialog
    if (subjectToEdit != null) {
        val target = subjectToEdit!!
        var subjectName by remember(target) { mutableStateOf(target.name) }
        var selectedLanguage by remember(target) { mutableStateOf(target.ttsLanguage) }
        var selectedColorTheme by remember(target) { mutableStateOf(target.colorTheme) }

        AlertDialog(
            onDismissRequest = { subjectToEdit = null },
            title = { Text(text = "教科の編集") },
            text = {
                Column {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text(text = "教科名") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("EditSubjectNameTextField"),
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
                    Spacer(modifier = Modifier.height(14.dp))
                    SubjectColorThemePicker(
                        selectedColorTheme = selectedColorTheme,
                        onColorThemeSelected = { selectedColorTheme = it },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.updateSubject(target.id, subjectName, selectedLanguage, selectedColorTheme)
                            subjectToEdit = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("「$subjectName」に更新しました")
                            }
                        }
                    },
                    modifier = Modifier.testTag("SaveEditSubjectButton"),
                ) {
                    Text(text = "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToEdit = null }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var jsonText by remember { mutableStateOf("") }
        var selectedTargetSubjectId by remember { mutableStateOf<String?>(null) }
        var showSubjectDropdown by remember { mutableStateOf(false) }

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
                    Spacer(modifier = Modifier.height(12.dp))

                    if (subjects.isNotEmpty()) {
                        Text(
                            text = "インポート先教科:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                        )
                        val targetSubjectName = subjects.find { it.id == selectedTargetSubjectId }?.name
                            ?: "＋ 新規教科として作成"
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showSubjectDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ImportTargetSubjectSelector"),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = targetSubjectName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start,
                                )
                            }
                            DropdownMenu(
                                expanded = showSubjectDropdown,
                                onDismissRequest = { showSubjectDropdown = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("＋ 新規教科として作成") },
                                    onClick = {
                                        selectedTargetSubjectId = null
                                        importTargetSubjectId = null
                                        showSubjectDropdown = false
                                    },
                                )
                                subjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name) },
                                        onClick = {
                                            selectedTargetSubjectId = sub.id
                                            importTargetSubjectId = sub.id
                                            showSubjectDropdown = false
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Prominent File Selection Button
                    Button(
                        onClick = {
                            importTargetSubjectId = selectedTargetSubjectId
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
                            .height(140.dp),
                        maxLines = 8,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonText.isNotBlank()) {
                            viewModel.importData(jsonText, targetSubjectId = selectedTargetSubjectId) { result ->
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

    // Lock Screen Study Settings Dialog
    if (showLockScreenDialog) {
        val actualLockPrefs = lockScreenPreferences
            ?: remember {
                (context.applicationContext as? com.aidomari.app.AidomariApp)?.lockScreenPreferences
                    ?: LockScreenPreferences(context)
            }
        val currentLockConfig by actualLockPrefs.config.collectAsState()
        LockScreenSettingsDialog(
            config = currentLockConfig,
            onConfigChanged = {
                actualLockPrefs.updateConfig(it)
            },
            onDismissRequest = { showLockScreenDialog = false },
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
    onEditSubject: (Subject) -> Unit = {},
    onLockScreenClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
) {
    var showTopBarMenu by remember { mutableStateOf(false) }

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
                        IconButton(
                            onClick = onCalendarClick,
                            modifier = Modifier.testTag("CalendarButton"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "学習カレンダー",
                            )
                        }
                        IconButton(
                            onClick = onLockScreenClick,
                            modifier = Modifier.testTag("LockScreenSettingsButton"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "ロック画面学習設定",
                            )
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
                        Box {
                            IconButton(
                                onClick = { showTopBarMenu = true },
                                modifier = Modifier.testTag("TopBarMenuButton"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "メニュー",
                                )
                            }
                            DropdownMenu(
                                expanded = showTopBarMenu,
                                onDismissRequest = { showTopBarMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "インポート") },
                                    modifier = Modifier.testTag("MenuImportItem"),
                                    onClick = {
                                        showTopBarMenu = false
                                        onImportClick()
                                    },
                                )
                                if (subjects.isNotEmpty() || archivedSubjects.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text(text = "エクスポート") },
                                        modifier = Modifier.testTag("MenuExportItem"),
                                        onClick = {
                                            showTopBarMenu = false
                                            onExportClick()
                                        },
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        val archiveLabel = if (archivedSubjects.isNotEmpty()) {
                                            "アーカイブ済み教科 (${archivedSubjects.size})"
                                        } else {
                                            "アーカイブ済み教科"
                                        }
                                        Text(text = archiveLabel)
                                    },
                                    modifier = Modifier.testTag("TopBarArchivedButton"),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Archive,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showTopBarMenu = false
                                        onSwitchToArchived()
                                    },
                                )
                            }
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!isArchivedView) {
                FloatingActionButton(
                    onClick = onAddSubjectClick,
                    modifier = Modifier.testTag("AddSubjectFab"),
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
                            val subjectTheme = subject.colorTheme?.let { themeName ->
                                try {
                                    AppColorTheme.valueOf(themeName)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            val accentColor = subjectTheme?.previewColor ?: MaterialTheme.colorScheme.primary

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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(accentColor.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
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
                            val subjectTheme = subject.colorTheme?.let { themeName ->
                                try {
                                    AppColorTheme.valueOf(themeName)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            val accentColor = subjectTheme?.previewColor ?: MaterialTheme.colorScheme.primary

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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(accentColor.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                            ),
                                        )
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
                                                    text = { Text("教科を編集") },
                                                    modifier = Modifier.testTag("EditSubjectMenuItem_${subject.id}"),
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = null,
                                                        )
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onEditSubject(subject)
                                                    },
                                                )
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

@Composable
private fun SubjectColorThemePicker(
    selectedColorTheme: String?,
    onColorThemeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "カラーテーマ:",
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isDefaultSelected = selectedColorTheme == null
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = if (isDefaultSelected) 2.5.dp else 1.dp,
                        color = if (isDefaultSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape,
                    )
                    .testTag("ColorThemeOption_DEFAULT")
                    .clickable { onColorThemeSelected(null) },
                contentAlignment = Alignment.Center,
            ) {
                if (isDefaultSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "標準テーマ",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "標準",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AppColorTheme.entries.forEach { theme ->
                val isSelected = selectedColorTheme == theme.name
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.previewColor)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape,
                        )
                        .testTag("ColorThemeOption_${theme.name}")
                        .clickable { onColorThemeSelected(theme.name) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = theme.displayName,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}
