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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
    SugorokuMapContent(
        uiState = uiState,
        onBack = onBack,
        onSelectNode = { viewModel.selectNode(it) },
        onDismissDialog = { viewModel.clearSelectedNode() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SugorokuMapContent(
    uiState: SugorokuMapUiState,
    onBack: () -> Unit,
    onSelectNode: (SugorokuNode) -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allNodes = HokkaidoAct1Route.nodes
    val tileCentersDp = HokkaidoBoardTrack.tileCenters
    val density = LocalDensity.current.density

    // Unified zoom scale: applied uniformly to entire canvas via withTransform
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var hasCenteredInitially by remember { mutableStateOf(false) }
    var isDebugScrollUnlocked by remember { mutableStateOf(false) }

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
                    // Temporary debug toggle for developer inspection across all 2,000 nodes
                    IconButton(
                        onClick = { isDebugScrollUnlocked = !isDebugScrollUnlocked },
                        modifier = Modifier.testTag("DebugScrollLockButton"),
                    ) {
                        Text(
                            text = if (isDebugScrollUnlocked) "🔓全域" else "🔒制限",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDebugScrollUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        )
                    }
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
                .background(Color(0xFF81D4FA)), // Calm blue ocean as base backdrop
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()

            val currentStep = uiState.progress.currentStepIndex
            val currentNode = uiState.currentNode

            // Clamp pan offset so camera cannot scroll too far ahead of player (Fog of War)
            fun clampPan(offset: Offset, scale: Float): Offset {
                if (tileCentersDp.isEmpty()) return offset
                val (minX, maxX, minY, maxY) = if (isDebugScrollUnlocked) {
                    listOf(
                        tileCentersDp.minOf { it.x * density },
                        tileCentersDp.maxOf { it.x * density },
                        tileCentersDp.minOf { it.y * density },
                        tileCentersDp.maxOf { it.y * density },
                    )
                } else {
                    val allowedScrollStep = (currentStep + 16).coerceAtMost(tileCentersDp.lastIndex)
                    val visiblePts = tileCentersDp.subList(0, allowedScrollStep + 1)
                    listOf(
                        visiblePts.minOf { it.x * density },
                        visiblePts.maxOf { it.x * density },
                        visiblePts.minOf { it.y * density },
                        visiblePts.maxOf { it.y * density },
                    )
                }

                val margin = if (isDebugScrollUnlocked) 600f else 320f
                val screenMinX = containerWidth - (maxX * scale) - margin
                val screenMaxX = -(minX * scale) + containerWidth + margin
                val screenMinY = containerHeight - (maxY * scale) - margin
                val screenMaxY = -(minY * scale) + containerHeight + margin

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

            fun centerOnStep(stepIndex: Int) {
                val ptDp = tileCentersDp.getOrNull(stepIndex) ?: Offset.Zero
                val targetX = ptDp.x * density * zoomScale
                val targetY = ptDp.y * density * zoomScale
                val newPan = Offset(
                    x = containerWidth / 2f - targetX,
                    y = containerHeight / 2f - targetY,
                )
                panOffset = clampPan(newPan, zoomScale)
            }

            // Smooth zoom centered at screen pivot point (pinch centroid or screen center)
            fun zoomAt(pivot: Offset, factor: Float, panDelta: Offset = Offset.Zero) {
                val oldScale = zoomScale
                val newScale = (oldScale * factor).coerceIn(0.75f, 1.45f)
                val scaleRatio = newScale / oldScale
                val targetPan = pivot - (pivot - panOffset) * scaleRatio + panDelta
                zoomScale = newScale
                panOffset = clampPan(targetPan, newScale)
            }

            // Centering on initial load
            LaunchedEffect(currentStep, containerWidth, containerHeight) {
                if (!hasCenteredInitially && containerWidth > 0 && containerHeight > 0) {
                    centerOnStep(currentStep)
                    hasCenteredInitially = true
                }
            }

            // Floating breathing animation for player piece
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

            // 2D Sugoroku Board Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("SugorokuCanvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            zoomAt(centroid, zoom, pan)
                        }
                    }
                    .pointerInput(tileCentersDp, zoomScale, panOffset, currentStep, isDebugScrollUnlocked) {
                        detectTapGestures { tapOffset ->
                            val worldX = (tapOffset.x - panOffset.x) / zoomScale
                            val worldY = (tapOffset.y - panOffset.y) / zoomScale
                            val threshold = 34.dp.toPx()

                            val maxVisibleTrackStep = if (isDebugScrollUnlocked) {
                                allNodes.lastIndex
                            } else {
                                (currentStep + 40).coerceAtMost(allNodes.lastIndex)
                            }
                            for (i in 0..maxVisibleTrackStep) {
                                val ptDp = tileCentersDp[i]
                                val sx = ptDp.x * density
                                val sy = ptDp.y * density
                                val dx = worldX - sx
                                val dy = worldY - sy
                                if (dx * dx + dy * dy <= threshold * threshold) {
                                    onSelectNode(allNodes[i])
                                    break
                                }
                            }
                        }
                    },
            ) {
                // The road is drawn far ahead (up to 40 steps ahead, or entire route in debug) so it extends off-screen naturally!
                val visibleTrackStep = if (isDebugScrollUnlocked) {
                    allNodes.lastIndex
                } else {
                    (currentStep + 40).coerceAtMost(allNodes.lastIndex)
                }

                // Unified transform: scale and translate applied equally to landscape and tiles
                withTransform({
                    translate(panOffset.x, panOffset.y)
                    scale(zoomScale, zoomScale, Offset.Zero)
                }) {
                    // 1. Natural real geography:
                    // Hakodate to Lake Onuma (step 0-99) is INLAND (Route 5 passes inland plains & pass).
                    // Coastline (Uchiura/Volcano Bay) appears when reaching Mori town (~step 100).
                    drawGeographicalLandscape(
                        tileCentersDp = tileCentersDp,
                        visibleStepCount = visibleTrackStep + 1,
                        density = density,
                    )

                    // 2. Wooden Bridge Connectors extending far ahead
                    drawConnectingBridges(
                        tileCentersDp = tileCentersDp,
                        visibleStepCount = visibleTrackStep + 1,
                        density = density,
                    )

                    // 3. Equidistant Square Sugoroku Tiles
                    drawSugorokuTiles(
                        nodes = allNodes,
                        tileCentersDp = tileCentersDp,
                        currentStep = currentStep,
                        visibleStepCount = visibleTrackStep + 1,
                        density = density,
                    )

                    // 4. Cute Traveler Avatar Piece at current step
                    val curPtDp = tileCentersDp.getOrNull(currentStep) ?: Offset.Zero
                    val curX = curPtDp.x * density
                    val curY = curPtDp.y * density

                    drawTravelerPiece(
                        x = curX,
                        y = curY + 12.dp.toPx() + playerFloatY * 1.5f,
                        shadowY = curY + 28.dp.toPx(),
                    )
                }
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

            // Accurate Wikipedia-standard Hokkaido Silhouette Mini-map in Bottom-Left
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
                        centerOnStep(currentStep)
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
                        zoomAt(Offset(containerWidth / 2f, containerHeight / 2f), 1.20f)
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
                        zoomAt(Offset(containerWidth / 2f, containerHeight / 2f), 1f / 1.20f)
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
            onDismissRequest = onDismissDialog,
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
                TextButton(onClick = onDismissDialog) {
                    Text("閉じる")
                }
            },
        )
    }
}

