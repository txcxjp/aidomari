package com.aidomari.app.ui.sugoroku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.HokkaidoRoadNetwork

/**
 * High-fidelity board game track aligned 1:1 with real Hokkaido geography and Wikipedia SVG silhouette.
 * Each node center is mapped directly from HokkaidoAct1Route coordinates multiplied by WORLD_SCALE.
 * With an average step distance of ~0.331 SVG units * 180dp = ~59.6dp, 46dp square tiles never overlap.
 */
object HokkaidoBoardTrack {
    const val WORLD_SCALE = 180f
    const val TILE_SIZE_DP = 46f

    val tileCenters: List<Offset> by lazy {
        HokkaidoAct1Route.nodes.map { node ->
            Offset(node.x * WORLD_SCALE, node.y * WORLD_SCALE)
        }
    }
}

/**
 * High-fidelity silhouette of Hokkaido traced directly from official Wikipedia SVG:
 * https://upload.wikimedia.org/wikipedia/commons/8/81/Map_of_Hokkaido_Prefecture_Ja.svg
 * ViewBox: width = 612.0, height = 387.0
 */
@Composable
fun HokkaidoMiniMap(
    currentStepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    // Parse SVG paths directly from Wikipedia standard SVG
    val donanPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.PATH3039_D).toPath() }
    val dohokuPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.PATH3041_D).toPath() }
    val doouPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.PATH3043_D).toPath() }
    val dotoPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.PATH3047_D).toPath() }
    val okushiriPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.PATH3037_D).toPath() }

    val context = LocalContext.current
    val segments = remember(context) { HokkaidoRoadNetwork.getHighDensitySegments(context) }
    val miniMapRoadPaths = remember(segments) {
        segments.filter { it.waypoints.size >= 2 }.map { seg ->
            Path().apply {
                moveTo(seg.waypoints[0].x, seg.waypoints[0].y)
                for (i in 1 until seg.waypoints.size) {
                    lineTo(seg.waypoints[i].x, seg.waypoints[i].y)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8F5E9).copy(alpha = 0.94f))
            .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(10.dp))
            .padding(6.dp)
            .size(width = 96.dp, height = 64.dp), // 612 : 387 aspect ratio (~1.58 : 1)
    ) {
        val landColor = Color(0xFFA5D6A7)
        val landBorderColor = Color(0xFF66BB6A)
        val currentDotColor = Color(0xFFD32F2F)

        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val scaleX = canvasW / HokkaidoSvgPaths.SVG_WIDTH
            val scaleY = canvasH / HokkaidoSvgPaths.SVG_HEIGHT

            // Scale Wikipedia SVG path directly onto canvas
            scale(scaleX, scaleY, pivot = Offset.Zero) {
                listOf(donanPath, dohokuPath, doouPath, dotoPath, okushiriPath).forEach { path ->
                    drawPath(path = path, color = landColor)
                    drawPath(path = path, color = landBorderColor, style = Stroke(width = 1f))
                }

                // 34,000+ Real GIS Highway network lines on mini-map
                val miniRoadColor = Color(0xFF1565C0).copy(alpha = 0.6f)
                miniMapRoadPaths.forEach { roadPath ->
                    drawPath(
                        path = roadPath,
                        color = miniRoadColor,
                        style = Stroke(width = 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }

            // Accurate position of player dot sampled directly from current node's real SVG coordinates:
            val node = HokkaidoAct1Route.getNodeByStepIndex(currentStepIndex)
            val svgX = node?.x ?: 115.0f
            val svgY = node?.y ?: 311.0f

            val dotCanvasX = svgX * scaleX
            val dotCanvasY = svgY * scaleY

            drawCircle(
                color = currentDotColor,
                radius = 3.5.dp.toPx(),
                center = Offset(dotCanvasX, dotCanvasY),
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(dotCanvasX, dotCanvasY),
            )
        }
    }
}
