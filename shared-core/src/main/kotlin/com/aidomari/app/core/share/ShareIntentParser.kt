package com.aidomari.app.core.share

data class ParsedShareContent(
    val url: String?,
    val title: String,
    val content: String,
    val isUrlOnly: Boolean,
)

object ShareIntentParser {
    private val URL_REGEX = Regex("""https?://[^\s/$.?#].[^\s]*""", RegexOption.IGNORE_CASE)

    fun parse(extraText: CharSequence?, extraSubject: CharSequence? = null): ParsedShareContent {
        val rawText = extraText?.toString()?.trim() ?: ""
        val rawSubject = extraSubject?.toString()?.trim() ?: ""

        // Extract first URL from text or subject
        val match = URL_REGEX.find(rawText) ?: if (rawSubject.isNotBlank()) URL_REGEX.find(rawSubject) else null
        val url = match?.value?.trimEnd('.', ',', ')', '>', ']', '}', '"', '\'')

        val remainingText = if (url != null) {
            rawText.replace(url, "").trim()
        } else {
            rawText
        }

        // Determine title
        val title = when {
            rawSubject.isNotBlank() && rawSubject != url -> rawSubject
            remainingText.isNotBlank() && remainingText.lines().isNotEmpty() -> {
                val firstLine = remainingText.lines().first { it.isNotBlank() }.trim()
                if (firstLine.length <= 80 && remainingText.lines().count { it.isNotBlank() } > 1) {
                    firstLine
                } else if (remainingText.length <= 80) {
                    remainingText
                } else {
                    extractFallbackTitleFromUrl(url)
                }
            }
            url != null -> extractFallbackTitleFromUrl(url)
            else -> "共有テキスト"
        }

        val content = if (remainingText.isNotBlank()) remainingText else ""
        val isUrlOnly = url != null && content.isBlank()

        return ParsedShareContent(
            url = url,
            title = title,
            content = content,
            isUrlOnly = isUrlOnly,
        )
    }

    private fun extractFallbackTitleFromUrl(url: String?): String {
        if (url.isNullOrBlank()) return "共有記事"
        val domain = url.substringAfter("://").substringBefore("/")
        val path = url.substringAfter("://").substringAfter("/", "")
        val lastSegment = path.trimEnd('/').substringAfterLast('/')
        return if (lastSegment.isNotBlank() && !lastSegment.contains("index")) {
            runCatching { java.net.URLDecoder.decode(lastSegment, "UTF-8") }.getOrDefault(lastSegment)
        } else if (domain.isNotBlank()) {
            domain
        } else {
            "共有記事"
        }
    }
}
