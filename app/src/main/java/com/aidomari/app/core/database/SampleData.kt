package com.aidomari.app.core.database

import androidx.sqlite.db.SupportSQLiteDatabase
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.Subject
import java.util.UUID

object SampleData {
    const val SAMPLE_SUBJECT_NAME = "英語（サンプル）"
    const val SAMPLE_DECK_NAME = "日常英会話・基本単語"

    val sampleSubjects = listOf(
        Subject(
            id = "sample-subject-1",
            name = SAMPLE_SUBJECT_NAME,
            ttsLanguage = "en-US",
            sortOrder = 0,
            colorTheme = "INDIGO",
        ),
        Subject(
            id = "sample-subject-2",
            name = "世界史",
            ttsLanguage = "ja-JP",
            sortOrder = 1,
            colorTheme = "AMBER",
        ),
        Subject(
            id = "sample-subject-3",
            name = "情報科学・IT用語",
            ttsLanguage = "ja-JP",
            sortOrder = 2,
            colorTheme = "FOREST",
        ),
    )

    val sampleDeck = Deck(
        id = "sample-deck-1",
        subjectId = "sample-subject-1",
        name = SAMPLE_DECK_NAME,
        splitSize = 50,
        autoSpeak = false,
        speechRate = 1.0f,
        sortOrder = 0,
    )

    val sampleCards = listOf(
        Card(
            id = "sample-card-1",
            deckId = "sample-deck-1",
            front = "Hello!",
            back = "こんにちは！\n\nAidomari（相泊）へようこそ。\nカードをタップしてめくったり、左右スワイプで正解・復習を振り分けられます。",
            userQuestion = "日常のあいさつ",
            userSupplement = "基本表現です。#英語 #挨拶",
            isBookmarked = true,
            orderIndex = 0,
        ),
        Card(
            id = "sample-card-2",
            deckId = "sample-deck-1",
            front = "active recall",
            back = "アクティブ・リコール（[能動的想起|のうどうてきそうき]）\n\n自力で記憶を検索・想起することで、脳の神経回路が強化され定着率が高まります。",
            userQuestion = "効率的な記憶学習法",
            userSupplement = "Aidomariの基本原則です。[漢字|かんじ]のように書くとルビが振られます。#学習法 #重要",
            isBookmarked = false,
            orderIndex = 1,
        ),
        Card(
            id = "sample-card-3",
            deckId = "sample-deck-1",
            front = "spaced repetition",
            back = "[分散学習|ぶんさんがくしゅう]（[間隔反復|かんかくはんぷく]）\n\n忘却曲線に合わせて復習の間隔を徐々に広げていく学習法です。",
            userQuestion = "復習タイミングの最適化",
            userSupplement = "#学習法 #重要",
            isBookmarked = false,
            orderIndex = 2,
        ),
        Card(
            id = "sample-card-4",
            deckId = "sample-deck-1",
            front = "consistency",
            back = "一貫性、着実さ\n\n例文: **Consistency** is the key to mastery.\n（継続こそが習得への鍵である。）",
            userQuestion = "名詞: 継続・一貫性",
            userSupplement = "マークダウンの太字 **太字** や斜体 *斜体* が使えます。#英語 #重要",
            isBookmarked = false,
            orderIndex = 3,
        ),
        Card(
            id = "sample-card-5",
            deckId = "sample-deck-1",
            front = "vocabulary",
            back = "[語彙|ごい]、単語力\n\n音声アイコンをタップすると、ネイティブ音声で読み上げられます（TTS機能）。",
            userQuestion = "名詞: 語彙",
            userSupplement = "デッキ設定から自動読み上げ（Auto-speak）も可能です。#英語",
            isBookmarked = false,
            orderIndex = 4,
        ),
    )

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
                    card.front,
                    card.back,
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
