package com.aidomari.app.domain.util

import com.aidomari.app.domain.model.CardStudyLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class StudyStatsCalculatorTest {

    private val zoneId = ZoneId.of("Asia/Tokyo")

    private fun dateToMillis(year: Int, month: Int, day: Int, hour: Int = 12): Long = ZonedDateTime.of(year, month, day, hour, 0, 0, 0, zoneId).toInstant().toEpochMilli()

    @Test
    fun calculateDailyStats_emptyLogs_returnsEmptyMap() {
        val stats = StudyStatsCalculator.calculateDailyStats(emptyList(), zoneId)
        assertEquals(0, stats.size)
    }

    @Test
    fun calculateDailyStats_firstTimeCorrect_countsAsLearned() {
        val day1 = dateToMillis(2026, 11, 1, 10)
        val logs = listOf(
            CardStudyLog(id = "log-1", cardId = "card-1", isCorrect = true, studiedAt = day1),
            CardStudyLog(id = "log-2", cardId = "card-2", isCorrect = true, studiedAt = day1),
        )

        val stats = StudyStatsCalculator.calculateDailyStats(logs, zoneId)
        val day1Stats = stats[LocalDate.of(2026, 11, 1)]!!

        assertEquals(2, day1Stats.learnedCount)
        assertEquals(0, day1Stats.reviewedCount)
        assertEquals(2, day1Stats.totalAnswers)
        assertEquals(2, day1Stats.correctAnswers)
        assertEquals(1.0f, day1Stats.accuracy)
    }

    @Test
    fun calculateDailyStats_reviewOnNextDay_countsAsReviewed() {
        val day1 = dateToMillis(2026, 11, 1, 10)
        val day2 = dateToMillis(2026, 11, 2, 10)
        val logs = listOf(
            // Day 1: card-1 first correct
            CardStudyLog(id = "log-1", cardId = "card-1", isCorrect = true, studiedAt = day1),
            // Day 2: card-1 reviewed (correct) & card-2 first correct
            CardStudyLog(id = "log-2", cardId = "card-1", isCorrect = true, studiedAt = day2),
            CardStudyLog(id = "log-3", cardId = "card-2", isCorrect = true, studiedAt = day2),
        )

        val stats = StudyStatsCalculator.calculateDailyStats(logs, zoneId)
        val day2Stats = stats[LocalDate.of(2026, 11, 2)]!!

        assertEquals(1, day2Stats.learnedCount) // card-2 only
        assertEquals(1, day2Stats.reviewedCount) // card-1
        assertEquals(2, day2Stats.totalAnswers)
        assertEquals(2, day2Stats.correctAnswers)
    }

    @Test
    fun calculateDailyStats_incorrectThenCorrectSameDay_countsAsLearnedOnce() {
        val day1Time1 = dateToMillis(2026, 11, 1, 10)
        val day1Time2 = dateToMillis(2026, 11, 1, 11)
        val logs = listOf(
            CardStudyLog(id = "log-1", cardId = "card-1", isCorrect = false, studiedAt = day1Time1),
            CardStudyLog(id = "log-2", cardId = "card-1", isCorrect = true, studiedAt = day1Time2),
        )

        val stats = StudyStatsCalculator.calculateDailyStats(logs, zoneId)
        val day1Stats = stats[LocalDate.of(2026, 11, 1)]!!

        assertEquals(1, day1Stats.learnedCount)
        assertEquals(0, day1Stats.reviewedCount)
        assertEquals(2, day1Stats.totalAnswers)
        assertEquals(1, day1Stats.correctAnswers)
        assertEquals(0.5f, day1Stats.accuracy)
    }

    @Test
    fun calculateSummaryStats_calculatesAccurately() {
        val day1 = dateToMillis(2026, 11, 1)
        val day2 = dateToMillis(2026, 11, 3)
        val logs = listOf(
            CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = day1),
            CardStudyLog(id = "l2", cardId = "c2", isCorrect = false, studiedAt = day1),
            CardStudyLog(id = "l3", cardId = "c1", isCorrect = true, studiedAt = day2),
        )

        val dailyStats = StudyStatsCalculator.calculateDailyStats(logs, zoneId)
        val summary = StudyStatsCalculator.calculateSummaryStats(dailyStats.values)

        assertEquals(2, summary.studyDaysCount)
        assertEquals(1, summary.totalLearnedCount) // c1 on day 1
        assertEquals(2, summary.totalReviewedCount) // c2 on day 1, c1 on day 2
        assertEquals(3, summary.totalAnswers)
        assertEquals(2.0f / 3.0f, summary.overallAccuracy, 0.001f)
    }
}
