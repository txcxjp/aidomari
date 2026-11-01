package com.aidomari.app.ui.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aidomari.app.core.input.VolumeKeyHandler
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.CardFaceBack
import com.aidomari.app.ui.study.components.CardFaceFront
import com.aidomari.app.ui.study.components.FlippableCard
import com.aidomari.app.ui.study.components.KeywordBottomSheet
import com.aidomari.app.ui.study.components.StudyCompletionDialog
import com.aidomari.app.ui.study.components.StudyProgressBar
import com.aidomari.app.ui.study.components.StudyTopBar

@OptIn(ExperimentalMaterial3Api::class)
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
    var bottomSheetCard by remember { mutableStateOf<Card?>(null) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val partTitle = "Part ${uiState.partIndex + 1}"
            StudyTopBar(
                title = uiState.deck?.name ?: "学習",
                subtitle = partTitle,
                isBookmarked = uiState.currentCard?.isBookmarked ?: false,
                onBack = onNavigateBack,
                onToggleBookmark = { viewModel.toggleBookmark() },
                onRestart = { viewModel.restartPart() },
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
                    StudyCompletionDialog(
                        partName = "Part ${uiState.partIndex + 1}",
                        totalCards = uiState.totalInPart,
                        hasNextPart = uiState.hasNextPart,
                        onNextPart = { onNavigateToPart(uiState.partIndex + 1) },
                        onRestartPart = { viewModel.restartPart() },
                        onBackToDecks = onNavigateBack,
                    )
                }

                uiState.currentCard != null -> {
                    val card = uiState.currentCard!!
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
                            FlippableCard(
                                isFlipped = uiState.isFlipped,
                                onFlip = { viewModel.flipCard() },
                                onDone = { viewModel.markDone() },
                                onRetry = { viewModel.markRetry() },
                                modifier = Modifier.fillMaxSize(),
                                frontContent = {
                                    CardFaceFront(
                                        card = card,
                                        onSpeak = { onSpeakText(card.front) },
                                    )
                                },
                                backContent = {
                                    CardFaceBack(
                                        card = card,
                                        candidateCards = uiState.subjectCards,
                                        onKeywordClick = { selected ->
                                            bottomSheetCard = selected
                                        },
                                        onDone = { viewModel.markDone() },
                                        onRetry = { viewModel.markRetry() },
                                    )
                                },
                            )
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

            if (bottomSheetCard != null) {
                KeywordBottomSheet(
                    initialCard = bottomSheetCard,
                    allSubjectCards = uiState.subjectCards,
                    onDismissRequest = { bottomSheetCard = null },
                    onSpeak = onSpeakText,
                )
            }
        }
    }
}
