package com.aidomari.app.ui.deck

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.ui.deck.components.RandomQuestionsDialog
import com.aidomari.app.ui.study.components.KeywordBottomSheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                when {
                    searchQuery.isBlank() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "教科内の単語を検索",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "見出し語、意味、メモから検索できます",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    isSearching -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    searchResults.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "「$searchQuery」に一致する単語はありません",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                Text(
                                    text = "${searchResults.size}件の単語が見つかりました",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }

                            items(searchResults, key = { it.id }) { card ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSearchCard = card },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Text(
                                                    text = card.front,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                IconButton(
                                                    onClick = { viewModel.toggleBookmark(card) },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .testTag("SearchBookmarkButton_${card.id}"),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = if (card.isBookmarked) "Bookmarked" else "Bookmark",
                                                        tint = if (card.isBookmarked) {
                                                            Color(0xFFFFB300)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                        },
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = card.back,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (card.userQuestion.isNotBlank() || card.userSupplement.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "📝 メモあり",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }

                                        IconButton(onClick = { onSpeakText(card.front) }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "発音",
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                    // Bookmark study card banner
                    if (bookmarkModel != null && bookmarkModel!!.totalCount > 0) {
                        val model = bookmarkModel!!
                        item(key = "bookmark_study_section") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Bookmark Study",
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = "苦手単語（ブックマーク）",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                        ) {
                                            Text(
                                                text = "${model.totalCount} 枚",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        model.parts.forEach { partWithSession ->
                                            val part = partWithSession.part
                                            val session = partWithSession.session

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onPartClick(model.deck.id, part.partIndex) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                ),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = if (model.parts.size > 1) part.displayName else "集中学習をはじめる",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                        Text(
                                                            text = "${part.count} 枚",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        when {
                                                            session?.isCompleted == true -> {
                                                                Surface(
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.CheckCircle,
                                                                            contentDescription = null,
                                                                            tint = Color(0xFF2E7D32),
                                                                            modifier = Modifier.height(14.dp),
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text(
                                                                            text = "完了",
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = Color(0xFF2E7D32),
                                                                            fontWeight = FontWeight.Bold,
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            session != null && !session.isCompleted -> {
                                                                Surface(
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.PlayArrow,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.primary,
                                                                            modifier = Modifier.height(14.dp),
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text(
                                                                            text = "再開 (${session.currentCardIndex}/${part.count})",
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                            fontWeight = FontWeight.Bold,
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.outline,
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

                    // Random questions study card banner
                    if (allSubjectCards.isNotEmpty()) {
                        item(key = "random_study_section") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shuffle,
                                                contentDescription = "Random Study",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = "ランダム出題",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                        ) {
                                            Text(
                                                text = "${allSubjectCards.size} 枚",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showRandomQuestionsDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                        ),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column {
                                                Text(
                                                    text = "ランダム学習を開始",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Text(
                                                    text = if (randomQuestionCount <= 0) "全問出題" else "${randomQuestionCount}問出題（タップで変更）",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Start Random Study",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(decksWithParts, key = { it.deck.id }) { item ->
                        val deck = item.deck
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                // Deck Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = deck.name,
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
                                                color = MaterialTheme.colorScheme.surface,
                                            ) {
                                                Text(
                                                    text = "${item.totalCardCount} 単語",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                            if (deck.autoContinue) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                ) {
                                                    Text(
                                                        text = "自動継続",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (deck.shuffleMode) {
                                                    MaterialTheme.colorScheme.tertiaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                },
                                                modifier = Modifier.clickable {
                                                    viewModel.toggleShuffle(deck.id)
                                                },
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shuffle,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = if (deck.shuffleMode) {
                                                            MaterialTheme.colorScheme.onTertiaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        },
                                                    )
                                                    Text(
                                                        text = if (deck.shuffleMode) "シャッフル" else "通常順",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (deck.shuffleMode) {
                                                            MaterialTheme.colorScheme.onTertiaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                        },
                                                        fontWeight = if (deck.shuffleMode) FontWeight.Bold else FontWeight.Normal,
                                                    )
                                                }
                                            }
                                            if (deck.reverseMode) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                ) {
                                                    Text(
                                                        text = "裏面スタート",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                            if (deck.autoSpeak) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                                ) {
                                                    Text(
                                                        text = "自動音声 (${deck.speechRate}x)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.toggleAutoSpeak(deck.id) }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = if (deck.autoSpeak) "Auto-Speak ON" else "Auto-Speak OFF",
                                                tint = if (deck.autoSpeak) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                },
                                            )
                                        }
                                        IconButton(
                                            onClick = { onManageCardsClick(deck.id) },
                                            modifier = Modifier.testTag("ManageCardsButton_${deck.id}"),
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.List,
                                                contentDescription = "Manage Cards",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(onClick = { deckToEdit = deck }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Deck",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(onClick = { deckToDelete = deck }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Deck",
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // List of Parts
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    item.parts.forEach { partWithSession ->
                                        val part = partWithSession.part
                                        val session = partWithSession.session

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onPartClick(deck.id, part.partIndex) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                            ),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Column {
                                                    Text(
                                                        text = part.displayName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                    Text(
                                                        text = "${part.count} 枚",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    when {
                                                        session?.isCompleted == true -> {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.CheckCircle,
                                                                        contentDescription = null,
                                                                        tint = Color(0xFF2E7D32),
                                                                        modifier = Modifier.height(14.dp),
                                                                    )
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text(
                                                                        text = "完了",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = Color(0xFF2E7D32),
                                                                        fontWeight = FontWeight.Bold,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        session != null && !session.isCompleted -> {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.PlayArrow,
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.height(14.dp),
                                                                    )
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text(
                                                                        text = "再開 (${session.currentCardIndex}/${part.count})",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                        fontWeight = FontWeight.Bold,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.outline,
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

    // Add Deck Dialog
    if (showAddDialog) {
        var deckName by remember { mutableStateOf("") }
        var splitSizeText by remember { mutableStateOf("50") }
        var autoContinue by remember { mutableStateOf(false) }
        var shuffleMode by remember { mutableStateOf(false) }
        var reverseMode by remember { mutableStateOf(false) }
        var autoSpeak by remember { mutableStateOf(false) }
        var speechRate by remember { mutableFloatStateOf(1.0f) }
        val rateOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f)

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "新規デッキの作成") },
            text = {
                Column {
                    OutlinedTextField(
                        value = deckName,
                        onValueChange = { deckName = it },
                        label = { Text(text = "デッキ名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = splitSizeText,
                        onValueChange = { splitSizeText = it },
                        label = { Text(text = "分割枚数（デフォルト: 50枚）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "自動継続（終了時に次のPartへ）")
                        Switch(checked = autoContinue, onCheckedChange = { autoContinue = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "シャッフル出題")
                        Switch(checked = shuffleMode, onCheckedChange = { shuffleMode = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "裏面から出題（和英・意味先出し）")
                        Switch(checked = reverseMode, onCheckedChange = { reverseMode = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "自動読み上げ（カード表示時に発音）")
                        Switch(checked = autoSpeak, onCheckedChange = { autoSpeak = it })
                    }
                    if (autoSpeak) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "読み上げ速度: ${speechRate}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rateOptions.forEach { rate ->
                                val isSelected = (speechRate - rate).let { it >= -0.01f && it <= 0.01f }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { speechRate = rate },
                                    label = { Text(text = "${rate}x") },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deckName.isNotBlank()) {
                            val splitSize = splitSizeText.toIntOrNull() ?: 50
                            viewModel.createDeck(
                                name = deckName,
                                splitSize = splitSize,
                                autoContinue = autoContinue,
                                shuffleMode = shuffleMode,
                                reverseMode = reverseMode,
                                autoSpeak = autoSpeak,
                                speechRate = speechRate,
                            )
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

    // Edit Deck Dialog
    if (deckToEdit != null) {
        val target = deckToEdit!!
        var deckName by remember(target) { mutableStateOf(target.name) }
        var splitSizeText by remember(target) { mutableStateOf(target.splitSize.toString()) }
        var autoContinue by remember(target) { mutableStateOf(target.autoContinue) }
        var shuffleMode by remember(target) { mutableStateOf(target.shuffleMode) }
        var reverseMode by remember(target) { mutableStateOf(target.reverseMode) }
        var autoSpeak by remember(target) { mutableStateOf(target.autoSpeak) }
        var speechRate by remember(target) { mutableFloatStateOf(target.speechRate) }
        val rateOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f)

        AlertDialog(
            onDismissRequest = { deckToEdit = null },
            title = { Text(text = "デッキ設定の変更") },
            text = {
                Column {
                    OutlinedTextField(
                        value = deckName,
                        onValueChange = { deckName = it },
                        label = { Text(text = "デッキ名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = splitSizeText,
                        onValueChange = { splitSizeText = it },
                        label = { Text(text = "分割枚数") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "自動継続（終了時に次のPartへ）")
                        Switch(checked = autoContinue, onCheckedChange = { autoContinue = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "シャッフル出題")
                        Switch(checked = shuffleMode, onCheckedChange = { shuffleMode = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "裏面から出題（和英・意味先出し）")
                        Switch(checked = reverseMode, onCheckedChange = { reverseMode = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "自動読み上げ（カード表示時に発音）")
                        Switch(checked = autoSpeak, onCheckedChange = { autoSpeak = it })
                    }
                    if (autoSpeak) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "読み上げ速度: ${speechRate}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rateOptions.forEach { rate ->
                                val isSelected = (speechRate - rate).let { it >= -0.01f && it <= 0.01f }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { speechRate = rate },
                                    label = { Text(text = "${rate}x") },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deckName.isNotBlank()) {
                            val splitSize = splitSizeText.toIntOrNull()?.coerceAtLeast(1) ?: target.splitSize
                            val updated = target.copy(
                                name = deckName.trim(),
                                splitSize = splitSize,
                                autoContinue = autoContinue,
                                shuffleMode = shuffleMode,
                                reverseMode = reverseMode,
                                autoSpeak = autoSpeak,
                                speechRate = speechRate,
                            )
                            viewModel.updateDeck(updated)
                            deckToEdit = null
                        }
                    },
                ) {
                    Text(text = "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToEdit = null }) {
                    Text(text = "キャンセル")
                }
            },
        )
    }

    // Delete confirmation dialog
    if (deckToDelete != null) {
        val target = deckToDelete!!
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text(text = "デッキの削除") },
            text = {
                Text(text = "「${target.name}」および所属するすべての単語カードを削除しますか？この操作は取り消せません。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeck(target.id)
                        deckToDelete = null
                    },
                ) {
                    Text(text = "削除する")
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text(text = "キャンセル")
                }
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
