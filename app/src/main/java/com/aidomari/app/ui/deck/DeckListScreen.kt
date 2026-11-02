package com.aidomari.app.ui.deck

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.ui.deck.components.AddDeckDialog
import com.aidomari.app.ui.deck.components.BookmarkDeckCard
import com.aidomari.app.ui.deck.components.DeckItemCard
import com.aidomari.app.ui.deck.components.DeckSearchContent
import com.aidomari.app.ui.deck.components.DeleteDeckConfirmDialog
import com.aidomari.app.ui.deck.components.EditDeckDialog
import com.aidomari.app.ui.deck.components.RandomQuestionsCard
import com.aidomari.app.ui.deck.components.RandomQuestionsDialog
import com.aidomari.app.ui.study.components.KeywordBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    subjectName: String,
    viewModel: DeckViewModel,
    onBack: () -> Unit,
    onPartClick: (deckId: String, partIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    onSpeakText: (String) -> Unit = {},
    onManageCardsClick: (deckId: String) -> Unit = {},
) {
    val decksWithParts by viewModel.decksWithParts.collectAsState()
    val bookmarkModel by viewModel.bookmarkDeckUiModel.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val allSubjectCards by viewModel.allSubjectCards.collectAsState()
    val randomQuestionCount by viewModel.randomQuestionCount.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var selectedSearchCard by remember { mutableStateOf<Card?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRandomQuestionsDialog by remember { mutableStateOf(false) }
    var deckToEdit by remember { mutableStateOf<Deck?>(null) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        viewModel.clearSearch()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
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
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.clearSearch()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "検索を終了",
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
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
                    title = { Text(text = subjectName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "単語検索",
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!isSearchActive) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Deck")
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (isSearchActive) {
                DeckSearchContent(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onCardClick = { selectedSearchCard = it },
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onSpeakText = onSpeakText,
                )
            } else if (decksWithParts.isEmpty() && (bookmarkModel == null || bookmarkModel?.totalCount == 0) && allSubjectCards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "デッキが登録されていません",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "右下の ＋ ボタンから単語帳デッキを作成してください。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (bookmarkModel != null && bookmarkModel!!.totalCount > 0) {
                        item(key = "bookmark_study_section") {
                            BookmarkDeckCard(
                                model = bookmarkModel!!,
                                onPartClick = onPartClick,
                            )
                        }
                    }

                    if (allSubjectCards.isNotEmpty()) {
                        item(key = "random_study_section") {
                            RandomQuestionsCard(
                                totalCards = allSubjectCards.size,
                                randomQuestionCount = randomQuestionCount,
                                onClick = { showRandomQuestionsDialog = true },
                            )
                        }
                    }

                    items(decksWithParts, key = { it.deck.id }) { item ->
                        DeckItemCard(
                            item = item,
                            onPartClick = onPartClick,
                            onToggleShuffle = { viewModel.toggleShuffle(item.deck.id) },
                            onToggleAutoSpeak = { viewModel.toggleAutoSpeak(item.deck.id) },
                            onManageCardsClick = { onManageCardsClick(item.deck.id) },
                            onEditDeck = { deckToEdit = item.deck },
                            onDeleteDeck = { deckToDelete = item.deck },
                        )
                    }
                }
            }

            if (selectedSearchCard != null) {
                KeywordBottomSheet(
                    initialCard = selectedSearchCard,
                    allSubjectCards = allSubjectCards,
                    onDismissRequest = { selectedSearchCard = null },
                    onSpeak = onSpeakText,
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onSaveNotes = { cardId, question, supplement ->
                        viewModel.updateCardNotes(cardId, question, supplement)
                    },
                )
            }
        }
    }

    if (showAddDialog) {
        AddDeckDialog(
            onDismissRequest = { showAddDialog = false },
            onConfirm = { name, splitSize, autoContinue, shuffleMode, reverseMode, autoSpeak, speechRate ->
                viewModel.createDeck(
                    name = name,
                    splitSize = splitSize,
                    autoContinue = autoContinue,
                    shuffleMode = shuffleMode,
                    reverseMode = reverseMode,
                    autoSpeak = autoSpeak,
                    speechRate = speechRate,
                )
                showAddDialog = false
            },
        )
    }

    if (deckToEdit != null) {
        EditDeckDialog(
            deck = deckToEdit!!,
            onDismissRequest = { deckToEdit = null },
            onConfirm = { updated ->
                viewModel.updateDeck(updated)
                deckToEdit = null
            },
        )
    }

    if (deckToDelete != null) {
        DeleteDeckConfirmDialog(
            deck = deckToDelete!!,
            onDismissRequest = { deckToDelete = null },
            onConfirm = {
                viewModel.deleteDeck(deckToDelete!!.id)
                deckToDelete = null
            },
        )
    }

    if (showRandomQuestionsDialog && allSubjectCards.isNotEmpty()) {
        RandomQuestionsDialog(
            totalCards = allSubjectCards.size,
            initialCount = randomQuestionCount,
            onDismissRequest = { showRandomQuestionsDialog = false },
            onStartStudy = { count ->
                viewModel.setRandomQuestionCount(count)
                showRandomQuestionsDialog = false
                val randomDeckId = "random_${count}_${viewModel.subjectId}"
                onPartClick(randomDeckId, 0)
            },
        )
    }
}
