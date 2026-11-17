package com.aidomari.app.ui.sugoroku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.HighwayCategory
import com.aidomari.app.domain.model.HighwaySegment
import com.aidomari.app.domain.model.HokkaidoAct1Route

/**
 * High-fidelity overall Hokkaido Mini-Map displayed at the bottom-left of the screen.
 * Displays:
 * 1. Official GSI coastline silhouette of Hokkaido and outer islands.
 * 2. Official MLIT carved-out inland lakes.
 * 3. High-density highway network across Hokkaido.
 * 4. Traveled journey track up to current step.
 * 5. Player's real-time position beacon.
 */
@Composable
fun HokkaidoMiniMap(
    currentStepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    hokkaidoPaths: List<Path>? = null,
    lakesPath: Path? = null,
    highwayPaths: List<Pair<HighwaySegment, Path>>? = null,
    onMiniMapClick: (() -> Unit)? = null,
) {
    // Fallback path parsing if not provided by caller
    val effectiveHokkaidoPaths = hokkaidoPaths ?: remember {
        val parser = PathParser()
        listOf(
            parser.parsePathString(HokkaidoSvgPaths.MAINLAND_D).toPath(),
            parser.parsePathString(HokkaidoSvgPaths.OKUSHIRI_D).toPath(),
            parser.parsePathString(HokkaidoSvgPaths.RISHIRI_D).toPath(),
            parser.parsePathString(HokkaidoSvgPaths.REBUN_D).toPath(),
            parser.parsePathString(HokkaidoSvgPaths.TEURI_D).toPath(),
            parser.parsePathString(HokkaidoSvgPaths.YAGISHIRI_D).toPath(),
        )
    }

    val effectiveLakesPath = lakesPath ?: remember {
        PathParser().parsePathString(HokkaidoSvgPaths.LAKES_D).toPath()
    }

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9).copy(alpha = 0.94f))
            .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
            .then(
                if (onMiniMapClick != null) Modifier.clickable { onMiniMapClick() } else Modifier,
            )
            .padding(4.dp)
            .size(width = 114.dp, height = 76.dp), // ~1.50 : 1 aspect ratio matching Hokkaido viewBox
    ) {
        val oceanBgColor = Color(0xFFE1F5FE)
        val landColor = Color(0xFFA5D6A7)
        val landBorderColor = Color(0xFF66BB6A)
        val lakeColor = Color(0xFF81D4FA)
        val routeColor = Color(0xFFFF9800)
        val currentDotColor = Color(0xFFD32F2F)

        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val scaleX = canvasW / HokkaidoSvgPaths.SVG_WIDTH
            val scaleY = canvasH / HokkaidoSvgPaths.SVG_HEIGHT

            // 1. Hokkaido landmass and lakes
            scale(scaleX, scaleY, pivot = Offset.Zero) {
                effectiveHokkaidoPaths.forEach { path ->
                    drawPath(path = path, color = landColor)
                    drawPath(path = path, color = landBorderColor, style = Stroke(width = 1.0f))
                }

                // Carved-out lakes
                drawPath(path = effectiveLakesPath, color = lakeColor)
                drawPath(path = effectiveLakesPath, color = landBorderColor, style = Stroke(width = 0.6f))

                // 2. Real Highway Network on mini-map
                if (highwayPaths != null) {
                    val roadStroke = Stroke(width = 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    val miniRoadColor = Color(0xFF1565C0).copy(alpha = 0.50f)
                    val scenicRoadColor = Color(0xFF00897B).copy(alpha = 0.60f)

                    highwayPaths.forEach { (segment, path) ->
                        if (segment.category == HighwayCategory.NATIONAL_HIGHWAY) {
                            drawPath(path = path, color = miniRoadColor, style = roadStroke)
                        } else if (segment.category == HighwayCategory.SCENIC_ROAD) {
                            drawPath(path = path, color = scenicRoadColor, style = roadStroke)
                        }
                    }
                }

                // 3. Traveled route line up to current step
                if (currentStepIndex > 0) {
                    val traveledPath = Path()
                    val firstNode = HokkaidoAct1Route.getNodeByStepIndex(0) ?: HokkaidoAct1Route.startNode
                    traveledPath.moveTo(firstNode.x, firstNode.y)
                    for (step in 1..currentStepIndex) {
                        val n = HokkaidoAct1Route.getNodeByStepIndex(step)
                        if (n != null) {
                            traveledPath.lineTo(n.x, n.y)
                        }
                    }
                    drawPath(
                        path = traveledPath,
                        color = routeColor,
                        style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }

            // 4. Current Player Position Marker
            val currentNode = HokkaidoAct1Route.getNodeByStepIndex(currentStepIndex) ?: HokkaidoAct1Route.startNode
            val dotCanvasX = currentNode.x * scaleX
            val dotCanvasY = currentNode.y * scaleY

            // Outer soft halo
            drawCircle(
                color = Color(0xFFFF5252).copy(alpha = 0.35f),
                radius = 6.dp.toPx(),
                center = Offset(dotCanvasX, dotCanvasY),
            )
            // Inner red beacon
            drawCircle(
                color = currentDotColor,
                radius = 3.2.dp.toPx(),
                center = Offset(dotCanvasX, dotCanvasY),
            )
            // Center crisp white core
            drawCircle(
                color = Color.White,
                radius = 1.3.dp.toPx(),
                center = Offset(dotCanvasX, dotCanvasY),
            )
        }

        // Tiny subtitle badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp)
                .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp),
        ) {
            Text(
                text = "北海道全図",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
            )
        }
    }
}
