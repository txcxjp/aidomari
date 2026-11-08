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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
    val tileCenters = HokkaidoBoardTrack.tileCenters

    // Scale fixed around 1.0f (substantial ~56dp square tiles, no full-map zoom-out)
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var hasCenteredInitially by remember { mutableStateOf(false) }

    fun centerOnStep(stepIndex: Int, containerWidth: Float, containerHeight: Float) {
        val pt = tileCenters.getOrNull(stepIndex) ?: Offset.Zero
        val targetX = pt.x * zoomScale
        val targetY = pt.y * zoomScale
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
                .background(Color(0xFF81D4FA)), // Sea of Japan & Okhotsk blue
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()

            val currentStep = uiState.progress.currentStepIndex
            val currentNode = uiState.currentNode

            // Centering on initial load
            LaunchedEffect(currentStep, containerWidth, containerHeight) {
                if (!hasCenteredInitially && containerWidth > 0 && containerHeight > 0) {
                    centerOnStep(currentStep, containerWidth, containerHeight)
                    hasCenteredInitially = true
                }
            }

            // Floating animation for player piece
            val infiniteTransition = rememberInfiniteTransition(label = "player_piece_anim")
            val playerFloatY by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "player_float_y",
            )

            // Clamp pan offset so camera cannot scroll beyond currentStep + 8 steps (Fog of War)
            fun clampPan(offset: Offset): Offset {
                if (tileCenters.isEmpty()) return offset
                val allowedMaxStep = (currentStep + 8).coerceAtMost(tileCenters.lastIndex)
                val visiblePts = tileCenters.subList(0, allowedMaxStep + 1)
                val minX = visiblePts.minOf { it.x }
                val maxX = visiblePts.maxOf { it.x }
                val minY = visiblePts.minOf { it.y }
                val maxY = visiblePts.maxOf { it.y }

                val margin = 260f // margin around visible bounding box in screen pixels
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

            // Colors for Board & Nature
            val landColor = Color(0xFFA5D6A7) // Hokkaido lush green
            val beachColor = Color(0xFFFFF9C4) // Sandy beach strip
            val waveColor = Color(0xFFE0F7FA).copy(alpha = 0.8f)

            // 2D Sugoroku Board Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("SugorokuCanvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Strict zoom boundaries: Keep scale large (0.85f to 1.35f), no zooming out
                            zoomScale = (zoomScale * zoom).coerceIn(0.85f, 1.35f)
                            panOffset = clampPan(panOffset + pan)
                        }
                    }
                    .pointerInput(tileCenters, zoomScale, panOffset, currentStep) {
                        detectTapGestures { tapOffset ->
                            val maxAllowedStep = (currentStep + 8).coerceAtMost(allNodes.lastIndex)
                            val threshold = 36.dp.toPx()

                            for (i in 0..maxAllowedStep) {
                                val pt = tileCenters[i]
                                val sx = pt.x * zoomScale + panOffset.x
                                val sy = pt.y * zoomScale + panOffset.y
                                val dx = tapOffset.x - sx
                                val dy = tapOffset.y - sy
                                if (dx * dx + dy * dy <= threshold * threshold) {
                                    viewModel.selectNode(allNodes[i])
                                    break
                                }
                            }
                        }
                    },
            ) {
                val maxAllowedStep = (currentStep + 8).coerceAtMost(allNodes.lastIndex)

                // 1. Draw Hokkaido Landmass and Beach along the track
                drawCoastlineAndLand(
                    tileCenters = tileCenters,
                    maxAllowedStep = maxAllowedStep,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    landColor = landColor,
                    beachColor = beachColor,
                    waveColor = waveColor,
                )

                // 2. Draw Wooden Connecting Bridge between steps
                drawConnectingBridges(
                    tileCenters = tileCenters,
                    maxAllowedStep = maxAllowedStep,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                )

                // 3. Draw Equidistant Square Sugoroku Tiles
                drawSugorokuTiles(
                    nodes = allNodes,
                    tileCenters = tileCenters,
                    currentStep = currentStep,
                    maxAllowedStep = maxAllowedStep,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                )

                // 4. Draw Cute Traveler Avatar Piece at current step
                val curPt = tileCenters.getOrNull(currentStep) ?: Offset.Zero
                val curScreenX = curPt.x * zoomScale + panOffset.x
                val curScreenY = curPt.y * zoomScale + panOffset.y

                drawTravelerPiece(
                    x = curScreenX,
                    y = curScreenY + playerFloatY * 1.5f,
                    shadowY = curScreenY + 22.dp.toPx(),
                )
            }

            // Top Status Bar (No spoilers: Only current location & walked steps)
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
                currentStepIndex = currentStep,
                totalSteps = allNodes.size,
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
                        centerOnStep(currentStep, containerWidth, containerHeight)
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
                        zoomScale = (zoomScale * 1.15f).coerceIn(0.85f, 1.35f)
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
                        zoomScale = (zoomScale / 1.15f).coerceIn(0.85f, 1.35f)
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
 * Draws the lush green landmass of Hokkaido on the right side of the track,
 * with sandy beach and sea waves along the left side.
 */
