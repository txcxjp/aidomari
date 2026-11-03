package com.aidomari.app.core.tts

object TtsTextSanitizer {
    private val rubyRegex = Regex("""\[([^|\]]+)\|([^\]]+)\]""")
    private val boldRegex = Regex("""\*\*([^*]+)\*\*""")
    private val italicRegex = Regex("""\*([^*]+)\*""")
    private val hashtagRegex = Regex("""#\S+""")
    private val multipleWhitespaceRegex = Regex("""\s+""")

    /**
     * Cleans text for Text-To-Speech:
     * - Replaces ruby notation [漢字|かんじ] with the phonetic reading "かんじ"
     * - Strips Markdown bold (**) and italic (*) syntax
     * - Removes hashtags (#tag)
     * - Normalizes whitespaces and trims
     */
    fun sanitize(text: String): String {
        if (text.isBlank()) return ""

        var result = rubyRegex.replace(text) { matchResult ->
            matchResult.groupValues[2]
        }
        result = boldRegex.replace(result, "$1")
        result = italicRegex.replace(result, "$1")
        result = hashtagRegex.replace(result, "")
        result = multipleWhitespaceRegex.replace(result, " ")

        return result.trim()
    }
}