/**
 * Draws real Hokkaido geography:
 * - Steps 0 to 15 (Hakodate city): Hakodate Port & Bay on the LEFT (west).
 * - Steps 16 to 55 (Nanae to Lake Onuma pass): INLAND lush countryside & Lake Onuma.
 * - Steps 56 to 185 (Mori, Yakumo, Oshamambe): Uchiura (Volcano) Bay on the RIGHT (east)!
 * - Steps 186 to 220 (Kuromatsunai pass): INLAND beech forest watershed.
 * - Steps 221 to 1206 (Suttsu, Shakotan, Otaru, Orolon line to Wakkanai): Sea of Japan on the LEFT!
 * - Steps 1207 to 1848 (Soya Cape to Abashiri & Shari): Sea of Okhotsk on the RIGHT!
 * - Steps 1849 to 1893 (Shiretoko Utoro): Sea of Okhotsk on the LEFT.
 * - Steps 1894 to 1933 (Shiretoko Pass): Alpine mountain summit road around Mt. Rausu.
 * - Steps 1934 to 1999 (Rausu to Aidomari terminus): Nemuro Strait on the RIGHT (facing Kunashiri Island)!
 */
private fun DrawScope.drawGeographicalLandscape(
    tileCentersDp: List<Offset>,
    visibleStepCount: Int,
    density: Float,
) {
    if (tileCentersDp.size < 2) return
    val count = visibleStepCount.coerceAtMost(tileCentersDp.size)

    val grassGreen = Color(0xFFA5D6A7)
    val beachColor = Color(0xFFFFF9C4)
    val waveColor = Color(0x70FFFFFF)
    val lakeBlue = Color(0xFF4FC3F7)

    val leftPoints = ArrayList<Offset>(count)
    val rightPoints = ArrayList<Offset>(count)
    val leftCoastFlags = BooleanArray(count)
    val rightCoastFlags = BooleanArray(count)

    val coastalThreshold = 180.dp.toPx()

    for (i in 0 until count) {
        val ptDp = tileCentersDp[i]
        val sx = ptDp.x * density
        val sy = ptDp.y * density

        val nextPtDp = if (i < tileCentersDp.lastIndex) tileCentersDp[i + 1] else tileCentersDp[i]
        val prevPtDp = if (i > 0) tileCentersDp[i - 1] else tileCentersDp[i]
        val dirX = nextPtDp.x - prevPtDp.x
        val dirY = nextPtDp.y - prevPtDp.y
        val angle = atan2(dirY, dirX)

        val leftNormX = cos(angle - Math.PI.toFloat() / 2f)
        val leftNormY = sin(angle - Math.PI.toFloat() / 2f)
        val rightNormX = cos(angle + Math.PI.toFloat() / 2f)
        val rightNormY = sin(angle + Math.PI.toFloat() / 2f)

        val leftDist = getCoastDistance(i, isLeft = true)
        val rightDist = getCoastDistance(i, isLeft = false)

        leftPoints.add(Offset(sx + leftNormX * leftDist, sy + leftNormY * leftDist))
        rightPoints.add(Offset(sx + rightNormX * rightDist, sy + rightNormY * rightDist))

        // Coastline line includes the smooth transitional boundary turning toward the horizon
        leftCoastFlags[i] = leftDist <= inlandWidthThreshold(i, isLeft = true)
        rightCoastFlags[i] = rightDist <= inlandWidthThreshold(i, isLeft = false)
    }

    // 1. Draw broad green Hokkaido land polygon
    val landPath = Path()
    if (leftPoints.isNotEmpty()) {
        landPath.moveTo(leftPoints[0].x, leftPoints[0].y)
        for (i in 1 until count) {
            landPath.lineTo(leftPoints[i].x, leftPoints[i].y)
        }
        for (i in count - 1 downTo 0) {
            landPath.lineTo(rightPoints[i].x, rightPoints[i].y)
        }
        landPath.close()
    }
    drawPath(landPath, color = grassGreen)

    // 2. Coastal beaches and shoreline waves along the sea side
    drawCoastlineBeaches(leftPoints, leftCoastFlags, beachColor, waveColor)
    drawCoastlineBeaches(rightPoints, rightCoastFlags, beachColor, waveColor)

    // 3. Lake Onuma inland accent (near step 32)
    if (count > 25) {
        val onumaPt = tileCentersDp[minOf(32, count - 1)]
        val ox = onumaPt.x * density + 65.dp.toPx()
        val oy = onumaPt.y * density
        drawCircle(
            color = lakeBlue,
            radius = 32.dp.toPx(),
            center = Offset(ox, oy),
        )
        drawCircle(
            color = grassGreen,
            radius = 7.dp.toPx(),
            center = Offset(ox - 8.dp.toPx(), oy - 4.dp.toPx()),
        )
    }
}

