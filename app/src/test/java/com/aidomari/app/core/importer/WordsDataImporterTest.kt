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

class WordsDataImporterTest {

    private lateinit var fakeRepository: FakeWordsRepository
    private lateinit var importer: WordsDataImporter

    @Before
    fun setup() {
        fakeRepository = FakeWordsRepository()
        importer = WordsDataImporter(fakeRepository)
    }

    @Test
    fun importCsvToJson_success() = runBlocking {
        val json = """
            [
              {
                "title": "英単語ブック1",
                "content": [
                  { "front": "apple", "back": "りんご" },
                  { "front": "banana", "back": "バナナ" }
                ]
              }
            ]
        """.trimIndent()

        val result = importer.importJson(json)
        assertTrue(result.isSuccess)

        val summary = result.getOrThrow()
        assertEquals(1, summary.subjectsCount)
        assertEquals(1, summary.decksCount)
        assertEquals(2, summary.cardsCount)

        assertEquals("英単語ブック1", fakeRepository.decks.first().name)
        assertEquals(2, fakeRepository.cards.size)
        assertEquals("apple", fakeRepository.cards[0].front)
        assertEquals("りんご", fakeRepository.cards[0].back)
    }

    @Test
    fun importElmModel_success() = runBlocking {
        val json = """
            {
              "modelState": "Menu",
              "menuModel": {
                "books": [
                  {
                    "bookHead": { "title": "ターゲット1900", "part": 1 },
                    "preference": {
                      "reverseMode": false,
                      "shuffleMode": true,
                      "autoContinue": true,
                      "numOfPreference": 50
                    },
                    "content": [
                      {
                        "word": "concern",
                        "meaning": "関係・関心",
                        "user_question": "メモ問題",
                        "user_supplement": "補足メモ",
                        "bookmark_id": 1
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val result = importer.importJson(json)
        assertTrue(result.isSuccess)

        val summary = result.getOrThrow()
        assertEquals(1, summary.decksCount)
        assertEquals(1, summary.cardsCount)

        val deck = fakeRepository.decks.first()
        assertEquals("ターゲット1900", deck.name)
        assertTrue(deck.shuffleMode)
        assertTrue(deck.autoContinue)

        val card = fakeRepository.cards.first()
        assertEquals("concern", card.front)
        assertEquals("関係・関心", card.back)
        assertEquals("メモ問題", card.userQuestion)
        assertEquals("補足メモ", card.userSupplement)
        assertTrue(card.isBookmarked) // bookmark_id > 0
    }

    @Test
    fun importElmModel_multipleBooks_mergedIntoSingleDeck() = runBlocking {
        val json = """
            {
              "modelState": "Menu",
              "menuModel": {
                "books": [
                  {
                    "bookHead": { "title": "ターゲット1900", "part": 1 },
                    "preference": {
                      "numOfPreference": 10,
                      "shuffleMode": true,
                      "autoContinue": true
                    },
                    "content": [
                      { "word": "concern", "meaning": "関係・関心" },
                      { "word": "deliver", "meaning": "配達する" }
                    ]
                  },
                  {
                    "bookHead": { "title": "ターゲット1900", "part": 2 },
                    "content": [
                      { "word": "establish", "meaning": "設立する" },
                      { "word": "feature", "meaning": "特徴" }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val result = importer.importJson(json)
        assertTrue(result.isSuccess)

        val summary = result.getOrThrow()
        // 2 books must be merged into 1 single deck
        assertEquals(1, summary.decksCount)
        assertEquals(4, summary.cardsCount)

        assertEquals(1, fakeRepository.decks.size)
        val deck = fakeRepository.decks.first()
        assertEquals("ターゲット1900", deck.name)
        assertEquals(10, deck.splitSize)
        assertTrue(deck.shuffleMode)
        assertTrue(deck.autoContinue)

        // All 4 cards must belong to the same deck with contiguous orderIndex
        assertEquals(4, fakeRepository.cards.size)
        assertTrue(fakeRepository.cards.all { it.deckId == deck.id })
        assertEquals(
            listOf("concern", "deliver", "establish", "feature"),
            fakeRepository.cards.map { it.front },
        )
        assertEquals(
            listOf("関係・関心", "配達する", "設立する", "特徴"),
            fakeRepository.cards.map { it.back },
        )
        assertEquals(listOf(0, 1, 2, 3), fakeRepository.cards.map { it.orderIndex })
    }

    @Test
    fun importCsvToJson_multipleChunks_mergedIntoSingleDeck() = runBlocking {
        val json = """
            [
              {
                "title": "基本英単語 (1)",
                "content": [
                  { "front": "apple", "back": "りんご" },
                  { "front": "banana", "back": "バナナ" }
                ]
              },
              {
                "title": "基本英単語 (2)",
                "content": [
                  { "front": "cherry", "back": "さくらんぼ" },
                  { "front": "date", "back": "デーツ" }
                ]
              }
            ]
        """.trimIndent()

        val result = importer.importJson(json)
        assertTrue(result.isSuccess)

        val summary = result.getOrThrow()
        assertEquals(1, summary.decksCount)
        assertEquals(4, summary.cardsCount)

        assertEquals(1, fakeRepository.decks.size)
        val deck = fakeRepository.decks.first()
        assertEquals("基本英単語", deck.name)
        assertEquals(10, deck.splitSize)

        assertEquals(4, fakeRepository.cards.size)
        assertTrue(fakeRepository.cards.all { it.deckId == deck.id })
        assertEquals(
            listOf("apple", "banana", "cherry", "date"),
            fakeRepository.cards.map { it.front },
        )
        assertEquals(listOf(0, 1, 2, 3), fakeRepository.cards.map { it.orderIndex })
    }

    @Test
    fun importModernFormat_success() = runBlocking {
        val json = """
            {
              "subjects": [
                {
                  "name": "世界史",
                  "ttsLanguage": "ja-JP",
                  "decks": [
                    {
                      "name": "古代オリエント",
                      "splitSize": 40,
                      "cards": [
                        { "front": "ハンムラビ法典", "back": "目には目を" }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val result = importer.importJson(json)
        assertTrue(result.isSuccess)

        val summary = result.getOrThrow()
        assertEquals(1, summary.subjectsCount)
        assertEquals(1, summary.decksCount)
        assertEquals(1, summary.cardsCount)

        assertEquals("世界史", fakeRepository.subjects.first().name)
        assertEquals("ja-JP", fakeRepository.subjects.first().ttsLanguage)
        assertEquals("古代オリエント", fakeRepository.decks.first().name)
        assertEquals("ハンムラビ法典", fakeRepository.cards.first().front)
    }

    // In-memory fake repository for testing
    private class FakeWordsRepository : WordsRepository {
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

        override suspend fun saveSession(session: StudySession) = Unit

        override suspend fun deleteSession(deckId: String) = Unit
    }
}
