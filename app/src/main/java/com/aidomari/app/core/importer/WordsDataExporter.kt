package com.aidomari.app.core.importer

import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExportSubject(
    val name: String,
    val ttsLanguage: String = "auto",
    val decks: List<ExportDeck> = emptyList(),
)

@Serializable
data class ExportDeck(
    val name: String,
    val splitSize: Int = 50,
    val autoContinue: Boolean = false,
    val shuffleMode: Boolean = false,
    val cards: List<ExportCard> = emptyList(),
)

@Serializable
data class ExportCard(
    val front: String,
    val back: String,
    val userQuestion: String = "",
    val userSupplement: String = "",
    val isBookmarked: Boolean = false,
)

@Serializable
data class ExportRoot(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val subjects: List<ExportSubject>,
)

class WordsDataExporter(
    private val repository: WordsRepository,
    private val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
    },
) {

    suspend fun exportAll(): String {
        val subjects = repository.getAllSubjects().first()
        val exportSubjects = subjects.map { subject ->
            val decks = repository.getDecksBySubject(subject.id).first()
            val exportDecks = decks.map { deck ->
                val cards = repository.getCardsByDeckSync(deck.id)
                val exportCards = cards.map { card ->
                    ExportCard(
                        front = card.front,
                        back = card.back,
                        userQuestion = card.userQuestion,
                        userSupplement = card.userSupplement,
                        isBookmarked = card.isBookmarked,
                    )
                }
                ExportDeck(
                    name = deck.name,
                    splitSize = deck.splitSize,
                    autoContinue = deck.autoContinue,
                    shuffleMode = deck.shuffleMode,
                    cards = exportCards,
                )
            }
            ExportSubject(
                name = subject.name,
                ttsLanguage = subject.ttsLanguage,
                decks = exportDecks,
            )
        }

        val root = ExportRoot(subjects = exportSubjects)
        return json.encodeToString(root)
    }
}
