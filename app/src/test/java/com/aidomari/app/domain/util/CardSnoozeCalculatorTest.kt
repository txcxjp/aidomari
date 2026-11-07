package com.aidomari.app.domain.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class CardSnoozeCalculatorTest {

    private val zoneId = ZoneId.of("Asia/Tokyo")
    private val today = LocalDate.of(2026, 11, 7)

    @Test
    fun calculateNextSnooze_fromLevel0_returnsTomorrow() {
        val result = CardSnoozeCalculator.calculateNextSnooze(
            currentLevel = 0,
            today = today,
            zoneId = zoneId,
        )

        val expectedDate = today.plusDays(1)
        val expectedMillis = expectedDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(1, result.newLevel)
        assertEquals("明日まで", result.durationLabel)
        assertEquals(expectedMillis, result.snoozedUntil)
    }

    @Test
    fun calculateNextSnooze_fromLevel1_returns1Week() {
        val result = CardSnoozeCalculator.calculateNextSnooze(
            currentLevel = 1,
            today = today,
            zoneId = zoneId,
        )

        val expectedDate = today.plusDays(7)
        val expectedMillis = expectedDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(2, result.newLevel)
        assertEquals("1週間後まで", result.durationLabel)
        assertEquals(expectedMillis, result.snoozedUntil)
    }

    @Test
    fun calculateNextSnooze_fromLevel2_returns2Weeks() {
        val result = CardSnoozeCalculator.calculateNextSnooze(
            currentLevel = 2,
            today = today,
            zoneId = zoneId,
        )

        val expectedDate = today.plusDays(14)
        val expectedMillis = expectedDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(3, result.newLevel)
        assertEquals("2週間後まで", result.durationLabel)
        assertEquals(expectedMillis, result.snoozedUntil)
    }

    @Test
    fun calculateNextSnooze_fromLevel3_returns1Month() {
        val result = CardSnoozeCalculator.calculateNextSnooze(
            currentLevel = 3,
            today = today,
            zoneId = zoneId,
        )

        val expectedDate = today.plusDays(30)
        val expectedMillis = expectedDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals(4, result.newLevel)
        assertEquals("1ヶ月後まで", result.durationLabel)
        assertEquals(expectedMillis, result.snoozedUntil)
    }

    @Test
    fun formatSnoozedUntilDate_formatsCorrectly() {
        val targetDate = LocalDate.of(2026, 11, 8)
        val timestamp = targetDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val formatted = CardSnoozeCalculator.formatSnoozedUntilDate(timestamp, zoneId)
        assertEquals("11月8日まで", formatted)
    }
}
