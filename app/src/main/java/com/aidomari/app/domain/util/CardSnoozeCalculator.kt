package com.aidomari.app.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SnoozeResult(
    val snoozedUntil: Long,
    val newLevel: Int,
    val durationLabel: String,
) {
    val level: Int get() = newLevel
}

object CardSnoozeCalculator {

    private val dateFormatter = DateTimeFormatter.ofPattern("M月d日")

    fun calculateNextSnooze(
        currentLevel: Int,
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): SnoozeResult {
        val nextLevel = (currentLevel + 1).coerceAtLeast(1)
        val (daysToAdd, label) = when (nextLevel) {
            1 -> 1L to "明日まで"
            2 -> 7L to "1週間後まで"
            3 -> 14L to "2週間後まで"
            4 -> 30L to "1ヶ月後まで"
            else -> {
                val months = nextLevel - 3
                (30L * months) to "${months}ヶ月後まで"
            }
        }
        val targetDate = today.plusDays(daysToAdd)
        val endOfDay = targetDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
        return SnoozeResult(
            snoozedUntil = endOfDay,
            newLevel = nextLevel,
            durationLabel = label,
        )
    }

    fun formatSnoozedUntilDate(
        snoozedUntil: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = "${dateFormatter.format(Instant.ofEpochMilli(snoozedUntil).atZone(zoneId).toLocalDate())}まで"

    fun formatSnoozeUntil(
        snoozedUntil: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = formatSnoozedUntilDate(snoozedUntil, zoneId)
}
