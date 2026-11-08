package com.aidomari.app.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.CardStudyLogEntity
import com.aidomari.app.core.database.entity.CardWithLatestStudiedAt
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.DeckWithCards
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity
import com.aidomari.app.core.database.entity.SubjectWithDecks
import com.aidomari.app.core.database.entity.SugorokuProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AidomariDao {

    // --- Subject Operations ---
    @Query("SELECT * FROM subjects ORDER BY sort_order ASC, created_at ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    fun getSubjectWithDecks(subjectId: String): Flow<SubjectWithDecks?>

    @Upsert
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Upsert
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

    // --- Deck Operations ---
    @Query("SELECT * FROM decks WHERE subject_id = :subjectId AND id NOT LIKE 'bookmark_%' AND id NOT LIKE 'random_%' AND id NOT LIKE 'past_%' ORDER BY sort_order ASC, created_at ASC")
    fun getDecksBySubject(subjectId: String): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: String): DeckEntity?

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckWithCards(deckId: String): Flow<DeckWithCards?>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckWithCardsSync(deckId: String): DeckWithCards?

    @Upsert
    suspend fun insertDeck(deck: DeckEntity): Long

    @Upsert
    suspend fun insertDecks(decks: List<DeckEntity>)

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    // --- Card Operations ---
    @Query("SELECT * FROM cards WHERE deck_id = :deckId ORDER BY order_index ASC, created_at ASC")
    fun getCardsByDeck(deckId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deck_id = :deckId ORDER BY order_index ASC, created_at ASC")
    suspend fun getCardsByDeckSync(deckId: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardById(id: String): CardEntity?

    @Upsert
    suspend fun insertCard(card: CardEntity): Long

    @Upsert
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("UPDATE cards SET is_bookmarked = :isBookmarked WHERE id = :cardId")
    suspend fun setBookmark(cardId: String, isBookmarked: Boolean)

    @Query("UPDATE cards SET snoozed_until = :snoozedUntil, snooze_level = :snoozeLevel WHERE id = :cardId")
    suspend fun setCardSnooze(cardId: String, snoozedUntil: Long?, snoozeLevel: Int)

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        WHERE decks.subject_id = :subjectId AND cards.is_bookmarked = 1
        ORDER BY cards.created_at DESC
        """,
    )
    fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<CardEntity>>

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        WHERE decks.subject_id = :subjectId AND (
            cards.front LIKE '%' || :query || '%'
            OR cards.back LIKE '%' || :query || '%'
            OR cards.user_question LIKE '%' || :query || '%'
            OR cards.user_supplement LIKE '%' || :query || '%'
        )
        ORDER BY cards.front ASC
        """,
    )
    suspend fun searchCardsInSubject(subjectId: String, query: String): List<CardEntity>

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        WHERE decks.subject_id = :subjectId AND cards.front = :frontWord
        LIMIT 1
        """,
    )
    suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): CardEntity?

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        WHERE decks.subject_id = :subjectId
        """,
    )
    suspend fun getAllCardsInSubject(subjectId: String): List<CardEntity>

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        INNER JOIN subjects ON decks.subject_id = subjects.id
        WHERE subjects.is_archived = 0
          AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= :nowTimestamp)
        ORDER BY RANDOM() LIMIT 1
        """,
    )
    suspend fun getRandomCard(nowTimestamp: Long = System.currentTimeMillis()): CardEntity?

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        INNER JOIN subjects ON decks.subject_id = subjects.id
        WHERE subjects.is_archived = 0 AND cards.is_bookmarked = 1
          AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= :nowTimestamp)
        ORDER BY RANDOM() LIMIT 1
        """,
    )
    suspend fun getRandomBookmarkedCard(nowTimestamp: Long = System.currentTimeMillis()): CardEntity?

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        INNER JOIN subjects ON decks.subject_id = subjects.id
        INNER JOIN (
            SELECT card_id, MAX(studied_at) AS max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest_log ON cards.id = latest_log.card_id
        WHERE subjects.is_archived = 0
          AND latest_log.max_studied_at <= :cutoffTimestamp
          AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= :nowTimestamp)
        ORDER BY RANDOM() LIMIT 1
        """,
    )
    suspend fun getRandomCardOverDaysAgo(
        cutoffTimestamp: Long,
        nowTimestamp: Long = System.currentTimeMillis(),
    ): CardEntity?

    @Query(
        """
        SELECT cards.* FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        INNER JOIN subjects ON decks.subject_id = subjects.id
        INNER JOIN (
            SELECT card_id, MAX(studied_at) AS max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest_log ON cards.id = latest_log.card_id
        WHERE subjects.is_archived = 0
          AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= :nowTimestamp)
        ORDER BY latest_log.max_studied_at ASC
        LIMIT 1
        """,
    )
    suspend fun getOldestStudiedCard(nowTimestamp: Long = System.currentTimeMillis()): CardEntity?

    // --- StudySession Operations (続きから再開・進捗永続化) ---
    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId")
    suspend fun getSession(deckId: String): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId")
    fun getSessionFlow(deckId: String): Flow<StudySessionEntity?>

    @Upsert
    suspend fun insertOrUpdateSession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions WHERE deck_id = :deckId")
    suspend fun deleteSession(deckId: String)

    // --- StudyLog Operations (出題履歴・実績) ---
    @Upsert
    suspend fun insertStudyLog(log: CardStudyLogEntity): Long

    @Query("SELECT * FROM card_study_logs WHERE card_id = :cardId ORDER BY studied_at DESC LIMIT :limit")
    suspend fun getRecentLogsForCard(cardId: String, limit: Int = 10): List<CardStudyLogEntity>

    @Query("SELECT * FROM card_study_logs WHERE card_id = :cardId ORDER BY studied_at DESC")
    fun getLogsForCardFlow(cardId: String): Flow<List<CardStudyLogEntity>>

    @Query(
        """
        SELECT l.* FROM card_study_logs l
        INNER JOIN (
            SELECT card_id, MAX(studied_at) as max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest ON l.card_id = latest.card_id AND l.studied_at = latest.max_studied_at
        INNER JOIN cards c ON c.id = l.card_id
        WHERE c.deck_id = :deckId
        """,
    )
    fun getLatestLogsForDeck(deckId: String): Flow<List<CardStudyLogEntity>>

    @Query(
        """
        SELECT l.* FROM card_study_logs l
        INNER JOIN (
            SELECT card_id, MAX(studied_at) as max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest ON l.card_id = latest.card_id AND l.studied_at = latest.max_studied_at
        INNER JOIN cards c ON c.id = l.card_id
        WHERE c.deck_id = :deckId
        """,
    )
    suspend fun getLatestLogsForDeckSync(deckId: String): List<CardStudyLogEntity>

    @Query("SELECT COUNT(*) FROM card_study_logs WHERE card_id = :cardId")
    suspend fun getReviewCountForCard(cardId: String): Int

    @Query("SELECT COUNT(*) FROM card_study_logs WHERE card_id = :cardId AND is_correct = 1")
    suspend fun getCorrectCountForCard(cardId: String): Int

    @Query(
        """
        SELECT cards.*, latest_log.max_studied_at
        FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        INNER JOIN (
            SELECT card_id, MAX(studied_at) AS max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest_log ON cards.id = latest_log.card_id
        WHERE decks.subject_id = :subjectId
        ORDER BY latest_log.max_studied_at DESC
        """,
    )
    fun getCardsWithLatestStudyTimeBySubject(subjectId: String): Flow<List<CardWithLatestStudiedAt>>

    @Query(
        """
        SELECT cards.*, latest_log.max_studied_at
        FROM cards
        INNER JOIN decks ON cards.deck_id = decks.id
        INNER JOIN (
            SELECT card_id, MAX(studied_at) AS max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest_log ON cards.id = latest_log.card_id
        WHERE decks.subject_id = :subjectId
        ORDER BY latest_log.max_studied_at DESC
        """,
    )
    suspend fun getCardsWithLatestStudyTimeBySubjectSync(subjectId: String): List<CardWithLatestStudiedAt>

    @Query(
        """
        SELECT cards.*, latest_log.max_studied_at
        FROM cards
        INNER JOIN (
            SELECT card_id, MAX(studied_at) AS max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest_log ON cards.id = latest_log.card_id
        WHERE cards.deck_id = :deckId
        ORDER BY latest_log.max_studied_at DESC
        """,
    )
    fun getCardsWithLatestStudyTimeByDeck(deckId: String): Flow<List<CardWithLatestStudiedAt>>

    @Query(
        """
        SELECT cards.*, latest_log.max_studied_at
        FROM cards
        INNER JOIN (
            SELECT card_id, MAX(studied_at) AS max_studied_at
            FROM card_study_logs
            GROUP BY card_id
        ) latest_log ON cards.id = latest_log.card_id
        WHERE cards.deck_id = :deckId
        ORDER BY latest_log.max_studied_at DESC
        """,
    )
    suspend fun getCardsWithLatestStudyTimeByDeckSync(deckId: String): List<CardWithLatestStudiedAt>

    @Query("SELECT * FROM card_study_logs ORDER BY studied_at ASC")
    fun getAllStudyLogsFlow(): Flow<List<CardStudyLogEntity>>

    @Query("SELECT * FROM card_study_logs ORDER BY studied_at ASC")
    suspend fun getAllStudyLogsSync(): List<CardStudyLogEntity>

    // --- Sugoroku Operations ---
    @Query("SELECT * FROM sugoroku_progress WHERE id = :id LIMIT 1")
    fun getSugorokuProgress(id: String = "default_progress"): Flow<SugorokuProgressEntity?>

    @Query("SELECT * FROM sugoroku_progress WHERE id = :id LIMIT 1")
    suspend fun getSugorokuProgressSync(id: String = "default_progress"): SugorokuProgressEntity?

    @Upsert
    suspend fun saveSugorokuProgress(progress: SugorokuProgressEntity)
}
