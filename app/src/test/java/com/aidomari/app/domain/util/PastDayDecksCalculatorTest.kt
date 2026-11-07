package com.aidomari.app.domain.util

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.PastDayDeckType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PastDayDecksCalculatorTest {

    private val zoneId = ZoneId.of("Asia/Tokyo")
    private val today = LocalDate.of(2026, 11, 7)

    private fun createCard(id: String): Card {
        return Card(
            id = id,
            deckId = "deck-1",
            front = "front-$id",
            back = "back-$id",
        )
    }

    private fun epochMillisFor(date: LocalDate, hour: Int = 12): Long {
        return date.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    @Test
    fun calculate_emptyInput_returnsEmptyMap() {
        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = emptyList(),
            today = today,
            zoneId = zoneId,
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun calculate_todayAndIntermediateDays_ignored() {
        val input = listOf(
            createCard("card-today") to epochMillisFor(today),
            createCard("card-2days") to epochMillisFor(today.minusDays(2)),
            createCard("card-5days") to epochMillisFor(today.minusDays(5)),
            createCard("card-6days") to epochMillisFor(today.minusDays(6)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun calculate_yesterdayCard_extractedCorrectly() {
        val card1 = createCard("card-y1")
        val card2 = createCard("card-y2")
        val input = listOf(
            card1 to epochMillisFor(today.minusDays(1), 9),
            card2 to epochMillisFor(today.minusDays(1), 22),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        assertEquals(1, result.size)
        val yesterdayGroup = result[PastDayDeckType.YESTERDAY]
        assertNotNull(yesterdayGroup)
        assertEquals(today.minusDays(1), yesterdayGroup?.targetDate)
        assertEquals(1L, yesterdayGroup?.daysAgo)
        assertEquals(2, yesterdayGroup?.cards?.size)
    }

    @Test
    fun calculate_weekCard_exact7DaysAgo() {
        val card = createCard("card-week-7")
        val input = listOf(
            card to epochMillisFor(today.minusDays(7)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        val weekGroup = result[PastDayDeckType.WEEK]
        assertNotNull(weekGroup)
        assertEquals(today.minusDays(7), weekGroup?.targetDate)
        assertEquals(7L, weekGroup?.daysAgo)
        assertEquals(listOf(card), weekGroup?.cards)
    }

    @Test
    fun calculate_weekCard_fallbackToEarlierDayWithinRange() {
        // 7日前がなく、9日前に学習があるケース
        val card9 = createCard("card-week-9")
        val card15 = createCard("card-week-15")
        val input = listOf(
            card9 to epochMillisFor(today.minusDays(9)),
            card15 to epochMillisFor(today.minusDays(15)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        val weekGroup = result[PastDayDeckType.WEEK]
        assertNotNull(weekGroup)
        assertEquals(today.minusDays(9), weekGroup?.targetDate)
        assertEquals(9L, weekGroup?.daysAgo)
        assertEquals(listOf(card9), weekGroup?.cards)
    }

    @Test
    fun calculate_weekCard_boundaryAt29Days() {
        val card29 = createCard("card-week-29")
        val input = listOf(
            card29 to epochMillisFor(today.minusDays(29)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        val weekGroup = result[PastDayDeckType.WEEK]
        assertNotNull(weekGroup)
        assertEquals(today.minusDays(29), weekGroup?.targetDate)
        assertEquals(29L, weekGroup?.daysAgo)
        assertEquals(listOf(card29), weekGroup?.cards)
    }

    @Test
    fun calculate_monthCard_exact30DaysAgo() {
        val card = createCard("card-month-30")
        val input = listOf(
            card to epochMillisFor(today.minusDays(30)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        val monthGroup = result[PastDayDeckType.MONTH]
        assertNotNull(monthGroup)
        assertEquals(today.minusDays(30), monthGroup?.targetDate)
        assertEquals(30L, monthGroup?.daysAgo)
        assertEquals(listOf(card), monthGroup?.cards)
        assertNull(result[PastDayDeckType.WEEK])
    }

    @Test
    fun calculate_monthCard_fallbackToClosestEarlierDay() {
        // 30日前はなく、35日前と45日前がある場合、30日前に最も近い35日前が選ばれる
        val card35 = createCard("card-month-35")
        val card45 = createCard("card-month-45")
        val input = listOf(
            card35 to epochMillisFor(today.minusDays(35)),
            card45 to epochMillisFor(today.minusDays(45)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        val monthGroup = result[PastDayDeckType.MONTH]
        assertNotNull(monthGroup)
        assertEquals(today.minusDays(35), monthGroup?.targetDate)
        assertEquals(35L, monthGroup?.daysAgo)
        assertEquals(listOf(card35), monthGroup?.cards)
    }

    @Test
    fun calculate_allThreePastDayDecksPresent() {
        val cardYesterday = createCard("card-y")
        val cardWeek = createCard("card-w")
        val cardMonth = createCard("card-m")

        val input = listOf(
            cardYesterday to epochMillisFor(today.minusDays(1)),
            cardWeek to epochMillisFor(today.minusDays(8)),
            cardMonth to epochMillisFor(today.minusDays(40)),
        )

        val result = PastDayDecksCalculator.calculate(
            cardsWithLatestStudy = input,
            today = today,
            zoneId = zoneId,
        )

        assertEquals(3, result.size)
        assertEquals(1L, result[PastDayDeckType.YESTERDAY]?.daysAgo)
        assertEquals(8L, result[PastDayDeckType.WEEK]?.daysAgo)
        assertEquals(40L, result[PastDayDeckType.MONTH]?.daysAgo)
    }
}
