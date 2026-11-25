package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DeckTest {

    @Test
    fun calculateParts_emptyDeck_returnsSingleEmptyPart() {
        val deck = Deck(id = "1", subjectId = "sub1", name = "Test", splitSize = 50)
        val parts = deck.calculateParts(totalCards = 0)

        assertEquals(1, parts.size)
        assertEquals(0, parts[0].count)
        assertEquals("0枚", parts[0].displayRange)
        assertEquals("Part 1 (0枚)", parts[0].displayName)
    }

    @Test
    fun calculateParts_cardsUnderSplitSize_returnsSinglePart() {
        val deck = Deck(id = "1", subjectId = "sub1", name = "Test", splitSize = 50)
        val parts = deck.calculateParts(totalCards = 30)

        assertEquals(1, parts.size)
        assertEquals(0, parts[0].partIndex)
        assertEquals(1, parts[0].partNumber)
        assertEquals(0, parts[0].startIndex)
        assertEquals(30, parts[0].endIndex)
        assertEquals(30, parts[0].count)
        assertEquals("1〜30", parts[0].displayRange)
    }

    @Test
    fun calculateParts_cardsOverSplitSize_splitsCorrectly() {
        val deck = Deck(id = "1", subjectId = "sub1", name = "Test", splitSize = 50)
        val parts = deck.calculateParts(totalCards = 120)

        assertEquals(3, parts.size)

        // Part 1: 0..50
        assertEquals(0, parts[0].partIndex)
        assertEquals(1, parts[0].partNumber)
        assertEquals(0, parts[0].startIndex)
        assertEquals(50, parts[0].endIndex)
        assertEquals(50, parts[0].count)
        assertEquals("1〜50", parts[0].displayRange)

        // Part 2: 50..100
        assertEquals(1, parts[1].partIndex)
        assertEquals(2, parts[1].partNumber)
        assertEquals(50, parts[1].startIndex)
        assertEquals(100, parts[1].endIndex)
        assertEquals(50, parts[1].count)
        assertEquals("51〜100", parts[1].displayRange)

        // Part 3: 100..120
        assertEquals(2, parts[2].partIndex)
        assertEquals(3, parts[2].partNumber)
        assertEquals(100, parts[2].startIndex)
        assertEquals(120, parts[2].endIndex)
        assertEquals(20, parts[2].count)
        assertEquals("101〜120", parts[2].displayRange)
    }
}
