package com.aidomari.app.core.ai

import com.aidomari.app.domain.model.ai.CardExtractionMode
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

sealed interface GenerationProgress {
    data class AnalyzingThemes(val message: String = "記事の構成と主要テーマを分析中...") : GenerationProgress
    data class GeneratingThemeCards(
        val currentThemeIndex: Int,
        val totalThemes: Int,
        val currentThemeName: String,
        val generatedCardsSoFar: Int,
    ) : GenerationProgress {
        val fraction: Float get() = if (totalThemes > 0) currentThemeIndex.toFloat() / totalThemes.toFloat() else 0f
        val displayMessage: String get() = "テーマ ($currentThemeIndex/$totalThemes) を生成中: $currentThemeName (${generatedCardsSoFar}問抽出済)"
    }
    data class Deduplicating(val totalExtracted: Int) : GenerationProgress {
        val displayMessage: String get() = "重複チェック & 統合中... (計${totalExtracted}問)"
    }
}

interface GeminiApiClient {
    suspend fun testConnection(apiKey: String, model: String = GeminiConfig.DEFAULT_MODEL): Result<Boolean>

    suspend fun fetchAvailableModels(apiKey: String): Result<List<String>>

    suspend fun extractThemes(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String = GeminiConfig.DEFAULT_MODEL,
        mode: CardExtractionMode = CardExtractionMode.SMART,
    ): Result<List<String>>

    suspend fun generateCardsForTheme(
        title: String,
        url: String,
        content: String,
        theme: String,
        apiKey: String,
        model: String = GeminiConfig.DEFAULT_MODEL,
        temperature: Float = GeminiConfig.DEFAULT_TEMPERATURE,
        mode: CardExtractionMode = CardExtractionMode.SMART,
    ): Result<List<GeneratedCardProposal>>

    suspend fun generateCardsFromArticle(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String = GeminiConfig.DEFAULT_MODEL,
        temperature: Float = GeminiConfig.DEFAULT_TEMPERATURE,
        mode: CardExtractionMode = CardExtractionMode.SMART,
        onProgress: (GenerationProgress) -> Unit = {},
    ): Result<List<GeneratedCardProposal>>
}

