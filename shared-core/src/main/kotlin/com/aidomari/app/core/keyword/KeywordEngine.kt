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
            .filter { it.term.isNotBlank() && it.id != currentCardId }
            .sortedByDescending { it.term.length }

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
                val candidateTerm = candidate.term
                if (text.regionMatches(
                        thisOffset = index,
                        other = candidateTerm,
                        otherOffset = 0,
                        length = candidateTerm.length,
                        ignoreCase = true,
                    )
                ) {
                    matched = MatchRange(
                        start = index,
                        end = index + candidateTerm.length,
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

    /**
     * Finds related cards that share a word stem or reference [card] in their meaning/notes,
     * excluding direct inline keywords already present in [card]'s definition.
     */
    fun findRelatedCards(
        card: Card,
        allSubjectCards: List<Card>,
    ): List<Card> {
        val rawTarget = card.term.substringBefore("<").trim()
        val targetLower = rawTarget.lowercase()
        if (targetLower.length < 3) return emptyList()

        // Cards already directly referenced in this card's description
        val directKeywords = extractRelatedCards(card.description, allSubjectCards, card.id).map { it.id }.toSet()

        return allSubjectCards
            .filter { candidate ->
                if (candidate.id == card.id || directKeywords.contains(candidate.id)) {
                    false
                } else {
                    val candTerm = candidate.term.substringBefore("<").trim().lowercase()
                    // 1. Stem / Substring sharing in term
                    val termMatches = (candTerm.length >= 3) &&
                        (candTerm.contains(targetLower) || targetLower.contains(candTerm))

                    // 2. Candidate's description, question or supplement mentions target word
                    val descMatches = candidate.description.contains(targetLower, ignoreCase = true) ||
                        candidate.userQuestion.contains(targetLower, ignoreCase = true) ||
                        candidate.userSupplement.contains(targetLower, ignoreCase = true)

                    termMatches || descMatches
                }
            }
            .distinctBy { it.id }
            .sortedBy { it.term.lowercase() }
    }
}