private fun inlandWidthThreshold(step: Int, isLeft: Boolean): Float {
    // If the step is part of the coastline corridor or the transition curve to the horizon, include it
    return 750f * 3f
}

private fun getCoastDistance(
    step: Int,
    isLeft: Boolean,
): Float {
    val coastal = 48f * 3f // 48dp * density approx, standard tile radius 29dp + 19dp beach
    val inland = 850f * 3f

    fun lerpDist(a: Float, b: Float, t: Float) = a + (b - a) * t

    return if (isLeft) {
        when (step) {
            in 0..13 -> coastal
            14 -> lerpDist(coastal, inland, 0.40f)
            15 -> lerpDist(coastal, inland, 0.80f)
            in 16..219 -> inland
            220 -> lerpDist(inland, coastal, 0.50f)
            in 221..1205 -> coastal
            1206 -> lerpDist(coastal, inland, 0.50f)
            in 1207..1847 -> inland
            1848 -> lerpDist(inland, coastal, 0.50f)
            in 1849..1892 -> coastal
            1893 -> lerpDist(coastal, inland, 0.50f)
            else -> inland
        }
    } else {
        when (step) {
            in 0..54 -> inland
            55 -> lerpDist(inland, coastal, 0.50f)
            in 56..184 -> coastal
            185 -> lerpDist(coastal, inland, 0.50f)
            in 186..1205 -> inland
            1206 -> lerpDist(inland, coastal, 0.50f)
            in 1207..1847 -> coastal
            1848 -> lerpDist(coastal, inland, 0.50f)
            in 1849..1932 -> inland
            1933 -> lerpDist(inland, coastal, 0.50f)
            in 1934..1999 -> coastal
            else -> inland
        }
    }
}

