package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Offset
import com.aidomari.app.domain.model.LandmarkMotionStyle
import com.aidomari.app.domain.model.RoadsideLandmark
import com.aidomari.app.domain.model.RoadsideLandmarkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

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
                assertEquals("flip wrap failed for $style", m0.flipHorizontal, m1.flipHorizontal)
            }
        }
    }

    @Test
    fun `grounded landmarks stay still at rest and pop on tap`() {
        for (style in listOf(LandmarkMotionStyle.NONE, LandmarkMotionStyle.SAKURA_PETALS)) {
            val resting = computeLandmarkMotion(
                style = style,
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
                style = style,
                phase = 0.37f,
                seedPhase = 0.42f,
                tapPopProgress = 0.5f,
            )
            assertTrue(popped.translateYDp < -5f)
            assertTrue(popped.scaleX > 1.08f)
            assertTrue(popped.scaleY > 1.08f)
        }
    }

    @Test
    fun `landmark styles match user directions across Goryokaku and Chapter 2 scenery`() {
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.GORYOKAKU_CHERRY.motionStyle)
        assertEquals(LandmarkMotionStyle.SAKURA_PETALS, RoadsideLandmarkType.CHERRY_ROW.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.MORNING_MARKET.motionStyle)

        assertEquals(LandmarkMotionStyle.WILDLIFE_WALK_2FRAME, RoadsideLandmarkType.WALKING_FOX.motionStyle)
        assertEquals(LandmarkMotionStyle.SHINKANSEN_GLIDE, RoadsideLandmarkType.HOKKAIDO_SHINKANSEN.motionStyle)
        assertEquals(LandmarkMotionStyle.BOAT_BOB, RoadsideLandmarkType.ONUMA_SWAN_BOAT.motionStyle)
        assertEquals(LandmarkMotionStyle.BOAT_BOB, RoadsideLandmarkType.FERRY_SHIP.motionStyle)
        assertEquals(LandmarkMotionStyle.LIGHTHOUSE_BEAM, RoadsideLandmarkType.CAPE_LIGHTHOUSE.motionStyle)
        assertEquals(LandmarkMotionStyle.ONSEN_STEAM, RoadsideLandmarkType.ROTENBURO_ONSEN.motionStyle)
        assertEquals(LandmarkMotionStyle.ROPEWAY_SPARKLE, RoadsideLandmarkType.MOUNTAIN_ROPEWAY.motionStyle)

        // Fox must appear on mountain trails, not in urban Goryokaku Park
        assertFalse(RoadsideLandmark.ACT1_LANDMARKS.any { it.id == "goryokaku_waving_fox" })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.WALKING_FOX })
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
    fun `wildlife walk toggles stride frame and turns horizontally back and forth`() {
        val samples = (0 until 48).map { i ->
            computeLandmarkMotion(
                style = LandmarkMotionStyle.WILDLIFE_WALK_2FRAME,
                phase = i / 48f,
                seedPhase = 0f,
            )
        }
        assertTrue("Expected alternating walk frame true", samples.any { it.useSecondFrame })
        assertTrue("Expected alternating walk frame false", samples.any { !it.useSecondFrame })
        assertTrue("Expected rightward turn (flipHorizontal=false)", samples.any { !it.flipHorizontal })
        assertTrue("Expected leftward return (flipHorizontal=true)", samples.any { it.flipHorizontal })
        assertTrue("Expected horizontal patrol motion", samples.maxOf { it.translateXDp } > 10f)
        assertTrue("Expected horizontal return motion", samples.minOf { it.translateXDp } < -10f)
    }

    @Test
    fun `computeRopewayGondolaState glides seamlessly along cable bounds`() {
        val s0 = computeRopewayGondolaState(phase = 0f)
        val s1 = computeRopewayGondolaState(phase = 1f)
        assertEquals(0.5f, s0.cableProgress, 1e-4f)
        assertEquals(s0.cableProgress, s1.cableProgress, 1e-4f)
        assertEquals(s0.swingDeg, s1.swingDeg, 1e-4f)

        val progressSamples = (0..40).map { i ->
            val st = computeRopewayGondolaState(phase = i / 40f)
            assertTrue("cableProgress out of range: ${st.cableProgress}", st.cableProgress in 0f..1f)
            st.cableProgress
        }
        assertTrue("Expected gondola to reach upper end > 0.95", progressSamples.max() > 0.95f)
        assertTrue("Expected gondola to reach lower end < 0.05", progressSamples.min() < 0.05f)
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
    fun `computeWorldUmigarasu wraps seamlessly and orbits boundedly in world space`() {
        val b0 = computeWorldUmigarasu(phase = 0f)
        val b1 = computeWorldUmigarasu(phase = 1f)
        assertEquals(b0.offsetXDp, b1.offsetXDp, 1e-3f)
        assertEquals(b0.offsetYDp, b1.offsetYDp, 1e-3f)
        assertEquals(b0.headingDeg, b1.headingDeg, 1e-2f)
        assertEquals(b0.flapFrame, b1.flapFrame)

        val headings = (0..40).map { i ->
            val b = computeWorldUmigarasu(phase = i / 40f)
            assertTrue(b.flapFrame in 0..2)
            assertTrue("X offset out of patrol bounds: ${b.offsetXDp}", abs(b.offsetXDp) <= 170f)
            assertTrue("Y offset out of patrol bounds: ${b.offsetYDp}", abs(b.offsetYDp) <= 120f)
            assertTrue(b.sizeDp > 20f)
            b.headingDeg
        }
        assertTrue("Expected eastward headings", headings.any { abs(it) < 90f })
        assertTrue("Expected westward headings", headings.any { abs(it) > 90f })
    }

    @Test
    fun `computeParallaxBirdScreenPosition scales offset from screen center for 2_5D altitude depth`() {
        val screenCenter = Offset(500f, 1000f)

        val centered = computeParallaxBirdScreenPosition(
            groundScreenPos = screenCenter,
            screenCenter = screenCenter,
        )
        assertEquals(500f, centered.x, 1e-4f)
        assertEquals(1000f, centered.y, 1e-4f)

        val shiftedGround = Offset(600f, 800f) // +100x, -200y relative to center
        val shiftedBird = computeParallaxBirdScreenPosition(
            groundScreenPos = shiftedGround,
            screenCenter = screenCenter,
        )
        assertEquals(500f + 100f * UMIGARASU_ALTITUDE_PARALLAX, shiftedBird.x, 1e-3f)
        assertEquals(1000f - 200f * UMIGARASU_ALTITUDE_PARALLAX, shiftedBird.y, 1e-3f)
        assertTrue(UMIGARASU_ALTITUDE_PARALLAX > 1.0f)
    }
}
