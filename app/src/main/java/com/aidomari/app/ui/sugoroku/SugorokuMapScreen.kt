package com.aidomari.app.ui.sugoroku

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.model.SugorokuNodeType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SugorokuMapScreen(
    viewModel: SugorokuMapViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val allNodes = HokkaidoAct1Route.nodes

    // Fixed zoomed-in scale: No zooming out to full-map (prevents spoiling distance)
    // 16.0f zoom level displays steps as substantial tiles (~30-40dp)
    var zoomScale by remember { mutableFloatStateOf(16.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var hasCenteredInitially by remember { mutableStateOf(false) }

    fun centerOnNode(node: SugorokuNode, containerWidth: Float, containerHeight: Float) {
        val targetX = node.x * zoomScale
        val targetY = node.y * zoomScale
        panOffset = Offset(
            x = containerWidth / 2f - targetX,
            y = containerHeight / 2f - targetY,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "北海道すごろく旅",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "北への旅路",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("SugorokuMapBackButton"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
                actions = {
                    Text(
                        text = "🗾",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF81D4FA)), // Sea of Japan & Okhotsk light blue
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()

            val currentStep = uiState.progress.currentStepIndex
            val currentNode = uiState.currentNode

            // Centering on initial load
            LaunchedEffect(currentNode, containerWidth, containerHeight) {
                if (!hasCenteredInitially && containerWidth > 0 && containerHeight > 0) {
                    centerOnNode(currentNode, containerWidth, containerHeight)
                    hasCenteredInitially = true
                }
            }

            // Gentle floating breathing animation for player character
            val infiniteTransition = rememberInfiniteTransition(label = "player_float")
            val playerFloatY by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "player_float_y",
            )

            // Clamp pan offset so player cannot pan beyond currentStep + 18 steps (Fog of War)
            fun clampPan(offset: Offset): Offset {
                if (allNodes.isEmpty()) return offset
                val allowedMaxStep = (currentStep + 18).coerceAtMost(allNodes.lastIndex)
                val visibleNodes = allNodes.subList(0, allowedMaxStep + 1)
                val minX = visibleNodes.minOf { it.x }
                val maxX = visibleNodes.maxOf { it.x }
                val minY = visibleNodes.minOf { it.y }
                val maxY = visibleNodes.maxOf { it.y }

                val margin = 200f // margin around visible bounding box in screen pixels
                val screenMinX = containerWidth - (maxX * zoomScale) - margin
                val screenMaxX = -(minX * zoomScale) + containerWidth + margin
                val screenMinY = containerHeight - (maxY * zoomScale) - margin
                val screenMaxY = -(minY * zoomScale) + containerHeight + margin

                val clampedX = offset.x.coerceIn(
                    minOf(screenMinX, screenMaxX),
                    maxOf(screenMinX, screenMaxX),
                )
                val clampedY = offset.y.coerceIn(
                    minOf(screenMinY, screenMaxY),
                    maxOf(screenMinY, screenMaxY),
                )
                return Offset(clampedX, clampedY)
            }

            // Colors for Sugoroku road
            val landColor = Color(0xFFA5D6A7) // Fresh Hokkaido greenery
            val coastlineColor = Color(0xFFE8F5E9) // Foam / sandy beach line
            val visitedRoadColor = Color(0xFFFFCC80) // Warm terracotta / sand for walked steps
            val visitedBorderColor = Color(0xFFE65100)
            val unvisitedRoadColor = Color(0xFFFFFFFF) // Crisp white road
            val unvisitedBorderColor = Color(0xFF90A4AE)
            val roadJointColor = Color(0xFF78909C)

            // 2D Sugoroku Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("SugorokuCanvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Strict zoom boundaries: Keep scale large (12.0f to 22.0f), no zoom-out
                            zoomScale = (zoomScale * zoom).coerceIn(12.0f, 22.0f)
                            panOffset = clampPan(panOffset + pan)
                        }
                    }
                    .pointerInput(allNodes, zoomScale, panOffset, currentStep) {
                        detectTapGestures { tapOffset ->
                            val worldX = (tapOffset.x - panOffset.x) / zoomScale
                            val worldY = (tapOffset.y - panOffset.y) / zoomScale
                            val maxAllowedStep = (currentStep + 18).coerceAtMost(allNodes.lastIndex)
                            val threshold = 2.5f // World units

                            // Only selectable up to allowed visible steps
                            val nearest = allNodes
                                .take(maxAllowedStep + 1)
                                .minByOrNull { node ->
                                    val dx = node.x - worldX
                                    val dy = node.y - worldY
                                    dx * dx + dy * dy
                                }
                            if (nearest != null) {
                                val dx = nearest.x - worldX
                                val dy = nearest.y - worldY
                                if (dx * dx + dy * dy <= threshold * threshold) {
                                    viewModel.selectNode(nearest)
                                }
                            }
                        }
                    },
            ) {
                // 1. Draw Hokkaido Land Contour Polygon
                val landPath = Path()
                HokkaidoMapData.coastlinePoints.forEachIndexed { idx, pt ->
                    val sx = pt.x * zoomScale + panOffset.x
                    val sy = pt.y * zoomScale + panOffset.y
                    if (idx == 0) landPath.moveTo(sx, sy) else landPath.lineTo(sx, sy)
                }
                landPath.close()

                // Draw Hokkaido green landmass
                drawPath(path = landPath, color = landColor)
                // Draw coastline edge
                drawPath(
                    path = landPath,
                    color = coastlineColor,
                    style = Stroke(width = 3.5.dp.toPx(), join = StrokeJoin.Round),
                )

                // 2. Draw Fun Illustrations on the map
                val maxAllowedStep = (currentStep + 18).coerceAtMost(allNodes.lastIndex)
                drawIllustrations(
                    illustrations = HokkaidoMapData.illustrations,
                    maxAllowedStep = maxAllowedStep,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                )

                // 3. Draw Sugoroku Road Ribbon with joint-separated steps
                val roadWidthPx = 34.dp.toPx()
                val visibleNodes = allNodes.take(maxAllowedStep + 1)

                if (visibleNodes.size > 1) {
                    drawSugorokuRoad(
                        nodes = visibleNodes,
                        currentStep = currentStep,
                        roadWidthPx = roadWidthPx,
                        zoomScale = zoomScale,
                        panOffset = panOffset,
                        visitedRoadColor = visitedRoadColor,
                        visitedBorderColor = visitedBorderColor,
                        unvisitedRoadColor = unvisitedRoadColor,
                        unvisitedBorderColor = unvisitedBorderColor,
                        roadJointColor = roadJointColor,
                    )
                }

                // 4. Draw Cute Traveler Avatar Piece at current node
                val curScreenX = currentNode.x * zoomScale + panOffset.x
                val curScreenY = currentNode.y * zoomScale + panOffset.y

                drawTravelerPiece(
                    x = curScreenX,
                    y = curScreenY + playerFloatY * 1.5f,
                    shadowY = curScreenY + 12.dp.toPx(),
                )
            }

            // Top Status Bar (No spoilers: Only current location & total walked steps)
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("SugorokuStatusCard"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📍",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            Text(
                                text = currentNode.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Text(
                            text = "${currentNode.municipality} を旅しています",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "歩んだ道のり",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "${currentStep + 1} マス",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // Detachable Mini-map in Bottom-Left (Option A)
            HokkaidoMiniMap(
                playerWorldX = currentNode.x,
                playerWorldY = currentNode.y,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 20.dp),
            )

            // Right Bottom Controls (Re-center & Zoom adjustment)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FloatingActionButton(
                    onClick = {
                        centerOnNode(currentNode, containerWidth, containerHeight)
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("SugorokuCenterButton"),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "現在地に戻る",
                        modifier = Modifier.size(20.dp),
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomScale = (zoomScale * 1.25f).coerceIn(12.0f, 22.0f)
                    },
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "拡大",
                        modifier = Modifier.size(20.dp),
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomScale = (zoomScale / 1.25f).coerceIn(12.0f, 22.0f)
                    },
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "縮小",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    // Step Detail Dialog when tapping any visible step
    uiState.selectedNode?.let { node ->
        val isReached = node.stepIndex <= uiState.progress.currentStepIndex
        AlertDialog(
            onDismissRequest = { viewModel.clearSelectedNode() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (node.type) {
                            SugorokuNodeType.START_HAKODATE -> "🎌 "
                            SugorokuNodeType.GOAL_AIDOMARI -> "🏁 "
                            SugorokuNodeType.SCENIC_SPOT -> "✨ "
                            SugorokuNodeType.TOWN_CENTER -> "🏘️ "
                            SugorokuNodeType.REST_AREA -> "☕ "
                            SugorokuNodeType.NORMAL -> "👣 "
                        },
                    )
                    Text(
                        text = node.name,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "自治体: ${node.municipality}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isReached) "✅ 到達済み（足跡が刻まれています）" else "🌱 未踏の地（これから進む道）",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isReached) Color(0xFF2E7D32) else Color(0xFF757575),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSelectedNode() }) {
                    Text("閉じる")
                }
            },
        )
    }
}

