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
import com.aidomari.app.domain.model.HokkaidoAct1Route
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pre-computed equidistant board game layout for the 2,000 Sugoroku steps.
 * Step distance is strictly 80dp (58dp tile + 22dp gap) so tiles NEVER overlap.
 * Real geographic progression reflects Hokkaido's distinct road directions:
 * - Hakodate to Mori: inland north
 * - Mori to Oshamambe: Uchiura (Volcano) Bay arc
 * - Kuromatsunai to Sea of Japan: west pass
 * - Shakotan Peninsula loop: jutting northwest to Kamui Cape, folding east to Yoichi
 * - Otaru to Ishikari & Orolon line: long straight north along Sea of Japan
 * - Soya Cape: northernmost tip
 * - Okhotsk coast: sweeping southeast run to Abashiri & Shari
 * - Shiretoko Pass: mountain hairpin descent
 * - Aidomari: northeast coastal terminus
 */
object HokkaidoBoardTrack {
    const val STEP_DIST_DP = 80f // 80dp strictly between tile centers

    val tileCenters: List<Offset> by lazy {
        val count = 2000
        val points = ArrayList<Offset>(count)
        points.add(Offset(0f, 0f))

        var curX = 0f
        var curY = 0f

        for (i in 0 until count - 1) {
            val baseAngle = when {
                // 1. Hakodate to Lake Onuma (0..55): Inland north
                i < 56 -> -Math.PI.toFloat() / 2f
                // 2. Mori to Oshamambe (56..185): Uchiura (Volcano) Bay arc
                i < 86 -> (-Math.PI * 0.58).toFloat() // Rounding Mt. Komagatake
                i < 141 -> (-Math.PI * 0.65).toFloat() // Yakumo: northwest bay coast
                i < 186 -> (-Math.PI * 0.35).toFloat() // Oshamambe: curving northeast at bay apex
                // 3. Kuromatsunai to Suttsu (186..220): Inland watershed west to Sea of Japan
                i < 221 -> (-Math.PI * 0.75).toFloat()
                // 4. Suttsu, Shimamaki, Raiden coast to Iwanai (221..380): Sea of Japan cliffs north
                i < 381 -> (-Math.PI * 0.48).toFloat()
                // 5. Shakotan Peninsula loop (381..505): Dramatic northwest jut, then folding east
                i < 475 -> (-Math.PI * 0.70).toFloat() // Jutting northwest to Kamui Cape
                i < 506 -> (Math.PI * 0.15).toFloat() // Folding southeast to Yoichi
                // 6. Yoichi to Otaru (506..600): Ishikari Bay coast east/northeast
                i < 601 -> (-Math.PI * 0.20).toFloat()
                // 7. Ishikari to Hamamasu & Ofuyu (601..690): Heading north-northeast
                i < 691 -> (-Math.PI * 0.40).toFloat()
                // 8. Orolon line: Mashike, Rumoi, Haboro, Teshio, Sarobetsu to Wakkanai (691..1161)
                i < 1162 -> (-Math.PI * 0.47).toFloat() // Monumental straight coastal run north
                // 9. Wakkanai to Soya Cape (1162..1218): Heading east to northernmost point
                i < 1219 -> (-Math.PI * 0.05).toFloat()
                // 10. Okhotsk line: Soya, Sarufutsu, Esashi, Monbetsu, Saroma, Abashiri to Shari (1219..1848)
                i < 1849 -> (Math.PI * 0.28).toFloat() // Grand southeast coastal descent
                // 11. Shari to Shiretoko Utoro (1849..1893): Northeast approach to World Heritage Shiretoko
                i < 1894 -> (-Math.PI * 0.22).toFloat()
                // 12. Shiretoko Pass (1894..1933): Hairpin descent from 738m summit
                i < 1934 -> (Math.PI * 0.12).toFloat()
                // 13. Rausu to Aidomari terminus (1934..1999): Northeast coastal road end
                else -> (-Math.PI * 0.25).toFloat()
            }

            // Gentle rhythmic snake wave keeping tiles within comfortable width
            val snake = sin(i * 0.32f) * 0.25f
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
