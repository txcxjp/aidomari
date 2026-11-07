package com.aidomari.app.ui.card

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.Card as DomainCard
import com.aidomari.app.domain.util.CardSnoozeCalculator
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.study.components.KeywordBottomSheet
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews

@Composable
fun CardListScreen(
    viewModel: CardListViewModel,
    onBack: () -> Unit,
    onSpeakText: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<DomainCard?>(null) }
    var selectedKeywordCard by remember { mutableStateOf<DomainCard?>(null) }

    CardListContent(
        uiState = uiState,
        isSearchActive = isSearchActive,
        onBack = onBack,
        onSearchActiveChanged = { isSearchActive = it },
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onAddCardClick = { showAddDialog = true },
        onCardClick = { selectedKeywordCard = it },
        onToggleBookmark = { viewModel.toggleBookmark(it) },
        onEditCardClick = { cardToEdit = it },
        onSpeakText = onSpeakText,
        onUnsnoozeCard = { viewModel.unsnoozeCard(it) },
        modifier = modifier,
    )

    // Add Card Dialog
    if (showAddDialog) {
        CardEditDialog(
            card = null,
            onDismissRequest = { showAddDialog = false },
            onSave = { front, back, question, supplement ->
                viewModel.saveCard(null, front, back, question, supplement)
                showAddDialog = false
            },
        )
    }

    // Edit Card Dialog
    if (cardToEdit != null) {
        val targetCard = cardToEdit!!
        CardEditDialog(
            card = targetCard,
            onDismissRequest = { cardToEdit = null },
            onSave = { front, back, question, supplement ->
                viewModel.saveCard(targetCard.id, front, back, question, supplement)
                cardToEdit = null
            },
        )
    }

    // Keyword Bottom Sheet for hyperlinked reading
    if (selectedKeywordCard != null) {
        KeywordBottomSheet(
            initialCard = selectedKeywordCard!!,
            allSubjectCards = uiState.allSubjectCards,
            onDismissRequest = { selectedKeywordCard = null },
            onSpeak = onSpeakText,
            onToggleBookmark = { viewModel.toggleBookmark(it) },
            onSaveNotes = { cardId, question, supplement ->
                viewModel.updateCardNotes(cardId, question, supplement)
            },
            onDeleteCard = { cardId ->
                viewModel.deleteCard(cardId)
            },
            onSnoozeCard = { viewModel.snoozeCard(it) },
            onUnsnoozeCard = { viewModel.unsnoozeCard(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListContent(
    uiState: CardListUiState,
    isSearchActive: Boolean,
    onBack: () -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddCardClick: () -> Unit,
    onCardClick: (DomainCard) -> Unit,
    onToggleBookmark: (DomainCard) -> Unit,
    onEditCardClick: (DomainCard) -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier,
    onUnsnoozeCard: (DomainCard) -> Unit = {},
) {
    val deckName = uiState.deck?.name ?: "単語帳"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("単語・意味・メモを検索...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("CardSearchTextField"),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onSearchActiveChanged(false)
                            onSearchQueryChanged("")
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "検索を終了",
                            )
                        }
                    },
                    actions = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "クリア",
                                )
                            }
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "カード管理",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "$deckName (${uiState.cards.size}枚)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("CardListBackButton"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "戻る",
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { onSearchActiveChanged(true) },
                            modifier = Modifier.testTag("SearchCardsButton"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "検索",
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("AddCardFab"),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新規カード追加",
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.filteredCards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) {
                            "「${uiState.searchQuery}」に一致する単語はありません"
                        } else {
                            "カードがまだ登録されていません"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "右下の「＋」ボタンから新しい単語を追加できます",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.filteredCards, key = { it.id }) { card ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("CardItem_${card.id}")
                                .clickable { onCardClick(card) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = card.front,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Bookmark icon button
                                        IconButton(
                                            onClick = { onToggleBookmark(card) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("BookmarkButton_${card.id}"),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = if (card.isBookmarked) "ブックマーク解除" else "ブックマーク",
                                                tint = if (card.isBookmarked) {
                                                    Color(0xFFFFB300)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                },
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }

                                        // TTS speak button
                                        IconButton(
                                            onClick = { onSpeakText(card.front) },
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "発音",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }

                                        // Edit card button
                                        IconButton(
                                            onClick = { onEditCardClick(card) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("EditCardButton_${card.id}"),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "編集",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.back,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (card.isSnoozed || card.userQuestion.isNotBlank() || card.userSupplement.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        if (card.isSnoozed) {
                                            val untilLabel = CardSnoozeCalculator.formatSnoozeUntil(card.snoozedUntil ?: 0L)
                                            SuggestionChip(
                                                onClick = { onUnsnoozeCard(card) },
                                                label = {
                                                    Text(
                                                        text = "スヌーズ中 ($untilLabel)",
                                                        maxLines = 1,
                                                        fontSize = 11.sp,
                                                    )
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Snooze,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp),
                                                    )
                                                },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                                ),
                                            )
                                        }
                                        if (card.userQuestion.isNotBlank()) {
                                            SuggestionChip(
                                                onClick = { onEditCardClick(card) },
                                                label = {
                                                    Text(
                                                        text = "Q: ${card.userQuestion}",
                                                        maxLines = 1,
                                                        fontSize = 11.sp,
                                                    )
                                                },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                ),
                                            )
                                        }
                                        if (card.userSupplement.isNotBlank()) {
                                            SuggestionChip(
                                                onClick = { onEditCardClick(card) },
                                                label = {
                                                    Text(
                                                        text = "メモ: ${card.userSupplement}",
                                                        maxLines = 1,
                                                        fontSize = 11.sp,
                                                    )
                                                },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                ),
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
}

@ThemePreviews
@Composable
private fun CardListContentPreview() {
    AidomariTheme {
        CardListContent(
            uiState = PreviewSampleData.sampleCardListUiState,
            isSearchActive = false,
            onBack = {},
            onSearchActiveChanged = {},
            onSearchQueryChanged = {},
            onAddCardClick = {},
            onCardClick = {},
            onToggleBookmark = {},
            onEditCardClick = {},
            onSpeakText = {},
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun CardListContentEmptyPreview() {
    AidomariTheme {
        CardListContent(
            uiState = PreviewSampleData.sampleCardListUiState.copy(cards = emptyList(), filteredCards = emptyList()),
            isSearchActive = false,
            onBack = {},
            onSearchActiveChanged = {},
            onSearchQueryChanged = {},
            onAddCardClick = {},
            onCardClick = {},
            onToggleBookmark = {},
            onEditCardClick = {},
            onSpeakText = {},
        )
    }
}
