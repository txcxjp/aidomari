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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.Card
import com.aidomari.app.ui.study.components.KeywordBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
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
    var cardToEdit by remember { mutableStateOf<Card?>(null) }
    var cardToDelete by remember { mutableStateOf<Card?>(null) }
    var selectedKeywordCard by remember { mutableStateOf<Card?>(null) }

    val deckName = uiState.deck?.name ?: "単語帳"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
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
                            isSearchActive = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "検索を終了",
                            )
                        }
                    },
                    actions = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
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
                            onClick = { isSearchActive = true },
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
                onClick = { showAddDialog = true },
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
                                .clickable { selectedKeywordCard = card },
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
                                            onClick = { viewModel.toggleBookmark(card) },
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
                                            onClick = { cardToEdit = card },
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

                                        // Delete card button
                                        IconButton(
                                            onClick = { cardToDelete = card },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("DeleteCardButton_${card.id}"),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "削除",
                                                tint = MaterialTheme.colorScheme.error,
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

                                if (card.userQuestion.isNotBlank() || card.userSupplement.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        if (card.userQuestion.isNotBlank()) {
                                            SuggestionChip(
                                                onClick = { cardToEdit = card },
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
                                                onClick = { cardToEdit = card },
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

    // Delete confirmation dialog
    if (cardToDelete != null) {
        val target = cardToDelete!!
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            modifier = Modifier.testTag("DeleteCardConfirmDialog"),
            title = { Text(text = "単語カードの削除") },
            text = { Text(text = "「${target.front}」を削除しますか？\nこの操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCard(target.id)
                        cardToDelete = null
                    },
                    modifier = Modifier.testTag("ConfirmDeleteButton"),
                ) {
                    Text(text = "削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { cardToDelete = null },
                    modifier = Modifier.testTag("CancelDeleteButton"),
                ) {
                    Text(text = "キャンセル")
                }
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
        )
    }
}
