package com.aidomari.app.ui.sugoroku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.HokkaidoAct1Route
import kotlin.math.hypot

/**
 * Pre-computed equidistant board game layout for the 2,000 Sugoroku steps.
 * Each step is strictly equidistant (step distance = 76dp) following the natural
 * contour of Hokkaido from Hakodate northwards to Soya and Shiretoko.
 */
object HokkaidoBoardTrack {
    const val STEP_DIST = 76f // 76dp between tile centers

    /**
     * 2,000 equidistant tile center coordinates in board space.
     * Calculated lazily once.
     */
    val tileCenters: List<Offset> by lazy {
        val rawNodes = HokkaidoAct1Route.nodes
        if (rawNodes.isEmpty()) return@lazy emptyList()

        // 1. Extract directional guide points from raw nodes
        val guide = mutableListOf<Offset>()
        guide.add(Offset(rawNodes[0].x, rawNodes[0].y))
        for (i in 1 until rawNodes.size - 1) {
            val last = guide.last()
            val dx = rawNodes[i].x - last.x
            val dy = rawNodes[i].y - last.y
            if (hypot(dx, dy) >= 15f) {
                guide.add(Offset(rawNodes[i].x, rawNodes[i].y))
            }
        }
        guide.add(Offset(rawNodes.last().x, rawNodes.last().y))

        // 2. Cumulative length along guide
        val cumulative = FloatArray(guide.size)
        var totalLen = 0f
        for (i in 0 until guide.size - 1) {
            val d = hypot(guide[i + 1].x - guide[i].x, guide[i + 1].y - guide[i].y)
            totalLen += d
            cumulative[i + 1] = totalLen
        }

        // 3. Generate 2,000 equidistant steps with step distance of STEP_DIST
        val points = ArrayList<Offset>(rawNodes.size)
        var curX = 0f
        var curY = 0f
        points.add(Offset(curX, curY))

        var guideIdx = 0
        val targetStepLenInGuide = if (rawNodes.size > 1) totalLen / (rawNodes.size - 1) else 1f

        for (i in 1 until rawNodes.size) {
            val targetDist = i * targetStepLenInGuide
            while (guideIdx < guide.size - 2 && cumulative[guideIdx + 1] < targetDist) {
                guideIdx++
            }
            val dx = guide[guideIdx + 1].x - guide[guideIdx].x
            val dy = guide[guideIdx + 1].y - guide[guideIdx].y
            val segLen = hypot(dx, dy)
            val dirX = if (segLen > 0) dx / segLen else 0f
            val dirY = if (segLen > 0) dy / segLen else -1f

            curX += dirX * STEP_DIST
            curY += dirY * STEP_DIST
            points.add(Offset(curX, curY))
        }

        points
    }
}

/**
 * Detachable small silhouette of Hokkaido in the corner of the map.
 * Shows only the contour of Hokkaido and a pulsing red dot at the player's current location.
 * Does NOT reveal route path, goal, or distant destinations.
 */
@Composable
fun HokkaidoMiniMap(
    currentStepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8F5E9).copy(alpha = 0.94f))
            .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(10.dp))
            .padding(6.dp)
            .size(width = 72.dp, height = 86.dp),
    ) {
        val landColor = Color(0xFFA5D6A7)
        val currentDotColor = Color(0xFFD32F2F)

        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Simplified contour points of Hokkaido
            val contour = listOf(
                Offset(0.20f, 0.96f), // Matsumae
                Offset(0.22f, 0.88f), // Esashi
                Offset(0.21f, 0.65f), // Suttsu
                Offset(0.23f, 0.58f), // Shakotan
                Offset(0.32f, 0.56f), // Otaru
                Offset(0.36f, 0.38f), // Rumoi
                Offset(0.33f, 0.15f), // Teshio
                Offset(0.37f, 0.04f), // Soya
                Offset(0.55f, 0.26f), // Mombetsu
                Offset(0.74f, 0.35f), // Abashiri
                Offset(0.92f, 0.22f), // Shiretoko tip
                Offset(0.88f, 0.36f), // Rausu
                Offset(0.98f, 0.44f), // Nemuro
                Offset(0.80f, 0.50f), // Kushiro
                Offset(0.68f, 0.70f), // Erimo
                Offset(0.48f, 0.62f), // Tomakomai
                Offset(0.42f, 0.72f), // Muroran
                Offset(0.31f, 0.71f), // Oshamambe
                Offset(0.32f, 0.78f), // Yakumo
                Offset(0.35f, 0.86f), // Mori
                Offset(0.30f, 0.92f), // Hakodate
            )

            val path = Path()
            contour.forEachIndexed { idx, pt ->
                val cx = pt.x * canvasW
                val cy = pt.y * canvasH
                if (idx == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
            }
            path.close()
            drawPath(path = path, color = landColor)

            // Approximate position of player dot based on progress ratio
            val ratio = (currentStepIndex.toFloat() / totalSteps.coerceAtLeast(1)).coerceIn(0f, 1f)
            // Interpolate roughly along the coast from Hakodate (0.30, 0.92) to Soya (0.37, 0.04) to Shiretoko (0.88, 0.36)
            val dotX: Float
            val dotY: Float
            if (ratio < 0.6f) {
                // Moving Hakodate -> Soya
                val subR = ratio / 0.6f
                dotX = (0.30f + subR * (0.37f - 0.30f)) * canvasW
                dotY = (0.92f + subR * (0.04f - 0.92f)) * canvasH
            } else {
                // Moving Soya -> Shiretoko
                val subR = (ratio - 0.6f) / 0.4f
                dotX = (0.37f + subR * (0.88f - 0.37f)) * canvasW
                dotY = (0.04f + subR * (0.36f - 0.04f)) * canvasH
            }

            drawCircle(
                color = currentDotColor,
                radius = 3.5.dp.toPx(),
                center = Offset(dotX, dotY),
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(dotX, dotY),
            )
        }
    }
}
