package com.aidomari.app.core.ai

import com.aidomari.app.domain.model.ai.CardExtractionDepth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiApiClientTest {

    private class FakeHttpTransport(
        var responseHandler: (url: String, jsonBody: String) -> HttpResponse,
    ) : HttpTransport {
        var lastUrl: String = ""
        var lastBody: String = ""

        override suspend fun post(
            url: String,
            jsonBody: String,
            headers: Map<String, String>,
        ): HttpResponse {
            lastUrl = url
            lastBody = jsonBody
            return responseHandler(url, jsonBody)
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

        // Verify prompt specifies flashcard rules and standard depth
        assertTrue(fakeTransport.lastBody.contains("単語帳"))
        assertTrue(fakeTransport.lastBody.contains("主要論点を網羅"))
        assertFalse(fakeTransport.lastBody.contains("3〜10"))
    }

    @Test
    fun generateCardsFromArticle_withEssenceDepth_includesEssenceInstruction() = runTest {
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
            depth = CardExtractionDepth.ESSENCE,
        )

        assertTrue(result.isSuccess)
        assertTrue(fakeTransport.lastBody.contains("エッセンスのみ厳選"))
        assertTrue(fakeTransport.lastBody.contains("中心命題"))
    }

    @Test
    fun generateCardsFromArticle_withDetailedDepth_includesDetailedInstruction() = runTest {
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
            depth = CardExtractionDepth.DETAILED,
        )

        assertTrue(result.isSuccess)
        assertTrue(fakeTransport.lastBody.contains("文中の知見を余さず拾う"))
        assertTrue(fakeTransport.lastBody.contains("枝葉に至るまで"))
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
}
