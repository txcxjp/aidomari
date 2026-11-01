package com.aidomari.app.ui.deck

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.Deck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    subjectName: String,
    viewModel: DeckViewModel,
    onBack: () -> Unit,
    onPartClick: (deckId: String, partIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val decksWithParts by viewModel.decksWithParts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Deck")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (decksWithParts.isEmpty()) {
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
                                            if (deck.shuffleMode) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                ) {
                                                    Text(
                                                        text = "シャッフル",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    IconButton(onClick = { deckToDelete = deck }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Deck",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
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
        }
    }

    // Add Deck Dialog
    if (showAddDialog) {
        var deckName by remember { mutableStateOf("") }
        var splitSizeText by remember { mutableStateOf("50") }
        var autoContinue by remember { mutableStateOf(false) }
        var shuffleMode by remember { mutableStateOf(false) }

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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deckName.isNotBlank()) {
                            val splitSize = splitSizeText.toIntOrNull() ?: 50
                            viewModel.createDeck(deckName, splitSize, autoContinue, shuffleMode)
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
}
