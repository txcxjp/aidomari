package com.aidomari.app.ui.sugoroku

import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.model.SugorokuProgress

data class SugorokuMapUiState(
    val progress: SugorokuProgress = SugorokuProgress(),
    val currentNode: SugorokuNode = HokkaidoAct1Route.startNode,
    val selectedNode: SugorokuNode? = null,
    val totalNodes: Int = HokkaidoAct1Route.totalNodes,
    val remainingSteps: Int = (HokkaidoAct1Route.totalNodes - 1).coerceAtLeast(0),
    val isLoading: Boolean = false,
)