private fun DrawScope.drawCoastlineAndLand(
    tileCenters: List<Offset>,
    maxAllowedStep: Int,
    zoomScale: Float,
    panOffset: Offset,
    landColor: Color,
    beachColor: Color,
    waveColor: Color,
) {
    if (tileCenters.size < 2) return
    val count = (maxAllowedStep + 1).coerceAtMost(tileCenters.size)

    val landPath = Path()
    val beachPath = Path()

    // Follow the track, expanding land to the right (+normal) and beach to the left (-normal)
    val rightPoints = ArrayList<Offset>(count)
    val beachPoints = ArrayList<Offset>(count)
    val deepLandPoints = ArrayList<Offset>(count)

    for (i in 0 until count) {
        val pt = tileCenters[i]
        val sx = pt.x * zoomScale + panOffset.x
        val sy = pt.y * zoomScale + panOffset.y

        // Calculate direction and normal
        val nextPt = if (i < count - 1) tileCenters[i + 1] else tileCenters[i]
        val prevPt = if (i > 0) tileCenters[i - 1] else tileCenters[i]
        val dirX = nextPt.x - prevPt.x
        val dirY = nextPt.y - prevPt.y
        val angle = atan2(dirY, dirX)
        // Right normal (+PI/2): Landward (Inland Hokkaido)
        val landAngle = angle + (Math.PI / 2.0).toFloat()

        val normX = cos(landAngle)
        val normY = sin(landAngle)

        // Beach line on seaward side (left of track, ~40dp away)
        beachPoints.add(Offset(sx - normX * 42.dp.toPx(), sy - normY * 42.dp.toPx()))
        // Land line near track
        rightPoints.add(Offset(sx + normX * 36.dp.toPx(), sy + normY * 36.dp.toPx()))
        // Deep inland land (500dp to the right)
        deepLandPoints.add(Offset(sx + normX * 520.dp.toPx(), sy + normY * 520.dp.toPx()))
    }

    // 1. Draw broad green Hokkaido landmass
    if (rightPoints.isNotEmpty()) {
        landPath.moveTo(rightPoints[0].x, rightPoints[0].y)
        for (i in 1 until rightPoints.size) {
            landPath.lineTo(rightPoints[i].x, rightPoints[i].y)
        }
        for (i in deepLandPoints.indices.reversed()) {
            landPath.lineTo(deepLandPoints[i].x, deepLandPoints[i].y)
        }
        landPath.close()
        drawPath(landPath, landColor)
    }

    // 2. Draw beach strip along the sea
    if (beachPoints.isNotEmpty() && rightPoints.isNotEmpty()) {
        beachPath.moveTo(beachPoints[0].x, beachPoints[0].y)
        for (i in 1 until beachPoints.size) {
            beachPath.lineTo(beachPoints[i].x, beachPoints[i].y)
        }
        for (i in rightPoints.indices.reversed()) {
            beachPath.lineTo(rightPoints[i].x, rightPoints[i].y)
        }
        beachPath.close()
        drawPath(beachPath, beachColor)

        // Draw foam wave line along outer beach edge
        val waveStroke = Path()
        waveStroke.moveTo(beachPoints[0].x, beachPoints[0].y)
        for (i in 1 until beachPoints.size) {
            waveStroke.lineTo(beachPoints[i].x, beachPoints[i].y)
        }
        drawPath(
            waveStroke,
            waveColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * Draws wooden bridge connecting adjacent steps.
 */
private fun DrawScope.drawConnectingBridges(
    tileCenters: List<Offset>,
    maxAllowedStep: Int,
    zoomScale: Float,
    panOffset: Offset,
) {
    val bridgeWidth = 16.dp.toPx()
    val bridgeColor = Color(0xFFD7CCC8) // Wooden bridge
    val bridgeBorderColor = Color(0xFF8D6E63)

    val count = (maxAllowedStep + 1).coerceAtMost(tileCenters.size)
    for (i in 0 until count - 1) {
        val p1 = tileCenters[i]
        val p2 = tileCenters[i + 1]
        val sx1 = p1.x * zoomScale + panOffset.x
        val sy1 = p1.y * zoomScale + panOffset.y
        val sx2 = p2.x * zoomScale + panOffset.x
        val sy2 = p2.y * zoomScale + panOffset.y

        // Border line
        drawLine(
            color = bridgeBorderColor,
            start = Offset(sx1, sy1),
            end = Offset(sx2, sy2),
            strokeWidth = bridgeWidth + 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        // Surface
        drawLine(
            color = bridgeColor,
            start = Offset(sx1, sy1),
            end = Offset(sx2, sy2),
            strokeWidth = bridgeWidth,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Draws the substantial, distinct 56dp square Sugoroku tiles with shadows,
 * step numbers, municipality labels, and icons.
 */
private fun DrawScope.drawSugorokuTiles(
    nodes: List<SugorokuNode>,
    tileCenters: List<Offset>,
    currentStep: Int,
    maxAllowedStep: Int,
    zoomScale: Float,
    panOffset: Offset,
) {
    val tileSize = 54.dp.toPx()
    val cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())

    val labelPaint = Paint().apply {
        color = android.graphics.Color.rgb(33, 33, 33)
        textSize = 11.dp.toPx()
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    val stepNumberPaint = Paint().apply {
        color = android.graphics.Color.rgb(117, 117, 117)
        textSize = 8.5.dp.toPx()
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    val iconPaint = Paint().apply {
        textSize = 14.dp.toPx()
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val count = (maxAllowedStep + 1).coerceAtMost(tileCenters.size)
    for (i in 0 until count) {
        val node = nodes[i]
        val pt = tileCenters[i]
        val cx = pt.x * zoomScale + panOffset.x
        val cy = pt.y * zoomScale + panOffset.y

        val half = tileSize / 2f
        val left = cx - half
        val top = cy - half

        val isVisited = i < currentStep
        val isCurrent = i == currentStep

        // 1. Drop shadow beneath tile
        drawRoundRect(
            color = Color(0x33000000),
            topLeft = Offset(left + 2.dp.toPx(), top + 3.dp.toPx()),
            size = Size(tileSize, tileSize),
            cornerRadius = cornerRadius,
        )

        // 2. Tile background & border colors
        val (bgColor, borderColor, borderWidth) = when {
            isCurrent -> Triple(Color(0xFFFFD54F), Color(0xFFFF6F00), 3.dp.toPx())
            isVisited -> Triple(Color(0xFFFFB74D), Color(0xFFE65100), 2.dp.toPx())
            node.type == SugorokuNodeType.SCENIC_SPOT -> Triple(Color(0xFFFFF9C4), Color(0xFFFBC02D), 2.5.dp.toPx())
            else -> Triple(Color(0xFFFFFFFF), Color(0xFF90A4AE), 1.8.dp.toPx())
        }

        // Draw tile body
        drawRoundRect(
            color = bgColor,
            topLeft = Offset(left, top),
            size = Size(tileSize, tileSize),
            cornerRadius = cornerRadius,
        )
        // Draw tile border
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = Size(tileSize, tileSize),
            cornerRadius = cornerRadius,
            style = Stroke(width = borderWidth),
        )

        // 3. Tile Contents: Step number on top
        val stepNumStr = "#${i + 1}"
        drawContext.canvas.nativeCanvas.drawText(
            stepNumStr,
            cx,
            top + 12.dp.toPx(),
            stepNumberPaint,
        )

        // Tile Contents: Name label in center
        val displayName = if (node.name.length > 4) node.name.take(4) else node.name
        drawContext.canvas.nativeCanvas.drawText(
            displayName,
            cx,
            top + 28.dp.toPx(),
            labelPaint,
        )

        // Tile Contents: Icon on bottom
        val iconStr = when (node.type) {
            SugorokuNodeType.START_HAKODATE -> "🎌"
            SugorokuNodeType.GOAL_AIDOMARI -> "🏁"
            SugorokuNodeType.SCENIC_SPOT -> "⭐"
            SugorokuNodeType.TOWN_CENTER -> "🏘️"
            SugorokuNodeType.REST_AREA -> "♨️"
            SugorokuNodeType.NORMAL -> if (isVisited) "👣" else "・"
        }
        drawContext.canvas.nativeCanvas.drawText(
            iconStr,
            cx,
            top + 45.dp.toPx(),
            iconPaint,
        )
    }
}

/**
 * Draws the cute traveler avatar piece with backpack and soft shadow.
 */
private fun DrawScope.drawTravelerPiece(
    x: Float,
    y: Float,
    shadowY: Float,
) {
    // 1. Soft oval shadow on the ground
    drawOval(
        color = Color(0x38000000),
        topLeft = Offset(x - 14.dp.toPx(), shadowY - 5.dp.toPx()),
        size = Size(28.dp.toPx(), 10.dp.toPx()),
    )

    // 2. Backpack (Red)
    drawRoundRect(
        color = Color(0xFFD32F2F),
        topLeft = Offset(x - 13.dp.toPx(), y - 10.dp.toPx()),
        size = Size(10.dp.toPx(), 16.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )

    // 3. Body (Blue Shirt)
    drawRoundRect(
        color = Color(0xFF1976D2),
        topLeft = Offset(x - 8.dp.toPx(), y - 6.dp.toPx()),
        size = Size(16.dp.toPx(), 17.dp.toPx()),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
    )

    // 4. Head / Face (Warm skin tone)
    val headRadius = 9.dp.toPx()
    val headCenter = Offset(x, y - 14.dp.toPx())
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
        center = Offset(headCenter.x, headCenter.y - 4.dp.toPx()),
    )
    // Hat brim
    drawOval(
        color = hatColor,
        topLeft = Offset(headCenter.x - 12.dp.toPx(), headCenter.y - 8.dp.toPx()),
        size = Size(24.dp.toPx(), 7.dp.toPx()),
    )

    // 6. Eyes (Happy traveler)
    val eyePaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawCircle(
        headCenter.x - 3.5f.dp.toPx(),
        headCenter.y + 1f.dp.toPx(),
        1.5f.dp.toPx(),
        eyePaint,
    )
    drawContext.canvas.nativeCanvas.drawCircle(
        headCenter.x + 3.5f.dp.toPx(),
        headCenter.y + 1f.dp.toPx(),
        1.5f.dp.toPx(),
        eyePaint,
    )
}
