package com.aidomari.app.ui.study.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aidomari.app.core.keyword.KeywordEngine
import com.aidomari.app.core.keyword.TextSegment
import com.aidomari.app.domain.model.Card

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordText(
    text: String,
    candidateCards: List<Card>,
    onKeywordClick: (Card) -> Unit,
    modifier: Modifier = Modifier,
    currentCardId: String? = null,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign = TextAlign.Center,
) {
    val keywordEngine = remember { KeywordEngine() }
    val segments = remember(text, candidateCards, currentCardId) {
        keywordEngine.parseKeywords(text, candidateCards, currentCardId)
    }
    val relatedCards = remember(text, candidateCards, currentCardId) {
        keywordEngine.extractRelatedCards(text, candidateCards, currentCardId)
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    val annotatedString = remember(segments, primaryColor) {
        buildAnnotatedString {
            segments.forEach { segment ->
                when (segment) {
                    is TextSegment.Plain -> append(segment.text)
                    is TextSegment.Keyword -> {
                        pushLink(
                            LinkAnnotation.Clickable(
                                tag = segment.matchedCard.id,
                                linkInteractionListener = { onKeywordClick(segment.matchedCard) },
                            ),
                        )
                        pushStyle(
                            SpanStyle(
                                color = primaryColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        append(segment.text)
                        pop() // pop style
                        pop() // pop link
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = annotatedString,
            style = style,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )

        if (relatedCards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "関連キーワード",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 6.dp),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                relatedCards.forEach { card ->
                    AssistChip(
                        onClick = { onKeywordClick(card) },
                        label = { Text(card.front) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        }
    }
}
