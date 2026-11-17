package com.aidomari.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Act1TravelRouteProgressTest {

    @Test
    fun calculateProgress_zeroDistance_returnsStartAnchor() {
        val legs = Act1TravelRoute.getLegs()
        val anchors = HokkaidoAct1Route.anchors

        val progress = Act1TravelRouteProgressCalculator.calculateProgress(0f, legs, anchors)

        assertEquals(0, progress.currentAnchorIndex)
        assertEquals(0, progress.currentLegIndex)
        assertEquals("hakodateyama", progress.currentAnchor.id)
        assertEquals(setOf("hakodateyama"), progress.visitedAnchorIds)
        assertEquals(0f, progress.progressRatioInCurrentLeg, 0.001f)
        assertFalse(progress.isAct1Finished)
    }

    @Test
    fun calculateProgress_intermediateDistanceInLeg0_interpolatesOffset() {
        val legs = Act1TravelRoute.getLegs()
        val anchors = HokkaidoAct1Route.anchors
        val leg0 = legs[0] // hakodateyama -> monument_of_yanagawa_kumakichi (558.02m)

        val halfDist = leg0.distanceMeters * 0.5f
        val progress = Act1TravelRouteProgressCalculator.calculateProgress(halfDist, legs, anchors)

        assertEquals(0, progress.currentLegIndex)
        assertEquals(0, progress.currentAnchorIndex)
        assertEquals(0.5f, progress.progressRatioInCurrentLeg, 0.01f)
        assertEquals(setOf("hakodateyama"), progress.visitedAnchorIds)
        assertFalse(progress.isAct1Finished)

        // World offset should be strictly between waypoints
        assertTrue(progress.currentWorldOffset.x > 0f)
        assertTrue(progress.currentWorldOffset.y > 0f)
    }

    @Test
    fun calculateProgress_reachesNextAnchor_unlocksSecondAnchor() {
        val legs = Act1TravelRoute.getLegs()
        val anchors = HokkaidoAct1Route.anchors
        val leg0 = legs[0]

        // Exactly at or just past the end of Leg 0
        val distPastLeg0 = leg0.distanceMeters + 10f
        val progress = Act1TravelRouteProgressCalculator.calculateProgress(distPastLeg0, legs, anchors)

        assertEquals(1, progress.currentLegIndex)
        assertEquals(1, progress.currentAnchorIndex)
        assertEquals("monument_of_yanagawa_kumakichi", progress.currentAnchor.id)
        assertTrue(progress.visitedAnchorIds.contains("hakodateyama"))
        assertTrue(progress.visitedAnchorIds.contains("monument_of_yanagawa_kumakichi"))
        assertFalse(progress.isAct1Finished)
    }

    @Test
    fun calculateProgress_largeDistanceBeyondAllLegs_clampsToEnd() {
        val legs = Act1TravelRoute.getLegs()
        val anchors = HokkaidoAct1Route.anchors
        val totalDistance = legs.sumOf { it.distanceMeters.toDouble() }.toFloat()

        val progress = Act1TravelRouteProgressCalculator.calculateProgress(totalDistance + 5000f, legs, anchors)

        assertEquals(anchors.lastIndex, progress.currentAnchorIndex)
        assertEquals(legs.lastIndex, progress.currentLegIndex)
        assertEquals(anchors.last().id, progress.currentAnchor.id)
        assertEquals(anchors.size, progress.visitedAnchorIds.size)
        assertEquals(1.0f, progress.progressRatioInCurrentLeg, 0.001f)
        assertTrue(progress.isAct1Finished)
    }
}
