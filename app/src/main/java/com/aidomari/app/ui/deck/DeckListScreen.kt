package com.aidomari.app.ui.deck

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.ui.deck.components.AddDeckDialog
import com.aidomari.app.ui.deck.components.BookmarkDeckCard
import com.aidomari.app.ui.deck.components.DeckItemCard
import com.aidomari.app.ui.deck.components.DeckSearchContent
import com.aidomari.app.ui.deck.components.DeleteDeckConfirmDialog
import com.aidomari.app.ui.deck.components.EditDeckDialog
import com.aidomari.app.ui.deck.components.PastDayDecksCard
import com.aidomari.app.ui.deck.components.RandomQuestionsCard
import com.aidomari.app.ui.deck.components.RandomQuestionsDialog
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.study.components.KeywordBottomSheet
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews
import kotlinx.coroutines.launch

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
    val pastDayDecks by viewModel.pastDayDecks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val allSubjectCards by viewModel.allSubjectCards.collectAsState()
    val learnedSubjectCards by viewModel.learnedCardsInSubject.collectAsState()
    val randomQuestionCount by viewModel.randomQuestionCount.collectAsState()
    val isSubjectArchived by viewModel.isSubjectArchived.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isSearchActive by remember { mutableStateOf(false) }
    var selectedSearchCard by remember { mutableStateOf<Card?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRandomQuestionsDialog by remember { mutableStateOf(false) }
    var deckForRandomStudy by remember { mutableStateOf<DeckWithPartsUiModel?>(null) }
    var deckToEdit by remember { mutableStateOf<Deck?>(null) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        viewModel.clearSearch()
    }

    DeckListContent(
        subjectName = subjectName,
        decksWithParts = decksWithParts,
        bookmarkModel = bookmarkModel,
        pastDayDecks = pastDayDecks,
        randomQuestionCount = randomQuestionCount,
        totalCards = allSubjectCards.size,
        isSubjectArchived = isSubjectArchived,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        searchResults = searchResults,
        isSearching = isSearching,
        onBack = onBack,
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onSearchActiveChanged = { isSearchActive = it },
        onClearSearch = { viewModel.clearSearch() },
        onSearchCardClick = { selectedSearchCard = it },
        onToggleBookmark = { viewModel.toggleBookmark(it) },
        onPartClick = { deckId, partIndex ->
            if (isSubjectArchived) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("アーカイブされた教科は演習できません。演習を行うには元に戻してください。")
                }
            } else {
                onPartClick(deckId, partIndex)
            }
        },
        onToggleShuffle = { viewModel.toggleShuffle(it.id) },
        onToggleAutoSpeak = { viewModel.toggleAutoSpeak(it.id) },
        onEditDeck = { deckToEdit = it },
        onDeleteDeck = { deckToDelete = it },
        onManageCardsClick = onManageCardsClick,
        onAddDeckClick = { showAddDialog = true },
        onRandomQuestionsClick = { showRandomQuestionsDialog = true },
        onDeckRandomStudyClick = { deckForRandomStudy = it },
        onUnarchiveSubject = {
            viewModel.unarchiveSubject {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("アーカイブを解除しました")
                }
            }
        },
        onSpeakText = onSpeakText,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )

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
        val activeSubjectCards = remember(allSubjectCards) { allSubjectCards.filterNot { it.isSnoozed } }
        val activeLearnedSubjectCards = remember(learnedSubjectCards) { learnedSubjectCards.filterNot { it.isSnoozed } }
        RandomQuestionsDialog(
            totalCards = activeSubjectCards.size,
            learnedCardsCount = activeLearnedSubjectCards.size,
            initialCount = randomQuestionCount,
            onDismissRequest = { showRandomQuestionsDialog = false },
            onStartStudy = { count, onlyLearned ->
                viewModel.setRandomQuestionCount(count)
                showRandomQuestionsDialog = false
                val prefix = if (onlyLearned) "random_learned_" else "random_"
                val randomDeckId = "${prefix}${count}_${viewModel.subjectId}"
                onPartClick(randomDeckId, 0)
            },
        )
    }

    if (deckForRandomStudy != null) {
        val targetDeckUi = deckForRandomStudy!!
        RandomQuestionsDialog(
            totalCards = targetDeckUi.activeCardCount,
            learnedCardsCount = targetDeckUi.activeStudiedCardCount,
            initialCount = randomQuestionCount,
            title = "「${targetDeckUi.deck.name}」のランダム出題",
            description = "この単語帳のカード（${targetDeckUi.activeCardCount}枚）からランダムに出題します。出題する問題数を選択してください。",
            onDismissRequest = { deckForRandomStudy = null },
            onStartStudy = { count, onlyLearned ->
                viewModel.setRandomQuestionCount(count)
                val targetDeckId = targetDeckUi.deck.id
                deckForRandomStudy = null
                val prefix = if (onlyLearned) "random_deck_learned_" else "random_deck_"
                val randomDeckId = "${prefix}${count}_$targetDeckId"
                onPartClick(randomDeckId, 0)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListContent(
    subjectName: String,
    decksWithParts: List<DeckWithPartsUiModel>,
    bookmarkModel: BookmarkDeckUiModel?,
    pastDayDecks: List<PastDayDeckUiModel> = emptyList(),
    randomQuestionCount: Int,
    totalCards: Int,
    isSubjectArchived: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    searchResults: List<Card>,
    isSearching: Boolean,
    onBack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    onClearSearch: () -> Unit,
    onSearchCardClick: (Card) -> Unit,
    onToggleBookmark: (Card) -> Unit,
    onPartClick: (deckId: String, partIndex: Int) -> Unit,
    onToggleShuffle: (Deck) -> Unit,
    onToggleAutoSpeak: (Deck) -> Unit,
    onEditDeck: (Deck) -> Unit,
    onDeleteDeck: (Deck) -> Unit,
    onManageCardsClick: (deckId: String) -> Unit,
    onAddDeckClick: () -> Unit,
    onRandomQuestionsClick: () -> Unit,
    onUnarchiveSubject: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onDeckRandomStudyClick: ((DeckWithPartsUiModel) -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
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
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onSearchActiveChanged(false)
                            onClearSearch()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "検索を終了",
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
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
                        IconButton(onClick = { onSearchActiveChanged(true) }) {
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
            if (!isSearchActive && !isSubjectArchived) {
                FloatingActionButton(onClick = onAddDeckClick) {
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
                    onCardClick = onSearchCardClick,
                    onToggleBookmark = onToggleBookmark,
                    onSpeakText = onSpeakText,
                )
            } else if (decksWithParts.isEmpty() && (bookmarkModel == null || bookmarkModel.totalCount == 0) && pastDayDecks.isEmpty() && totalCards == 0) {
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (isSubjectArchived) {
                        item(key = "archived_subject_banner") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ArchivedSubjectBanner"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "アーカイブされた教科",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "この教科はアーカイブされています。演習は行えませんが、単語カードの閲覧・管理が可能です。",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = onUnarchiveSubject) {
                                        Text("元に戻す")
                                    }
                                }
                            }
                        }
                    }

                    if (!isSubjectArchived && bookmarkModel != null && bookmarkModel.totalCount > 0) {
                        item(key = "bookmark_study_section") {
                            BookmarkDeckCard(
                                model = bookmarkModel,
                                onPartClick = onPartClick,
                            )
                        }
                    }

                    if (!isSubjectArchived && pastDayDecks.isNotEmpty()) {
                        item(key = "past_day_decks_section") {
                            PastDayDecksCard(
                                decks = pastDayDecks,
                                onPartClick = onPartClick,
                            )
                        }
                    }

                    if (!isSubjectArchived && totalCards > 0) {
                        item(key = "random_study_section") {
                            RandomQuestionsCard(
                                totalCards = totalCards,
                                randomQuestionCount = randomQuestionCount,
                                onClick = onRandomQuestionsClick,
                            )
                        }
                    }

                    items(decksWithParts, key = { it.deck.id }) { item ->
                        DeckItemCard(
                            item = item,
                            onPartClick = onPartClick,
                            onToggleShuffle = { onToggleShuffle(item.deck) },
                            onToggleAutoSpeak = { onToggleAutoSpeak(item.deck) },
                            onManageCardsClick = { onManageCardsClick(item.deck.id) },
                            onEditDeck = { onEditDeck(item.deck) },
                            onDeleteDeck = { onDeleteDeck(item.deck) },
                            onRandomStudyClick = { onDeckRandomStudyClick?.invoke(item) },
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DeckListContentPreview() {
    AidomariTheme {
        DeckListContent(
            subjectName = "英語（サンプル）",
            decksWithParts = listOf(PreviewSampleData.sampleDeckWithParts),
            bookmarkModel = PreviewSampleData.sampleBookmarkDeck,
            randomQuestionCount = 10,
            totalCards = 5,
            isSubjectArchived = false,
            isSearchActive = false,
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            onBack = {},
            onSearchQueryChanged = {},
            onSearchActiveChanged = {},
            onClearSearch = {},
            onSearchCardClick = {},
            onToggleBookmark = {},
            onPartClick = { _, _ -> },
            onToggleShuffle = {},
            onToggleAutoSpeak = {},
            onEditDeck = {},
            onDeleteDeck = {},
            onManageCardsClick = {},
            onAddDeckClick = {},
            onRandomQuestionsClick = {},
            onUnarchiveSubject = {},
            onSpeakText = {},
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun DeckListContentEmptyPreview() {
    AidomariTheme {
        DeckListContent(
            subjectName = "新規教科",
            decksWithParts = emptyList(),
            bookmarkModel = null,
            randomQuestionCount = 10,
            totalCards = 0,
            isSubjectArchived = false,
            isSearchActive = false,
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            onBack = {},
            onSearchQueryChanged = {},
            onSearchActiveChanged = {},
            onClearSearch = {},
            onSearchCardClick = {},
            onToggleBookmark = {},
            onPartClick = { _, _ -> },
            onToggleShuffle = {},
            onToggleAutoSpeak = {},
            onEditDeck = {},
            onDeleteDeck = {},
            onManageCardsClick = {},
            onAddDeckClick = {},
            onRandomQuestionsClick = {},
            onUnarchiveSubject = {},
            onSpeakText = {},
        )
    }
}
