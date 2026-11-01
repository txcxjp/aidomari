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
}