private fun DrawScope.drawCoastlineBeaches(
    points: List<Offset>,
    coastFlags: BooleanArray,
    beachColor: Color,
    waveColor: Color,
) {
    var inSegment = false
    val beachPath = Path()

    for (i in points.indices) {
        if (coastFlags[i]) {
            if (!inSegment) {
                beachPath.moveTo(points[i].x, points[i].y)
                inSegment = true
            } else {
                beachPath.lineTo(points[i].x, points[i].y)
            }
        } else {
            inSegment = false
        }
    }

    if (!beachPath.isEmpty) {
        drawPath(
            path = beachPath,
            color = beachColor,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawPath(
            path = beachPath,
            color = waveColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/**
 * Draws wooden bridge deck between step centers.
 */
private fun DrawScope.drawConnectingBridges(
    tileCentersDp: List<Offset>,
    visibleStepCount: Int,
    density: Float,
) {
    val bridgeWidth = 18.dp.toPx()
    val bridgeColor = Color(0xFFD7CCC8)
    val bridgeBorderColor = Color(0xFF8D6E63)

    val count = visibleStepCount.coerceAtMost(tileCentersDp.size)
    for (i in 0 until count - 1) {
        val p1 = tileCentersDp[i]
        val p2 = tileCentersDp[i + 1]
        val sx1 = p1.x * density
        val sy1 = p1.y * density
        val sx2 = p2.x * density
        val sy2 = p2.y * density

        // Border
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
 * Draws the 58dp square Sugoroku tiles.
 * Tile size is 58dp and step distance is 80dp, guaranteeing a clean 22dp gap without overlapping.
 */
private fun DrawScope.drawSugorokuTiles(
    nodes: List<SugorokuNode>,
    tileCentersDp: List<Offset>,
    currentStep: Int,
    visibleStepCount: Int,
    density: Float,
) {
    val tileSizePx = 58.dp.toPx()
    val cornerRadius = CornerRadius(13.dp.toPx(), 13.dp.toPx())

    val labelPaint = Paint().apply {
        color = android.graphics.Color.rgb(33, 33, 33)
        textSize = 11.5.dp.toPx()
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    val stepNumberPaint = Paint().apply {
        color = android.graphics.Color.rgb(120, 144, 156)
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

    val count = visibleStepCount.coerceAtMost(tileCentersDp.size)
    for (i in 0 until count) {
        val node = nodes[i]
        val ptDp = tileCentersDp[i]
        val cx = ptDp.x * density
        val cy = ptDp.y * density

        val half = tileSizePx / 2f
        val left = cx - half
        val top = cy - half

        val isVisited = i < currentStep
        val isCurrent = i == currentStep

        // 1. Soft drop shadow beneath tile
        drawRoundRect(
            color = Color(0x38000000),
            topLeft = Offset(left + 2.dp.toPx(), top + 3.dp.toPx()),
            size = Size(tileSizePx, tileSizePx),
            cornerRadius = cornerRadius,
        )

        // 2. Tile background & border colors
        val (bgColor, borderColor, borderWidth) = when {
            isCurrent -> Triple(Color(0xFFFFF176), Color(0xFFF57F17), 3.dp.toPx())
            isVisited -> Triple(Color(0xFFFFE0B2), Color(0xFFFB8C00), 2.dp.toPx())
            node.type == SugorokuNodeType.SCENIC_SPOT -> Triple(Color(0xFFFFF9C4), Color(0xFFFBC02D), 2.5.dp.toPx())
            else -> Triple(Color(0xFFFFFFFF), Color(0xFFB0BEC5), 1.8.dp.toPx())
        }

        // Draw tile body
        drawRoundRect(
            color = bgColor,
            topLeft = Offset(left, top),
            size = Size(tileSizePx, tileSizePx),
            cornerRadius = cornerRadius,
        )
        // Draw tile border
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = Size(tileSizePx, tileSizePx),
            cornerRadius = cornerRadius,
            style = Stroke(width = borderWidth),
        )

        // 3. Tile Contents: Step number on top
        drawContext.canvas.nativeCanvas.drawText(
            "#${i + 1}",
            cx,
            top + 13.dp.toPx(),
            stepNumberPaint,
        )

        if (isCurrent) {
            // For current tile: player avatar stands inside tile.
            // Draw a crisp floating location pill badge beneath tile so text is never obscured!
            val badgeText = node.name
            val badgePaint = Paint().apply {
                color = android.graphics.Color.rgb(33, 33, 33)
                textSize = 10.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val textWidth = badgePaint.measureText(badgeText)
            val badgeWidth = textWidth + 14.dp.toPx()
            val badgeHeight = 18.dp.toPx()
            val badgeLeft = cx - badgeWidth / 2f
            val badgeTop = top + tileSizePx + 4.dp.toPx()

            // Badge shadow & white pill background
            drawRoundRect(
                color = Color(0x33000000),
                topLeft = Offset(badgeLeft + 1.dp.toPx(), badgeTop + 1.5.dp.toPx()),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(badgeLeft, badgeTop),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFF57F17),
                topLeft = Offset(badgeLeft, badgeTop),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx()),
            )
            drawContext.canvas.nativeCanvas.drawText(
                badgeText,
                cx,
                badgeTop + 13.dp.toPx(),
                badgePaint,
            )
        } else {
            // Normal tile: name in center (with automatic multi-line wrapping so names are never truncated)
            drawTileLabel(
                text = node.name,
                cx = cx,
                cy = top + 28.dp.toPx(),
                maxTextWidthPx = 48.dp.toPx(),
                baseTextSizePx = 11.dp.toPx(),
                paint = labelPaint,
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
                top + 46.dp.toPx(),
                iconPaint,
            )
        }
    }
}

/**
 * Draws tile label text gracefully without truncation, wrapping into 2 lines if needed.
 */
private fun DrawScope.drawTileLabel(
    text: String,
    cx: Float,
    cy: Float,
    maxTextWidthPx: Float,
    baseTextSizePx: Float,
    paint: Paint,
) {
    paint.textSize = baseTextSizePx
    val textWidth = paint.measureText(text)
    if (textWidth <= maxTextWidthPx) {
        drawContext.canvas.nativeCanvas.drawText(text, cx, cy, paint)
    } else if (text.length >= 4) {
        val (line1, line2) = splitJapaneseTileName(text)
        paint.textSize = (baseTextSizePx * 0.82f).coerceAtLeast(8.dp.toPx())
        val lineHeight = paint.fontSpacing * 0.88f
        drawContext.canvas.nativeCanvas.drawText(line1, cx, cy - lineHeight * 0.42f, paint)
        drawContext.canvas.nativeCanvas.drawText(line2, cx, cy + lineHeight * 0.58f, paint)
    } else {
        paint.textSize = baseTextSizePx * (maxTextWidthPx / textWidth)
        drawContext.canvas.nativeCanvas.drawText(text, cx, cy, paint)
    }
}

/**
 * Gracefully splits Japanese landmark and street names at natural word boundaries.
 */
private fun splitJapaneseTileName(text: String): Pair<String, String> {
    val keywords = listOf(
        "タワー前", "交差点", "公園前", "駅前通", "本町通", "温泉街", "市街地",
        "タワー", "公園", "駅前", "本町", "温泉", "案内所", "展望台", "灯台", "漁港", "海岸", "通り", "神社", "寺院", "岬",
    )
    for (kw in keywords) {
        val idx = text.indexOf(kw)
        if (idx in 2..4) {
            return Pair(text.substring(0, idx), text.substring(idx))
        }
    }
    val mid = (text.length + 1) / 2
    return Pair(text.substring(0, mid), text.substring(mid))
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
