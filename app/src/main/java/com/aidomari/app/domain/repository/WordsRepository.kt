package com.aidomari.app.domain.repository

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.CardStudyLog
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface WordsRepository {

    // Subject
    fun getAllSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: String): Subject?
    suspend fun saveSubject(subject: Subject)
    suspend fun deleteSubject(id: String)

    // Deck
    fun getDecksBySubject(subjectId: String): Flow<List<Deck>>
    suspend fun getDeckById(deckId: String): Deck?
    suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck {
        val id = "bookmark_$subjectId"
        val existing = getDeckById(id)
        if (existing != null) return existing
        val newDeck = Deck(
            id = id,
            subjectId = subjectId,
            name = "苦手単語（ブックマーク）",
            splitSize = 50,
            autoContinue = true,
            shuffleMode = false,
        )
        saveDeck(newDeck)
        return newDeck
    }
    suspend fun getOrCreateRandomDeck(subjectId: String): Deck {
        val id = "random_$subjectId"
        val existing = getDeckById(id)
        if (existing != null) return existing
        val newDeck = Deck(
            id = id,
            subjectId = subjectId,
            name = "ランダム出題",
            splitSize = 10,
            autoContinue = false,
            shuffleMode = true,
        )
        saveDeck(newDeck)
        return newDeck
    }
    suspend fun saveDeck(deck: Deck)
    suspend fun deleteDeck(deckId: String)

    // Card
    fun getCardsByDeck(deckId: String): Flow<List<Card>>
    suspend fun getCardsByDeckSync(deckId: String): List<Card>
    suspend fun getCardById(cardId: String): Card?
    suspend fun saveCard(card: Card)
    suspend fun saveCards(cards: List<Card>)
    suspend fun deleteCard(cardId: String)
    suspend fun setBookmark(cardId: String, isBookmarked: Boolean)
    fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>>
    suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card>
    suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card?
    suspend fun getAllCardsInSubject(subjectId: String): List<Card>
    suspend fun getRandomCard(bookmarkOnly: Boolean = false): Card? = null

    // Session (続きから再開)
    suspend fun getSession(deckId: String): StudySession?
    fun getSessionFlow(deckId: String): Flow<StudySession?>
    suspend fun saveSession(session: StudySession)
    suspend fun deleteSession(deckId: String)

    // StudyLog (出題履歴・実績)
    suspend fun recordStudyLog(cardId: String, isCorrect: Boolean): CardStudyLog =
        CardStudyLog(id = UUID.randomUUID().toString(), cardId = cardId, isCorrect = isCorrect)
    suspend fun getRecentLogsForCard(cardId: String, limit: Int = 10): List<CardStudyLog> = emptyList()
    fun getLogsForCardFlow(cardId: String): Flow<List<CardStudyLog>> = emptyFlow()
    fun getLatestLogsForDeck(deckId: String): Flow<List<CardStudyLog>> = emptyFlow()
    suspend fun getLatestLogsForDeckSync(deckId: String): List<CardStudyLog> = emptyList()
}
