package com.aidomari.app.domain.repository

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface WordsRepository {

    // Subject
    fun getAllSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: String): Subject?
    suspend fun saveSubject(subject: Subject)
    suspend fun deleteSubject(id: String)

    // Deck
    fun getDecksBySubject(subjectId: String): Flow<List<Deck>>
    suspend fun getDeckById(deckId: String): Deck?
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

    // Session (続きから再開)
    suspend fun getSession(deckId: String): StudySession?
    fun getSessionFlow(deckId: String): Flow<StudySession?>
    suspend fun saveSession(session: StudySession)
    suspend fun deleteSession(deckId: String)
}
