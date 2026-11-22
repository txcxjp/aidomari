package com.aidomari.app.core.ai

import com.aidomari.app.domain.model.ai.GeneratedCardProposal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

interface GeminiApiClient {
    suspend fun testConnection(apiKey: String, model: String = GeminiConfig.DEFAULT_MODEL): Result<Boolean>

    suspend fun generateCardsFromArticle(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String = GeminiConfig.DEFAULT_MODEL,
        temperature: Float = GeminiConfig.DEFAULT_TEMPERATURE,
    ): Result<List<GeneratedCardProposal>>
}

class GeminiApiClientImpl(
    private val httpTransport: HttpTransport = DefaultHttpsUrlConnectionTransport(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) : GeminiApiClient {

    override suspend fun testConnection(apiKey: String, model: String): Result<Boolean> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが入力されていません" }

        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(
                                buildJsonObject {
                                    put("text", "ping")
                                },
                            )
                        }
                    },
                )
            }
            putJsonObject("generationConfig") {
                put("maxOutputTokens", 10)
            }
        }.toString()

        val response = httpTransport.post(endpoint, requestBody)
        if (response.isSuccessful) {
            true
        } else {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalArgumentException(errorMsg)
        }
    }

    override suspend fun generateCardsFromArticle(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String,
        temperature: Float,
    ): Result<List<GeneratedCardProposal>> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが設定されていません" }
        require(content.isNotBlank()) { "解析対象の記事本文が空です" }

        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
        val prompt = buildKnowledgeExtractionPrompt(title = title, url = url, content = content)

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(
                                buildJsonObject {
                                    put("text", prompt)
                                },
                            )
                        }
                    },
                )
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
                put("responseMimeType", "application/json")
            }
        }.toString()

        val response = httpTransport.post(endpoint, requestBody)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalStateException(errorMsg)
        }

        parseCardsFromGeminiResponse(response.body, sourceUrl = url, sourceTitle = title)
    }

    private fun buildKnowledgeExtractionPrompt(title: String, url: String, content: String): String = """
        あなたは効率的な暗記・学習（単語帳・フラッシュカード）を支援する専門のアシスタントです。
        以下のWeb記事を読み、読者が「このページから得られたはずの重要用語・概念・知識」を抽出して、Aidomari単語帳カード形式のリストを作成してください。

        【記事情報】
        タイトル: $title
        URL: $url
        本文:
        $content

        【単語帳カード生成ルール】
        1. 表面 (front):
           - 記事の理解において中核となる重要キーワード・専門用語・概念（見出し語名詞、3〜15文字程度）。
           - 疑問文や長文（「〜とは何か？」「なぜ〜か？」）は絶対に禁止です。純粋な用語名・概念名にしてください。
        2. 裏面 (back):
           - その用語の核となる簡潔な定義・意味・要点（20〜30文字程度の極めて簡潔かつシャープな短文、1文以内）。
           - 単語帳のカード裏面に大きく表示されるため、パッと見て数秒で想起・暗記できる文章にしてください。長文や論述調の説明は絶対に書かないでください。
        3. 補足メモ (userSupplement):
           - 記事内での具体的な文脈、詳細な背景・理由・因果関係・付随データ（1〜3文程度）。
           - 詳しい解説や引用はすべてここに記述してください。
        4. 件数:
           - 記事の長さに応じて、本当に価値のある重要な専門用語・概念を 3〜10 件生成してください。無意味な細部や浅いトリビアは除外してください。
        5. 出力は必ず以下のJSON配列形式のみを出力してください（Markdownコードブロックは不要です）：
        [
          {
            "front": "重要キーワード",
            "back": "簡潔な定義・要点（20〜30文字）",
            "userSupplement": "記事の背景・文脈・詳細な解説"
          }
        ]
    """.trimIndent()

    private fun parseCardsFromGeminiResponse(
        responseJson: String,
        sourceUrl: String,
        sourceTitle: String,
    ): List<GeneratedCardProposal> {
        val root = json.parseToJsonElement(responseJson).jsonObject
        val candidates = root["candidates"]?.jsonArray
            ?: throw IllegalStateException("Gemini応答に候補(candidates)が含まれていません")

        if (candidates.isEmpty()) {
            return emptyList()
        }

        val firstCandidate = candidates[0].jsonObject
        val contentObj = firstCandidate["content"]?.jsonObject
            ?: throw IllegalStateException("Gemini応答にcontentが含まれていません")
        val parts = contentObj["parts"]?.jsonArray
            ?: throw IllegalStateException("Gemini応答にpartsが含まれていません")

        if (parts.isEmpty()) {
            return emptyList()
        }

        val rawText = parts[0].jsonObject["text"]?.jsonPrimitive?.content ?: ""
        val cleanedJsonText = cleanJsonCodeFence(rawText)

        val arrayElement = json.parseToJsonElement(cleanedJsonText)
        if (arrayElement !is JsonArray) {
            throw IllegalStateException("期待されるJSON配列ではありませんでした")
        }

        val cards = mutableListOf<GeneratedCardProposal>()
        for (item in arrayElement) {
            if (item !is JsonObject) continue
            val front = item["front"]?.jsonPrimitive?.content?.trim() ?: ""
            val back = item["back"]?.jsonPrimitive?.content?.trim() ?: ""
            val supplement = item["userSupplement"]?.jsonPrimitive?.content?.trim() ?: ""

            if (front.isNotBlank() && back.isNotBlank()) {
                cards.add(
                    GeneratedCardProposal(
                        front = front,
                        back = back,
                        userSupplement = supplement,
                        sourceUrl = sourceUrl,
                        sourceTitle = sourceTitle,
                    ),
                )
            }
        }
        return cards
    }

    private fun cleanJsonCodeFence(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        return text
    }

    private fun parseErrorMessage(errorBody: String, statusCode: Int): String = try {
        val root = json.parseToJsonElement(errorBody).jsonObject
        val errorObj = root["error"]?.jsonObject
        val message = errorObj?.get("message")?.jsonPrimitive?.content
        val status = errorObj?.get("status")?.jsonPrimitive?.content
        if (!message.isNullOrBlank()) {
            "Gemini APIエラー ($statusCode / $status): $message"
        } else {
            "HTTP $statusCode エラーが発生しました"
        }
    } catch (_: Exception) {
        when (statusCode) {
            400 -> "APIキーが無効、またはリクエスト形式が不正です (400)"
            403 -> "APIキーの権限がないか、許可されていない操作です (403)"
            429 -> "APIの利用上限（レート制限）に達しました。少し待ってから再試行してください (429)"
            else -> "通信エラーが発生しました (HTTP $statusCode)"
        }
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }
}
