package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Offset
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.RoadsideLandmark
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.model.SugorokuProgress
import com.aidomari.app.domain.model.TravelerCompanion

data class SugorokuMapUiState(
    val progress: SugorokuProgress = SugorokuProgress(),
    val currentNode: SugorokuNode = HokkaidoAct1Route.startNode,
    val selectedNode: SugorokuNode? = null,
    val arrivalCelebrationNode: SugorokuNode? = null,
    val totalNodes: Int = HokkaidoAct1Route.totalNodes,
    val remainingSteps: Int = (HokkaidoAct1Route.totalNodes - 1).coerceAtLeast(0),
    val totalDistanceMeters: Float = 0.0f,
    val playerWorldOffset: Offset = Offset(currentNode.x, currentNode.y),
    val speedMultiplier: Float = 1.0f,
    val isAct1Finished: Boolean = false,
    val isLoading: Boolean = false,
    val isProgressLoaded: Boolean = true,
    val companion: TravelerCompanion = TravelerCompanion.DEFAULT,
    val roadsideLandmarks: List<RoadsideLandmark> = RoadsideLandmark.ACT1_LANDMARKS,
    val nearbyLandmark: RoadsideLandmark? = null,
)
