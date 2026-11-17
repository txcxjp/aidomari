package com.aidomari.app.ui.sugoroku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.HokkaidoAct1Route
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(SugorokuMapUiState())
    val uiState: StateFlow<SugorokuMapUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        _uiState.update { it.copy(isLoading = true) }
        repository.getSugorokuProgress()
            .onEach { progress ->
                val node = HokkaidoAct1Route.getNodeByStepIndex(progress.currentStepIndex)
                    ?: HokkaidoAct1Route.startNode
                val remaining = (HokkaidoAct1Route.totalNodes - 1 - progress.currentStepIndex).coerceAtLeast(0)
                _uiState.update { current ->
                    current.copy(
                        progress = progress,
                        currentNode = node,
                        remainingSteps = remaining,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectNode(node: SugorokuNode?) {
        _uiState.update { it.copy(selectedNode = node) }
    }

    fun clearSelectedNode() {
        selectNode(null)
    }

    fun advanceProgress(questionsDelta: Int = 1) {
        viewModelScope.launch {
            repository.recordSugorokuProgress(questionsDelta)
        }
    }
}
