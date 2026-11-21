package com.aidomari.app.ui.sugoroku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.core.debug.SugorokuDebugPreferences
import com.aidomari.app.domain.model.Act1TravelRoute
import com.aidomari.app.domain.model.Act1TravelRouteProgressCalculator
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.RoadsideLandmark
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SugorokuMapViewModel(
    private val repository: WordsRepository,
    private val sugorokuDebugPreferences: SugorokuDebugPreferences? = null,
) : ViewModel() {

    private var lastCelebratedAnchorIndex: Int? = null

    private val _uiState = MutableStateFlow(
        SugorokuMapUiState(
            isLoading = true,
            isProgressLoaded = false,
        ),
    )
    val uiState: StateFlow<SugorokuMapUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
        observeDebugPreferences()
    }

    private fun loadProgress() {
        _uiState.update { it.copy(isLoading = true) }
        val legs = Act1TravelRoute.getLegs()
        repository.getSugorokuProgress()
            .onEach { progress ->
                val routeProgress = Act1TravelRouteProgressCalculator.calculateProgress(
                    totalDistanceMeters = progress.totalDistanceMeters,
                    legs = legs,
                    anchors = HokkaidoAct1Route.anchors,
                )
                val node = HokkaidoAct1Route.getNodeByStepIndex(routeProgress.currentAnchorIndex)
                    ?: HokkaidoAct1Route.startNode
                val remaining = (HokkaidoAct1Route.totalNodes - 1 - routeProgress.currentAnchorIndex).coerceAtLeast(0)
                val nearby = RoadsideLandmark.ACT1_LANDMARKS.firstOrNull { landmark ->
                    val dx = routeProgress.currentWorldOffset.x - landmark.x
                    val dy = routeProgress.currentWorldOffset.y - landmark.y
                    dx * dx + dy * dy <= 2.25f
                }

                val isFirstEmission = (lastCelebratedAnchorIndex == null)
                val prevAnchorIndex = lastCelebratedAnchorIndex ?: routeProgress.currentAnchorIndex
                lastCelebratedAnchorIndex = routeProgress.currentAnchorIndex

                val newlyReachedNode = if (!isFirstEmission && routeProgress.currentAnchorIndex > prevAnchorIndex) {
                    node
                } else {
                    null
                }

                _uiState.update { current ->
                    current.copy(
                        progress = progress,
                        currentNode = node,
                        remainingSteps = remaining,
                        totalDistanceMeters = progress.totalDistanceMeters,
                        playerWorldOffset = routeProgress.currentWorldOffset,
                        isAct1Finished = routeProgress.isAct1Finished,
                        speedMultiplier = sugorokuDebugPreferences?.speedMultiplier?.value ?: 1.0f,
                        nearbyLandmark = nearby,
                        arrivalCelebrationNode = newlyReachedNode ?: current.arrivalCelebrationNode,
                        isLoading = false,
                        isProgressLoaded = true,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDebugPreferences() {
        sugorokuDebugPreferences?.speedMultiplier?.onEach { multiplier ->
            _uiState.update { it.copy(speedMultiplier = multiplier) }
        }?.launchIn(viewModelScope)
    }

    fun selectNode(node: SugorokuNode?) {
        _uiState.update { it.copy(selectedNode = node) }
    }

    fun clearSelectedNode() {
        selectNode(null)
    }

    fun triggerCelebration(node: SugorokuNode = _uiState.value.currentNode) {
        _uiState.update { it.copy(arrivalCelebrationNode = node) }
    }

    fun dismissCelebration() {
        _uiState.update { it.copy(arrivalCelebrationNode = null) }
    }

    fun advanceProgress(questionsDelta: Int = 1) {
        viewModelScope.launch {
            repository.recordSugorokuProgress(questionsDelta)
        }
    }

    fun addDistance(meters: Float) {
        viewModelScope.launch {
            repository.recordSugorokuDistance(meters)
        }
    }

    fun advanceToNextAnchor() {
        viewModelScope.launch {
            repository.advanceToNextAnchor()
        }
    }

    fun resetProgress() {
        lastCelebratedAnchorIndex = 0
        _uiState.update { it.copy(arrivalCelebrationNode = null) }
        viewModelScope.launch {
            repository.resetSugorokuProgress()
        }
    }

    fun setSpeedMultiplier(multiplier: Float) {
        sugorokuDebugPreferences?.setSpeedMultiplier(multiplier)
    }
}
