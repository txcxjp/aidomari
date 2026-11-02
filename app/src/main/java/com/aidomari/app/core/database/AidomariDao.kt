package com.aidomari.app.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.DeckWithCards
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity
import com.aidomari.app.core.database.entity.SubjectWithDecks
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

    // --- Deck Operations ---
    @Query("SELECT * FROM decks WHERE subject_id = :subjectId AND id NOT LIKE 'bookmark_%' ORDER BY sort_order ASC, created_at ASC")
    fun getDecksBySubject(subjectId: String): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: String): DeckEntity?

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckWithCards(deckId: String): Flow<DeckWithCards?>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckWithCardsSync(deckId: String): DeckWithCards?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("UPDATE cards SET is_bookmarked = :isBookmarked WHERE id = :cardId")
    suspend fun setBookmark(cardId: String, isBookmarked: Boolean)

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
        WHERE decks.subject_id = :subjectId AND (cards.front LIKE '%' || :query || '%' OR cards.back LIKE '%' || :query || '%')
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

    // --- StudySession Operations (続きから再開・進捗永続化) ---
    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId")
    suspend fun getSession(deckId: String): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId")
    fun getSessionFlow(deckId: String): Flow<StudySessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions WHERE deck_id = :deckId")
    suspend fun deleteSession(deckId: String)
}
