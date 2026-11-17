package com.aidomari.app.domain.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot

/**
 * Resolved progress state along the authoritative Act 1 travel route.
 */
data class Act1RouteProgress(
    val totalDistanceMeters: Float,
    val currentLegIndex: Int,
    val currentWorldOffset: Offset,
    val currentAnchorIndex: Int,
    val currentAnchor: RouteAnchor,
    val visitedAnchorIds: Set<String>,
    val isAct1Finished: Boolean,
    val progressRatioInCurrentLeg: Float,
)

/**
 * High-precision resolver mapping continuous walking distance (meters)
 * to exact world coordinates along the Act 1 road network and anchor unlocks.
 */
object Act1TravelRouteProgressCalculator {

    /**
     * Resolves traveler position and anchor status given cumulative walking meters.
     */
    fun calculateProgress(
        totalDistanceMeters: Float,
        legs: List<Act1RouteLeg>,
        anchors: List<RouteAnchor> = HokkaidoAct1Route.anchors,
    ): Act1RouteProgress {
        val safeAnchors = if (anchors.isNotEmpty()) anchors else HokkaidoAct1Route.anchors
        val firstAnchor = safeAnchors.first()

        if (legs.isEmpty() || safeAnchors.isEmpty()) {
            return Act1RouteProgress(
                totalDistanceMeters = totalDistanceMeters,
                currentLegIndex = 0,
                currentWorldOffset = Offset(firstAnchor.x, firstAnchor.y),
                currentAnchorIndex = 0,
                currentAnchor = firstAnchor,
                visitedAnchorIds = setOf(firstAnchor.id),
                isAct1Finished = false,
                progressRatioInCurrentLeg = 0f,
            )
        }

        val clampedDistance = totalDistanceMeters.coerceAtLeast(0.0f)
        var accumulated = 0.0f

        for (i in legs.indices) {
            val leg = legs[i]
            val legDist = leg.distanceMeters.coerceAtLeast(0.0f)

            if (accumulated + legDist > clampedDistance) {
                val rem = clampedDistance - accumulated
                val ratio = if (legDist > 0.0f) (rem / legDist).coerceIn(0.0f, 1.0f) else 0.0f
                val fromAnchor = safeAnchors.getOrNull(i) ?: firstAnchor
                val offset = interpolateOffset(
                    waypoints = leg.waypoints,
                    ratio = ratio,
                    fallback = Offset(fromAnchor.x, fromAnchor.y),
                )
                val visited = safeAnchors.subList(0, (i + 1).coerceAtMost(safeAnchors.size))
                    .map { it.id }
                    .toSet()

                return Act1RouteProgress(
                    totalDistanceMeters = clampedDistance,
                    currentLegIndex = i,
                    currentWorldOffset = offset,
                    currentAnchorIndex = i,
                    currentAnchor = fromAnchor,
                    visitedAnchorIds = visited,
                    isAct1Finished = false,
                    progressRatioInCurrentLeg = ratio,
                )
            }
            accumulated += legDist
        }

        // Traversed all legs - arrived at destination
        val lastAnchor = safeAnchors.last()
        val lastLeg = legs.last()
        val finalOffset = lastLeg.waypoints.lastOrNull() ?: Offset(lastAnchor.x, lastAnchor.y)
        val allVisited = safeAnchors.map { it.id }.toSet()

        return Act1RouteProgress(
            totalDistanceMeters = clampedDistance,
            currentLegIndex = legs.lastIndex,
            currentWorldOffset = finalOffset,
            currentAnchorIndex = safeAnchors.lastIndex,
            currentAnchor = lastAnchor,
            visitedAnchorIds = allVisited,
            isAct1Finished = true,
            progressRatioInCurrentLeg = 1.0f,
        )
    }

    /**
     * Linearly interpolates a position along a polyline of waypoints given a ratio in [0, 1].
     */
    fun interpolateOffset(
        waypoints: List<Offset>,
        ratio: Float,
        fallback: Offset,
    ): Offset {
        if (waypoints.isEmpty()) return fallback
        if (waypoints.size == 1) return waypoints[0]

        val r = ratio.coerceIn(0.0f, 1.0f)
        if (r <= 0.0f) return waypoints.first()
        if (r >= 1.0f) return waypoints.last()

        val segmentLengths = FloatArray(waypoints.size - 1)
        var totalLength = 0.0f
        for (i in 0 until waypoints.size - 1) {
            val dx = waypoints[i + 1].x - waypoints[i].x
            val dy = waypoints[i + 1].y - waypoints[i].y
            val len = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            segmentLengths[i] = len
            totalLength += len
        }

        if (totalLength <= 1e-6f) return waypoints.first()

        val targetDistance = r * totalLength
        var accumulated = 0.0f
        for (i in segmentLengths.indices) {
            val segLen = segmentLengths[i]
            if (accumulated + segLen >= targetDistance || i == segmentLengths.lastIndex) {
                val localRatio = if (segLen > 1e-6f) {
                    ((targetDistance - accumulated) / segLen).coerceIn(0.0f, 1.0f)
                } else {
                    0.0f
                }
                val p0 = waypoints[i]
                val p1 = waypoints[i + 1]
                return Offset(
                    x = p0.x + localRatio * (p1.x - p0.x),
                    y = p0.y + localRatio * (p1.y - p0.y),
                )
            }
            accumulated += segLen
        }

        return waypoints.last()
    }
}
