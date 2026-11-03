package com.aidomari.app.ui.study.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.core.keyword.KeywordEngine
import com.aidomari.app.domain.model.Card

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordBottomSheet(
    initialCard: Card?,
    allSubjectCards: List<Card>,
    onDismissRequest: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    onToggleBookmark: ((Card) -> Unit)? = null,
    onSaveNotes: ((cardId: String, userQuestion: String, userSupplement: String) -> Unit)? = null,
    onDeleteCard: ((cardId: String) -> Unit)? = null,
) {
    if (initialCard == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardStack = remember(initialCard) {
        mutableStateListOf(initialCard)
    }
    var editingCard by remember { mutableStateOf<Card?>(null) }
    var cardToDelete by remember { mutableStateOf<Card?>(null) }

    // Intercept hardware/system back button when deeper in recursive keyword stack
    BackHandler(enabled = cardStack.size > 1) {
        cardStack.removeAt(cardStack.lastIndex)
    }

    if (cardStack.isEmpty()) {
        LaunchedEffect(Unit) {
            onDismissRequest()
        }
        return
    }

    val currentCard = cardStack.last()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        KeywordSheetContent(
            currentCard = currentCard,
            cardStackSize = cardStack.size,
            allSubjectCards = allSubjectCards,
            onBackClick = { cardStack.removeAt(cardStack.lastIndex) },
            onCloseClick = onDismissRequest,
            onKeywordClick = { nextCard -> cardStack.add(nextCard) },
            onSpeak = onSpeak,
            onToggleBookmark = onToggleBookmark?.let { callback ->
                { card ->
                    val updated = card.copy(isBookmarked = !card.isBookmarked)
                    val idx = cardStack.indexOfFirst { it.id == card.id }
                    if (idx >= 0) {
                        cardStack[idx] = updated
                    }
                    callback(card)
                }
            },
            onEditNotesClick = if (onSaveNotes != null) {
                { editingCard = currentCard }
            } else {
                null
            },
            onDeleteClick = if (onDeleteCard != null) {
                { cardToDelete = currentCard }
            } else {
                null
            },
        )

        if (editingCard != null) {
            val targetCard = editingCard!!
            CardEditNotesDialog(
                card = targetCard,
                onDismissRequest = { editingCard = null },
                onSave = { question, supplement ->
                    val updated = targetCard.copy(
                        userQuestion = question.trim(),
                        userSupplement = supplement.trim(),
                    )
                    val idx = cardStack.indexOfFirst { it.id == targetCard.id }
                    if (idx >= 0) {
                        cardStack[idx] = updated
                    }
                    editingCard = null
                    onSaveNotes?.invoke(targetCard.id, updated.userQuestion, updated.userSupplement)
                },
            )
        }

        if (cardToDelete != null) {
            val target = cardToDelete!!
            AlertDialog(
                onDismissRequest = { cardToDelete = null },
                title = { Text(text = "カードを削除") },
                text = { Text(text = "「${target.front}」を削除しますか？\nこの操作は取り消せません。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idToDelete = target.id
                            cardToDelete = null
                            val isLast = cardStack.size <= 1
                            if (!isLast) {
                                cardStack.remove(target)
                            }
                            onDeleteCard?.invoke(idToDelete)
                            if (isLast) {
                                onDismissRequest()
                            }
                        },
                    ) {
                        Text(text = "削除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { cardToDelete = null }) {
                        Text(text = "キャンセル")
                    }
                },
            )
        }
    }
}

@Composable
fun KeywordSheetContent(
    currentCard: Card,
    cardStackSize: Int,
    allSubjectCards: List<Card>,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onKeywordClick: (Card) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    onToggleBookmark: ((Card) -> Unit)? = null,
    onEditNotesClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val scrollState = rememberScrollState()
    val keywordEngine = remember { KeywordEngine() }
    val relatedCards = remember(currentCard, allSubjectCards) {
        keywordEngine.findRelatedCards(currentCard, allSubjectCards)
    }

    // Reset scroll to top when current card in stack changes
    LaunchedEffect(currentCard.id) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
    ) {
        // Header: Back navigation & Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cardStackSize > 1) {
                TextButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(text = "戻る (${cardStackSize - 1})")
                }
            } else {
                Text(
                    text = "関連キーワード詳細",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.semantics { contentDescription = "Close" },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Front word + speaker icon + bookmark + edit notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = currentCard.front,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleBookmark != null) {
                    IconButton(
                        onClick = { onToggleBookmark(currentCard) },
                        modifier = Modifier.semantics { contentDescription = "Toggle Bookmark" },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = if (currentCard.isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (currentCard.isBookmarked) {
                                Color(0xFFFFB300)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            },
                        )
                    }
                }

                IconButton(onClick = { onSpeak(currentCard.front) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                if (onEditNotesClick != null || onDeleteClick != null) {
                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.semantics { contentDescription = "More Options" },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                        ) {
                            if (onEditNotesClick != null) {
                                DropdownMenuItem(
                                    text = { Text("メモ・疑問点を編集") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onEditNotesClick()
                                    },
                                )
                            }
                            if (onDeleteClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "カードを削除",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onDeleteClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meaning / Back text with hyperlinked recursive keywords
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            KeywordText(
                text = currentCard.back,
                candidateCards = allSubjectCards,
                currentCardId = currentCard.id,
                onKeywordClick = onKeywordClick,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                textAlign = TextAlign.Start,
            )
        }

        // User question if present
        if (currentCard.userQuestion.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onEditNotesClick != null) Modifier.clickable { onEditNotesClick() } else Modifier),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "疑問点・質問",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentCard.userQuestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // User supplement if present
        if (currentCard.userSupplement.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onEditNotesClick != null) Modifier.clickable { onEditNotesClick() } else Modifier),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "補足メモ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentCard.userSupplement,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (relatedCards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            RelatedItemsSection(
                relatedCards = relatedCards,
                onCardClick = onKeywordClick,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
