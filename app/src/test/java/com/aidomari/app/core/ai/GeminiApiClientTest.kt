package com.aidomari.app.core.ai

import com.aidomari.app.domain.model.ai.CardExtractionMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiApiClientTest {

    private class FakeHttpTransport(
        var postHandler: (url: String, jsonBody: String) -> HttpResponse = { _, _ -> HttpResponse(200, "{}") },
        var getHandler: (url: String) -> HttpResponse = { _ -> HttpResponse(200, "{}") },
    ) : HttpTransport {
        var lastUrl: String = ""
        var lastBody: String = ""

        constructor(responseHandler: (url: String, jsonBody: String) -> HttpResponse) : this(
            postHandler = responseHandler,
        )

        override suspend fun post(
            url: String,
            jsonBody: String,
            headers: Map<String, String>,
        ): HttpResponse {
            lastUrl = url
            lastBody = jsonBody
            return postHandler(url, jsonBody)
        }

        override suspend fun get(
            url: String,
            headers: Map<String, String>,
        ): HttpResponse {
            lastUrl = url
            return getHandler(url)
        }
    }

    @Test
    fun testConnection_withSuccess200_returnsSuccessTrue() = runTest {
        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(
                statusCode = 200,
                body = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "pong" }
                        ]
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.testConnection("valid_key")
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        assertTrue(fakeTransport.lastUrl.contains("key=valid_key"))
    }

    @Test
    fun testConnection_withBlankKey_failsFast() = runTest {
        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(statusCode = 200, body = "{}")
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.testConnection("   ")
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testConnection_with400Error_returnsMeaningfulMessage() = runTest {
        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(
                statusCode = 400,
                body = """
                {
                  "error": {
                    "code": 400,
                    "message": "API key not valid. Please pass a valid API key.",
                    "status": "INVALID_ARGUMENT"
                  }
                }
                """.trimIndent(),
            )
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.testConnection("invalid_key")
        assertFalse(result.isSuccess)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue(msg.contains("API key not valid"))
    }

    @Test
    fun generateCardsFromArticle_withValidJson_parsesCardsCorrectly() = runTest {
        val mockGeminiBody = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  {
                    "text": "[{\"front\":\"Kotlin Coroutines\",\"back\":\"非同期処理を簡潔に書く軽量スレッド機構。\",\"userSupplement\":\"Jetpack ComposeやStateFlowと親和性が高い\"},{\"front\":\"StateFlow\",\"back\":\"最新の値を保持するホットなデータフロー。\",\"userSupplement\":\"LiveDataの代替として推奨される\"}]"
                  }
                ]
              }
            }
          ]
        }
        """.trimIndent()

        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(statusCode = 200, body = mockGeminiBody)
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.generateCardsFromArticle(
            title = "Kotlin入門",
            url = "https://example.com/kotlin",
            content = "Kotlin CoroutinesとStateFlowについての記事本文...",
            apiKey = "valid_key",
        )

        assertTrue(result.isSuccess)
        val cards = result.getOrNull() ?: emptyList()
        assertEquals(2, cards.size)
        assertEquals("Kotlin Coroutines", cards[0].front)
        assertEquals("非同期処理を簡潔に書く軽量スレッド機構。", cards[0].back)
        assertEquals("Jetpack ComposeやStateFlowと親和性が高い", cards[0].userSupplement)
        assertEquals("https://example.com/kotlin", cards[0].sourceUrl)
        assertEquals("Kotlin入門", cards[0].sourceTitle)

        assertEquals("StateFlow", cards[1].front)

        // Verify prompt specifies flashcard rules, smart mode, systemInstruction, responseSchema, and few-shot examples
        assertTrue(fakeTransport.lastBody.contains("単語帳"))
        assertTrue(fakeTransport.lastBody.contains("スマート自動最適化"))
        assertTrue(fakeTransport.lastBody.contains("systemInstruction"))
        assertTrue(fakeTransport.lastBody.contains("responseSchema"))
        assertTrue(fakeTransport.lastBody.contains("出力例（Good）"))
        assertFalse(fakeTransport.lastBody.contains("3〜10"))
    }

    @Test
    fun generateCardsFromArticle_withCoreMode_includesCoreInstruction() = runTest {
        val mockGeminiBody = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": "[{\"front\":\"要点語\",\"back\":\"要点解説。\",\"userSupplement\":\"補足\"}]" }
                ]
              }
            }
          ]
        }
        """.trimIndent()

        val fakeTransport = FakeHttpTransport { _, _ -> HttpResponse(200, mockGeminiBody) }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.generateCardsFromArticle(
            title = "テスト",
            url = "https://example.com",
            content = "本文",
            apiKey = "key",
            mode = CardExtractionMode.CORE,
        )

        assertTrue(result.isSuccess)
        assertTrue(fakeTransport.lastBody.contains("コア概念・要旨の凝縮"))
        assertTrue(fakeTransport.lastBody.contains("中心命題"))
        assertTrue(fakeTransport.lastBody.contains("systemInstruction"))
        assertTrue(fakeTransport.lastBody.contains("responseSchema"))
        assertTrue(fakeTransport.lastBody.contains("出力例（Good）"))
    }

    @Test
    fun generateCardsFromArticle_withKnowledgeMode_includesKnowledgeInstruction() = runTest {
        val mockGeminiBody = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": "[{\"front\":\"詳細語\",\"back\":\"詳細解説。\",\"userSupplement\":\"補足\"}]" }
                ]
              }
            }
          ]
        }
        """.trimIndent()

        val fakeTransport = FakeHttpTransport { _, _ -> HttpResponse(200, mockGeminiBody) }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.generateCardsFromArticle(
            title = "テスト",
            url = "https://example.com",
            content = "本文",
            apiKey = "key",
            mode = CardExtractionMode.KNOWLEDGE,
        )

        assertTrue(result.isSuccess)
        assertTrue(fakeTransport.lastBody.contains("ナレッジ徹底網羅"))
        assertTrue(fakeTransport.lastBody.contains("幹から枝葉まで網羅"))
        assertTrue(fakeTransport.lastBody.contains("systemInstruction"))
        assertTrue(fakeTransport.lastBody.contains("responseSchema"))
        assertTrue(fakeTransport.lastBody.contains("出力例（Good）"))
    }

    @Test
    fun generateCardsFromArticle_withMarkdownFences_cleansAndParses() = runTest {
        val mockFencedText = "```json\n[{\"front\":\"問1\",\"back\":\"答1\",\"userSupplement\":\"補足1\"}]\n```"
        val mockGeminiBody = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": ${kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.serializer(), mockFencedText)} }
                ]
              }
            }
          ]
        }
        """.trimIndent()

        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(statusCode = 200, body = mockGeminiBody)
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.generateCardsFromArticle(
            title = "記事",
            url = "https://example.com",
            content = "本文",
            apiKey = "key",
        )

        assertTrue(result.isSuccess)
        val cards = result.getOrNull() ?: emptyList()
        assertEquals(1, cards.size)
        assertEquals("問1", cards[0].front)
        assertEquals("答1", cards[0].back)
    }

    @Test
    fun generateCardsFromArticle_withBlankContent_fails() = runTest {
        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(statusCode = 200, body = "{}")
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.generateCardsFromArticle(
            title = "タイトル",
            url = "https://example.com",
            content = "   ",
            apiKey = "key",
        )

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun fetchAvailableModels_withValidResponse_parsesAndFiltersModels() = runTest {
        val fakeTransport = FakeHttpTransport(
            getHandler = { url ->
                assertEquals("https://generativelanguage.googleapis.com/v1beta/models?key=valid_key", url)
                HttpResponse(
                    statusCode = 200,
                    body = """
                    {
                      "models": [
                        {
                          "name": "models/gemini-2.5-flash",
                          "supportedGenerationMethods": ["generateContent", "countTokens"]
                        },
                        {
                          "name": "models/gemini-2.5-pro",
                          "supportedGenerationMethods": ["generateContent"]
                        },
                        {
                          "name": "models/text-embedding-004",
                          "supportedGenerationMethods": ["embedContent"]
                        },
                        {
                          "name": "models/imagen-3.0",
                          "supportedGenerationMethods": ["imageGeneration"]
                        }
                      ]
                    }
                    """.trimIndent(),
                )
            },
        )
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.fetchAvailableModels("valid_key")
        assertTrue(result.isSuccess)
        val models = result.getOrNull() ?: emptyList()
        assertEquals(2, models.size)
        assertTrue(models.contains("gemini-2.5-flash"))
        assertTrue(models.contains("gemini-2.5-pro"))
        assertFalse(models.contains("text-embedding-004"))
        assertFalse(models.contains("imagen-3.0"))
    }

    @Test
    fun fetchAvailableModels_withBlankKey_failsFast() = runTest {
        val fakeTransport = FakeHttpTransport()
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.fetchAvailableModels("   ")
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun extractThemes_withValidJson_parsesThemesCorrectly() = runTest {
        val fakeTransport = FakeHttpTransport { _, _ ->
            HttpResponse(
                statusCode = 200,
                body = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "[\"地理・自然環境\", \"歴史とアイヌ文化\", \"産業と農業\"]" }
                        ]
                      }
                    }
                  ]
                }
                """.trimIndent(),
            )
        }
        val client = GeminiApiClientImpl(httpTransport = fakeTransport)

        val result = client.extractThemes(
            title = "北海道",
            url = "https://example.com/hokkaido",
            content = "北海道の自然と歴史と産業の解説です。",
            apiKey = "valid_key",
        )

        assertTrue(result.isSuccess)
        val themes = result.getOrNull() ?: emptyList()
        assertEquals(3, themes.size)
        assertEquals("地理・自然環境", themes[0])
        assertEquals("歴史とアイヌ文化", themes[1])
        assertEquals("産業と農業", themes[2])
    }

    @Test
    fun deduplicateCardProposals_mergesDuplicateFronts_andCombinesSupplements() {
        val client = GeminiApiClientImpl(httpTransport = FakeHttpTransport())

        val card1 = com.aidomari.app.domain.model.ai.GeneratedCardProposal(
            front = "大雪山",
            back = "北海道の中央部にある山群",
            userSupplement = "最高峰は旭岳（標高2,291m）。",
            sourceUrl = "https://example.com",
            sourceTitle = "北海道",
        )
        val card2 = com.aidomari.app.domain.model.ai.GeneratedCardProposal(
            front = "「大雪山」",
            back = "北海道の屋根と呼ばれる山岳地帯",
            userSupplement = "日本最大の国立公園に指定されている。",
            sourceUrl = "https://example.com",
            sourceTitle = "北海道",
        )
        val card3 = com.aidomari.app.domain.model.ai.GeneratedCardProposal(
            front = "知床半島",
            back = "オホーツク海に面した世界自然遺産",
            userSupplement = "手付かずの原生自然が残る。",
            sourceUrl = "https://example.com",
            sourceTitle = "北海道",
        )

        val deduplicated = client.deduplicateCardProposals(listOf(card1, card2, card3))

        assertEquals(2, deduplicated.size)
        val daisetuzan = deduplicated.first { it.front == "大雪山" }
        assertTrue(daisetuzan.userSupplement.contains("旭岳"))
        assertTrue(daisetuzan.userSupplement.contains("国立公園"))
    }

    @Test
    fun generateCardsFromArticle_withMultiStage_orchestratesThemesAndDeduplicates() = runTest {
        var callCount = 0
        val fakeTransport = FakeHttpTransport { _, jsonBody ->
            callCount++
            when (callCount) {
                1 -> {
                    // Call 1: Theme extraction
                    HttpResponse(
                        statusCode = 200,
                        body = """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  { "text": "[\"自然環境\", \"歴史文化\"]" }
                                ]
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                    )
                }
                2 -> {
                    // Call 2: Cards for "自然環境"
                    HttpResponse(
                        statusCode = 200,
                        body = """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  { "text": "[{\"front\": \"大雪山\", \"back\": \"北海道中央の山群\", \"userSupplement\": \"旭岳がある\"}]" }
                                ]
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                    )
                }
                3 -> {
                    // Call 3: Cards for "歴史文化"
                    HttpResponse(
                        statusCode = 200,
                        body = """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  { "text": "[{\"front\": \"大雪山\", \"back\": \"アイヌ語でヌタクカムシュペ\", \"userSupplement\": \"神々の遊ぶ庭と呼ばれる\"}, {\"front\": \"松前藩\", \"back\": \"蝦夷地を統治した藩\", \"userSupplement\": \"城下町が栄えた\"}]" }
                                ]
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                    )
                }
                else -> HttpResponse(statusCode = 200, body = "{}")
            }
        }

        val client = GeminiApiClientImpl(httpTransport = fakeTransport)
        val progressEvents = mutableListOf<GenerationProgress>()

        val result = client.generateCardsFromArticle(
            title = "北海道",
            url = "https://example.com/hokkaido",
            content = "北海道の自然と歴史についての総合記事です。",
            apiKey = "valid_key",
            mode = CardExtractionMode.KNOWLEDGE,
            onProgress = { progressEvents.add(it) },
        )

        assertTrue(result.isSuccess)
        val cards = result.getOrNull() ?: emptyList()
        assertEquals(2, cards.size) // "大雪山" (deduplicated) and "松前藩"
        assertEquals(3, callCount) // 1 theme call + 2 theme card calls

        // Verify progress sequence
        assertTrue(progressEvents.any { it is GenerationProgress.AnalyzingThemes })
        assertTrue(progressEvents.any { it is GenerationProgress.GeneratingThemeCards && it.currentThemeName == "自然環境" })
        assertTrue(progressEvents.any { it is GenerationProgress.GeneratingThemeCards && it.currentThemeName == "歴史文化" })
        assertTrue(progressEvents.any { it is GenerationProgress.Deduplicating })
    }

    @Test
    fun generateCardsFromArticle_whenThemeCardCallFails_failsFastWithoutContinuing() = runTest {
        var callCount = 0
        val fakeTransport = FakeHttpTransport { _, _ ->
            callCount++
            when (callCount) {
                1 -> {
                    // Call 1: Theme extraction succeeds with 2 themes
                    HttpResponse(
                        statusCode = 200,
                        body = """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  { "text": "[\"テーマ1\", \"テーマ2\"]" }
                                ]
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                    )
                }
                in 2..5 -> {
                    // Calls 2-5: First theme fails with 503 Overloaded across initial attempt + 3 retries
                    HttpResponse(
                        statusCode = 503,
                        body = """
                        {
                          "error": {
                            "code": 503,
                            "message": "The model is overloaded. Please try again later.",
                            "status": "UNAVAILABLE"
                          }
                        }
                        """.trimIndent(),
                    )
                }
                else -> HttpResponse(statusCode = 200, body = "{}")
            }
        }

        val client = GeminiApiClientImpl(httpTransport = fakeTransport)
        val result = client.generateCardsFromArticle(
            title = "北海道",
            url = "https://example.com/hokkaido",
            content = "本文",
            apiKey = "valid_key",
            mode = CardExtractionMode.KNOWLEDGE,
            onProgress = {},
        )

        assertTrue(result.isFailure)
        assertEquals(5, callCount) // 1 theme call + 1 initial attempt for theme 1 + 3 retries = 5 calls; theme 2 never called
        assertTrue(result.exceptionOrNull()?.message?.contains("503") == true)
    }

    @Test
    fun generateCardsForTheme_when503Occurs_retriesAndSucceeds() = runTest {
        var callCount = 0
        val fakeTransport = FakeHttpTransport { _, _ ->
            callCount++
            if (callCount == 1) {
                HttpResponse(
                    statusCode = 503,
                    body = """
                    {
                      "error": {
                        "code": 503,
                        "message": "The model is overloaded. Please try again later.",
                        "status": "UNAVAILABLE"
                      }
                    }
                    """.trimIndent(),
                )
            } else {
                HttpResponse(
                    statusCode = 200,
                    body = """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              { "text": "[{\"front\":\"復旧語\",\"back\":\"リトライ成功\",\"userSupplement\":\"補足\"}]" }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )
            }
        }

        val client = GeminiApiClientImpl(httpTransport = fakeTransport)
        val result = client.generateCardsForTheme(
            title = "テスト",
            url = "https://example.com",
            content = "本文",
            theme = "テーマ",
            apiKey = "key",
        )

        assertTrue(result.isSuccess)
        assertEquals(2, callCount)
        assertEquals("復旧語", result.getOrNull()?.first()?.front)
    }

    @Test
    fun fetchAvailableModels_when503Occurs_retriesAndSucceeds() = runTest {
        var callCount = 0
        val fakeTransport = FakeHttpTransport(
            getHandler = { _ ->
                callCount++
                if (callCount == 1) {
                    HttpResponse(
                        statusCode = 503,
                        body = """
                        {
                          "error": {
                            "code": 503,
                            "message": "The model is overloaded. Please try again later.",
                            "status": "UNAVAILABLE"
                          }
                        }
                        """.trimIndent(),
                    )
                } else {
                    HttpResponse(
                        statusCode = 200,
                        body = """
                        {
                          "models": [
                            {
                              "name": "models/gemini-2.5-flash",
                              "supportedGenerationMethods": ["generateContent"]
                            }
                          ]
                        }
                        """.trimIndent(),
                    )
                }
            },
        )

        val client = GeminiApiClientImpl(httpTransport = fakeTransport)
        val result = client.fetchAvailableModels("key")

        assertTrue(result.isSuccess)
        assertEquals(2, callCount)
        assertEquals(listOf("gemini-2.5-flash"), result.getOrNull())
    }

    @Test
    fun generateCardsForTheme_when429Occurs_retriesAndSucceeds() = runTest {
        var callCount = 0
        val fakeTransport = FakeHttpTransport { _, _ ->
            callCount++
            if (callCount == 1) {
                HttpResponse(
                    statusCode = 429,
                    body = """
                    {
                      "error": {
                        "code": 429,
                        "message": "Resource has been exhausted (e.g. check quota).",
                        "status": "RESOURCE_EXHAUSTED"
                      }
                    }
                    """.trimIndent(),
                )
            } else {
                HttpResponse(
                    statusCode = 200,
                    body = """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              { "text": "[{\"front\":\"レート制限復旧\",\"back\":\"リトライ成功\",\"userSupplement\":\"補足\"}]" }
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                )
            }
        }

        val client = GeminiApiClientImpl(httpTransport = fakeTransport)
        val result = client.generateCardsForTheme(
            title = "テスト",
            url = "https://example.com",
            content = "本文",
            theme = "テーマ",
            apiKey = "key",
        )

        assertTrue(result.isSuccess)
        assertEquals(2, callCount)
        assertEquals("レート制限復旧", result.getOrNull()?.first()?.front)
    }
}
