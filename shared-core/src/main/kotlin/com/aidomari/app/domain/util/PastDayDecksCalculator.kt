package com.aidomari.app.domain.util

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.PastDayDeckType
import com.aidomari.app.domain.model.PastDayGroup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object PastDayDecksCalculator {

    fun calculate(
        cardsWithLatestStudy: List<Pair<Card, Long>>,
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Map<PastDayDeckType, PastDayGroup> {
        if (cardsWithLatestStudy.isEmpty()) return emptyMap()

        // カードごとの最新学習日時（エポックミリ秒）を端末タイムゾーンの日付に変換してグルーピング
        val cardsByDate: Map<LocalDate, List<Card>> = cardsWithLatestStudy
            .groupBy { (_, timestamp) ->
                Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
            }
            .mapValues { entry -> entry.value.map { it.first } }

        val result = mutableMapOf<PastDayDeckType, PastDayGroup>()

        // 1. 昨日のカード: 1日前 (today.minusDays(1))
        val yesterday = today.minusDays(1)
        val yesterdayCards = cardsByDate[yesterday]
        if (!yesterdayCards.isNullOrEmpty()) {
            result[PastDayDeckType.YESTERDAY] = PastDayGroup(
                type = PastDayDeckType.YESTERDAY,
                targetDate = yesterday,
                daysAgo = 1,
                cards = yesterdayCards,
            )
        }

        // 2. 先週のカード: 7日前から29日前まで遡り、最初に見つかった日のカード
        for (days in 7L..29L) {
            val date = today.minusDays(days)
            val weekCards = cardsByDate[date]
            if (!weekCards.isNullOrEmpty()) {
                result[PastDayDeckType.WEEK] = PastDayGroup(
                    type = PastDayDeckType.WEEK,
                    targetDate = date,
                    daysAgo = days,
                    cards = weekCards,
                )
                break
            }
        }

        // 3. 先月のカード: 30日前以降で、最新の日付（30日に最も近い日）のカード
        val monthThreshold = today.minusDays(30)
        val monthCardsEntry = cardsByDate
            .filter { (date, cards) -> date <= monthThreshold && cards.isNotEmpty() }
            .maxByOrNull { it.key }

        if (monthCardsEntry != null) {
            val days = ChronoUnit.DAYS.between(monthCardsEntry.key, today)
            result[PastDayDeckType.MONTH] = PastDayGroup(
                type = PastDayDeckType.MONTH,
                targetDate = monthCardsEntry.key,
                daysAgo = days,
                cards = monthCardsEntry.value,
            )
        }

        return result
    }
}