/**
 * Draws the Sugoroku road ribbon with distinct joint lines and step labels.
 */
private fun DrawScope.drawSugorokuRoad(
    nodes: List<SugorokuNode>,
    currentStep: Int,
    roadWidthPx: Float,
    zoomScale: Float,
    panOffset: Offset,
    visitedRoadColor: Color,
    visitedBorderColor: Color,
    unvisitedRoadColor: Color,
    unvisitedBorderColor: Color,
    roadJointColor: Color,
) {
    val textPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 9.dp.toPx()
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    // 1. Draw continuous road ribbon
    for (i in 0 until nodes.size - 1) {
        val n1 = nodes[i]
        val n2 = nodes[i + 1]
        val p1 = Offset(n1.x * zoomScale + panOffset.x, n1.y * zoomScale + panOffset.y)
        val p2 = Offset(n2.x * zoomScale + panOffset.x, n2.y * zoomScale + panOffset.y)

        val isVisited = i <= currentStep
        val baseColor = if (isVisited) visitedRoadColor else unvisitedRoadColor
        val borderColor = if (isVisited) visitedBorderColor else unvisitedBorderColor

        // Outer border
        drawLine(
            color = borderColor,
            start = p1,
            end = p2,
            strokeWidth = roadWidthPx + 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
        // Road surface
        drawLine(
            color = baseColor,
            start = p1,
            end = p2,
            strokeWidth = roadWidthPx,
            cap = StrokeCap.Round,
        )
    }

    // 2. Draw step joints and step labels
    for (i in nodes.indices) {
        val node = nodes[i]
        val sx = node.x * zoomScale + panOffset.x
        val sy = node.y * zoomScale + panOffset.y

        // Step divider joint line
        if (i < nodes.size - 1) {
            val next = nodes[i + 1]
            val nx = next.x * zoomScale + panOffset.x
            val ny = next.y * zoomScale + panOffset.y
            val angle = atan2(ny - sy, nx - sx)
            val perpAngle = angle + (Math.PI / 2.0).toFloat()
            val halfW = roadWidthPx / 2f
            val jx1 = sx + halfW * cos(perpAngle)
            val jy1 = sy + halfW * sin(perpAngle)
            val jx2 = sx - halfW * cos(perpAngle)
            val jy2 = sy - halfW * sin(perpAngle)

            drawLine(
                color = roadJointColor.copy(alpha = 0.6f),
                start = Offset(jx1, jy1),
                end = Offset(jx2, jy2),
                strokeWidth = 1.5.dp.toPx(),
            )
        }

        // Special icon or label for step
        when (node.type) {
            SugorokuNodeType.START_HAKODATE -> {
                drawContext.canvas.nativeCanvas.drawText("🎌", sx, sy + 4.dp.toPx(), textPaint)
            }
            SugorokuNodeType.SCENIC_SPOT -> {
                drawContext.canvas.nativeCanvas.drawText("⭐", sx, sy + 4.dp.toPx(), textPaint)
            }
            SugorokuNodeType.TOWN_CENTER -> {
                drawContext.canvas.nativeCanvas.drawText("🏘️", sx, sy + 4.dp.toPx(), textPaint)
            }
            SugorokuNodeType.REST_AREA -> {
                drawContext.canvas.nativeCanvas.drawText("☕", sx, sy + 4.dp.toPx(), textPaint)
            }
            else -> {
                // Short name label for steps
                val displayName = node.name.take(3)
                drawContext.canvas.nativeCanvas.drawText(displayName, sx, sy + 3.dp.toPx(), textPaint)
            }
        }
    }
}

/**
 * Draws fun illustrations scattered on the land and sea.
 */
private fun DrawScope.drawIllustrations(
    illustrations: List<MapIllustration>,
    maxAllowedStep: Int,
    zoomScale: Float,
    panOffset: Offset,
) {
    val emojiPaint = Paint().apply {
        textSize = 20.dp.toPx()
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    val labelPaint = Paint().apply {
        color = android.graphics.Color.rgb(38, 50, 56)
        textSize = 9.dp.toPx()
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    for (item in illustrations) {
        // Only show illustrations up to the current unlocked region
        if (item.minVisibleStep <= maxAllowedStep) {
            val sx = item.x * zoomScale + panOffset.x
            val sy = item.y * zoomScale + panOffset.y

            // Draw emoji icon
            drawContext.canvas.nativeCanvas.drawText(item.emoji, sx, sy, emojiPaint)
            // Draw small label below emoji
            drawContext.canvas.nativeCanvas.drawText(
                item.label,
                sx,
                sy + 12.dp.toPx(),
                labelPaint,
            )
        }
    }
}

/**
 * Draws the cute traveler avatar piece with backpack and shadow.
 */
private fun DrawScope.drawTravelerPiece(
    x: Float,
    y: Float,
    shadowY: Float,
) {
    // 1. Soft oval shadow on the ground
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(x - 12.dp.toPx(), shadowY - 4.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 8.dp.toPx()),
    )

    // 2. Backpack (Red)
    drawRoundRect(
        color = Color(0xFFD32F2F),
        topLeft = Offset(x - 10.dp.toPx(), y - 8.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 12.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
    )

    // 3. Body (Blue Shirt)
    drawRoundRect(
        color = Color(0xFF1976D2),
        topLeft = Offset(x - 6.dp.toPx(), y - 4.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 13.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )

    // 4. Head / Face (Warm skin tone)
    val headRadius = 7.dp.toPx()
    val headCenter = Offset(x, y - 11.dp.toPx())
    drawCircle(
        color = Color(0xFFFFCC80),
        radius = headRadius,
        center = headCenter,
    )

    // 5. Yellow Travel Hat
    val hatColor = Color(0xFFFFB300)
    drawCircle(
        color = hatColor,
        radius = headRadius * 0.9f,
        center = Offset(headCenter.x, headCenter.y - 3.dp.toPx()),
    )
    // Hat brim
    drawOval(
        color = hatColor,
        topLeft = Offset(headCenter.x - 9.dp.toPx(), headCenter.y - 6.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 5.dp.toPx()),
    )

    // 6. Eyes (Happy traveler)
    val eyePaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawCircle(
        headCenter.x - 2.5f.dp.toPx(),
        headCenter.y + 1f.dp.toPx(),
        1.2f.dp.toPx(),
        eyePaint,
    )
    drawContext.canvas.nativeCanvas.drawCircle(
        headCenter.x + 2.5f.dp.toPx(),
        headCenter.y + 1f.dp.toPx(),
        1.2f.dp.toPx(),
        eyePaint,
    )
}
