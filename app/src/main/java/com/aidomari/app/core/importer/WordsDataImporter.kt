package com.aidomari.app.core.importer

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        val subject = getOrCreateSubject(targetSubjectId, "インポート教科")
        var decksImported = 0
        var cardsImported = 0

        for (item in array) {
            val bookObj = item.jsonObject
            val title = bookObj["title"]?.jsonPrimitive?.content ?: "名称未設定デッキ"
            val contentArray = bookObj["content"]?.jsonArray ?: JsonArray(emptyList())

            val deckId = UUID.randomUUID().toString()
            val deck = Deck(
                id = deckId,
                subjectId = subject.id,
                name = title,
                splitSize = 50,
                autoContinue = false,
                sortOrder = decksImported,
            )
            repository.saveDeck(deck)
            decksImported++

            val cards = mutableListOf<Card>()
            contentArray.forEachIndexed { index, cardElem ->
                val cardObj = cardElem.jsonObject
                val front = cardObj["front"]?.jsonPrimitive?.content ?: ""
                val back = cardObj["back"]?.jsonPrimitive?.content ?: ""
                if (front.isNotBlank() || back.isNotBlank()) {
                    cards.add(
                        Card(
                            id = UUID.randomUUID().toString(),
                            deckId = deckId,
                            front = front,
                            back = back,
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

        return ImportSummary(
            subjectsCount = if (targetSubjectId == null) 1 else 0,
            decksCount = decksImported,
            cardsCount = cardsImported,
        )
    }

    private suspend fun importElmModelFormat(
        rootObj: JsonObject,
        targetSubjectId: String?,
    ): ImportSummary {
        val subject = getOrCreateSubject(targetSubjectId, "移行データ（Elm）")
        var decksImported = 0
        var cardsImported = 0

        // Extract books array from menuModel or root
        val booksArray = rootObj["books"]?.jsonArray
            ?: rootObj["menuModel"]?.jsonObject?.get("books")?.jsonArray
            ?: rootObj["preferenceModel"]?.jsonObject?.get("menuModel")?.jsonObject?.get("books")?.jsonArray
            ?: rootObj["playingModel"]?.jsonObject?.get("menuModel")?.jsonObject?.get("books")?.jsonArray
            ?: JsonArray(emptyList())

        for (bookElem in booksArray) {
            val bookObj = bookElem.jsonObject

            // Title extraction from bookHead
            val title = extractElmBookTitle(bookObj["bookHead"])
            val prefObj = bookObj["preference"]?.jsonObject
            val reverseMode = prefObj?.get("reverseMode")?.jsonPrimitive?.booleanOrNull ?: false
            val shuffleMode = prefObj?.get("shuffleMode")?.jsonPrimitive?.booleanOrNull ?: false
            val autoContinue = prefObj?.get("autoContinue")?.jsonPrimitive?.booleanOrNull ?: false
            val splitSize = prefObj?.get("numOfPreference")?.jsonPrimitive?.intOrNull ?: 50

            val deckId = UUID.randomUUID().toString()
            val deck = Deck(
                id = deckId,
                subjectId = subject.id,
                name = title,
                splitSize = splitSize,
                autoContinue = autoContinue,
                shuffleMode = shuffleMode,
                reverseMode = reverseMode,
                sortOrder = decksImported,
            )
            repository.saveDeck(deck)
            decksImported++

            val contentArray = bookObj["content"]?.jsonArray ?: JsonArray(emptyList())
            val cards = mutableListOf<Card>()

            contentArray.forEachIndexed { index, cardElem ->
                val cardObj = cardElem.jsonObject
                // Elm uses "word" and "meaning"
                val front = cardObj["word"]?.jsonPrimitive?.content
                    ?: cardObj["front"]?.jsonPrimitive?.content
                    ?: ""
                val back = cardObj["meaning"]?.jsonPrimitive?.content
                    ?: cardObj["back"]?.jsonPrimitive?.content
                    ?: ""
                val userQuestion = cardObj["user_question"]?.jsonPrimitive?.content ?: ""
                val userSupplement = cardObj["user_supplement"]?.jsonPrimitive?.content ?: ""
                val bookmarkId = cardObj["bookmark_id"]?.jsonPrimitive?.intOrNull ?: 0

                if (front.isNotBlank() || back.isNotBlank()) {
                    cards.add(
                        Card(
                            id = UUID.randomUUID().toString(),
                            deckId = deckId,
                            front = front,
                            back = back,
                            userQuestion = userQuestion,
                            userSupplement = userSupplement,
                            isBookmarked = bookmarkId > 0,
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

        return ImportSummary(
            subjectsCount = if (targetSubjectId == null) 1 else 0,
            decksCount = decksImported,
            cardsCount = cardsImported,
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
                val reverseMode = deckObj["reverseMode"]?.jsonPrimitive?.booleanOrNull ?: false

                val deckId = UUID.randomUUID().toString()
                val deck = Deck(
                    id = deckId,
                    subjectId = subjectId,
                    name = deckName,
                    splitSize = splitSize,
                    autoContinue = autoContinue,
                    shuffleMode = shuffleMode,
                    reverseMode = reverseMode,
                    sortOrder = decksImported,
                )
                repository.saveDeck(deck)
                decksImported++

                val cardsArray = deckObj["cards"]?.jsonArray ?: JsonArray(emptyList())
                val cards = mutableListOf<Card>()
                cardsArray.forEachIndexed { index, cardElem ->
                    val cardObj = cardElem.jsonObject
                    val front = cardObj["front"]?.jsonPrimitive?.content ?: ""
                    val back = cardObj["back"]?.jsonPrimitive?.content ?: ""
                    val userQuestion = cardObj["userQuestion"]?.jsonPrimitive?.content ?: ""
                    val userSupplement = cardObj["userSupplement"]?.jsonPrimitive?.content ?: ""
                    val isBookmarked = cardObj["isBookmarked"]?.jsonPrimitive?.booleanOrNull ?: false

                    if (front.isNotBlank() || back.isNotBlank()) {
                        cards.add(
                            Card(
                                id = UUID.randomUUID().toString(),
                                deckId = deckId,
                                front = front,
                                back = back,
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
                val part = bookHeadElem["part"]?.jsonPrimitive?.intOrNull
                if (title != null && part != null) {
                    "$title (Part $part)"
                } else {
                    title ?: "単語帳"
                }
            }
            else -> {
                val text = bookHeadElem.jsonPrimitive.content
                if (text == "Bookmark") "ブックマーク帳" else text
            }
        }
    }
}
