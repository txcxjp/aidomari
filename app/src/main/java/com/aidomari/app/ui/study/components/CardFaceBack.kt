package com.aidomari.app.ui.study.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.core.keyword.KeywordEngine
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews
import com.aidomari.app.domain.model.Card as DomainCard

@Composable
fun CardFaceBack(
    card: DomainCard,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    candidateCards: List<DomainCard> = emptyList(),
    onKeywordClick: (DomainCard) -> Unit = {},
    isReverse: Boolean = false,
    onSpeak: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onSnooze: (() -> Unit)? = null,
) {
    val scrollState = rememberScrollState()
    val keywordEngine = remember { KeywordEngine() }
    val relatedCards = remember(card, candidateCards) {
        keywordEngine.findRelatedCards(card, candidateCards)
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (card.isBookmarked) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Bookmarked",
                tint = Color(0xFFFFB300),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
            )
        }

        // Speaker icon at top end to replay back face audio
        if (onSpeak != null) {
            IconButton(
                onClick = onSpeak,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speak",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Center scrollable content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 40.dp)
                .verticalScroll(scrollState, enabled = scrollState.maxValue > 0),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isReverse) {
                // In reverse mode, answer is the target word (card.front)
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Also display meaning and keyword links
                KeywordText(
                    text = card.back,
                    candidateCards = candidateCards,
                    currentCardId = card.id,
                    onKeywordClick = onKeywordClick,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign = TextAlign.Center,
                )
            } else {
                KeywordText(
                    text = card.back,
                    candidateCards = candidateCards,
                    currentCardId = card.id,
                    onKeywordClick = onKeywordClick,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            if (card.userSupplement.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (onEditClick != null) Modifier.clickable { onEditClick() } else Modifier),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "補足メモ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.userSupplement,
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
        }

        // Bottom grading action buttons (Left: Retry, Center: Snooze, Right: Done)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFC62828),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "もう一度")
            }

            if (onSnooze != null) {
                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "休眠")
                }
            }

            OutlinedButton(
                onClick = onDone,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2E7D32),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "覚えた")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun CardFaceBackPreview() {
    AidomariTheme {
        CardFaceBack(
            card = PreviewSampleData.sampleCard,
            onDone = {},
            onRetry = {},
            candidateCards = PreviewSampleData.sampleCards,
            onKeywordClick = {},
            isReverse = false,
            onSpeak = {},
            onEditClick = {},
        )
    }
}
