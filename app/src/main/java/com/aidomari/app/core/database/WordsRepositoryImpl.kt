package com.aidomari.app.core.database

import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.CardStudyLogEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity
import com.aidomari.app.core.database.entity.SugorokuProgressEntity
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.CardStudyLog
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.PastDayDeckType
import com.aidomari.app.domain.model.PastDayGroup
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.SugorokuProgress
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.domain.util.CardSnoozeCalculator
import com.aidomari.app.domain.util.PastDayDecksCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WordsRepositoryImpl(
    private val dao: AidomariDao,
) : WordsRepository {

    override fun getAllSubjects(): Flow<List<Subject>> =
        dao.getAllSubjects().map { list -> list.map { it.toDomain() } }

    override suspend fun getSubjectById(id: String): Subject? =
        dao.getSubjectById(id)?.toDomain()

    override suspend fun saveSubject(subject: Subject) {
        dao.insertSubject(subject.toEntity())
    }

    override suspend fun deleteSubject(id: String) {
        dao.deleteSubjectById(id)
    }

    override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> =
        dao.getDecksBySubject(subjectId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDeckById(deckId: String): Deck? =
        dao.getDeckById(deckId)?.toDomain()

    override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck {
        val id = "bookmark_$subjectId"
        val existing = dao.getDeckById(id)
        if (existing != null) {
            return existing.toDomain()
        }
        val newDeck = Deck(
            id = id,
            subjectId = subjectId,
            name = "苦手単語（ブックマーク）",
            splitSize = 50,
            autoContinue = true,
            shuffleMode = false,
        )
        dao.insertDeck(newDeck.toEntity())
        return newDeck
    }

    override suspend fun getOrCreateRandomDeck(subjectId: String): Deck {
        val id = "random_$subjectId"
        val existing = dao.getDeckById(id)
        if (existing != null) {
            return existing.toDomain()
        }
        val newDeck = Deck(
            id = id,
            subjectId = subjectId,
            name = "ランダム出題",
            splitSize = 10,
            autoContinue = false,
            shuffleMode = true,
        )
        dao.insertDeck(newDeck.toEntity())
        return newDeck
    }

    override suspend fun getOrCreatePastDayDeck(subjectId: String, type: PastDayDeckType): Deck {
        val id = "${type.idPrefix}$subjectId"
        val existing = dao.getDeckById(id)
        if (existing != null) {
            return existing.toDomain()
        }
        val newDeck = Deck(
            id = id,
            subjectId = subjectId,
            name = type.title,
            splitSize = 50,
            autoContinue = true,
            shuffleMode = false,
        )
        dao.insertDeck(newDeck.toEntity())
        return newDeck
    }

    override suspend fun saveDeck(deck: Deck) {
        dao.insertDeck(deck.toEntity())
    }

    override suspend fun deleteDeck(deckId: String) {
        val deck = dao.getDeckById(deckId)
        if (deck != null) {
            dao.deleteDeck(deck)
        }
    }

    override fun getCardsByDeck(deckId: String): Flow<List<Card>> =
        dao.getCardsByDeck(deckId).map { list -> list.map { it.toDomain() } }

    override suspend fun getCardsByDeckSync(deckId: String): List<Card> =
        dao.getCardsByDeckSync(deckId).map { it.toDomain() }

    override suspend fun getCardById(cardId: String): Card? =
        dao.getCardById(cardId)?.toDomain()

    override suspend fun saveCard(card: Card) {
        dao.insertCard(card.toEntity())
    }

    override suspend fun saveCards(cards: List<Card>) {
        dao.insertCards(cards.map { it.toEntity() })
    }

    override suspend fun deleteCard(cardId: String) {
        val card = dao.getCardById(cardId)
        if (card != null) {
            dao.deleteCard(card)
        }
    }

    override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
        dao.setBookmark(cardId, isBookmarked)
    }

    override suspend fun snoozeCard(cardId: String): Card? {
        val card = dao.getCardById(cardId)?.toDomain() ?: return null
        val snoozeResult = CardSnoozeCalculator.calculateNextSnooze(card.snoozeLevel)
        val updated = card.copy(
            snoozedUntil = snoozeResult.snoozedUntil,
            snoozeLevel = snoozeResult.newLevel,
        )
        dao.setCardSnooze(cardId, snoozeResult.snoozedUntil, snoozeResult.newLevel)
        return updated
    }

    override suspend fun unsnoozeCard(cardId: String): Card? {
        val card = dao.getCardById(cardId)?.toDomain() ?: return null
        val updated = card.copy(
            snoozedUntil = null,
            snoozeLevel = 0,
        )
        dao.setCardSnooze(cardId, snoozedUntil = null, snoozeLevel = 0)
        return updated
    }

    override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> =
        dao.getBookmarkedCardsBySubject(subjectId).map { list -> list.map { it.toDomain() } }

    override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> =
        dao.searchCardsInSubject(subjectId, query).map { it.toDomain() }

    override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? =
        dao.findCardByFrontInSubject(subjectId, frontWord)?.toDomain()

    override suspend fun getAllCardsInSubject(subjectId: String): List<Card> =
        dao.getAllCardsInSubject(subjectId).map { it.toDomain() }

    override fun getLearnedCardsBySubject(subjectId: String): Flow<List<Card>> =
        dao.getCardsWithLatestStudyTimeBySubject(subjectId).map { list ->
            list.map { it.card.toDomain() }
        }

    override suspend fun getLearnedCardsBySubjectSync(subjectId: String): List<Card> =
        dao.getCardsWithLatestStudyTimeBySubjectSync(subjectId).map { it.card.toDomain() }

    override fun getLearnedCardsByDeck(deckId: String): Flow<List<Card>> =
        dao.getCardsWithLatestStudyTimeByDeck(deckId).map { list ->
            list.map { it.card.toDomain() }
        }

    override suspend fun getLearnedCardsByDeckSync(deckId: String): List<Card> =
        dao.getCardsWithLatestStudyTimeByDeckSync(deckId).map { it.card.toDomain() }

    override fun getPastDayCardsBySubject(subjectId: String): Flow<Map<PastDayDeckType, PastDayGroup>> =
        dao.getCardsWithLatestStudyTimeBySubject(subjectId).map { list ->
            val pairs = list.map { it.card.toDomain() to it.maxStudiedAt }
            PastDayDecksCalculator.calculate(pairs)
        }

    override suspend fun getCardsForPastDayDeckSync(subjectId: String, type: PastDayDeckType): List<Card> {
        val list = dao.getCardsWithLatestStudyTimeBySubjectSync(subjectId)
        val pairs = list.map { it.card.toDomain() to it.maxStudiedAt }
        val result = PastDayDecksCalculator.calculate(pairs)
        return result[type]?.cards ?: emptyList()
    }

    override suspend fun getRandomCard(bookmarkOnly: Boolean): Card? {
        val entity = if (bookmarkOnly) {
            dao.getRandomBookmarkedCard()
        } else {
            dao.getRandomCard()
        }
        return entity?.toDomain()
    }

    override suspend fun getRandomCardOverDaysAgo(cutoffTimestamp: Long): Card? =
        dao.getRandomCardOverDaysAgo(cutoffTimestamp)?.toDomain()

    override suspend fun getOldestStudiedCard(): Card? =
        dao.getOldestStudiedCard()?.toDomain()

    override suspend fun getSession(deckId: String): StudySession? =
        dao.getSession(deckId)?.toDomain()

    override fun getSessionFlow(deckId: String): Flow<StudySession?> =
        dao.getSessionFlow(deckId).map { it?.toDomain() }

    override suspend fun saveSession(session: StudySession) {
        dao.insertOrUpdateSession(session.toEntity())
    }

    override suspend fun deleteSession(deckId: String) {
        dao.deleteSession(deckId)
    }

    override suspend fun recordStudyLog(cardId: String, isCorrect: Boolean): CardStudyLog {
        val log = CardStudyLog(
            id = UUID.randomUUID().toString(),
            cardId = cardId,
            isCorrect = isCorrect,
            studiedAt = System.currentTimeMillis(),
        )
        dao.insertStudyLog(log.toEntity())
        return log
    }

    override suspend fun getRecentLogsForCard(cardId: String, limit: Int): List<CardStudyLog> =
        dao.getRecentLogsForCard(cardId, limit).map { it.toDomain() }

    override fun getLogsForCardFlow(cardId: String): Flow<List<CardStudyLog>> =
        dao.getLogsForCardFlow(cardId).map { list -> list.map { it.toDomain() } }

    override fun getLatestLogsForDeck(deckId: String): Flow<List<CardStudyLog>> =
        dao.getLatestLogsForDeck(deckId).map { list -> list.map { it.toDomain() } }

    override suspend fun getLatestLogsForDeckSync(deckId: String): List<CardStudyLog> =
        dao.getLatestLogsForDeckSync(deckId).map { it.toDomain() }

    override fun getAllStudyLogsFlow(): Flow<List<CardStudyLog>> =
        dao.getAllStudyLogsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllStudyLogsSync(): List<CardStudyLog> =
        dao.getAllStudyLogsSync().map { it.toDomain() }

    // Sugoroku (北海道すごろくマップ)
    override fun getSugorokuProgress(): Flow<SugorokuProgress> =
        dao.getSugorokuProgress().map { entity ->
            entity?.toDomain() ?: SugorokuProgress()
        }

    override suspend fun getSugorokuProgressSync(): SugorokuProgress =
        dao.getSugorokuProgressSync()?.toDomain() ?: SugorokuProgress().also {
            saveSugorokuProgress(it)
        }

    override suspend fun saveSugorokuProgress(progress: SugorokuProgress) {
        dao.saveSugorokuProgress(SugorokuProgressEntity.fromDomain(progress))
    }

    override suspend fun recordSugorokuProgress(questionsDelta: Int): SugorokuProgress {
        val current = getSugorokuProgressSync()
        val updated = current.advanceByQuestions(questionsDelta)
        saveSugorokuProgress(updated)
        return updated
    }

    // --- Mappers ---
    private fun SubjectEntity.toDomain() = Subject(
        id = id,
        name = name,
        ttsLanguage = ttsLanguage,
        sortOrder = sortOrder,
        createdAt = createdAt,
        isArchived = isArchived,
        colorTheme = colorTheme,
    )

    private fun Subject.toEntity() = SubjectEntity(
        id = id,
        name = name,
        ttsLanguage = ttsLanguage,
        sortOrder = sortOrder,
        createdAt = createdAt,
        isArchived = isArchived,
        colorTheme = colorTheme,
    )

    private fun DeckEntity.toDomain() = Deck(
        id = id,
        subjectId = subjectId,
        name = name,
        splitSize = splitSize,
        autoContinue = autoContinue,
        shuffleMode = shuffleMode,
        reverseMode = reverseMode,
        autoSpeak = autoSpeak,
        speechRate = speechRate,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

    private fun Deck.toEntity() = DeckEntity(
        id = id,
        subjectId = subjectId,
        name = name,
        splitSize = splitSize,
        autoContinue = autoContinue,
        shuffleMode = shuffleMode,
        reverseMode = reverseMode,
        autoSpeak = autoSpeak,
        speechRate = speechRate,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

    private fun CardEntity.toDomain() = Card(
        id = id,
        deckId = deckId,
        front = front,
        back = back,
        userQuestion = userQuestion,
        userSupplement = userSupplement,
        isBookmarked = isBookmarked,
        orderIndex = orderIndex,
        createdAt = createdAt,
        snoozedUntil = snoozedUntil,
        snoozeLevel = snoozeLevel,
    )

    private fun Card.toEntity() = CardEntity(
        id = id,
        deckId = deckId,
        front = front,
        back = back,
        userQuestion = userQuestion,
        userSupplement = userSupplement,
        isBookmarked = isBookmarked,
        orderIndex = orderIndex,
        createdAt = createdAt,
        snoozedUntil = snoozedUntil,
        snoozeLevel = snoozeLevel,
    )

    private fun StudySessionEntity.toDomain() = StudySession(
        deckId = deckId,
        partIndex = partIndex,
        currentCardIndex = currentCardIndex,
        queueCardIds = StudySession.parseCardIds(queueCardIdsJson),
        retryCardIds = StudySession.parseCardIds(retryCardIdsJson),
        isCompleted = isCompleted,
        lastStudiedAt = lastStudiedAt,
        completedPartIndices = StudySession.parsePartIndices(completedPartIndicesJson),
    )

    private fun StudySession.toEntity() = StudySessionEntity(
        deckId = deckId,
        partIndex = partIndex,
        currentCardIndex = currentCardIndex,
        queueCardIdsJson = queueCardIdsJson(),
        retryCardIdsJson = retryCardIdsJson(),
        isCompleted = isCompleted,
        lastStudiedAt = lastStudiedAt,
        completedPartIndicesJson = completedPartIndicesJson(),
    )

    private fun CardStudyLogEntity.toDomain() = CardStudyLog(
        id = id,
        cardId = cardId,
        isCorrect = isCorrect,
        studiedAt = studiedAt,
    )

    private fun CardStudyLog.toEntity() = CardStudyLogEntity(
        id = id,
        cardId = cardId,
        isCorrect = isCorrect,
        studiedAt = studiedAt,
    )
}
