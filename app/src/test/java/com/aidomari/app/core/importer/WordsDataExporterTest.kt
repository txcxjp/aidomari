package com.aidomari.app.core.importer

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordsDataExporterTest {

    private lateinit var fakeRepository: FakeExportRepository
    private lateinit var exporter: WordsDataExporter
    private lateinit var importer: WordsDataImporter

    @Before
    fun setup() {
        fakeRepository = FakeExportRepository()
        exporter = WordsDataExporter(fakeRepository)
        importer = WordsDataImporter(fakeRepository)
    }

    @Test
    fun exportAll_generatesValidJsonAndCanBeReimported() = runBlocking {
        // Setup initial data
        val sub = Subject(id = "s1", name = "英語", ttsLanguage = "en-US")
        val deck = Deck(id = "d1", subjectId = "s1", name = "英単語", splitSize = 50)
        val card = Card(id = "c1", deckId = "d1", front = "apple", back = "りんご", isBookmarked = true)

        fakeRepository.subjects.add(sub)
        fakeRepository.decks.add(deck)
        fakeRepository.cards.add(card)

        // Export
        val jsonOutput = exporter.exportAll()
        assertTrue(jsonOutput.contains("英語"))
        assertTrue(jsonOutput.contains("英単語"))
        assertTrue(jsonOutput.contains("apple"))
        assertTrue(jsonOutput.contains("りんご"))

        // Clear and re-import (round-trip)
        fakeRepository.subjects.clear()
        fakeRepository.decks.clear()
        fakeRepository.cards.clear()

        val importResult = importer.importJson(jsonOutput)
        assertTrue(importResult.isSuccess)

        val summary = importResult.getOrThrow()
        assertEquals(1, summary.subjectsCount)
        assertEquals(1, summary.decksCount)
        assertEquals(1, summary.cardsCount)

        assertEquals("英語", fakeRepository.subjects.first().name)
        assertEquals("英単語", fakeRepository.decks.first().name)
        assertEquals("apple", fakeRepository.cards.first().front)
    }

    private class FakeExportRepository : WordsRepository {
        val subjects = mutableListOf<Subject>()
        val decks = mutableListOf<Deck>()
        val cards = mutableListOf<Card>()

        override fun getAllSubjects(): Flow<List<Subject>> = flowOf(subjects)
        override suspend fun getSubjectById(id: String): Subject? = subjects.find { it.id == id }
        override suspend fun saveSubject(subject: Subject) {
            subjects.add(subject)
        }
        override suspend fun deleteSubject(id: String) {
            subjects.removeIf { it.id == id }
        }

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> =
            flowOf(decks.filter { it.subjectId == subjectId })
        override suspend fun getDeckById(deckId: String): Deck? = decks.find { it.id == deckId }
        override suspend fun saveDeck(deck: Deck) {
            decks.add(deck)
        }
        override suspend fun deleteDeck(deckId: String) {
            decks.removeIf { it.id == deckId }
        }

        override fun getCardsByDeck(deckId: String): Flow<List<Card>> =
            flowOf(cards.filter { it.deckId == deckId })
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> =
            cards.filter { it.deckId == deckId }
        override suspend fun getCardById(cardId: String): Card? = cards.find { it.id == cardId }
        override suspend fun saveCard(card: Card) {
            cards.add(card)
        }
        override suspend fun saveCards(newCards: List<Card>) {
            cards.addAll(newCards)
        }
        override suspend fun deleteCard(cardId: String) {
            cards.removeIf { it.id == cardId }
        }
        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
            val idx = cards.indexOfFirst { it.id == cardId }
            if (idx != -1) cards[idx] = cards[idx].copy(isBookmarked = isBookmarked)
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> =
            flowOf(cards.filter { it.isBookmarked })
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cards

        override suspend fun getSession(deckId: String): StudySession? = null
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = flowOf(null)
        override suspend fun saveSession(session: StudySession) {
        }
        override suspend fun deleteSession(deckId: String) {
        }
    }
}
