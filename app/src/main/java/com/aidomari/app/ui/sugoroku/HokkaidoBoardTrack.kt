package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Offset
import com.aidomari.app.domain.model.HokkaidoAct1Route

/**
 * High-fidelity board game track aligned 1:1 with real Hokkaido geography and Wikipedia SVG silhouette.
 * Each node center is mapped directly from HokkaidoAct1Route coordinates multiplied by WORLD_SCALE.
 * With generous anchor spacing (closest pair is >114dp apart), 54dp square tiles never overlap.
 */
object HokkaidoBoardTrack {
    const val WORLD_SCALE = 180f
    const val TILE_SIZE_DP = 54f

    val tileCenters: List<Offset> by lazy {
        HokkaidoAct1Route.nodes.map { node ->
            Offset(node.x * WORLD_SCALE, node.y * WORLD_SCALE)
        }
    }
}
