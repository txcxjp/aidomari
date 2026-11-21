package com.aidomari.app.ui.digest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aidomari.app.core.ai.GeminiApiClientImpl
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "本日の閲覧デッキ化",
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
                                "AIカード生成を利用するには、Google AI StudioのAPIキーを登録してください。",
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

            // フィルタータブ
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ArticleFilterTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            val count = when (tab) {
                                ArticleFilterTab.PENDING -> uiState.articles.count { it.status == ArticleStatus.PENDING }
                                ArticleFilterTab.GENERATED -> uiState.articles.count { it.status == ArticleStatus.GENERATED }
                                ArticleFilterTab.ALL -> uiState.articles.size
                            }
                            Text("${tab.label} ($count)")
                        },
                    )
                }
            }

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
                            "記事がありません",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "ブラウザのAidomari拡張機能から記事を送信すると、ここに表示されます。",
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
                            onGenerateClick = { viewModel.generateCards(article) },
                            onDeleteClick = { viewModel.deleteArticle(article.id) },
                        )
                    }
                }
            }
        }
    }

    // カード候補レビューダイアログ
    if (uiState.reviewArticle != null) {
        CardReviewDialog(
            article = uiState.reviewArticle!!,
            candidates = uiState.candidateCards,
            subjects = uiState.subjects,
            selectedSubjectId = uiState.selectedSubjectId,
            newDeckName = uiState.newDeckName,
            onSubjectSelect = { viewModel.selectSubject(it) },
            onDeckNameChange = { viewModel.updateNewDeckName(it) },
            onToggleSelect = { viewModel.toggleCardSelection(it) },
            onSelectAll = { viewModel.selectAllCandidates(it) },
            onCandidateChange = { id, f, b, s -> viewModel.updateCandidate(id, f, b, s) },
            onSave = { viewModel.saveSelectedCards() },
            onDismiss = { viewModel.closeReview() },
        )
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
            onDismissRequest = { showGeminiSettings = false },
        )
    }
}

@Composable
fun ArticleItemCard(
    article: SourceArticle,
    isGenerating: Boolean,
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
            // メタ行: ドメイン、日時、ステータス
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

                StatusBadge(status = article.status, cardCount = article.generatedCardCount)
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

            // 本文抜粋
            if (article.extractedText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (expanded) article.extractedText else article.extractedText.take(120) + (if (article.extractedText.length > 120) "..." else ""),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )
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
                            Text("生成中...", fontSize = 12.sp)
                        } else {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (article.status == ArticleStatus.GENERATED) "再生成" else "AIカード生成",
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ArticleStatus, cardCount: Int, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        ArticleStatus.PENDING -> Triple("未処理", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        ArticleStatus.GENERATED -> Triple("${cardCount}枚生成済", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        ArticleStatus.DISCARDED -> Triple("破棄", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = fg)
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
    onDeckNameChange: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onCandidateChange: (String, String, String, String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedCount = candidates.count { it.isSelected }
    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("生成カードの精査・承認", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        .padding(16.dp),
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
                            }
                        }

                        // デッキ名
                        OutlinedTextField(
                            value = newDeckName,
                            onValueChange = onDeckNameChange,
                            label = { Text("デッキ名", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

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
                        onChange = { f, b, s -> onCandidateChange(candidate.id, f, b, s) },
                    )
                }
            }
        }
    }
}

@Composable
fun CandidateCardItem(
    candidate: CardCandidateItem,
    onToggle: () -> Unit,
    onChange: (front: String, back: String, supplement: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (candidate.isSelected) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            },
        ),
        border = if (candidate.isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = candidate.isSelected,
                onCheckedChange = { onToggle() },
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = candidate.front,
                    onValueChange = { onChange(it, candidate.back, candidate.supplement) },
                    label = { Text("表面（問題・用語）", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = candidate.back,
                    onValueChange = { onChange(candidate.front, it, candidate.supplement) },
                    label = { Text("裏面（解答・定義）", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = candidate.supplement,
                    onValueChange = { onChange(candidate.front, candidate.back, it) },
                    label = { Text("補足・出典メモ", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                )
            }
        }
    }
}
