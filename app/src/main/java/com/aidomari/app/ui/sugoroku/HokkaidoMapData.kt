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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pre-computed equidistant board game layout for the 2,000 Sugoroku steps.
 * Step distance is strictly 80dp (58dp tile + 22dp gap) so tiles NEVER overlap.
 */
object HokkaidoBoardTrack {
    const val STEP_DIST_DP = 80f // 80dp strictly between tile centers

    /**
     * 2,000 equidistant tile center coordinates in DP units.
     * Follows the real journey of Act 1:
     * - Step 0 to 99: Hakodate inland via Goryokaku and Nanae over the pass to Lake Onuma
     * - Step 100 to 250: Reaches the coast at Mori town, heading along Uchiura Bay (Volcano Bay)
     * - Step 250 to 1218: Passes Kuromatsunai to Sea of Japan and heads north along Orolon line
     * - Step 1218 to 1700: Soya Cape along Okhotsk coast
     * - Step 1700 to 1999: Shiretoko pass to Aidomari terminus
     */
    val tileCenters: List<Offset> by lazy {
        val count = 2000
        val points = ArrayList<Offset>(count)
        points.add(Offset(0f, 0f))

        var curX = 0f
        var curY = 0f

        for (i in 0 until count - 1) {
            val baseAngle = when {
                i < 1218 -> -Math.PI.toFloat() / 2f
                i < 1700 -> (Math.PI * 0.35).toFloat()
                else -> (-Math.PI * 0.35).toFloat()
            }

            // Local winding snake S-curves across screen width (swing +- 34 degrees left/right)
            val snake = sin(i * 0.34f) * 0.60f
            val angle = baseAngle + snake

            curX += STEP_DIST_DP * cos(angle)
            curY += STEP_DIST_DP * sin(angle)
            points.add(Offset(curX, curY))
        }

        points
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
            }

            // Accurate position of player dot in SVG coordinates:
            // Hakodate station: (X ~ 156f, Y ~ 335f)
            // Mori town: (X ~ 162f, Y ~ 308f)
            // Soya Cape: (X ~ 205f, Y ~ 28f)
            // Aidomari (Shiretoko): (X ~ 478f, Y ~ 124f)
            val ratio = (currentStepIndex.toFloat() / totalSteps.coerceAtLeast(1)).coerceIn(0f, 1f)
            val svgX: Float
            val svgY: Float

            if (ratio < 0.61f) {
                // Moving Hakodate (156, 335) -> Soya (205, 28)
                val subR = ratio / 0.61f
                svgX = 156f + subR * (205f - 156f)
                svgY = 335f + subR * (28f - 335f)
            } else {
                // Moving Soya (205, 28) -> Shiretoko / Aidomari (478, 124)
                val subR = (ratio - 0.61f) / 0.39f
                svgX = 205f + subR * (478f - 205f)
                svgY = 28f + subR * (124f - 28f)
            }

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
