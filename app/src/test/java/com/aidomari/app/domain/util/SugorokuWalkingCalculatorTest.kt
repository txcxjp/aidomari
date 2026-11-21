package com.aidomari.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SugorokuWalkingCalculatorTest {

    @Test
    fun calculateDistanceMeters_zeroOrNegative_returnsZero() {
        assertEquals(0f, SugorokuWalkingCalculator.calculateDistanceMeters(0f), 0.001f)
        assertEquals(0f, SugorokuWalkingCalculator.calculateDistanceMeters(-5f), 0.001f)
    }

    @Test
    fun calculateDistanceMeters_normalDuration_calculatesCorrectDistance() {
        // 80m / 60s ≈ 1.3333334 m/s
        // 5 seconds => ~6.6667 m
        val d5 = SugorokuWalkingCalculator.calculateDistanceMeters(5f)
        assertEquals(5f * (80f / 60f), d5, 0.01f)

        // 15 seconds => 20.0 m
        val d15 = SugorokuWalkingCalculator.calculateDistanceMeters(15f)
        assertEquals(20.0f, d15, 0.01f)
    }

    @Test
    fun calculateDistanceMeters_capsAt20Seconds() {
        val d20 = SugorokuWalkingCalculator.calculateDistanceMeters(20f)
        val d60 = SugorokuWalkingCalculator.calculateDistanceMeters(60f)
        val d300 = SugorokuWalkingCalculator.calculateDistanceMeters(300f)

        assertEquals(20f * (80f / 60f), d20, 0.01f)
        assertEquals(d20, d60, 0.001f)
        assertEquals(d20, d300, 0.001f)
    }

    @Test
    fun calculateDistanceMeters_appliesSpeedMultiplier() {
        // 10 seconds * 1.3333 m/s = 13.333 m * 10x = 133.33 m
        val d10x = SugorokuWalkingCalculator.calculateDistanceMeters(10f, speedMultiplier = 10f)
        assertEquals(10f * (80f / 60f) * 10f, d10x, 0.1f)

        // 50x multiplier
        val d50x = SugorokuWalkingCalculator.calculateDistanceMeters(10f, speedMultiplier = 50f)
        assertEquals(10f * (80f / 60f) * 50f, d50x, 0.1f)
    }

    @Test
    fun formatDistance_formatsMetersAndKilometersCorrectly() {
        assertEquals("0.0 m", SugorokuWalkingCalculator.formatDistance(0f))
        assertEquals("13.3 m", SugorokuWalkingCalculator.formatDistance(13.333f))
        assertEquals("999.0 m", SugorokuWalkingCalculator.formatDistance(999.0f))
        assertEquals("1.00 km", SugorokuWalkingCalculator.formatDistance(1000f))
        assertEquals("42.50 km", SugorokuWalkingCalculator.formatDistance(42500f))
        assertEquals("1652.94 km", SugorokuWalkingCalculator.formatDistance(1652940f))
    }
}
