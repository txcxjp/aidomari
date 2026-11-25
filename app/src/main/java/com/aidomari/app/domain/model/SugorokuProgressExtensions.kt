package com.aidomari.app.domain.model

fun SugorokuProgress.advanceByDistance(
    distanceMetersDelta: Float,
    legs: List<Act1RouteLeg>,
    anchors: List<RouteAnchor> = HokkaidoAct1Route.anchors,
    questionsAnsweredDelta: Int = 0,
): SugorokuProgress {
    val newDistance = (totalDistanceMeters + distanceMetersDelta.coerceAtLeast(0.0f)).coerceAtLeast(0.0f)
    val routeProgress = Act1TravelRouteProgressCalculator.calculateProgress(newDistance, legs, anchors)
    return copy(
        currentStepIndex = routeProgress.currentAnchorIndex,
        totalDistanceMeters = newDistance,
        totalQuestionsAnswered = totalQuestionsAnswered + questionsAnsweredDelta,
        visitedNodeIds = routeProgress.visitedAnchorIds,
        updatedAt = System.currentTimeMillis(),
    )
}
