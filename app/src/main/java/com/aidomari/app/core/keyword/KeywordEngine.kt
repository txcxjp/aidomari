package com.aidomari.app.core.keyword

import com.aidomari.app.domain.model.Card

sealed interface TextSegment {
    val text: String

    data class Plain(override val text: String) : TextSegment
    data class Keyword(override val text: String, val matchedCard: Card) : TextSegment
}

class KeywordEngine {

    /**
     * Finds keyword matches in [text] against [candidateCards] in the same subject.
     * Longer matches take precedence (greedy matching).
     */
    fun parseKeywords(
        text: String,
        candidateCards: List<Card>,
        currentCardId: String? = null,
    ): List<TextSegment> {
        if (text.isEmpty() || candidateCards.isEmpty()) {
            return if (text.isEmpty()) emptyList() else listOf(TextSegment.Plain(text))
        }

        // Filter and sort candidates by length descending
        val validCandidates = candidateCards
            .filter { it.front.isNotBlank() && it.id != currentCardId }
            .sortedByDescending { it.front.length }

        if (validCandidates.isEmpty()) {
            return listOf(TextSegment.Plain(text))
        }

        data class MatchRange(
            val start: Int,
            val end: Int,
            val card: Card,
        )

        // Find all non-overlapping matches
        val matches = mutableListOf<MatchRange>()
        var index = 0

        while (index < text.length) {
            var matched: MatchRange? = null

            for (candidate in validCandidates) {
                val candidateFront = candidate.front
                if (text.regionMatches(
                        thisOffset = index,
                        other = candidateFront,
                        otherOffset = 0,
                        length = candidateFront.length,
                        ignoreCase = true,
                    )
                ) {
                    matched = MatchRange(
                        start = index,
                        end = index + candidateFront.length,
                        card = candidate,
                    )
                    break // Since validCandidates is sorted by length descending, this is longest match
                }
            }

            if (matched != null) {
                matches.add(matched)
                index = matched.end
            } else {
                index++
            }
        }

        if (matches.isEmpty()) {
            return listOf(TextSegment.Plain(text))
        }

        // Build segments
        val segments = mutableListOf<TextSegment>()
        var cursor = 0

        for (m in matches) {
            if (m.start > cursor) {
                segments.add(TextSegment.Plain(text.substring(cursor, m.start)))
            }
            segments.add(TextSegment.Keyword(text.substring(m.start, m.end), m.card))
            cursor = m.end
        }

        if (cursor < text.length) {
            segments.add(TextSegment.Plain(text.substring(cursor)))
        }

        return segments
    }

    /**
     * Extracts unique cards referenced in [text] in appearance order.
     */
    fun extractRelatedCards(
        text: String,
        candidateCards: List<Card>,
        currentCardId: String? = null,
    ): List<Card> {
        val segments = parseKeywords(text, candidateCards, currentCardId)
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Card>()

        for (segment in segments) {
            if (segment is TextSegment.Keyword) {
                if (seen.add(segment.matchedCard.id)) {
                    result.add(segment.matchedCard)
                }
            }
        }

        return result
    }
}
