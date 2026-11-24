package com.aidomari.app.ui.digest

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aidomari.app.core.ai.GeminiApiClientImpl
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.domain.model.StudyDirection
import com.aidomari.app.domain.model.ai.CardExtractionMode
import com.aidomari.app.domain.model.article.SourceArticle
import com.aidomari.app.ui.components.StudyDirectionSelector
import com.aidomari.app.ui.settings.GeminiSettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowsingDigestScreen(
    viewModel: BrowsingDigestViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showGeminiSettings by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { _ -> }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            android.os.Build.FINGERPRINT != "robolectric" &&
            !com.aidomari.app.core.ai.CardGenerationService.canPostNotifications(context)
        ) {
            runCatching {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // エラー / 成功通知のスナックバー表示
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    if (uiState.reviewArticle != null) {
        CardReviewDialog(
            article = uiState.reviewArticle!!,
            candidates = uiState.candidateCards,
            subjects = uiState.subjects,
            selectedSubjectId = uiState.selectedSubjectId,
            newDeckName = uiState.newDeckName,
            onSubjectSelect = { viewModel.selectSubject(it) },
            onCreateSubject = { viewModel.createSubject(it) },
            onDeckNameChange = { viewModel.updateNewDeckName(it) },
            onToggleSelect = { viewModel.toggleCardSelection(it) },
            onSelectAll = { viewModel.selectAllCandidates(it) },
            onCandidateChange = { id, f, b, s -> viewModel.updateCandidate(id, f, b, s) },
            onSave = { viewModel.saveSelectedCards() },
            onDismiss = { viewModel.closeReview() },
            selectedStudyDirection = uiState.selectedStudyDirection,
            onStudyDirectionSelect = { viewModel.selectStudyDirection(it) },
            modifier = modifier,
            snackbarHostState = snackbarHostState,
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "自動デッキ作成",
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState.pendingCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    "${uiState.pendingCount}件",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { showGeminiSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Gemini設定")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Gemini API 未設定バナー
            if (!uiState.isGeminiConfigured) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Gemini APIキーが必要です",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                            Text(
                                "カードの自動作成を利用するには、Google AI StudioのAPIキーを登録してください。",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            onClick = { showGeminiSettings = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("設定", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 抽出モードセレクター
            ExtractionModeSelectorBar(
                selectedMode = uiState.selectedMode,
                onSelectMode = { viewModel.selectMode(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // 記事リスト
            val articles = uiState.filteredArticles
            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "未処理の記事はありません",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "共有メニューやブラウザ拡張機能から記事を送ると、ここに表示されます。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(articles, key = { it.id }) { article ->
                        ArticleItemCard(
                            article = article,
                            isGenerating = uiState.isGenerating && uiState.generatingArticleId == article.id,
                            progressFraction = if (uiState.generatingArticleId == article.id) uiState.generationProgressFraction else null,
                            onGenerateClick = { viewModel.generateCards(article) },
                            onDeleteClick = { viewModel.deleteArticle(article.id) },
                        )
                    }
                }
            }
        }
    }

    // Gemini設定ダイアログ
    if (showGeminiSettings) {
        val geminiPrefs = remember { GeminiPreferences(context) }
        val geminiConfig by geminiPrefs.config.collectAsState()
        val geminiClient = remember { GeminiApiClientImpl() }
        GeminiSettingsDialog(
            config = geminiConfig,
            onConfigChanged = { geminiPrefs.updateConfig(it) },
            onTestConnection = { key, model -> geminiClient.testConnection(key, model) },
            onFetchModels = { key -> geminiClient.fetchAvailableModels(key) },
            onDismissRequest = { showGeminiSettings = false },
        )
    }
}

@Composable
fun ExtractionModeSelectorBar(
    selectedMode: CardExtractionMode,
    onSelectMode: (CardExtractionMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CardExtractionMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    val icon: ImageVector = when (mode) {
                        CardExtractionMode.SMART -> Icons.Default.Explore
                        CardExtractionMode.CORE -> Icons.Default.Lightbulb
                        CardExtractionMode.KNOWLEDGE -> Icons.Default.EditNote
                    }
                    val bgColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                    val contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable(enabled = enabled) { onSelectMode(mode) }
                            .padding(vertical = 7.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedMode.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ArticleItemCard(
    article: SourceArticle,
    isGenerating: Boolean,
    progressFraction: Float? = null,
    onGenerateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // メタ行: ドメイン、文字数
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.domain.ifBlank { "Web記事" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${article.extractedText.length.coerceAtLeast(0)}文字",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // タイトル
            Text(
                text = article.title.ifBlank { "無題の記事" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )

            // 本文抜粋 / 全文プレビュー
            if (article.extractedText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                if (expanded) {
                    val previewScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                            ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "全文プレビュー",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { expanded = false }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandLess,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "閉じる",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(previewScrollState)
                                .padding(10.dp),
                        ) {
                            Text(
                                text = article.extractedText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                } else {
                    val snippet = article.extractedText.take(120) +
                        if (article.extractedText.length > 120) "..." else ""
                    Text(
                        text = snippet,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                    )
                }
            }

            // 進捗バー表示（パーセンテージのみ）
            if (isGenerating) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (progressFraction != null) {
                        LinearProgressIndicator(
                            progress = { progressFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt().coerceIn(0, 100)}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 下部アクション行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.height(32.dp),
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (expanded) "閉じる" else "全文プレビュー", fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "削除",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onGenerateClick,
                        enabled = !isGenerating,
                        modifier = Modifier.height(36.dp),
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("作成中...", fontSize = 12.sp)
                        } else {
                            Icon(
                                Icons.Default.Style,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "カード作成",
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardReviewDialog(
    article: SourceArticle,
    candidates: List<CardCandidateItem>,
    subjects: List<com.aidomari.app.domain.model.Subject>,
    selectedSubjectId: String?,
    newDeckName: String,
    onSubjectSelect: (String) -> Unit,
    onCreateSubject: (String) -> Unit = {},
    onDeckNameChange: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onCandidateChange: (String, String, String, String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    selectedStudyDirection: StudyDirection = StudyDirection.DESCRIPTION_TO_TERM,
    onStudyDirectionSelect: (StudyDirection) -> Unit = {},
) {
    val selectedCount = candidates.count { it.isSelected }
    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }
    var showCreateSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectNameInput by remember { mutableStateOf("") }
    var editingCandidate by remember { mutableStateOf<CardCandidateItem?>(null) }

    BackHandler(onBack = onDismiss)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("作成カードの確認・登録", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    "$selectedCount/${candidates.size}件",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                        Text(
                            article.title.take(30),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSelectAll(selectedCount != candidates.size) },
                    ) {
                        Text(if (selectedCount == candidates.size) "全解除" else "全選択", fontSize = 12.sp)
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                // 保存先設定セクション
                Text("保存先設定", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 科目選択ドロップダウン
                    ExposedDropdownMenuBox(
                        expanded = isSubjectDropdownExpanded,
                        onExpandedChange = { isSubjectDropdownExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        val currentSubjectName = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "新規科目を作成"
                        OutlinedTextField(
                            value = currentSubjectName,
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(fontSize = 13.sp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            label = { Text("科目", fontSize = 10.sp) },
                            singleLine = true,
                        )
                        ExposedDropdownMenu(
                            expanded = isSubjectDropdownExpanded,
                            onDismissRequest = { isSubjectDropdownExpanded = false },
                        ) {
                            subjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub.name) },
                                    onClick = {
                                        onSubjectSelect(sub.id)
                                        isSubjectDropdownExpanded = false
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "＋ 新規科目を作成...",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                },
                                onClick = {
                                    isSubjectDropdownExpanded = false
                                    newSubjectNameInput = ""
                                    showCreateSubjectDialog = true
                                },
                            )
                        }
                    }

                    // デッキ名
                    OutlinedTextField(
                        value = newDeckName,
                        onValueChange = onDeckNameChange,
                        textStyle = TextStyle(fontSize = 13.sp),
                        label = { Text("デッキ名", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "出題の向き",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                StudyDirectionSelector(
                    selected = selectedStudyDirection,
                    onSelect = onStudyDirectionSelect,
                )

                Spacer(Modifier.height(10.dp))

                // 承認・保存ボタン
                Button(
                    onClick = onSave,
                    enabled = selectedCount > 0,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${selectedCount}枚のカードをデッキに登録", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(candidates, key = { it.id }) { candidate ->
                CandidateCardItem(
                    candidate = candidate,
                    onToggle = { onToggleSelect(candidate.id) },
                    onEditClick = { editingCandidate = candidate },
                )
            }
        }
    }

    if (showCreateSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSubjectDialog = false },
            title = { Text("新規科目の作成", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newSubjectNameInput,
                    onValueChange = { newSubjectNameInput = it },
                    label = { Text("科目名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSubjectNameInput.isNotBlank()) {
                            onCreateSubject(newSubjectNameInput)
                            showCreateSubjectDialog = false
                        }
                    },
                    enabled = newSubjectNameInput.isNotBlank(),
                ) {
                    Text("作成", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSubjectDialog = false }) {
                    Text("キャンセル")
                }
            },
        )
    }

    val targetCandidate = editingCandidate
    if (targetCandidate != null) {
        CandidateCardEditDialog(
            candidate = targetCandidate,
            onDismiss = { editingCandidate = null },
            onSave = { front, back, supplement ->
                onCandidateChange(targetCandidate.id, front, back, supplement)
                editingCandidate = null
            },
        )
    }
}

@Composable
fun CandidateCardItem(
    candidate: CardCandidateItem,
    onToggle: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (candidate.isSelected) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (candidate.isSelected) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
            },
        ),
        border = if (candidate.isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = candidate.isSelected,
                    onCheckedChange = { onToggle() },
                )
                Text(
                    text = candidate.term,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "編集",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        modifier = Modifier.size(19.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
            ) {
                Text(
                    text = candidate.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                )

                if (candidate.supplement.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(
                            alpha = if (candidate.isSelected) 0.75f else 0.4f,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
                        val memoText = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = primaryColor)) {
                                append("メモ: ")
                            }
                            append(candidate.supplement)
                        }
                        Text(
                            text = memoText,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateCardEditDialog(
    candidate: CardCandidateItem,
    onDismiss: () -> Unit,
    onSave: (term: String, description: String, supplement: String) -> Unit,
) {
    var term by remember(candidate.id) { mutableStateOf(candidate.term) }
    var description by remember(candidate.id) { mutableStateOf(candidate.description) }
    var supplement by remember(candidate.id) { mutableStateOf(candidate.supplement) }
    val canSave = term.isNotBlank() && description.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "カードを編集",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "閉じる",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = term,
                        onValueChange = { term = it },
                        label = { Text("ことば（単語・用語）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 1,
                        maxLines = 3,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("説明（意味・定義）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                    )
                    OutlinedTextField(
                        value = supplement,
                        onValueChange = { supplement = it },
                        label = { Text("補足・出典メモ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("キャンセル")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (canSave) {
                                onSave(term.trim(), description.trim(), supplement.trim())
                            }
                        },
                        enabled = canSave,
                    ) {
                        Text("反映", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
