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
        val displayMessage: String get() = "テーマ ($currentThemeIndex/$totalThemes) を作成中: $currentThemeName (${generatedCardsSoFar}問抽出済)"
    }
    data class Deduplicating(val totalExtracted: Int) : GenerationProgress {
        val displayMessage: String get() = "重複チェック & 統合中... (計${totalExtracted}問)"
    }
}

internal data class SmartArticleAnalysis(
    val resolvedMode: CardExtractionMode,
    val themes: List<String>,
)

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
        otherThemes: List<String> = emptyList(),
        alreadyExtractedFronts: List<String> = emptyList(),
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

    internal suspend fun analyzeArticleForSmartMode(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String = GeminiConfig.DEFAULT_MODEL,
    ): Result<SmartArticleAnalysis> = runCatching {
        require(apiKey.isNotBlank()) { "APIキーが設定されていません" }
        require(content.isNotBlank()) { "解析対象の記事本文が空です" }

        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
        val systemInstruction = buildSmartAnalysisSystemInstruction()
        val prompt = buildThemeExtractionPrompt(
            title = title,
            url = url,
            content = content,
            mode = CardExtractionMode.SMART,
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
                put("temperature", 0.2f)
                put("responseMimeType", "application/json")
                put("responseSchema", buildSmartAnalysisResponseSchema())
            }
        }.toString()

        val response = postWithRetry(endpoint, requestBody)
        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(response.body, response.statusCode)
            throw IllegalStateException(errorMsg)
        }

        parseSmartAnalysisFromGeminiResponse(response.body)
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
        otherThemes: List<String>,
        alreadyExtractedFronts: List<String>,
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
            otherThemes = otherThemes,
            alreadyExtractedFronts = alreadyExtractedFronts,
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

        val (effectiveMode, rawThemes) = when (mode) {
            CardExtractionMode.CORE -> {
                onProgress(GenerationProgress.AnalyzingThemes("コア概念を抽出中..."))
                return@runCatching generateCardsDirectly(
                    title,
                    url,
                    content,
                    apiKey,
                    model,
                    temperature,
                    CardExtractionMode.CORE,
                )
            }
            CardExtractionMode.SMART -> {
                onProgress(GenerationProgress.AnalyzingThemes("記事のタイプと主要テーマを分析中..."))
                val analysis = analyzeArticleForSmartMode(title, url, content, apiKey, model).getOrThrow()
                if (analysis.resolvedMode == CardExtractionMode.CORE) {
                    onProgress(GenerationProgress.AnalyzingThemes("要旨重視の記事と判定（コアモードで抽出中...）"))
                    return@runCatching generateCardsDirectly(
                        title,
                        url,
                        content,
                        apiKey,
                        model,
                        temperature,
                        CardExtractionMode.CORE,
                    )
                }
                CardExtractionMode.KNOWLEDGE to analysis.themes
            }
            CardExtractionMode.KNOWLEDGE -> {
                onProgress(GenerationProgress.AnalyzingThemes())
                CardExtractionMode.KNOWLEDGE to extractThemes(
                    title,
                    url,
                    content,
                    apiKey,
                    model,
                    CardExtractionMode.KNOWLEDGE,
                ).getOrThrow()
            }
        }

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
                mode = effectiveMode,
                otherThemes = themes.filter { it != theme },
                alreadyExtractedFronts = allProposals.map { it.front }.distinct(),
            )

            val cards = themeCardsResult.getOrThrow()
            allProposals.addAll(cards)
        }

        // Deduplicate across themes
        onProgress(GenerationProgress.Deduplicating(allProposals.size))
        val deduplicated = deduplicateCardProposals(allProposals)

        if (deduplicated.isEmpty()) {
            generateCardsDirectly(title, url, content, apiKey, model, temperature, effectiveMode)
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

    private fun buildSmartAnalysisResponseSchema(): JsonObject = buildJsonObject {
        put("type", "OBJECT")
        put("description", "記事のタイプ判定結果と、主要テーマ（章・トピック）のリスト")
        putJsonObject("properties") {
            putJsonObject("recommendedMode") {
                put("type", "STRING")
                putJsonArray("enum") {
                    add(kotlinx.serialization.json.JsonPrimitive("CORE"))
                    add(kotlinx.serialization.json.JsonPrimitive("KNOWLEDGE"))
                }
                put(
                    "description",
                    "記事のジャンルに最適な抽出モード。論説・エッセイ・コラム・意見記事・ニュース解説などはCORE、事典・技術ドキュメント・学習教材・用語解説・体系的リファレンスはKNOWLEDGEとする",
                )
            }
            putJsonObject("themes") {
                put("type", "ARRAY")
                put(
                    "description",
                    "記事から特定された主要テーマ（章・トピック）のリスト。KNOWLEDGE判定時は主要セクションごとに分割し、CORE判定時は中心テーマ1〜2個とする",
                )
                putJsonObject("items") {
                    put("type", "STRING")
                }
            }
        }
        putJsonArray("required") {
            add(kotlinx.serialization.json.JsonPrimitive("recommendedMode"))
            add(kotlinx.serialization.json.JsonPrimitive("themes"))
        }
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
                    put(
                        "description",
                        "暗記・想起の対象となる固有名詞・専門用語・制度名・中核概念（名詞句。backの定義文から一意に逆引きできる用語とすること。単なる一般名詞や『〜の概要』『〜の動向』のような抽象的な見出し句・状況ラベル、疑問文は不可）",
                    )
                }
                putJsonObject("back") {
                    put("type", "STRING")
                    put("description", "frontが何であるかを一目で想起できる核となる定義・要点（体言止め、または常体の短い1文。記事本文の記述のみに忠実であること）")
                }
                putJsonObject("userSupplement") {
                    put("type", "STRING")
                    put("description", "記事内でその用語が登場した具体的な文脈、背景エピソード、根拠データ、筆者の主張との関わり（常体「〜である・〜した」で統一された1〜3文。記事本文に書かれた事実・年号・数値のみに基づき、推測や記事外情報の補完は禁止）")
                }
            }
            putJsonArray("required") {
                add(kotlinx.serialization.json.JsonPrimitive("front"))
                add(kotlinx.serialization.json.JsonPrimitive("back"))
                add(kotlinx.serialization.json.JsonPrimitive("userSupplement"))
            }
        }
    }

    internal fun buildSmartAnalysisSystemInstruction(): String = """
        提供されたWeb記事の性質と構成を分析し、フラッシュカード（単語帳）として最適な抽出モード（CORE または KNOWLEDGE）と、主要テーマのリストを決定する。

        【抽出モード判定基準（recommendedMode）】
        - CORE（要旨・論点重視）を選ぶべき記事：
          ・エッセイ、コラム、オピニオン、ブログ随筆、書評、対談記事、ニュース解説・社説など、「筆者の主張・問題提起・思考の枠組み」を理解することが主目的の記事。
          ・記事中の固有名詞の多くが主張を補強するための例え話・引用エピソードにとどまる記事。
        - KNOWLEDGE（体系的知識・用語網羅）を選ぶべき記事：
          ・百科事典（Wikipedia等）、技術ドキュメント、プログラミング・IT解説、学習教材、用語集、歴史・科学・法律・経済などの体系的な解説記事など、「記事内に登場する専門用語・固有名詞・仕組み・制度・ファクト」を幅広く定着させることが主目的の記事。

        【テーマ特定の基準（themes）】
        - CORE と判定した場合：記事全体を貫く中心テーマを1〜2個特定する。
        - KNOWLEDGE と判定した場合：記事の規模に応じて、扱われている主要な章・領域・トピックごとに互いに重複しないよう分割する（短〜中編は2〜4個、長大な事典・解説記事は5〜8個）。
        - 各テーマ名は、担当範囲が明確に分担された簡潔な名詞句（例: "基本概念と仕組み", "歴史的背景", "主要な制度と構造", "応用と課題"）とする。
    """.trimIndent()

    internal fun buildThemeExtractionSystemInstruction(mode: CardExtractionMode): String {
        val scaleCriteria = when (mode) {
            CardExtractionMode.CORE -> "記事全体を貫く中心テーマを1〜2個特定する。"
            CardExtractionMode.SMART,
            CardExtractionMode.KNOWLEDGE,
            -> "記事全体の知識を漏れなく体系化できるよう、扱われている主要な章・領域・トピックごとに、規模に応じて互いに領域が重ならないよう分割する（短〜中編は2〜4個、長大な事典・解説記事は5〜8個）。"
        }
        return """
            提供されたWeb記事の構成を分析し、知識を単語帳カードとして抽出するための主要テーマ（章・トピック）のリストを特定する。

            【テーマ特定の基準】
            - $scaleCriteria
            - 各テーマの担当範囲が互いに重複しないよう、明確に分担された簡潔な名詞句（例: "基本概念と仕組み", "歴史的背景", "主要な制度と構造", "応用と課題"）とする。
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
                - 記事の中心命題（メインメッセージ）、思考の枠組み、議論の骨格をなす最重要概念・キー用語のみを抽出する。
                - 主張を補強するためだけの一過性の具体例・例え話・引用作品名・付随的な人名やトリビアは除外する。
            """.trimIndent()
            CardExtractionMode.KNOWLEDGE,
            CardExtractionMode.SMART,
            -> """
                【抽出基準：ナレッジ徹底網羅】
                - 階層的な網羅（幹から枝葉まで網羅）：その領域で最も代表性の高い基本概念・最重要固有名詞（幹）を確実にすべて含めた上で、文中に登場する専門用語・固有名詞・歴史的事項・制度・仕組みなどの詳細知識（枝葉）まで網羅的に抽出する。
                - 基本概念を省略してマニアックな用語だけを抽出してはならない（常にコア概念・代表用語の上位集合とすること）。
                - セクション・小見出し間のバランス網羅：特定の1段落や1つの小項目だけに偏らず、対象テーマに含まれるすべての小見出し・観点から満遍なく代表的な用語を選ぶこと。
                - 同種リストの列挙偏り防止：本文中に並んでいる同種の具体例・一覧（例：多数の個別施設名、端点の羅列、バージョン・期数などの単純な連番、類似事例の連続列挙など）から偏って大量に抽出せず、最も象徴的な代表例に絞ること。
            """.trimIndent()
        }
        val scopeRule = if (isThemeScoped) {
            """
                【テーマ分担と重複防止の厳守】
                - 今回は記事全体のうち、指定された「対象テーマ」の領域に属する用語のみを抽出すること。
                - 「他テーマ一覧」に含まれる別テーマの領域には踏み込まず、対象テーマ固有の重要語を深掘りすること。
                - 「抽出済みの用語」リストが指定されている場合、そこに含まれる用語（およびその表記ゆれ・同義語）は絶対に出力しないこと。
            """.trimIndent().let { "\n\n$it" }
        } else {
            ""
        }

        return """
            提供されたWeb記事から知識を抽出し、フラッシュカード（単語帳）の配列を生成する。

            【ソース厳格準拠の原則（最重要）】
            - すべての記述（定義・年号・数値・固有名詞・因果関係・経緯）は、提供された記事本文に記載された情報のみを根拠とすること。
            - 記事本文に書かれていない事実・経緯・原因・年号・関連づけを、推測や事前知識から書き足したり取り違えたりしてはならない。

            【各フィールドの定義】
            - front（表面）: 暗記・想起の対象となる固有名詞・専門用語・制度名・中核概念（名詞句）。
              ・双方向（リバースモード）成立の原則：frontを見てbackが言えるだけでなく、逆にback（定義文）だけを読んだときにも正解のfrontがただ1つに一意に定まる用語にすること（節見出しになっている重要な固有名詞や制度名も、1対1で定義できるなら積極的に抽出してよい）。
              ・単なる一般名詞（例：「農業」「データベース」「法律」）や、抽象的な見出し句・状況ラベル（例：「〇〇の課題」「近年の動向」「一般的な分類」）は、裏面から一意に答えられないためfrontにしてはならない。
              ・疑問文（「〜とは」「なぜ〜か」）や文章にはしない。
            - back（裏面）: frontが何であるかを一目で想起できる核となる定義または要点。
              ・文体は「体言止め（名詞止め）」または簡潔な1文（常体）に統一する。
              ・記事固有の細かいエピソードは含めず、本文の記述に忠実な本質的定義・役割に絞る。
            - userSupplement（補足メモ）: 記事内でその用語が登場した具体的な文脈、背景エピソード、根拠データ、筆者の主張との関わり（1〜3文）。
              ・本文に明記された事実・数値・年号のみを正確に記述し、本文にない事実や推測は絶対に補完しない。
              ・複数テーマ間で文体がブレないよう、文末はすべて「常体（〜である・〜した）」に統一し、敬体（です・ます調）は使用しない。

            $modeCriteria$scopeRule

            【出力例（Good）】
            - front: "機会費用"
              back: "ある選択を行ったことで断念せざるを得なかった他の選択肢から得られたはずの最大利益。"
              userSupplement: "限られた予算や時間をどこに投じるかを判断する際、目に見える支出だけでなく機会費用を考慮する必要があると論じられている。"
            - front: "墾田永年私財法"
              back: "743年に発布された、開墾した土地の永代私有を認める法令。"
              userSupplement: "班田収授法の維持が困難になる中で、貴族や寺社による大規模な土地開墾と荘園の発達を促す契機となった。"

            【避けるべき例（Bad）】
            - front: "農業" / "法律"（※一般名詞単体になっており、他デッキと混ざった際にどの固有の概念・制度か判別できない。記事固有の固有名詞や専門用語を抽出すること）
            - front: "日本の農業の課題" / "近年の動向" / "一般的な分類"（※1対1の用語ではなくテーマ句・状況ラベルになっており、何を答えるべきか定まらずリバースモードでも破綻する）
            - front: "なぜインフレが起きると金利が上がるのか？"（※疑問文・長文になっている）
            - back: "記事によると中央銀行は物価上昇を抑えるために政策金利を引き上げて景気を冷やしています。"（※定義ではなく長文の経過説明になっており、敬体が混ざっている）
        """.trimIndent()
    }

    internal fun buildThemeCardExtractionPrompt(
        title: String,
        url: String,
        content: String,
        theme: String,
        mode: CardExtractionMode,
        otherThemes: List<String> = emptyList(),
        alreadyExtractedFronts: List<String> = emptyList(),
    ): String {
        val othersText = if (otherThemes.isNotEmpty()) otherThemes.joinToString(", ") else "なし"
        val alreadyText = if (alreadyExtractedFronts.isNotEmpty()) alreadyExtractedFronts.joinToString(", ") else "なし"
        return """
            対象テーマ: $theme
            他テーマ一覧（これらに属する用語は今回の抽出対象外）: $othersText
            抽出済みの用語（重複・同義出力禁止）: $alreadyText
            抽出モード: ${mode.label}
            タイトル: $title
            URL: $url

            本文:
            $content
        """.trimIndent()
    }

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

    internal fun parseSmartAnalysisFromGeminiResponse(responseJson: String): SmartArticleAnalysis {
        val fallback = SmartArticleAnalysis(
            resolvedMode = CardExtractionMode.KNOWLEDGE,
            themes = emptyList(),
        )
        val root = runCatching { json.parseToJsonElement(responseJson).jsonObject }.getOrNull() ?: return fallback
        val candidates = root["candidates"]?.jsonArray ?: return fallback
        if (candidates.isEmpty()) return fallback

        val firstCandidate = candidates[0].jsonObject
        val contentObj = firstCandidate["content"]?.jsonObject ?: return fallback
        val parts = contentObj["parts"]?.jsonArray ?: return fallback
        if (parts.isEmpty()) return fallback

        val rawText = parts[0].jsonObject["text"]?.jsonPrimitive?.content ?: ""
        val cleaned = cleanJsonCodeFence(rawText)
        val parsed = runCatching { json.parseToJsonElement(cleaned) }.getOrNull() ?: return fallback

        return when (parsed) {
            is JsonObject -> {
                val modeStr = runCatching { parsed["recommendedMode"]?.jsonPrimitive?.content?.trim()?.uppercase() }.getOrNull()
                val resolvedMode = when (modeStr) {
                    "CORE" -> CardExtractionMode.CORE
                    "KNOWLEDGE" -> CardExtractionMode.KNOWLEDGE
                    else -> CardExtractionMode.KNOWLEDGE
                }
                val themes = parsed["themes"]?.jsonArray?.mapNotNull {
                    val str = runCatching { it.jsonPrimitive.content.trim() }.getOrNull()
                    if (!str.isNullOrBlank()) str else null
                }.orEmpty()
                SmartArticleAnalysis(resolvedMode = resolvedMode, themes = themes)
            }
            is JsonArray -> {
                val themes = parsed.mapNotNull {
                    val str = runCatching { it.jsonPrimitive.content.trim() }.getOrNull()
                    if (!str.isNullOrBlank()) str else null
                }
                SmartArticleAnalysis(resolvedMode = CardExtractionMode.KNOWLEDGE, themes = themes)
            }
            else -> fallback
        }
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
            a.userSupplement.length >= 45 && b.userSupplement.length >= 45 ->
                if (b.userSupplement.length > a.userSupplement.length) b.userSupplement else a.userSupplement
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
