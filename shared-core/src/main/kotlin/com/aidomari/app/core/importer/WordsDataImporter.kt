package com.aidomari.app.core.importer

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudyDirection
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

data class ImportSummary(
    val subjectsCount: Int,
    val decksCount: Int,
    val cardsCount: Int,
)

class WordsDataImporter(
    private val repository: WordsRepository,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) {

    suspend fun importJson(
        jsonString: String,
        targetSubjectId: String? = null,
    ): Result<ImportSummary> = runCatching {
        val rootElement = json.parseToJsonElement(jsonString)

        when {
            // 1. Array format: words-csv-to-json ([ { "title": "...", "content": [ ... ] } ])
            rootElement is JsonArray -> {
                importCsvToJsonArray(rootElement, targetSubjectId)
            }

            // 2. Modern Aidomari format: { "subjects": [ ... ] }
            rootElement is JsonObject && rootElement.containsKey("subjects") -> {
                importModernFormat(rootElement)
            }

            // 3. Elm V1 / V2 Model format: { "modelState": "...", "menuModel": { ... } } or { "books": [ ... ] }
            rootElement is JsonObject -> {
                importElmModelFormat(rootElement, targetSubjectId)
            }

            else -> {
                throw IllegalArgumentException("認識できないデータフォーマットです")
            }
        }
    }

    private suspend fun importCsvToJsonArray(
        array: JsonArray,
        targetSubjectId: String?,
    ): ImportSummary {
        if (array.isEmpty()) return ImportSummary(0, 0, 0)

        val firstBook = array.first().jsonObject
        val rawTitle = firstBook["title"]?.jsonPrimitive?.content ?: "名称未設定デッキ"
        val deckName = cleanDeckTitle(rawTitle)
        val subject = getOrCreateSubject(targetSubjectId, deckName)

        val deckId = UUID.randomUUID().toString()
        val deck = Deck(
            id = deckId,
            subjectId = subject.id,
            name = deckName,
            splitSize = 10,
            autoContinue = false,
            sortOrder = 0,
        )
        repository.saveDeck(deck)

        val allCards = mutableListOf<Card>()
        for (item in array) {
            val bookObj = item.jsonObject
            val contentArray = bookObj["content"]?.jsonArray ?: continue
            contentArray.forEach { cardElem ->
                val cardObj = cardElem.jsonObject
                val term = extractCardField(cardObj, "term", "front")
                val description = extractCardField(cardObj, "description", "back")
                if (term.isNotBlank() || description.isNotBlank()) {
                    allCards.add(
                        Card(
                            id = UUID.randomUUID().toString(),
                            deckId = deckId,
                            term = term,
                            description = description,
                            orderIndex = allCards.size,
                        ),
                    )
                }
            }
        }
        if (allCards.isNotEmpty()) {
            repository.saveCards(allCards)
        }

        return ImportSummary(
            subjectsCount = if (targetSubjectId == null) 1 else 0,
            decksCount = 1,
            cardsCount = allCards.size,
        )
    }

    private suspend fun importElmModelFormat(
        rootObj: JsonObject,
        targetSubjectId: String?,
    ): ImportSummary {
        val booksArray = rootObj["books"]?.jsonArray
            ?: rootObj["menuModel"]?.jsonObject?.get("books")?.jsonArray
            ?: rootObj["preferenceModel"]?.jsonObject?.get("menuModel")?.jsonObject?.get("books")?.jsonArray
            ?: rootObj["playingModel"]?.jsonObject?.get("menuModel")?.jsonObject?.get("books")?.jsonArray
            ?: JsonArray(emptyList())

        if (booksArray.isEmpty()) return ImportSummary(0, 0, 0)

        val firstBook = booksArray.first().jsonObject
        val title = extractElmBookTitle(firstBook["bookHead"])
        val subject = getOrCreateSubject(targetSubjectId, title)

        val prefObj = booksArray.mapNotNull { it.jsonObject["preference"]?.jsonObject }.firstOrNull()
        val studyDirection = parseStudyDirection(prefObj)
        val shuffleMode = prefObj?.get("shuffleMode")?.jsonPrimitive?.booleanOrNull ?: false
        val autoContinue = prefObj?.get("autoContinue")?.jsonPrimitive?.booleanOrNull ?: false
        val autoSpeak = prefObj?.get("autoSpeak")?.jsonPrimitive?.booleanOrNull ?: false
        val speechRate = prefObj?.get("speechRate")?.jsonPrimitive?.content?.toFloatOrNull() ?: 1.0f
        val splitSize = prefObj?.get("numOfPreference")?.jsonPrimitive?.intOrNull ?: 10

        val deckId = UUID.randomUUID().toString()
        val deck = Deck(
            id = deckId,
            subjectId = subject.id,
            name = title,
            splitSize = splitSize,
            autoContinue = autoContinue,
            shuffleMode = shuffleMode,
            studyDirection = studyDirection,
            autoSpeak = autoSpeak,
            speechRate = speechRate,
            sortOrder = 0,
        )
        repository.saveDeck(deck)

        val allCards = mutableListOf<Card>()
        for (bookElem in booksArray) {
            val bookObj = bookElem.jsonObject
            val contentArray = bookObj["content"]?.jsonArray ?: continue
            contentArray.forEach { cardElem ->
                val cardObj = cardElem.jsonObject
                // Elm uses "word" and "meaning"
                val term = extractCardField(cardObj, "word", "term", "front")
                val description = extractCardField(cardObj, "meaning", "description", "back")
                val userQuestion = cardObj["user_question"]?.jsonPrimitive?.content ?: ""
                val userSupplement = cardObj["user_supplement"]?.jsonPrimitive?.content ?: ""
                val bookmarkId = cardObj["bookmark_id"]?.jsonPrimitive?.intOrNull ?: 0

                if (term.isNotBlank() || description.isNotBlank()) {
                    allCards.add(
                        Card(
                            id = UUID.randomUUID().toString(),
                            deckId = deckId,
                            term = term,
                            description = description,
                            userQuestion = userQuestion,
                            userSupplement = userSupplement,
                            isBookmarked = bookmarkId > 0,
                            orderIndex = allCards.size,
                        ),
                    )
                }
            }
        }

        if (allCards.isNotEmpty()) {
            repository.saveCards(allCards)
        }

        return ImportSummary(
            subjectsCount = if (targetSubjectId == null) 1 else 0,
            decksCount = 1,
            cardsCount = allCards.size,
        )
    }

    private suspend fun importModernFormat(rootObj: JsonObject): ImportSummary {
        val subjectsArray = rootObj["subjects"]?.jsonArray ?: JsonArray(emptyList())
        var subjectsImported = 0
        var decksImported = 0
        var cardsImported = 0

        for (subElem in subjectsArray) {
            val subObj = subElem.jsonObject
            val subjectName = subObj["name"]?.jsonPrimitive?.content ?: "新規教科"
            val ttsLanguage = subObj["ttsLanguage"]?.jsonPrimitive?.content ?: "auto"

            val subjectId = UUID.randomUUID().toString()
            val subject = Subject(
                id = subjectId,
                name = subjectName,
                ttsLanguage = ttsLanguage,
                sortOrder = subjectsImported,
            )
            repository.saveSubject(subject)
            subjectsImported++

            val decksArray = subObj["decks"]?.jsonArray ?: JsonArray(emptyList())
            for (deckElem in decksArray) {
                val deckObj = deckElem.jsonObject
                val deckName = deckObj["name"]?.jsonPrimitive?.content ?: "新規デッキ"
                val splitSize = deckObj["splitSize"]?.jsonPrimitive?.intOrNull ?: 50
                val autoContinue = deckObj["autoContinue"]?.jsonPrimitive?.booleanOrNull ?: false
                val shuffleMode = deckObj["shuffleMode"]?.jsonPrimitive?.booleanOrNull ?: false
                val studyDirection = parseStudyDirection(deckObj)
                val autoSpeak = deckObj["autoSpeak"]?.jsonPrimitive?.booleanOrNull ?: false
                val speechRate = deckObj["speechRate"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 1.0f

                val deckId = UUID.randomUUID().toString()
                val deck = Deck(
                    id = deckId,
                    subjectId = subjectId,
                    name = deckName,
                    splitSize = splitSize,
                    autoContinue = autoContinue,
                    shuffleMode = shuffleMode,
                    studyDirection = studyDirection,
                    autoSpeak = autoSpeak,
                    speechRate = speechRate,
                    sortOrder = decksImported,
                )
                repository.saveDeck(deck)
                decksImported++

                val cardsArray = deckObj["cards"]?.jsonArray ?: JsonArray(emptyList())
                val cards = mutableListOf<Card>()
                cardsArray.forEachIndexed { index, cardElem ->
                    val cardObj = cardElem.jsonObject
                    val term = extractCardField(cardObj, "term", "front")
                    val description = extractCardField(cardObj, "description", "back")
                    val userQuestion = cardObj["userQuestion"]?.jsonPrimitive?.content ?: ""
                    val userSupplement = cardObj["userSupplement"]?.jsonPrimitive?.content ?: ""
                    val isBookmarked = cardObj["isBookmarked"]?.jsonPrimitive?.booleanOrNull ?: false

                    if (term.isNotBlank() || description.isNotBlank()) {
                        cards.add(
                            Card(
                                id = UUID.randomUUID().toString(),
                                deckId = deckId,
                                term = term,
                                description = description,
                                userQuestion = userQuestion,
                                userSupplement = userSupplement,
                                isBookmarked = isBookmarked,
                                orderIndex = index,
                            ),
                        )
                    }
                }
                if (cards.isNotEmpty()) {
                    repository.saveCards(cards)
                    cardsImported += cards.size
                }
            }
        }

        return ImportSummary(
            subjectsCount = subjectsImported,
            decksCount = decksImported,
            cardsCount = cardsImported,
        )
    }

    private fun extractCardField(cardObj: JsonObject, vararg keys: String): String =
        keys.firstNotNullOfOrNull { key ->
            runCatching { cardObj[key]?.jsonPrimitive?.content }.getOrNull()
        } ?: ""

    private fun parseStudyDirection(obj: JsonObject?): StudyDirection {
        val raw = runCatching { obj?.get("studyDirection")?.jsonPrimitive?.content?.trim() }.getOrNull()
        if (!raw.isNullOrBlank()) {
            StudyDirection.entries.find { it.name.equals(raw, ignoreCase = true) }?.let { return it }
        }
        val reverseMode = runCatching { obj?.get("reverseMode")?.jsonPrimitive?.booleanOrNull }.getOrNull() ?: false
        return if (reverseMode) StudyDirection.DESCRIPTION_TO_TERM else StudyDirection.TERM_TO_DESCRIPTION
    }

    private suspend fun getOrCreateSubject(targetSubjectId: String?, defaultName: String): Subject {
        if (targetSubjectId != null) {
            val existing = repository.getSubjectById(targetSubjectId)
            if (existing != null) return existing
        }
        val newSubject = Subject(
            id = UUID.randomUUID().toString(),
            name = defaultName,
        )
        repository.saveSubject(newSubject)
        return newSubject
    }

    private fun extractElmBookTitle(bookHeadElem: JsonElement?): String {
        if (bookHeadElem == null) return "単語帳"
        return when (bookHeadElem) {
            is JsonObject -> {
                val title = bookHeadElem["title"]?.jsonPrimitive?.content
                cleanDeckTitle(title ?: "単語帳")
            }
            else -> {
                val text = bookHeadElem.jsonPrimitive.content
                if (text == "Bookmark") "ブックマーク帳" else cleanDeckTitle(text)
            }
        }
    }

    private fun cleanDeckTitle(title: String): String = title
        .replace(Regex("""\s*[\(\[（]\s*(?:Part\s*)?\d+\s*[\)\]）]\s*$""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("""\s+Part\s*\d+\s*$""", RegexOption.IGNORE_CASE), "")
        .trim()
        .ifBlank { title }
}
