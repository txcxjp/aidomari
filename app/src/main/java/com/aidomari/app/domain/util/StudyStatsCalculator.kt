package com.aidomari.app.domain.util

import com.aidomari.app.domain.model.CardStudyLog
import com.aidomari.app.domain.model.DailyStudyStats
import com.aidomari.app.domain.model.StudySummaryStats
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StudyStatsCalculator {
    fun calculateDailyStats(
        logs: List<CardStudyLog>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Map<LocalDate, DailyStudyStats> {
        if (logs.isEmpty()) return emptyMap()

        val sortedLogs = logs.sortedBy { it.studiedAt }

        // Find the date of the very first correct answer for each card
        val firstCorrectDateMap = mutableMapOf<String, LocalDate>()
        for (log in sortedLogs) {
            if (log.isCorrect && !firstCorrectDateMap.containsKey(log.cardId)) {
                val date = Instant.ofEpochMilli(log.studiedAt).atZone(zoneId).toLocalDate()
                firstCorrectDateMap[log.cardId] = date
            }
        }

        // Group logs by date
        val logsByDate = sortedLogs.groupBy {
            Instant.ofEpochMilli(it.studiedAt).atZone(zoneId).toLocalDate()
        }

        val resultMap = mutableMapOf<LocalDate, DailyStudyStats>()

        for ((date, dayLogs) in logsByDate) {
            val totalAnswers = dayLogs.size
            val correctAnswers = dayLogs.count { it.isCorrect }

            val cardsInDay = dayLogs.map { it.cardId }.distinct()
            val learnedCards = cardsInDay.filter { cardId ->
                firstCorrectDateMap[cardId] == date
            }.toSet()

            val learnedCount = learnedCards.size
            val reviewedCount = (cardsInDay - learnedCards).size

            resultMap[date] = DailyStudyStats(
                date = date,
                learnedCount = learnedCount,
                reviewedCount = reviewedCount,
                totalAnswers = totalAnswers,
                correctAnswers = correctAnswers,
            )
        }

        return resultMap
    }

    fun calculateSummaryStats(dailyStats: Collection<DailyStudyStats>): StudySummaryStats {
        if (dailyStats.isEmpty()) {
            return StudySummaryStats(
                studyDaysCount = 0,
                totalLearnedCount = 0,
                totalReviewedCount = 0,
                totalAnswers = 0,
                overallAccuracy = 0f,
            )
        }

        val studyDaysCount = dailyStats.count { it.totalAnswers > 0 }
        val totalLearnedCount = dailyStats.sumOf { it.learnedCount }
        val totalReviewedCount = dailyStats.sumOf { it.reviewedCount }
        val totalAnswers = dailyStats.sumOf { it.totalAnswers }
        val totalCorrect = dailyStats.sumOf { it.correctAnswers }
        val overallAccuracy = if (totalAnswers > 0) totalCorrect.toFloat() / totalAnswers else 0f

        return StudySummaryStats(
            studyDaysCount = studyDaysCount,
            totalLearnedCount = totalLearnedCount,
            totalReviewedCount = totalReviewedCount,
            totalAnswers = totalAnswers,
            overallAccuracy = overallAccuracy,
        )
    }
}
