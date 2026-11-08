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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pre-computed equidistant board game layout for the 2,000 Sugoroku steps.
 * Designed as a winding, snake-like board game track across Hokkaido.
 * Step distance is strictly 80dp so tiles (58dp) NEVER overlap.
 */
object HokkaidoBoardTrack {
    const val STEP_DIST_DP = 80f // 80dp strictly between tile centers (58dp tile + 22dp gap)

    /**
     * 2,000 equidistant tile center coordinates in DP units.
     * Starts at Hakodate (0, 0), snakes gently left and right, and travels northwards
     * up the coast towards Soya and then southeast to Shiretoko.
     */
    val tileCenters: List<Offset> by lazy {
        val count = 2000
        val points = ArrayList<Offset>(count)
        points.add(Offset(0f, 0f))

        var curX = 0f
        var curY = 0f

        for (i in 0 until count - 1) {
            // General direction of Hokkaido journey:
            // 0 - 1218 (Hakodate to Cape Soya): Heading North (-PI/2)
            // 1218 - 1700 (Soya along Okhotsk to Abashiri): Heading South-East (PI * 0.35)
            // 1700 - 1999 (Abashiri to Shiretoko): Heading North-East (-PI * 0.35)
            val baseAngle = when {
                i < 1218 -> -Math.PI.toFloat() / 2f
                i < 1700 -> (Math.PI * 0.35).toFloat()
                else -> (-Math.PI * 0.35).toFloat()
            }

            // Local winding snake S-curves across screen width (swing +- 32 degrees left/right)
            val snake = sin(i * 0.32f) * 0.55f
            val angle = baseAngle + snake

            curX += STEP_DIST_DP * cos(angle)
            curY += STEP_DIST_DP * sin(angle)
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
            val dotX: Float
            val dotY: Float
            if (ratio < 0.6f) {
                val subR = ratio / 0.6f
                dotX = (0.30f + subR * (0.37f - 0.30f)) * canvasW
                dotY = (0.92f + subR * (0.04f - 0.92f)) * canvasH
            } else {
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
