package com.aidomari.app.domain.model

data class Deck(
    val id: String,
    val subjectId: String,
    val name: String,
    val splitSize: Int = 50,
    val autoContinue: Boolean = false,
    val shuffleMode: Boolean = false,
    val reverseMode: Boolean = false,
    val autoSpeak: Boolean = false,
    val speechRate: Float = 1.0f,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    /**
     * カード総数から自動分割された Part リストを生成
     */
    fun calculateParts(totalCards: Int): List<DeckPart> {
        if (totalCards <= 0) {
            return listOf(DeckPart(partIndex = 0, partNumber = 1, startIndex = 0, endIndex = 0, count = 0))
        }
        val safeSplitSize = if (splitSize > 0) splitSize else 50
        val parts = mutableListOf<DeckPart>()
        var start = 0
        var partIndex = 0

        while (start < totalCards) {
            val end = (start + safeSplitSize).coerceAtMost(totalCards)
            parts.add(
                DeckPart(
                    partIndex = partIndex,
                    partNumber = partIndex + 1,
                    startIndex = start,
                    endIndex = end,
                    count = end - start,
                ),
            )
            start = end
            partIndex++
        }
        return parts
    }
}

data class DeckPart(
    val partIndex: Int,
    val partNumber: Int,
    val startIndex: Int,
    val endIndex: Int,
    val count: Int,
) {
    val displayRange: String get() = if (count <= 0) "0枚" else "${startIndex + 1}〜$endIndex"
    val displayName: String get() = if (count <= 0) "Part $partNumber (0枚)" else "Part $partNumber ($displayRange)"
}
