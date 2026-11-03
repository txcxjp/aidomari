package com.aidomari.app.ui.study.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.aidomari.app.core.keyword.KeywordEngine
import com.aidomari.app.core.keyword.TextSegment
import com.aidomari.app.domain.model.Card

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

    Text(
        text = annotatedString,
        style = style,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth(),
    )
}
