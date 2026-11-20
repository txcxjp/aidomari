package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.aidomari.app.domain.model.HighwayCategory
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
    fun `landmark styles match user directions across Goryokaku and Chapter 2 wild nature`() {
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.GORYOKAKU_CHERRY.motionStyle)
        assertEquals(LandmarkMotionStyle.SAKURA_PETALS, RoadsideLandmarkType.CHERRY_ROW.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.MORNING_MARKET.motionStyle)

        assertEquals(LandmarkMotionStyle.WILDLIFE_WALK_2FRAME, RoadsideLandmarkType.WALKING_FOX.motionStyle)
        assertEquals(LandmarkMotionStyle.SHINKANSEN_GLIDE, RoadsideLandmarkType.HOKKAIDO_SHINKANSEN.motionStyle)
        assertEquals(LandmarkMotionStyle.CAMPFIRE_FLAME, RoadsideLandmarkType.HIGHLAND_CAMP.motionStyle)
        assertEquals(LandmarkMotionStyle.SWAN_GLIDE, RoadsideLandmarkType.ONUMA_SWAN_BOAT.motionStyle)
        assertEquals(LandmarkMotionStyle.BOAT_BOB, RoadsideLandmarkType.UCHIURA_FISHING_BOAT.motionStyle)
        assertEquals(LandmarkMotionStyle.ONSEN_STEAM, RoadsideLandmarkType.WOODEN_ONSEN_BATH.motionStyle)
        assertEquals(LandmarkMotionStyle.RIVER_SHIMMER, RoadsideLandmarkType.RURAL_ARCH_BRIDGE.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.HOLSTEIN_COWS.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.WHITE_BIRCH_GROVE.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.HAY_ROLLS_SILO.motionStyle)
        assertEquals(LandmarkMotionStyle.DEER_GRAZE, RoadsideLandmarkType.EZO_SHIKA_DEER.motionStyle)
        assertTrue(RoadsideLandmarkType.EZO_SHIKA_DEER.animFrame2DrawableResId != null)
        assertEquals(LandmarkMotionStyle.MOUNT_CLOUD_DRIFT, RoadsideLandmarkType.MOUNT_KOMAGATAKE.motionStyle)
        assertEquals(LandmarkMotionStyle.COASTAL_WAVE_CRASH, RoadsideLandmarkType.COASTAL_ROCKS_WAVES.motionStyle)

        assertFalse(RoadsideLandmark.ACT1_LANDMARKS.any { it.id == "goryokaku_waving_fox" })
        assertFalse(RoadsideLandmark.ACT1_LANDMARKS.any { it.id == "funkawan_yakumo_pasture" })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.HOLSTEIN_COWS })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.WALKING_FOX })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.LILAC_BLOSSOMS })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.HAMANASU_ROSES })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.LUPINE_WILDFLOWERS })
        assertTrue(RoadsideLandmark.ACT1_LANDMARKS.any { it.type == RoadsideLandmarkType.POTATO_BLOSSOMS })
        assertEquals(LandmarkMotionStyle.WIND_TURBINE_SPIN, RoadsideLandmarkType.WIND_TURBINE_PAIR.motionStyle)
        assertEquals(LandmarkMotionStyle.WIND_TURBINE_SPIN, RoadsideLandmarkType.OTONRUI_WIND_FARM.motionStyle)
        assertEquals(LandmarkMotionStyle.WHALE_SPOUT, RoadsideLandmarkType.WHALE_ORCA_SEA.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.EZO_SQUIRREL.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.EZO_SHIKA_GRAZING.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.REINDEER_HERD.motionStyle)
        assertEquals(LandmarkMotionStyle.RIVER_SHIMMER, RoadsideLandmarkType.CUTE_BROWN_BEAR.motionStyle)
        assertEquals(LandmarkMotionStyle.SWAN_GLIDE, RoadsideLandmarkType.LAKE_SWANS.motionStyle)
        assertEquals(LandmarkMotionStyle.COASTAL_WAVE_CRASH, RoadsideLandmarkType.COASTAL_CLIFF_ARCH.motionStyle)
        assertEquals(LandmarkMotionStyle.TRAM_CHUG, RoadsideLandmarkType.KIHA40_LOCAL_TRAIN.motionStyle)
    }

    @Test
    fun `ACT1_LANDMARKS strictly never reuses identical landmark drawable assets`() {
        val landmarks = RoadsideLandmark.ACT1_LANDMARKS
        assertEquals("Expected all 61 curated unique Hokkaido landmarks", 61, landmarks.size)
        assertEquals(
            "Duplicate RoadsideLandmarkType found in ACT1_LANDMARKS",
            landmarks.size,
            landmarks.map { it.type }.toSet().size,
        )
        assertEquals(
            "Duplicate drawableResId found in ACT1_LANDMARKS",
            landmarks.size,
            landmarks.map { it.type.drawableResId }.toSet().size,
        )
        val primaryIds = landmarks.map { it.type.drawableResId }.toSet()
        val secondaryIds = landmarks.mapNotNull { it.type.animFrame2DrawableResId }.toSet()
        assertTrue(
            "animFrame2DrawableResId must not collide with any primary drawableResId",
            primaryIds.intersect(secondaryIds).isEmpty(),
        )
    }

    @Test
    fun `deer 2-frame walk toggles stride frames without distorting static props`() {
        val samples = (0 until 40).map { i ->
            computeLandmarkMotion(
                style = LandmarkMotionStyle.DEER_GRAZE,
                phase = i / 40f,
                seedPhase = 0f,
            )
        }
        assertTrue(samples.any { it.useSecondFrame })
        assertTrue(samples.any { !it.useSecondFrame })
        assertTrue(samples.any { it.flipHorizontal })
        assertTrue(samples.any { !it.flipHorizontal })
    }

    @Test
    fun `computeEdgeEntryPatrolCenter places bird outside viewport moving inward`() {
        val viewport = Rect(left = 1000f, top = 500f, right = 1393f, bottom = 1351f)
        val density = 3f
        for (enterFromLeft in listOf(true, false)) {
            for (phase in listOf(0f, 0.25f, 0.55f, 0.85f)) {
                val placement = computeEdgeEntryPatrolCenter(
                    viewportWorld = viewport,
                    currentPhase = phase,
                    density = density,
                    zoomScale = 1.0f,
                    enterFromLeft = enterFromLeft,
                )
                val startEffPhase = (phase + placement.phaseOffset).mod(1f)
                val startBird = computeWorldUmigarasu(startEffPhase)
                val startX = placement.patrolCenterWorld.x + startBird.offsetXDp * density
                val startY = placement.patrolCenterWorld.y + startBird.offsetYDp * density

                assertTrue("Bird Y should align in upper viewport", startY in viewport.top..viewport.center.y)
                if (enterFromLeft) {
                    assertTrue("Left entry should start outside left edge: $startX vs ${viewport.left}", startX < viewport.left)
                    assertTrue("Left entry heading should point eastward: ${startBird.headingDeg}", abs(startBird.headingDeg) < 90f)
                } else {
                    assertTrue("Right entry should start outside right edge: $startX vs ${viewport.right}", startX > viewport.right)
                    assertTrue("Right entry heading should point westward: ${startBird.headingDeg}", abs(startBird.headingDeg) > 90f)
                }

                val nextBird = computeWorldUmigarasu((startEffPhase + 0.08f).mod(1f))
                val nextX = placement.patrolCenterWorld.x + nextBird.offsetXDp * density
                if (enterFromLeft) {
                    assertTrue("Left entry bird should advance rightward: $nextX > $startX", nextX > startX)
                } else {
                    assertTrue("Right entry bird should advance leftward: $nextX < $startX", nextX < startX)
                }
            }
        }
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
    fun `CachedHighwayPath intersects respects viewport bounds accurately`() {
        val viewport = Rect(left = 100f, top = 200f, right = 500f, bottom = 800f)
        fun seg(minX: Float, minY: Float, maxX: Float, maxY: Float) = CachedHighwayPath(
            category = HighwayCategory.NATIONAL_HIGHWAY,
            path = Path(),
            minX = minX,
            minY = minY,
            maxX = maxX,
            maxY = maxY,
        )

        assertTrue(seg(200f, 300f, 400f, 600f).intersects(viewport))
        assertTrue(seg(50f, 300f, 100f, 400f).intersects(viewport)) // touching left edge
        assertTrue(seg(450f, 750f, 700f, 900f).intersects(viewport)) // partial overlap bottom-right

        assertFalse(seg(0f, 300f, 99f, 500f).intersects(viewport))
        assertFalse(seg(501f, 300f, 700f, 500f).intersects(viewport))
        assertFalse(seg(200f, 0f, 400f, 199f).intersects(viewport))
        assertFalse(seg(200f, 801f, 400f, 1000f).intersects(viewport))
    }
}
