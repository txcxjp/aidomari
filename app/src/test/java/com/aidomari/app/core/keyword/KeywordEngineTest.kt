package com.aidomari.app.core.keyword

import com.aidomari.app.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeywordEngineTest {

    private lateinit var keywordEngine: KeywordEngine

    private val cardApple = Card(id = "c1", deckId = "d1", front = "apple", back = "りんご")
    private val cardPineapple = Card(id = "c2", deckId = "d1", front = "pineapple", back = "パイナップル")
    private val cardRome = Card(id = "c3", deckId = "d2", front = "ローマ", back = "イタリアの首都")
    private val cardRomanEmpire = Card(id = "c4", deckId = "d2", front = "ローマ帝国", back = "古代の大帝国")

    @Before
    fun setup() {
        keywordEngine = KeywordEngine()
    }

    @Test
    fun parseKeywords_greedyMatching_matchesLongerTermFirst() {
        val candidates = listOf(cardApple, cardPineapple)
        val text = "I like pineapple and apple."

        val segments = keywordEngine.parseKeywords(text, candidates)

        // Expected: "I like ", [pineapple], " and ", [apple], "."
        assertEquals(5, segments.size)
        assertTrue(segments[0] is TextSegment.Plain)
        assertEquals("I like ", segments[0].text)

        assertTrue(segments[1] is TextSegment.Keyword)
        assertEquals("pineapple", segments[1].text)
        assertEquals("c2", (segments[1] as TextSegment.Keyword).matchedCard.id)

        assertTrue(segments[2] is TextSegment.Plain)
        assertEquals(" and ", segments[2].text)

        assertTrue(segments[3] is TextSegment.Keyword)
        assertEquals("apple", segments[3].text)
        assertEquals("c1", (segments[3] as TextSegment.Keyword).matchedCard.id)

        assertTrue(segments[4] is TextSegment.Plain)
        assertEquals(".", segments[4].text)
    }

    @Test
    fun parseKeywords_japaneseLongestMatch() {
        val candidates = listOf(cardRome, cardRomanEmpire)
        val text = "古代のローマ帝国の首都はローマです。"

        val segments = keywordEngine.parseKeywords(text, candidates)

        // "古代の", [ローマ帝国], "の首都は", [ローマ], "です。"
        assertEquals(5, segments.size)
        assertEquals("ローマ帝国", segments[1].text)
        assertEquals("c4", (segments[1] as TextSegment.Keyword).matchedCard.id)

        assertEquals("ローマ", segments[3].text)
        assertEquals("c3", (segments[3] as TextSegment.Keyword).matchedCard.id)
    }

    @Test
    fun parseKeywords_excludesCurrentCardId() {
        val candidates = listOf(cardApple, cardPineapple)
        val text = "An apple is not a pineapple."

        // Exclude cardApple (id: c1)
        val segments = keywordEngine.parseKeywords(text, candidates, currentCardId = "c1")

        // apple is ignored, only pineapple is matched
        val keywordSegments = segments.filterIsInstance<TextSegment.Keyword>()
        assertEquals(1, keywordSegments.size)
        assertEquals("pineapple", keywordSegments[0].text)
    }

    @Test
    fun extractRelatedCards_returnsUniqueCardsInAppearanceOrder() {
        val candidates = listOf(cardApple, cardPineapple)
        val text = "apple pie with pineapple and another apple."

        val related = keywordEngine.extractRelatedCards(text, candidates)

        // apple appears first, then pineapple, duplicate apple ignored
        assertEquals(2, related.size)
        assertEquals("c1", related[0].id)
        assertEquals("c2", related[1].id)
    }

    @Test
    fun parseKeywords_emptyTextOrCandidates_returnsSafeResult() {
        val emptyResult = keywordEngine.parseKeywords("", listOf(cardApple))
        assertTrue(emptyResult.isEmpty())

        val plainResult = keywordEngine.parseKeywords("Hello world", emptyList())
        assertEquals(1, plainResult.size)
        assertTrue(plainResult[0] is TextSegment.Plain)
        assertEquals("Hello world", plainResult[0].text)
    }

    @Test
    fun findRelatedCards_findsCardsByStemAndContent_excludingSelfAndDirectKeywords() {
        val cardFruit = Card(id = "c0", deckId = "d1", front = "fruit", back = "Seed-bearing structure including apple")
        val cardApple = Card(id = "c1", deckId = "d1", front = "apple", back = "A red sweet fruit")
        val cardPie = Card(id = "c2", deckId = "d1", front = "apple pie", back = "A pastry with fruit filling")
        val cardOrchard = Card(id = "c3", deckId = "d1", front = "orchard", back = "A piece of land planted with fruit trees")
        val cardOrange = Card(id = "c4", deckId = "d1", front = "orange", back = "A citrus crop")

        val allCards = listOf(cardFruit, cardApple, cardPie, cardOrchard, cardOrange)

        val relatedToFruit = keywordEngine.findRelatedCards(cardFruit, allCards)
        val relatedIds = relatedToFruit.map { it.id }.toSet()

        assertTrue(relatedIds.contains("c2"))
        assertTrue(relatedIds.contains("c3"))
        assertTrue(!relatedIds.contains("c0"))
        assertTrue(!relatedIds.contains("c1"))
        assertTrue(!relatedIds.contains("c4"))

        val relatedToApple = keywordEngine.findRelatedCards(cardApple, allCards)
        val appleRelatedIds = relatedToApple.map { it.id }.toSet()
        assertTrue(appleRelatedIds.contains("c2"))
    }

    @Test
    fun findRelatedCards_shortTargetLength_returnsEmptyList() {
        val shortCard = Card(id = "s1", deckId = "d1", front = "in", back = "inside")
        val otherCard = Card(id = "s2", deckId = "d1", front = "inn", back = "hotel")
        val related = keywordEngine.findRelatedCards(shortCard, listOf(shortCard, otherCard))
        assertTrue(related.isEmpty())
    }
}
