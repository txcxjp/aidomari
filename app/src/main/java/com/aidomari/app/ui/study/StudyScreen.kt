package com.aidomari.app.ui.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aidomari.app.core.input.VolumeKeyHandler
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.util.CardSnoozeCalculator
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.study.components.CardEditNotesDialog
import com.aidomari.app.ui.study.components.CardFaceBack
import com.aidomari.app.ui.study.components.CardFaceFront
import com.aidomari.app.ui.study.components.FlippableCard
import com.aidomari.app.ui.study.components.KeywordBottomSheet
import com.aidomari.app.ui.study.components.StudyCompletionDialog
import com.aidomari.app.ui.study.components.StudyProgressBar
import com.aidomari.app.ui.study.components.StudySearchDialog
import com.aidomari.app.ui.study.components.StudyTopBar
import com.aidomari.app.ui.study.components.StudyTtsSettingsDialog
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews
import kotlinx.coroutines.launch

@Composable
fun StudyScreen(
    deckId: String,
    partIndex: Int,
    viewModel: StudyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPart: (nextPartIndex: Int) -> Unit,
    onSpeakText: (text: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var bottomSheetCard by remember { mutableStateOf<Card?>(null) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showTtsSettingsDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val autoSpeak = uiState.deck?.autoSpeak == true
    val isReverse = uiState.deck?.reverseMode == true
    val currentCard = uiState.currentCard
    val currentCardId = currentCard?.id
    val nextSnoozeLabel = currentCard?.let {
        CardSnoozeCalculator.calculateNextSnooze(it.snoozeLevel).durationLabel
    }

    // Auto-speak on card display (オモテ面: 設問)
    LaunchedEffect(currentCardId, uiState.cardSequenceIndex, autoSpeak) {
        if (autoSpeak && currentCard != null) {
            val frontText = if (isReverse) currentCard.back else currentCard.front
            onSpeakText(frontText)
        }
    }

    // Auto-speak on card flip (ウラ面: 解答)
    LaunchedEffect(uiState.isFlipped, autoSpeak) {
        if (autoSpeak && currentCard != null && uiState.isFlipped) {
            val backText = if (isReverse) currentCard.front else currentCard.back
            onSpeakText(backText)
        }
    }

    // Register volume key handler while this screen is active
    DisposableEffect(viewModel) {
        VolumeKeyHandler.listener = { isUp ->
            viewModel.onVolumeKey(isUp)
        }
        onDispose {
            VolumeKeyHandler.listener = null
        }
    }

    // Load deck and part when screen opens or parameters change
    LaunchedEffect(deckId, partIndex) {
        viewModel.loadDeckAndPart(deckId, partIndex)
    }

    val isAutoContinue = uiState.deck?.autoContinue == true
    val isPartCompleted = uiState.isPartCompleted
    val hasNextPart = uiState.hasNextPart
    val nextPartIndex = uiState.partIndex + 1

    // Automatically advance to next part if autoContinue is enabled and there is a next part
    LaunchedEffect(uiState.isLoading, isPartCompleted, isAutoContinue, hasNextPart, nextPartIndex) {
        if (!uiState.isLoading && isPartCompleted && isAutoContinue && hasNextPart) {
            onNavigateToPart(nextPartIndex)
        }
    }

    StudyContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNavigateToPart = onNavigateToPart,
        onToggleBookmark = { viewModel.toggleBookmark() },
        onRestartPart = { viewModel.restartPart() },
        onContinueRandomStudy = { viewModel.continueRandomStudy() },
        onFlipCard = { viewModel.flipCard() },
        onMarkDone = { viewModel.markDone() },
        onMarkRetry = { viewModel.markRetry() },
        onSpeakText = onSpeakText,
        onOpenTtsSettings = { showTtsSettingsDialog = true },
        onSearchClick = { showSearchDialog = true },
        onDeleteCard = { showDeleteConfirmDialog = true },
        onEditNotes = { showEditNotesDialog = true },
        onKeywordClick = { bottomSheetCard = it },
        onSnoozeCard = if (uiState.currentCard != null) {
            {
                val front = uiState.currentCard?.front ?: ""
                viewModel.snoozeCurrentCard { label ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("「$front」を${label}非表示にしました")
                    }
                }
            }
        } else {
            null
        },
        snoozeDurationLabel = nextSnoozeLabel,
        modifier = modifier,
    )

    if (showEditNotesDialog && uiState.currentCard != null) {
        val editingCard = uiState.currentCard!!
        CardEditNotesDialog(
            card = editingCard,
            onDismissRequest = { showEditNotesDialog = false },
            onSave = { question, supplement ->
                viewModel.updateCardNotes(editingCard.id, question, supplement)
                showEditNotesDialog = false
            },
        )
    }

    if (showTtsSettingsDialog && uiState.deck != null) {
        val currentDeck = uiState.deck!!
        StudyTtsSettingsDialog(
            autoSpeak = currentDeck.autoSpeak,
            speechRate = currentDeck.speechRate,
            onDismissRequest = { showTtsSettingsDialog = false },
            onSave = { newAutoSpeak, newSpeechRate ->
                viewModel.updateTtsSettings(newAutoSpeak, newSpeechRate)
                showTtsSettingsDialog = false
            },
            onTestSpeak = {
                onSpeakText(uiState.currentCard?.front ?: "相泊")
            },
        )
    }

    if (showSearchDialog) {
        StudySearchDialog(
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            onQueryChange = { viewModel.searchCards(it) },
            onCardClick = { card ->
                bottomSheetCard = card
            },
            onDismissRequest = {
                viewModel.clearSearch()
                showSearchDialog = false
            },
        )
    }

    if (bottomSheetCard != null) {
        KeywordBottomSheet(
            initialCard = bottomSheetCard,
            allSubjectCards = uiState.subjectCards,
            onDismissRequest = { bottomSheetCard = null },
            onSpeak = onSpeakText,
            onToggleBookmark = { viewModel.toggleCardBookmark(it) },
            onSaveNotes = { cardId, question, supplement ->
                viewModel.updateCardNotes(cardId, question, supplement)
            },
            onDeleteCard = { cardId ->
                viewModel.deleteCard(cardId)
            },
            onSnoozeCard = { card ->
                viewModel.snoozeCard(card) { label ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("「${card.front}」を${label}非表示にしました")
                    }
                }
            },
            onUnsnoozeCard = { card ->
                viewModel.unsnoozeCard(card) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("「${card.front}」のスヌーズを解除しました")
                    }
                }
            },
        )
    }

    if (showDeleteConfirmDialog && uiState.currentCard != null) {
        val cardToDelete = uiState.currentCard!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "カードを削除") },
            text = { Text(text = "「${cardToDelete.front}」を削除しますか？\nこの操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCurrentCard()
                    },
                ) {
                    Text(text = "削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }
}

@Composable
fun StudyContent(
    uiState: StudyUiState,
    onNavigateBack: () -> Unit,
    onNavigateToPart: (nextPartIndex: Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onRestartPart: () -> Unit,
    onContinueRandomStudy: () -> Unit = {},
    onFlipCard: () -> Unit,
    onMarkDone: () -> Unit,
    onMarkRetry: () -> Unit,
    onSpeakText: (text: String) -> Unit,
    onOpenTtsSettings: () -> Unit,
    onSearchClick: () -> Unit,
    onDeleteCard: () -> Unit,
    onEditNotes: () -> Unit,
    onKeywordClick: (Card) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSnoozeCard: (() -> Unit)? = null,
    snoozeDurationLabel: String? = null,
) {
    val autoSpeak = uiState.deck?.autoSpeak == true
    val isReverse = uiState.deck?.reverseMode == true
    val isAutoContinue = uiState.deck?.autoContinue == true
    val hasNextPart = uiState.hasNextPart

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val partTitle = "Part ${uiState.partIndex + 1}"
            StudyTopBar(
                title = uiState.deck?.name ?: "学習",
                subtitle = partTitle,
                isBookmarked = uiState.currentCard?.isBookmarked ?: false,
                onBack = onNavigateBack,
                onToggleBookmark = onToggleBookmark,
                onRestart = onRestartPart,
                autoSpeakEnabled = autoSpeak,
                onOpenTtsSettings = onOpenTtsSettings,
                onSearchClick = onSearchClick,
                onDeleteCard = if (uiState.currentCard != null) onDeleteCard else null,
                onEditNotes = if (uiState.currentCard != null) onEditNotes else null,
                onSnoozeCard = onSnoozeCard,
                snoozeDurationLabel = snoozeDurationLabel,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.isPartCompleted -> {
                    if (isAutoContinue && hasNextPart) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        StudyCompletionDialog(
                            partName = "Part ${uiState.partIndex + 1}",
                            totalCards = uiState.totalInPart,
                            hasNextPart = uiState.hasNextPart,
                            onNextPart = { onNavigateToPart(uiState.partIndex + 1) },
                            onRestartPart = onRestartPart,
                            onBackToDecks = onNavigateBack,
                            isRandomMode = uiState.isRandomMode,
                            onContinueRandomStudy = onContinueRandomStudy,
                        )
                    }
                }

                uiState.currentCard != null -> {
                    val card = uiState.currentCard
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        StudyProgressBar(
                            completedCount = uiState.completedCount,
                            totalInPart = uiState.totalInPart,
                            retryCount = uiState.retryCount,
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // 1. Background card stack preview
                            uiState.nextCard?.let { nextCard ->
                                androidx.compose.material3.Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = 0.94f
                                            scaleY = 0.94f
                                            alpha = 0.85f
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                                        defaultElevation = 2.dp,
                                    ),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                    ) {
                                        CardFaceFront(
                                            card = nextCard,
                                            onSpeak = {},
                                            isReverse = isReverse,
                                        )
                                    }
                                }
                            }

                            // 2. Foreground active card
                            key(card.id, uiState.cardSequenceIndex) {
                                FlippableCard(
                                    isFlipped = uiState.isFlipped,
                                    onFlip = onFlipCard,
                                    onDone = onMarkDone,
                                    onRetry = onMarkRetry,
                                    modifier = Modifier.fillMaxSize(),
                                    frontContent = {
                                        CardFaceFront(
                                            card = card,
                                            onSpeak = {
                                                val frontText = if (isReverse) card.back else card.front
                                                onSpeakText(frontText)
                                            },
                                            isReverse = isReverse,
                                            onEditClick = onEditNotes,
                                        )
                                    },
                                    backContent = {
                                        CardFaceBack(
                                            card = card,
                                            candidateCards = uiState.subjectCards,
                                            onKeywordClick = onKeywordClick,
                                            onDone = onMarkDone,
                                            onRetry = onMarkRetry,
                                            isReverse = isReverse,
                                            onSpeak = {
                                                val backText = if (isReverse) card.front else card.back
                                                onSpeakText(backText)
                                            },
                                            onEditClick = onEditNotes,
                                            onSnooze = onSnoozeCard,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        text = "カードが見つかりません",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun StudyContentFrontPreview() {
    AidomariTheme {
        StudyContent(
            uiState = PreviewSampleData.sampleStudyUiState.copy(isFlipped = false),
            onNavigateBack = {},
            onNavigateToPart = {},
            onToggleBookmark = {},
            onRestartPart = {},
            onFlipCard = {},
            onMarkDone = {},
            onMarkRetry = {},
            onSpeakText = {},
            onOpenTtsSettings = {},
            onSearchClick = {},
            onDeleteCard = {},
            onEditNotes = {},
            onKeywordClick = {},
        )
    }
}

@Preview(name = "Flipped Back Face", showBackground = true)
@Composable
private fun StudyContentBackPreview() {
    AidomariTheme {
        StudyContent(
            uiState = PreviewSampleData.sampleStudyUiState.copy(isFlipped = true),
            onNavigateBack = {},
            onNavigateToPart = {},
            onToggleBookmark = {},
            onRestartPart = {},
            onFlipCard = {},
            onMarkDone = {},
            onMarkRetry = {},
            onSpeakText = {},
            onOpenTtsSettings = {},
            onSearchClick = {},
            onDeleteCard = {},
            onEditNotes = {},
            onKeywordClick = {},
        )
    }
}
