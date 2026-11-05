package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AidomariDaoTest {

    private lateinit var db: AidomariDatabase
    private lateinit var dao: AidomariDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.aidomariDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetSubject() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語", ttsLanguage = "en-US")
        dao.insertSubject(subject)

        val retrieved = dao.getSubjectById("sub-1")
        assertNotNull(retrieved)
        assertEquals("英語", retrieved?.name)
        assertEquals("en-US", retrieved?.ttsLanguage)
    }

    @Test
    fun insertDeckAndCards_cascadeDelete() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A", splitSize = 50)
        dao.insertDeck(deck)

        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "book", back = "本")
        dao.insertCards(listOf(card1, card2))

        val cards = dao.getCardsByDeckSync("deck-1")
        assertEquals(2, cards.size)

        // Delete subject -> deck & cards should cascade delete
        dao.deleteSubjectById("sub-1")
        val remainingDecks = dao.getDecksBySubject("sub-1").first()
        val remainingCards = dao.getCardsByDeckSync("deck-1")

        assertTrue(remainingDecks.isEmpty())
        assertTrue(remainingCards.isEmpty())
    }

    @Test
    fun updateSubject_doesNotCascadeDeleteDecksAndCards() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A", splitSize = 50)
        dao.insertDeck(deck)

        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "book", back = "本")
        dao.insertCards(listOf(card1, card2))

        // Update subject (e.g. archiving subject) using insertSubject (@Upsert)
        dao.insertSubject(subject.copy(isArchived = true))

        val updatedSubject = dao.getSubjectById("sub-1")
        assertNotNull(updatedSubject)
        assertTrue(updatedSubject?.isArchived == true)

        // Verify decks and cards are NOT deleted
        val remainingDecks = dao.getDecksBySubject("sub-1").first()
        val remainingCards = dao.getCardsByDeckSync("deck-1")
        assertEquals(1, remainingDecks.size)
        assertEquals(2, remainingCards.size)
    }

    @Test
    fun updateDeck_doesNotCascadeDeleteCards() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A", splitSize = 50)
        dao.insertDeck(deck)

        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "book", back = "本")
        dao.insertCards(listOf(card1, card2))

        // Update deck name using insertDeck (@Upsert)
        dao.insertDeck(deck.copy(name = "単語帳A（改）"))

        val updatedDeck = dao.getDeckById("deck-1")
        assertNotNull(updatedDeck)
        assertEquals("単語帳A（改）", updatedDeck?.name)

        // Verify cards are NOT deleted
        val remainingCards = dao.getCardsByDeckSync("deck-1")
        assertEquals(2, remainingCards.size)
    }

    @Test
    fun getDecksBySubject_excludesBookmarkDecks() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val regularDeck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A")
        val bookmarkDeck = DeckEntity(id = "bookmark_sub-1", subjectId = "sub-1", name = "苦手単語（ブックマーク）")
        dao.insertDeck(regularDeck)
        dao.insertDeck(bookmarkDeck)

        val decks = dao.getDecksBySubject("sub-1").first()
        assertEquals(1, decks.size)
        assertEquals("deck-1", decks[0].id)
    }

    @Test
    fun insertAndGetDeck_preservesTtsSettings() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(
            id = "deck-tts",
            subjectId = "sub-1",
            name = "TTS Deck",
            autoSpeak = true,
            speechRate = 1.25f,
        )
        dao.insertDeck(deck)

        val decks = dao.getDecksBySubject("sub-1").first()
        assertEquals(1, decks.size)
        assertTrue(decks[0].autoSpeak)
        assertEquals(1.25f, decks[0].speechRate, 0.001f)
    }

    @Test
    fun bookmarkToggleAndFilter() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A")
        dao.insertDeck(deck)

        val card = CardEntity(id = "card-1", deckId = "deck-1", front = "difficult", back = "難しい", isBookmarked = false)
        dao.insertCard(card)

        dao.setBookmark("card-1", true)
        val bookmarked = dao.getBookmarkedCardsBySubject("sub-1").first()
        assertEquals(1, bookmarked.size)
        assertEquals("difficult", bookmarked[0].front)
    }

    @Test
    fun studySession_saveAndRestore() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A")
        dao.insertDeck(deck)

        val session = StudySessionEntity(
            deckId = "deck-1",
            partIndex = 1,
            currentCardIndex = 15,
            queueCardIdsJson = "[\"c1\",\"c2\"]",
            retryCardIdsJson = "[\"c3\"]",
            isCompleted = false,
        )
        dao.insertOrUpdateSession(session)

        val restored = dao.getSession("deck-1")
        assertNotNull(restored)
        assertEquals(1, restored?.partIndex)
        assertEquals(15, restored?.currentCardIndex)
        assertEquals("[\"c1\",\"c2\"]", restored?.queueCardIdsJson)
    }

    @Test
    fun searchCardsInSubject_matchesFrontBackAndNotes() = runBlocking {
        val subject = SubjectEntity(id = "sub-search", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-search", subjectId = "sub-search", name = "単語帳S")
        dao.insertDeck(deck)

        val c1 = CardEntity(id = "c1", deckId = "deck-search", front = "apple", back = "りんご")
        val c2 = CardEntity(
            id = "c2",
            deckId = "deck-search",
            front = "banana",
            back = "バナナ",
            userSupplement = "tropical fruit",
        )
        val c3 = CardEntity(
            id = "c3",
            deckId = "deck-search",
            front = "cat",
            back = "ねこ",
            userQuestion = "kitten?",
        )
        dao.insertCards(listOf(c1, c2, c3))

        val frontMatches = dao.searchCardsInSubject("sub-search", "app")
        assertEquals(1, frontMatches.size)
        assertEquals("apple", frontMatches[0].front)

        val backMatches = dao.searchCardsInSubject("sub-search", "バナナ")
        assertEquals(1, backMatches.size)
        assertEquals("banana", backMatches[0].front)

        val supplementMatches = dao.searchCardsInSubject("sub-search", "tropical")
        assertEquals(1, supplementMatches.size)
        assertEquals("banana", supplementMatches[0].front)

        val questionMatches = dao.searchCardsInSubject("sub-search", "kitten")
        assertEquals(1, questionMatches.size)
        assertEquals("cat", questionMatches[0].front)

        val emptyMatches = dao.searchCardsInSubject("sub-search", "zebra")
        assertTrue(emptyMatches.isEmpty())
    }

    @Test
    fun insertAndGetStudySession_withCompletedPartIndices() = runBlocking {
        val subject = SubjectEntity(id = "sub-session", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-session", subjectId = "sub-session", name = "単語帳")
        dao.insertDeck(deck)

        val session = StudySessionEntity(
            deckId = "deck-session",
            partIndex = 1,
            isCompleted = false,
            completedPartIndicesJson = "[0,1]",
        )
        dao.insertOrUpdateSession(session)

        val retrieved = dao.getSession("deck-session")
        assertNotNull(retrieved)
        assertEquals("[0,1]", retrieved?.completedPartIndicesJson)
        assertEquals(1, retrieved?.partIndex)
        assertEquals(false, retrieved?.isCompleted)
    }

    @Test
    fun getRandomCard_excludesCardsFromArchivedSubjects() = runBlocking {
        // Active subject with card
        val activeSubject = SubjectEntity(id = "sub-active", name = "アクティブ教科", isArchived = false)
        val activeDeck = DeckEntity(id = "deck-active", subjectId = "sub-active", name = "アクティブデッキ")
        val activeCard = CardEntity(id = "card-active", deckId = "deck-active", front = "active", back = "有効")
        dao.insertSubject(activeSubject)
        dao.insertDeck(activeDeck)
        dao.insertCard(activeCard)

        // Archived subject with card
        val archivedSubject = SubjectEntity(id = "sub-archived", name = "アーカイブ教科", isArchived = true)
        val archivedDeck = DeckEntity(id = "deck-archived", subjectId = "sub-archived", name = "アーカイブデッキ")
        val archivedCard = CardEntity(id = "card-archived", deckId = "deck-archived", front = "archived", back = "保存")
        dao.insertSubject(archivedSubject)
        dao.insertDeck(archivedDeck)
        dao.insertCard(archivedCard)

        // Verify getRandomCard only returns the card from active subject
        val result = dao.getRandomCard()
        assertNotNull(result)
        assertEquals("card-active", result?.id)

        // Archive activeSubject as well
        dao.insertSubject(activeSubject.copy(isArchived = true))
        val emptyResult = dao.getRandomCard()
        assertTrue(emptyResult == null)
    }

    @Test
    fun getRandomBookmarkedCard_excludesCardsFromArchivedSubjects() = runBlocking {
        val activeSubject = SubjectEntity(id = "sub-bm-active", name = "アクティブ教科", isArchived = false)
        val activeDeck = DeckEntity(id = "deck-bm-active", subjectId = "sub-bm-active", name = "デッキ")
        val activeBmCard = CardEntity(id = "card-bm-active", deckId = "deck-bm-active", front = "active", back = "有効", isBookmarked = true)
        dao.insertSubject(activeSubject)
        dao.insertDeck(activeDeck)
        dao.insertCard(activeBmCard)

        val archivedSubject = SubjectEntity(id = "sub-bm-archived", name = "アーカイブ教科", isArchived = true)
        val archivedDeck = DeckEntity(id = "deck-bm-archived", subjectId = "sub-bm-archived", name = "デッキ")
        val archivedBmCard = CardEntity(id = "card-bm-archived", deckId = "deck-bm-archived", front = "archived", back = "保存", isBookmarked = true)
        dao.insertSubject(archivedSubject)
        dao.insertDeck(archivedDeck)
        dao.insertCard(archivedBmCard)

        val result = dao.getRandomBookmarkedCard()
        assertNotNull(result)
        assertEquals("card-bm-active", result?.id)
    }
}
