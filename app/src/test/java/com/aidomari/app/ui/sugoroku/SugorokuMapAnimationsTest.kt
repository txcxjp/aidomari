package com.aidomari.app.ui.sugoroku

import com.aidomari.app.domain.model.LandmarkMotionStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SugorokuMapAnimationsTest {

    @Test
    fun `computeLandmarkMotion wraps seamlessly between phase 0 and phase 1`() {
        val tol = 1e-4f
        for (style in LandmarkMotionStyle.values()) {
            for (seed in listOf(0f, 0.25f, 0.63f)) {
                val m0 = computeLandmarkMotion(style = style, phase = 0f, seedPhase = seed)
                val m1 = computeLandmarkMotion(style = style, phase = 1f, seedPhase = seed)
                assertEquals("tx wrap failed for $style", m0.translateXDp, m1.translateXDp, tol)
                assertEquals("ty wrap failed for $style", m0.translateYDp, m1.translateYDp, tol)
                assertEquals("rot wrap failed for $style", m0.rotationDeg, m1.rotationDeg, tol)
                assertEquals("sx wrap failed for $style", m0.scaleX, m1.scaleX, tol)
                assertEquals("sy wrap failed for $style", m0.scaleY, m1.scaleY, tol)
                assertEquals("frame wrap failed for $style", m0.useSecondFrame, m1.useSecondFrame)
            }
        }
    }

    @Test
    fun `grounded landmarks stay still at rest and pop on tap`() {
        val resting = computeLandmarkMotion(
            style = LandmarkMotionStyle.NONE,
            phase = 0.37f,
            seedPhase = 0.42f,
            tapPopProgress = 0f,
        )
        assertEquals(0f, resting.translateXDp, 1e-5f)
        assertEquals(0f, resting.translateYDp, 1e-5f)
        assertEquals(0f, resting.rotationDeg, 1e-5f)
        assertEquals(1f, resting.scaleX, 1e-5f)
        assertEquals(1f, resting.scaleY, 1e-5f)

        val popped = computeLandmarkMotion(
            style = LandmarkMotionStyle.NONE,
            phase = 0.37f,
            seedPhase = 0.42f,
            tapPopProgress = 0.5f,
        )
        assertTrue(popped.translateYDp < -5f)
        assertTrue(popped.scaleX > 1.08f)
        assertTrue(popped.scaleY > 1.08f)
    }

    @Test
    fun `waving dance toggles second frame across phase`() {
        val samples = (0 until 36).map { i ->
            computeLandmarkMotion(
                style = LandmarkMotionStyle.WAVE_DANCE_2FRAME,
                phase = i / 36f,
                seedPhase = 0.1f,
            ).useSecondFrame
        }
        assertTrue(samples.any { it })
        assertTrue(samples.any { !it })
    }

    @Test
    fun `birdFlapFrameIndex produces valid ping-pong wingbeat sequence`() {
        val seq = listOf(0, 1, 2, 3).map { step ->
            birdFlapFrameIndex(phase = (step + 0.1f) / 4f, cyclesPerLoop = 1)
        }
        assertEquals(listOf(0, 1, 2, 1), seq)

        for (i in 0..100) {
            val idx = birdFlapFrameIndex(phase = i / 100f)
            assertTrue("Index out of range: $idx", idx in 0..2)
        }
    }

    @Test
    fun `computeOverheadBirds has visible gliding birds at phase 0 and bounded trajectories`() {
        val initialBirds = computeOverheadBirds(phase = 0f)
        assertEquals(3, initialBirds.size)
        val visibleInitial = initialBirds.count {
            it.normX in 0.05f..0.95f && it.normY in 0.15f..0.85f
        }
        assertTrue("Expected >= 2 visible birds at phase 0, got $visibleInitial", visibleInitial >= 2)

        for (i in 0..40) {
            val birds = computeOverheadBirds(phase = i / 40f)
            for (bird in birds) {
                assertTrue(bird.flapFrame in 0..2)
                assertTrue(bird.normX in -0.2f..1.2f)
                assertTrue(bird.normY in 0.05f..0.95f)
                assertTrue(bird.sizeDp > 20f)
            }
        }
    }
}
