package com.aidomari.app.core.database

import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

object SampleData {
    const val SAMPLE_SUBJECT_NAME = DefaultSampleData.SAMPLE_SUBJECT_NAME
    const val SAMPLE_DECK_NAME = DefaultSampleData.SAMPLE_DECK_NAME

    val sampleSubjects = DefaultSampleData.sampleSubjects
    val sampleDeck = DefaultSampleData.sampleDeck
    val sampleCards = DefaultSampleData.sampleCards

    fun seedDatabase(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val subjectId = UUID.randomUUID().toString()
        val deckId = UUID.randomUUID().toString()

        db.execSQL(
            """
            INSERT INTO subjects (id, name, tts_language, sort_order, created_at, is_archived, color_theme)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf(subjectId, SAMPLE_SUBJECT_NAME, "en-US", 0, now, 0, "INDIGO"),
        )

        db.execSQL(
            """
            INSERT INTO decks (id, subject_id, name, split_size, auto_continue, shuffle_mode, reverse_mode, auto_speak, speech_rate, sort_order, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf(deckId, subjectId, SAMPLE_DECK_NAME, 50, 0, 0, 0, 0, 1.0f, 0, now),
        )

        sampleCards.forEachIndexed { index, card ->
            val cardId = UUID.randomUUID().toString()
            val isBookmarked = if (card.isBookmarked) 1 else 0
            db.execSQL(
                """
                INSERT INTO cards (id, deck_id, front, back, user_question, user_supplement, is_bookmarked, order_index, created_at, snoozed_until, snooze_level)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(
                    cardId,
                    deckId,
                    card.term,
                    card.description,
                    card.userQuestion,
                    card.userSupplement,
                    isBookmarked,
                    index,
                    now + index,
                    null,
                    0,
                ),
            )
        }
    }
}
