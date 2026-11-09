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
    val allNodes = HokkaidoAct1Route.nodes
    val tileCentersDp = HokkaidoBoardTrack.tileCenters
    val density = LocalDensity.current.density

    // Unified zoom scale: applied uniformly to entire canvas via withTransform
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var hasCenteredInitially by remember { mutableStateOf(false) }
    var isDebugScrollUnlocked by remember { mutableStateOf(false) }

    fun centerOnStep(stepIndex: Int, containerWidth: Float, containerHeight: Float) {
        val ptDp = tileCentersDp.getOrNull(stepIndex) ?: Offset.Zero
        val targetX = ptDp.x * density * zoomScale
        val targetY = ptDp.y * density * zoomScale
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
                .background(Color(0xFFE8F5E9)), // Warm natural Hokkaido land tone
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

            // Clamp pan offset so camera cannot scroll too far ahead of player (Fog of War)
            fun clampPan(offset: Offset): Offset {
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

            // 2D Sugoroku Board Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("SugorokuCanvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.75f, 1.45f)
                            panOffset = clampPan(panOffset + pan)
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
                                    viewModel.selectNode(allNodes[i])
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
                        zoomScale = (zoomScale * 1.15f).coerceIn(0.75f, 1.45f)
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
                        zoomScale = (zoomScale / 1.15f).coerceIn(0.75f, 1.45f)
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
    val forestGreen = Color(0xFF81C784)
    val seaBlue = Color(0xFF81D4FA)
    val beachColor = Color(0xFFFFF9C4)
    val lakeBlue = Color(0xFF4FC3F7)

    // 1. Draw broad green Hokkaido land corridor around track
    for (i in 0 until count - 1) {
        val p1 = tileCentersDp[i]
        val p2 = tileCentersDp[i + 1]
        val sx1 = p1.x * density
        val sy1 = p1.y * density
        val sx2 = p2.x * density
        val sy2 = p2.y * density

        drawLine(
            color = grassGreen,
            start = Offset(sx1, sy1),
            end = Offset(sx2, sy2),
            strokeWidth = 320.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }

    // 2. Coastal Seas:
    // Hakodate Port (Left sea)
    drawCoastalSea(tileCentersDp, 0, minOf(16, count), isSeaOnRight = false, density, beachColor, seaBlue)

    // Volcano Bay (Mori to Oshamambe, Right sea)
    drawCoastalSea(tileCentersDp, 56, minOf(186, count), isSeaOnRight = true, density, beachColor, seaBlue)

    // Sea of Japan & Orolon line (Suttsu to Wakkanai, Left sea)
    drawCoastalSea(tileCentersDp, 221, minOf(1207, count), isSeaOnRight = false, density, beachColor, seaBlue)

    // Okhotsk Sea (Soya Cape to Shari, Right sea)
    drawCoastalSea(tileCentersDp, 1207, minOf(1849, count), isSeaOnRight = true, density, beachColor, seaBlue)

    // Shiretoko Utoro (Left sea)
    drawCoastalSea(tileCentersDp, 1849, minOf(1894, count), isSeaOnRight = false, density, beachColor, seaBlue)

    // Nemuro Strait (Rausu to Aidomari, Right sea)
    drawCoastalSea(tileCentersDp, 1934, minOf(2000, count), isSeaOnRight = true, density, beachColor, seaBlue)

    // 3. Lake Onuma inland accent (near step 32)
    if (count > 25) {
        val onumaPt = tileCentersDp[minOf(32, count - 1)]
        val ox = onumaPt.x * density + 65.dp.toPx()
        val oy = onumaPt.y * density
        drawCircle(
            color = lakeBlue,
            radius = 28.dp.toPx(),
            center = Offset(ox, oy),
        )
        drawCircle(
            color = forestGreen,
            radius = 6.dp.toPx(),
            center = Offset(ox - 8.dp.toPx(), oy - 4.dp.toPx()),
        )
    }
}

/**
 * Common helper to draw coastal beach and open sea smoothly along a track section.
 */
private fun DrawScope.drawCoastalSea(
    tileCentersDp: List<Offset>,
    startIdx: Int,
    endIdx: Int,
    isSeaOnRight: Boolean,
    density: Float,
    beachColor: Color,
    seaColor: Color,
) {
    if (startIdx >= endIdx || startIdx >= tileCentersDp.size) return
    val seaPath = Path()
    val beachPath = Path()
    var first = true

    val sign = if (isSeaOnRight) 1.0f else -1.0f

    for (i in startIdx until endIdx) {
        val ptDp = tileCentersDp[i]
        val sx = ptDp.x * density
        val sy = ptDp.y * density

        val nextPtDp = if (i < tileCentersDp.lastIndex) tileCentersDp[i + 1] else tileCentersDp[i]
        val prevPtDp = if (i > 0) tileCentersDp[i - 1] else tileCentersDp[i]
        val dirX = nextPtDp.x - prevPtDp.x
        val dirY = nextPtDp.y - prevPtDp.y
        val angle = atan2(dirY, dirX)
        val normalAngle = angle + (Math.PI / 2.0).toFloat() * sign
        val normX = cos(normalAngle)
        val normY = sin(normalAngle)

        val beachEdgeX = sx + normX * 42.dp.toPx()
        val beachEdgeY = sy + normY * 42.dp.toPx()

        if (first) {
            beachPath.moveTo(beachEdgeX, beachEdgeY)
            seaPath.moveTo(beachEdgeX + normX * 12.dp.toPx(), beachEdgeY + normY * 12.dp.toPx())
            first = false
        } else {
            beachPath.lineTo(beachEdgeX, beachEdgeY)
            seaPath.lineTo(beachEdgeX + normX * 12.dp.toPx(), beachEdgeY + normY * 12.dp.toPx())
        }
    }

    drawPath(
        beachPath,
        beachColor,
        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
    )
    drawPath(
        seaPath,
        seaColor,
        style = Stroke(width = 280.dp.toPx(), cap = StrokeCap.Round),
    )
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

        // Tile Contents: Name label in center
        val displayName = if (node.name.length > 4) node.name.take(4) else node.name
        drawContext.canvas.nativeCanvas.drawText(
            displayName,
            cx,
            top + 28.dp.toPx(),
            labelPaint,
        )

        // Tile Contents: Icon on bottom (only if not current step, to avoid overlap with piece)
        if (!isCurrent) {
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
