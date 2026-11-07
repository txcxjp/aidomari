package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SampleDataTest {

    private lateinit var context: Context
    private lateinit var db: AidomariDatabase
    private lateinit var dao: AidomariDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        SampleData.seedDatabase(db)
                    }
                },
            )
            .build()
        dao = db.aidomariDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun sampleData_seedsSubjectDeckAndCardsCorrectly() = runTest {
        // Query subjects from DAO
        val subjects = dao.getAllSubjects().first()
        assertEquals(1, subjects.size)

        val subject = subjects.first()
        assertEquals(SampleData.SAMPLE_SUBJECT_NAME, subject.name)
        assertEquals("en-US", subject.ttsLanguage)
        assertEquals("INDIGO", subject.colorTheme)
        assertFalse(subject.isArchived)

        // Query decks for subject
        val decks = dao.getDecksBySubject(subject.id).first()
        assertEquals(1, decks.size)

        val deck = decks.first()
        assertEquals(SampleData.SAMPLE_DECK_NAME, deck.name)
        assertEquals(50, deck.splitSize)
        assertEquals(1.0f, deck.speechRate)

        // Query cards for deck
        val cards = dao.getCardsByDeckSync(deck.id)
        assertEquals(5, cards.size)

        // Verify first card (Welcome card)
        val firstCard = cards.first()
        assertEquals("Hello!", firstCard.front)
        assertTrue(firstCard.back.contains("Aidomari（相泊）へようこそ"))
        assertTrue(firstCard.isBookmarked)

        // Verify active recall card with ruby
        val recallCard = cards.find { it.front == "active recall" }
        assertNotNull(recallCard)
        assertTrue(recallCard!!.back.contains("[能動的想起|のうどうてきそうき]"))

        // Verify spaced repetition card
        val spacedRepetitionCard = cards.find { it.front == "spaced repetition" }
        assertNotNull(spacedRepetitionCard)
        assertTrue(spacedRepetitionCard!!.back.contains("[分散学習|ぶんさんがくしゅう]"))
    }
}
