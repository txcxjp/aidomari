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
        val systemInstruction = buildThemeExtractionSystemInstruction(mode = mode)
        val prompt = buildThemeExtractionPrompt(title = title, url = url, content = content, mode = mode)

        val requestBody = buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    add(buildJsonObject { put("text", systemInstruction) })
                }
            }
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
                put("responseSchema", buildThemeResponseSchema())
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
        val systemInstruction = buildCardExtractionSystemInstruction(mode = mode, isThemeScoped = true)
        val prompt = buildThemeCardExtractionPrompt(
            title = title,
            url = url,
            content = content,
            theme = theme,
            mode = mode,
        )

        val requestBody = buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    add(buildJsonObject { put("text", systemInstruction) })
                }
            }
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
                put("responseSchema", buildCardResponseSchema())
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
        val systemInstruction = buildCardExtractionSystemInstruction(mode = mode, isThemeScoped = false)
        val prompt = buildKnowledgeExtractionPrompt(title = title, url = url, content = content, mode = mode)

        val requestBody = buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    add(buildJsonObject { put("text", systemInstruction) })
                }
            }
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
                put("responseSchema", buildCardResponseSchema())
            }
        }.toString()

        val response = postWithRetry(endpoint, requestBody)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalStateException(errorMsg)
        }

        return parseCardsFromGeminiResponse(response.body, sourceUrl = url, sourceTitle = title)
    }

    private fun buildThemeResponseSchema(): JsonObject = buildJsonObject {
        put("type", "ARRAY")
        put("description", "記事から特定された主要テーマ（章・トピック）のリスト")
        putJsonObject("items") {
            put("type", "STRING")
        }
    }

    private fun buildCardResponseSchema(): JsonObject = buildJsonObject {
        put("type", "ARRAY")
        put("description", "抽出された単語帳カードのリスト")
        putJsonObject("items") {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("front") {
                    put("type", "STRING")
                    put("description", "単体で意味が特定できる自立した固有名詞・専門用語・固有概念（名詞句。一般名詞単体や見出しラベル、疑問文は不可）")
                }
                putJsonObject("back") {
                    put("type", "STRING")
                    put("description", "frontが何であるかを一目で想起できる核となる定義・要点（体言止め、または常体の短い1文）")
                }
                putJsonObject("userSupplement") {
                    put("type", "STRING")
                    put("description", "記事内でその用語が登場した具体的な文脈、背景エピソード、根拠データ、筆者の主張との関わり（常体「〜である・〜した」で統一された1〜3文）")
                }
            }
            putJsonArray("required") {
                add(kotlinx.serialization.json.JsonPrimitive("front"))
                add(kotlinx.serialization.json.JsonPrimitive("back"))
                add(kotlinx.serialization.json.JsonPrimitive("userSupplement"))
            }
        }
    }

    internal fun buildThemeExtractionSystemInstruction(mode: CardExtractionMode): String {
        val scaleCriteria = when (mode) {
            CardExtractionMode.CORE -> "記事全体を貫く中心テーマを1〜2個特定する。"
            CardExtractionMode.SMART -> "記事のジャンルに応じてテーマ分割の粒度を調整する。単一トピックのコラム・エッセイ・ニュースは1〜2個、複数の章や観点を持つ解説・事典記事は主要トピックごとに3〜5個に分割する。"
            CardExtractionMode.KNOWLEDGE -> "記事全体の知識を漏れなく体系化できるよう、扱われている主要な章・領域・トピックごとに3〜7個のテーマに分割する。"
        }
        return """
            提供されたWeb記事の構成を分析し、知識を単語帳カードとして抽出するための主要テーマ（章・トピック）のリストを特定する。

            【テーマ特定の基準】
            - $scaleCriteria
            - 各テーマ名は、対象領域が明確にわかる簡潔な名詞句（例: "地理・自然環境", "歴史とアイヌ文化", "産業と農業"）とする。
        """.trimIndent()
    }

    internal fun buildThemeExtractionPrompt(
        title: String,
        url: String,
        content: String,
        mode: CardExtractionMode,
    ): String = """
        タイトル: $title
        URL: $url
        抽出モード: ${mode.label}

        本文:
        $content
    """.trimIndent()

    internal fun buildCardExtractionSystemInstruction(
        mode: CardExtractionMode,
        isThemeScoped: Boolean,
    ): String {
        val modeCriteria = when (mode) {
            CardExtractionMode.CORE -> """
                【抽出基準：コア概念・要旨の凝縮】
                - 記事の中心命題（メインメッセージ）、思考の枠組み、議論の骨格をなす最重要概念のみを抽出する。
                - 主張を補強するためだけの一過性の具体例、付随的な固有名詞、トリビアは除外する。
            """.trimIndent()
            CardExtractionMode.SMART -> """
                【抽出基準：スマート自動最適化】
                - 記事のジャンルを判別して抽出対象を最適化する：
                  ・事典・技術解説・教材記事の場合：その領域で最も代表性の高い基本固有名詞・中核概念（幹）を最優先で押さえた上で、理解に不可欠な専門用語・制度・歴史的事項をバランスよく抽出する（周辺的な専門用語ばかり拾って代表的な基本用語を落とさないこと）。
                  ・論説・エッセイ・コラムの場合：筆者の中心的な主張・キー概念に加え、議論の転換点や象徴的な対比として機能している重要キーワードを抽出する。
            """.trimIndent()
            CardExtractionMode.KNOWLEDGE -> """
                【抽出基準：ナレッジ徹底網羅】
                - 階層的な網羅（幹から枝葉まで網羅）：その領域で最も代表性の高い基本概念・最重要固有名詞（幹）を確実にすべて含めた上で、文中に登場する専門用語・固有名詞・歴史的事項・制度・統計データなどの詳細知識（枝葉）まで網羅的に抽出する。
                - 基本概念を省略してマニアックな用語だけを抽出してはならない（常にコア概念・代表用語の上位集合とすること）。
            """.trimIndent()
        }
        val scopeRule = if (isThemeScoped) {
            "\n- 指定された「対象テーマ」に関連する用語・概念・事象を記事本文から抽出する（記事全体との関わりやテーマをまたぐ基本概念も含めてよい）。"
        } else {
            ""
        }

        return """
            提供されたWeb記事から知識を抽出し、フラッシュカード（単語帳）の配列を生成する。

            【各フィールドの定義】
            - front（表面）: 暗記・想起の対象となる固有名詞・専門用語・固有概念（名詞句）。
              ・カードの表面だけを見たときに何の用語・概念かが一意に特定できる自立した見出し語にすること。
              ・単なる一般名詞（例：「酪農」「道の駅」「大規模畑作」）や、記事の見出し・状況ラベル（例：「過疎地域指定」「一般的4地域区分」）をそのままfrontにしてはならない。
              ・疑問文（「〜とは」「なぜ〜か」）や文章にはしない。
            - back（裏面）: frontが何であるかを一目で想起できる核となる定義または要点。
              ・文体は「体言止め（名詞止め）」または簡潔な1文（常体）に統一する。
              ・記事固有の細かいエピソードは含めず、用語の本質的な定義に絞る。
            - userSupplement（補足メモ）: 記事内でその用語が登場した具体的な文脈、背景エピソード、根拠データ、筆者の主張との関わり（1〜3文）。
              ・複数テーマ間で文体がブレないよう、文末はすべて「常体（〜である・〜した）」に統一し、敬体（です・ます調）は使用しない。

            $modeCriteria$scopeRule

            【出力例（Good）】
            - front: "メタ認知"
              back: "自身の思考や認知状態を客観的に把握・制御する能力。"
              userSupplement: "人間は文章を自ら書く過程で自分が何を考えているかを知るメタ認知を養っており、AI代筆に頼るとこの能力開発が放棄されると指摘されている。"
            - front: "石狩平野"
              back: "北海道中西部、石狩川流域に広がる道内最大の沖積平野。"
              userSupplement: "泥炭地の客土事業や排水改良によって日本有数の稲作地帯へと発展した。"

            【避けるべき例（Bad）】
            - front: "酪農" / "道の駅"（※一般名詞単体になっており、他デッキと混ざった際に北海道のどの固有名詞・地域の話か判別できない。「根釧台地」など記事固有の対象・固有名詞を抽出すること）
            - front: "過疎地域指定" / "一般的4地域区分"（※用語ではなく単なる見出しラベル・状況説明になっている）
            - front: "なぜAIに文章を書かせると作文能力が低下するのか？"（※疑問文・長文になっている）
            - back: "米国での研究によるとAIを使うと作文能力が低下し、人間は書くことを通じてメタ認知を養っています。"（※定義ではなく長文の理由説明になっており、敬体が混ざっている）
        """.trimIndent()
    }

    internal fun buildThemeCardExtractionPrompt(
        title: String,
        url: String,
        content: String,
        theme: String,
        mode: CardExtractionMode,
    ): String = """
        対象テーマ: $theme
        抽出モード: ${mode.label}
        タイトル: $title
        URL: $url

        本文:
        $content
    """.trimIndent()

    private fun buildKnowledgeExtractionPrompt(
        title: String,
        url: String,
        content: String,
        mode: CardExtractionMode,
    ): String = """
        抽出モード: ${mode.label}
        タイトル: $title
        URL: $url

        本文:
        $content
    """.trimIndent()

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
