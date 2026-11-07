package com.aidomari.app.domain.model

import java.time.LocalDate

enum class PastDayDeckType(
    val idPrefix: String,
    val title: String,
) {
    YESTERDAY(
        idPrefix = "past_yesterday_",
        title = "昨日のカード",
    ),
    WEEK(
        idPrefix = "past_week_",
        title = "先週のカード",
    ),
    MONTH(
        idPrefix = "past_month_",
        title = "先月のカード",
    ),
    ;

    companion object {
        fun fromDeckId(deckId: String): Pair<PastDayDeckType, String>? {
            return entries.firstOrNull { deckId.startsWith(it.idPrefix) }?.let {
                it to deckId.removePrefix(it.idPrefix)
            }
        }
    }
}

data class PastDayGroup(
    val type: PastDayDeckType,
    val targetDate: LocalDate,
    val daysAgo: Long,
    val cards: List<Card>,
)
