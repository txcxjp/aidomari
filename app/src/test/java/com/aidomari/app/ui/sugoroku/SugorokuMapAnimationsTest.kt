package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.aidomari.app.domain.model.Act1TravelRoute
import com.aidomari.app.domain.model.HighwayCategory
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.LandmarkMotionStyle
import com.aidomari.app.domain.model.RoadsideLandmark
import com.aidomari.app.domain.model.RoadsideLandmarkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

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
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.OTONRUI_WIND_FARM.motionStyle)
        assertEquals(LandmarkMotionStyle.WHALE_SPOUT, RoadsideLandmarkType.WHALE_ORCA_SEA.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.EZO_SQUIRREL.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.EZO_SHIKA_GRAZING.motionStyle)
        assertEquals(LandmarkMotionStyle.NONE, RoadsideLandmarkType.REINDEER_HERD.motionStyle)
        assertEquals(LandmarkMotionStyle.RIVER_SHIMMER, RoadsideLandmarkType.CUTE_BROWN_BEAR.motionStyle)
        assertEquals(LandmarkMotionStyle.SWAN_GLIDE, RoadsideLandmarkType.LAKE_SWANS.motionStyle)
        assertEquals(LandmarkMotionStyle.COASTAL_WAVE_CRASH, RoadsideLandmarkType.COASTAL_CLIFF_ARCH.motionStyle)
        assertEquals(LandmarkMotionStyle.TRAM_CHUG, RoadsideLandmarkType.KIHA40_LOCAL_TRAIN.motionStyle)
        assertEquals(LandmarkMotionStyle.FISHING_BOAT_LIGHTS, RoadsideLandmarkType.SUTTSU_SQUID_FISHING_BOAT.motionStyle)
        assertEquals(LandmarkMotionStyle.GAS_LAMP_GLOW, RoadsideLandmarkType.OTARU_CANAL_WAREHOUSES.motionStyle)
        assertEquals(LandmarkMotionStyle.ICEBREAKER_DRILL, RoadsideLandmarkType.MOMBETSU_GARINKO_ICEBREAKER.motionStyle)
        assertEquals(LandmarkMotionStyle.WIND_TURBINE_SPIN, RoadsideLandmarkType.TOBETSU_NORDIC_WINDMILL_HOUSE.motionStyle)
        assertEquals(LandmarkMotionStyle.LIGHTHOUSE_BEAM, RoadsideLandmarkType.BENKEI_CAPE_LIGHTHOUSE.motionStyle)
        assertEquals(LandmarkMotionStyle.LIGHTHOUSE_BEAM, RoadsideLandmarkType.ISHIKARI_RED_WHITE_LIGHTHOUSE.motionStyle)
        assertEquals(LandmarkMotionStyle.ONSEN_STEAM, RoadsideLandmarkType.TESHIO_SHIJIMI_BUCKET.motionStyle)
    }

    @Test
    fun `ACT1_LANDMARKS strictly never reuses identical landmark drawable assets`() {
        val landmarks = RoadsideLandmark.ACT1_LANDMARKS
        assertEquals("Expected all 261 curated unique Hokkaido landmarks", 261, landmarks.size)
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
        val previewIds = landmarks.mapNotNull { it.type.tapPreviewDrawableResId }.toSet()
        assertTrue(
            "animFrame2DrawableResId must not collide with any primary drawableResId",
            primaryIds.intersect(secondaryIds).isEmpty(),
        )
        assertTrue(
            "tapPreviewDrawableResId must not collide with any primary drawableResId",
            primaryIds.intersect(previewIds).isEmpty(),
        )
        assertEquals(
            "Duplicate tapPreviewDrawableResId found in ACT1_LANDMARKS",
            landmarks.count { it.type.tapPreviewDrawableResId != null },
            previewIds.size,
        )
    }

    @Test
    fun `ACT1_LANDMARKS bounding boxes do not overlap orange travel route at default zoom`() {
        val worldScale = HokkaidoBoardTrack.WORLD_SCALE
        val travelLegs = Act1TravelRoute.getLegs()
        assertTrue("Expected high-density Act1TravelRoute legs in test resources", travelLegs.isNotEmpty())

        val routePoints = buildList {
            for (leg in travelLegs) {
                val pts = leg.waypoints
                for (i in 0 until pts.lastIndex) {
                    val p0 = pts[i]
                    val p1 = pts[i + 1]
                    val steps = max(2, (hypot(p1.x - p0.x, p1.y - p0.y) * worldScale / 2f).toInt() + 1)
                    for (s in 0 until steps) {
                        val t = s / (steps - 1).toFloat()
                        add((p0.x + (p1.x - p0.x) * t) to (p0.y + (p1.y - p0.y) * t))
                    }
                }
            }
            val nodes = HokkaidoAct1Route.nodes
            for (i in 0 until nodes.lastIndex) {
                val n0 = nodes[i]
                val n1 = nodes[i + 1]
                val steps = max(2, (hypot(n1.x - n0.x, n1.y - n0.y) * worldScale / 2f).toInt() + 1)
                for (s in 0 until steps) {
                    val t = s / (steps - 1).toFloat()
                    add((n0.x + (n1.x - n0.x) * t) to (n0.y + (n1.y - n0.y) * t))
                }
            }
        }

        val violations = mutableListOf<String>()
        for (lm in RoadsideLandmark.ACT1_LANDMARKS) {
            val patrolX = when (lm.type.motionStyle) {
                LandmarkMotionStyle.WILDLIFE_WALK_2FRAME -> 16f
                LandmarkMotionStyle.DEER_GRAZE -> 11f
                LandmarkMotionStyle.SHINKANSEN_GLIDE -> 4.5f
                LandmarkMotionStyle.TRAM_CHUG -> 2.8f
                LandmarkMotionStyle.SWAN_GLIDE -> 2.0f
                else -> 0f
            }
            val patrolY = when (lm.type.motionStyle) {
                LandmarkMotionStyle.BOAT_BOB,
                LandmarkMotionStyle.WHALE_SPOUT,
                LandmarkMotionStyle.FISHING_BOAT_LIGHTS,
                LandmarkMotionStyle.ICEBREAKER_DRILL,
                -> 2.5f
                LandmarkMotionStyle.WAVE_DANCE_2FRAME -> 4.5f
                else -> 1.5f
            }
            val hw = (lm.type.baseDisplayWidthDp * 0.5f + patrolX) * lm.scale / worldScale
            val hh = (lm.type.baseDisplayHeightDp * 0.5f + patrolY) * lm.scale / worldScale

            var minDistDp = Float.POSITIVE_INFINITY
            for ((rx, ry) in routePoints) {
                if (abs(rx - lm.x) > 3.5f || abs(ry - lm.y) > 3.5f) continue
                val dx = max(0f, max((lm.x - hw) - rx, rx - (lm.x + hw))) * worldScale
                val dy = max(0f, max((lm.y - hh) - ry, ry - (lm.y + hh))) * worldScale
                val d = hypot(dx, dy)
                if (d < minDistDp) minDistDp = d
            }
            if (minDistDp < 14.0f) {
                violations += "${lm.id} (minDist=${"%.1f".format(minDistDp)}dp < 14.0dp)"
            }
        }
        assertTrue(
            "Landmarks overlapping orange TRAVEL_ROUTE (9dp half-width): $violations",
            violations.isEmpty(),
        )
    }

    @Test
    fun `fishing boat lights and icebreaker drill periodic motion wrap seamlessly at phase 0 and 1`() {
        for (style in listOf(LandmarkMotionStyle.FISHING_BOAT_LIGHTS, LandmarkMotionStyle.ICEBREAKER_DRILL)) {
            val start = computeLandmarkMotion(style = style, phase = 0f, seedPhase = 0.3f)
            val end = computeLandmarkMotion(style = style, phase = 1f, seedPhase = 0.3f)
            assertEquals("Expected seamless periodic X motion for $style", start.translateXDp, end.translateXDp, 0.001f)
            assertEquals("Expected seamless periodic Y motion for $style", start.translateYDp, end.translateYDp, 0.001f)
            assertEquals("Expected seamless periodic rotation for $style", start.rotationDeg, end.rotationDeg, 0.001f)
        }
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
