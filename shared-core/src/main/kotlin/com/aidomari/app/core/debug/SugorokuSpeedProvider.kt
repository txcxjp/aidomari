package com.aidomari.app.core.debug

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides a speed multiplier for study walking progress simulations.
 */
interface SugorokuSpeedProvider {
    val speedMultiplier: StateFlow<Float>
}