class GeminiApiClientImpl(
    private val httpTransport: HttpTransport = DefaultHttpsUrlConnectionTransport(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val initialRetryDelayMs: Long = DEFAULT_INITIAL_RETRY_DELAY_MS,
) : GeminiApiClient {

    private suspend fun postWithRetry(
        endpoint: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse {
        var currentDelay = initialRetryDelayMs
        var lastResponse: HttpResponse? = null
        for (attempt in 0..maxRetries) {
            try {
                val response = httpTransport.post(endpoint, jsonBody, headers)
                if (response.statusCode != 503 && response.statusCode != 429) {
                    return response
                }
                lastResponse = response
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt >= maxRetries) throw e
            }
            if (attempt < maxRetries) {
                kotlinx.coroutines.delay(currentDelay)
                currentDelay *= 2
            }
        }
        return lastResponse ?: httpTransport.post(endpoint, jsonBody, headers)
    }

    private suspend fun getWithRetry(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse {
        var currentDelay = initialRetryDelayMs
        var lastResponse: HttpResponse? = null
        for (attempt in 0..maxRetries) {
            try {
                val response = httpTransport.get(endpoint, headers)
                if (response.statusCode != 503 && response.statusCode != 429) {
                    return response
                }
                lastResponse = response
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt >= maxRetries) throw e
            }
            if (attempt < maxRetries) {
                kotlinx.coroutines.delay(currentDelay)
                currentDelay *= 2
            }
        }
        return lastResponse ?: httpTransport.get(endpoint, headers)
    }

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

        val response = postWithRetry(endpoint, requestBody)
        if (response.isSuccessful) {
            true
        } else {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalArgumentException(errorMsg)
        }
    }

    override suspend fun fetchAvailableModels(apiKey: String): Result<List<String>> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが入力されていません" }

        val endpoint = "$BASE_URL?key=$apiKey"
        val response = getWithRetry(endpoint)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalArgumentException(errorMsg)
        }

        val root = json.parseToJsonElement(response.body).jsonObject
        val modelsArray = root["models"]?.jsonArray ?: emptyList()

        val extracted = modelsArray.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.content?.removePrefix("models/") ?: return@mapNotNull null
            val methods = obj["supportedGenerationMethods"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.content
            } ?: emptyList()

            if ("generateContent" in methods && name.startsWith("gemini-")) {
                name
            } else {
                null
            }
        }

        if (extracted.isEmpty()) {
            GeminiConfig.AVAILABLE_MODELS
        } else {
            extracted.distinct().sortedWith(
                Comparator { a, b ->
                    when {
                        a == GeminiConfig.DEFAULT_MODEL -> -1
                        b == GeminiConfig.DEFAULT_MODEL -> 1
                        a.contains("flash") && !b.contains("flash") -> -1
                        !a.contains("flash") && b.contains("flash") -> 1
                        else -> b.compareTo(a)
                    }
                },
            )
        }
    }

    override suspend fun extractThemes(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String,
        mode: CardExtractionMode,
    ): Result<List<String>> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが設定されていません" }
        require(content.isNotBlank()) { "解析対象の記事本文が空です" }

        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
        val prompt = buildThemeExtractionPrompt(title = title, url = url, content = content, mode = mode)

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(buildJsonObject { put("text", prompt) })
                        }
                    },
                )
            }
            putJsonObject("generationConfig") {
                put("temperature", 0.2f)
                put("responseMimeType", "application/json")
            }
        }.toString()

        val response = postWithRetry(endpoint, requestBody)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalStateException(errorMsg)
        }

        parseThemesFromGeminiResponse(response.body)
    }

    override suspend fun generateCardsForTheme(
        title: String,
        url: String,
        content: String,
        theme: String,
        apiKey: String,
        model: String,
        temperature: Float,
        mode: CardExtractionMode,
    ): Result<List<GeneratedCardProposal>> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが設定されていません" }
        require(content.isNotBlank()) { "解析対象の記事本文が空です" }
        require(theme.isNotBlank()) { "対象テーマが空です" }

        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
        val prompt = buildThemeCardExtractionPrompt(
            title = title,
            url = url,
            content = content,
            theme = theme,
            mode = mode,
        )

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(buildJsonObject { put("text", prompt) })
                        }
                    },
                )
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
                put("responseMimeType", "application/json")
            }
        }.toString()

        val response = postWithRetry(endpoint, requestBody)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalStateException(errorMsg)
        }

        parseCardsFromGeminiResponse(response.body, sourceUrl = url, sourceTitle = title)
    }

    override suspend fun generateCardsFromArticle(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String,
        temperature: Float,
        mode: CardExtractionMode,
        onProgress: (GenerationProgress) -> Unit,
    ): Result<List<GeneratedCardProposal>> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが設定されていません" }
        require(content.isNotBlank()) { "解析対象の記事本文が空です" }

        // If CORE, direct single-turn generation is optimal (concise backbone)
        if (mode == CardExtractionMode.CORE) {
            onProgress(GenerationProgress.AnalyzingThemes("コア概念を抽出中..."))
            return@runCatching generateCardsDirectly(title, url, content, apiKey, model, temperature, mode)
        }

        // Multi-stage extraction for SMART and KNOWLEDGE
        onProgress(GenerationProgress.AnalyzingThemes())
        val themesResult = extractThemes(title, url, content, apiKey, model, mode)
        val rawThemes = themesResult.getOrThrow()
        val themes = if (rawThemes.isNotEmpty()) {
            rawThemes
        } else {
            listOf(title.ifBlank { "主要論点" })
        }

        val allProposals = mutableListOf<GeneratedCardProposal>()
        for ((index, theme) in themes.withIndex()) {
            if (index > 0) {
                kotlinx.coroutines.delay(300L)
            }
            onProgress(
                GenerationProgress.GeneratingThemeCards(
                    currentThemeIndex = index + 1,
                    totalThemes = themes.size,
                    currentThemeName = theme,
                    generatedCardsSoFar = allProposals.size,
                ),
            )

            val themeCardsResult = generateCardsForTheme(
                title = title,
                url = url,
                content = content,
                theme = theme,
                apiKey = apiKey,
                model = model,
                temperature = temperature,
                mode = mode,
            )

            val cards = themeCardsResult.getOrThrow()
            allProposals.addAll(cards)
        }

        // Deduplicate across themes
        onProgress(GenerationProgress.Deduplicating(allProposals.size))
        val deduplicated = deduplicateCardProposals(allProposals)

        if (deduplicated.isEmpty()) {
            generateCardsDirectly(title, url, content, apiKey, model, temperature, mode)
        } else {
            deduplicated
        }
    }

    private suspend fun generateCardsDirectly(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String,
        temperature: Float,
        mode: CardExtractionMode,
    ): List<GeneratedCardProposal> {
        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
        val prompt = buildKnowledgeExtractionPrompt(title = title, url = url, content = content, mode = mode)

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(buildJsonObject { put("text", prompt) })
                        }
                    },
                )
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
                put("responseMimeType", "application/json")
            }
        }.toString()

        val response = postWithRetry(endpoint, requestBody)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalStateException(errorMsg)
        }

        return parseCardsFromGeminiResponse(response.body, sourceUrl = url, sourceTitle = title)
    }

    internal fun buildThemeExtractionPrompt(
        title: String,
        url: String,
        content: String,
        mode: CardExtractionMode,
    ): String {
        val scaleGuidance = when (mode) {
            CardExtractionMode.CORE -> "記事の最も根幹となる中核テーマを1つ（最大でも2つ）特定してください。"
            CardExtractionMode.SMART -> "記事の性質（事典解説、論説エッセイ、ニュース等）を見極め、読者が最も効率的に暗記・復習できるテーマ構成（単一の話題なら1〜2個、多岐にわたる解説記事なら3〜5個程度）を柔軟に特定してください。"
            CardExtractionMode.KNOWLEDGE -> "記事全体を余さず体系的に網羅するため、扱われている主要な章・トピック・テーマ（大作記事なら3〜7個程度）を漏れなくリストアップしてください。"
        }

        return """
            あなたは教材作成と文章分析の専門家です。
            以下のWeb記事を読み、フラッシュカード（単語帳）を網羅的に生成するための【主要テーマ（章・トピック）】を1つないし複数挙げてください。

            【記事情報】
            タイトル: $title
            URL: $url
            本文:
            $content

            【テーマ特定のルール】
            1. 記事の規模や内容に応じて、単一の話題なら1つ、多岐にわたる記事なら複数の独立したテーマを特定してください。
            2. $scaleGuidance
            3. 各テーマ名は、その領域の知識が想起しやすい具体的で簡潔な名前（例:「地理・自然環境」「歴史とアイヌ文化」「産業と農業」「交通インフラ」など）にしてください。
            4. 出力は必ず以下のJSON配列形式のみを出力してください（Markdownコードブロックは不要です）：
            [
              "テーマ名1",
              "テーマ名2"
            ]
        """.trimIndent()
    }

    internal fun buildThemeCardExtractionPrompt(
        title: String,
        url: String,
        content: String,
        theme: String,
        mode: CardExtractionMode,
    ): String {
        val modeInstruction = when (mode) {
            CardExtractionMode.CORE -> """
                4. 抽出の姿勢（コア概念・要旨の凝縮）:
                   - この記事・テーマの最も中核的な中心命題・思考の枠組み・不可欠な基本概念（コア）のみを厳選してください。
                   - 単なる具体例や固有名詞・付随エピソード（トリビア）は除外してください。
            """.trimIndent()
            CardExtractionMode.SMART -> """
                4. 抽出の姿勢（スマート自動最適化）:
                   - 記事の性質に応じ、事典・解説文であれば専門用語を網羅的に、論説・エッセイであれば筆者の主張・キー概念を中心に、読者が最も暗記・復習すべき知識を最適に抽出してください。
                   - その分野の基本概念・中核用語を押さえつつ、理解に必要な重要キーワードを過不足なく抽出してください。
            """.trimIndent()
            CardExtractionMode.KNOWLEDGE -> """
                4. 抽出の姿勢（ナレッジ徹底網羅）:
                   - このテーマの根幹となる最重要用語（幹）を確実に含めた上で、文中に登場する専門用語・固有名称・背景データ・付随エピソードなどの細部に至るまで、単語帳として学習価値のある知識を余さず徹底的に拾い上げてください。
                   - 【重要】基本概念を省略してマニアックな枝葉だけにしてはいけません。幹から枝葉まで網羅してください。
            """.trimIndent()
        }

        return """
            あなたは効率的な暗記・学習（単語帳・フラッシュカード）を支援する専門のアシスタントです。
            以下のWeb記事の中から、【指定されたテーマ】に属する重要用語・知識を抽出し、Aidomari単語帳カード形式のリストを作成してください。

            【記事情報】
            タイトル: $title
            URL: $url
            本文:
            $content

            【抽出対象のテーマ】
            $theme

            【単語帳カード生成ルール】
            1. 表面 (front):
               - このテーマにおいて中核となる重要キーワード・専門用語・概念（見出し語名詞、3〜15文字程度）。
               - 疑問文や長文（「〜とは何か？」等）は禁止です。
            2. 裏面 (back):
               - その用語の核となる簡潔な定義・意味・要点（20〜30文字程度の極めて簡潔かつシャープな短文、1文以内）。
               - パッと見て想起できる文章にしてください。
            3. 補足メモ (userSupplement):
               - このテーマにおける具体的な文脈、詳細な背景・理由・付随データ（1〜3文程度）。
            $modeInstruction
               - 指定テーマ【$theme】に関する知識に集中し、他の無関係な話題は含めないでください。
            5. 出力は必ず以下のJSON配列形式のみを出力してください（Markdownコードブロックは不要です）：
            [
              {
                "front": "重要キーワード",
                "back": "簡潔な定義・要点（20〜30文字）",
                "userSupplement": "文脈・背景・詳細解説"
              }
            ]
        """.trimIndent()
    }

    private fun buildKnowledgeExtractionPrompt(
        title: String,
        url: String,
        content: String,
        mode: CardExtractionMode,
    ): String {
        val modeInstruction = when (mode) {
            CardExtractionMode.CORE -> """
                4. 抽出の姿勢（コア概念・要旨の凝縮）:
                   - この記事を成立させている最も中核的な中心命題・思考の枠組み・不可欠な基本概念（コア）のみを厳選してください。
                   - 派生的な論点や単なる具体例・固有名詞などのトリビアは削ぎ落とし、記事の背骨となる用語のみに絞り込んでください。
            """.trimIndent()
            CardExtractionMode.SMART -> """
                4. 抽出の姿勢（スマート自動最適化）:
                   - 記事の性質（事典解説、論説エッセイ、ニュース等）を見極め、事典であれば重要用語・専門用語を網羅し、エッセイであれば筆者の主張・キー概念を中心に、読者がその文章から得るべき最適な知識を過不足なく抽出してください。
            """.trimIndent()
            CardExtractionMode.KNOWLEDGE -> """
                4. 抽出の姿勢（ナレッジ徹底網羅）:
                   - 記事の根幹となる最重要概念（幹）を確実に含めた上で、文中に登場する固有名称・専門用語・付随データ・背景エピソードなどの細部に至るまで、単語帳として学習価値のある知識を余さず徹底的に拾い上げてください。
                   - 【重要】基本概念を省略してマニアックな枝葉だけにしてはいけません。幹から枝葉まで網羅してください。
            """.trimIndent()
        }

        return """
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
            $modeInstruction
            5. 出力は必ず以下のJSON配列形式のみを出力してください（Markdownコードブロックは不要です）：
            [
              {
                "front": "重要キーワード",
                "back": "簡潔な定義・要点（20〜30文字）",
                "userSupplement": "記事の背景・文脈・詳細な解説"
              }
            ]
        """.trimIndent()
    }

    internal fun parseThemesFromGeminiResponse(responseJson: String): List<String> {
        val root = json.parseToJsonElement(responseJson).jsonObject
        val candidates = root["candidates"]?.jsonArray ?: return emptyList()
        if (candidates.isEmpty()) return emptyList()

        val firstCandidate = candidates[0].jsonObject
        val contentObj = firstCandidate["content"]?.jsonObject ?: return emptyList()
        val parts = contentObj["parts"]?.jsonArray ?: return emptyList()
        if (parts.isEmpty()) return emptyList()

        val rawText = parts[0].jsonObject["text"]?.jsonPrimitive?.content ?: ""
        val cleaned = cleanJsonCodeFence(rawText)

        val arrayElement = runCatching { json.parseToJsonElement(cleaned) }.getOrNull()
        if (arrayElement !is JsonArray) return emptyList()

        return arrayElement.mapNotNull {
            val str = runCatching { it.jsonPrimitive.content.trim() }.getOrNull()
            if (!str.isNullOrBlank()) str else null
        }
    }

    internal fun deduplicateCardProposals(cards: List<GeneratedCardProposal>): List<GeneratedCardProposal> {
        val seenNormalized = mutableMapOf<String, GeneratedCardProposal>()
        val result = mutableListOf<GeneratedCardProposal>()

        for (card in cards) {
            val normalized = normalizeFront(card.front)
            if (normalized.isBlank()) continue

            val existing = seenNormalized[normalized]
            if (existing == null) {
                seenNormalized[normalized] = card
                result.add(card)
            } else {
                val index = result.indexOf(existing)
                if (index >= 0) {
                    val merged = mergeDuplicateCards(existing, card)
                    result[index] = merged
                    seenNormalized[normalized] = merged
                }
            }
        }
        return result
    }

    internal fun normalizeFront(front: String): String = front.trim()
        .removePrefix("「").removeSuffix("」")
        .removePrefix("『").removeSuffix("』")
        .removePrefix("\"").removeSuffix("\"")
        .lowercase()
        .trim()

    internal fun mergeDuplicateCards(a: GeneratedCardProposal, b: GeneratedCardProposal): GeneratedCardProposal {
        val bestBack = if (b.back.length > a.back.length) b.back else a.back
        val combinedSupplement = when {
            a.userSupplement.isBlank() -> b.userSupplement
            b.userSupplement.isBlank() -> a.userSupplement
            a.userSupplement.contains(b.userSupplement) -> a.userSupplement
            b.userSupplement.contains(a.userSupplement) -> b.userSupplement
            else -> "${a.userSupplement}\n${b.userSupplement}"
        }
        return a.copy(back = bestBack, userSupplement = combinedSupplement)
    }

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
            503 -> "Geminiサーバーが一時的に過負荷です。少し時間をおいて再試行してください (503)"
            else -> "通信エラーが発生しました (HTTP $statusCode)"
        }
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        internal const val DEFAULT_MAX_RETRIES = 3
        internal const val DEFAULT_INITIAL_RETRY_DELAY_MS = 1000L
    }
}
