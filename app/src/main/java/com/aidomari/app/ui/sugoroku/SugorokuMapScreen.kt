package com.aidomari.app.ui.sugoroku

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.util.LruCache
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.R
import com.aidomari.app.core.debug.SugorokuDebugPreferences
import com.aidomari.app.domain.model.HighwayCategory
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.HokkaidoRoadNetwork
import com.aidomari.app.domain.model.RoadsideLandmark
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.model.SugorokuNodeType
import com.aidomari.app.domain.model.TravelerCompanion
import com.aidomari.app.domain.util.SugorokuWalkingCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

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
        onAddDistance = { viewModel.addDistance(it) },
        onAdvanceToNextAnchor = { viewModel.advanceToNextAnchor() },
        onResetProgress = { viewModel.resetProgress() },
        onSetSpeedMultiplier = { viewModel.setSpeedMultiplier(it) },
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
    onAddDistance: (Float) -> Unit = {},
    onAdvanceToNextAnchor: () -> Unit = {},
    onResetProgress: () -> Unit = {},
    onSetSpeedMultiplier: (Float) -> Unit = {},
    theme: SugorokuMapTheme = SugorokuMapTheme.Default,
    modifier: Modifier = Modifier,
) {
    val allNodes = HokkaidoAct1Route.nodes
    val tileCentersDp = HokkaidoBoardTrack.tileCenters
    val density = LocalDensity.current.density
    val context = LocalContext.current

    // Lazy LruCache bitmap loader for roadside landmarks so off-screen assets never decode on startup
    val companionBitmap = remember(uiState.companion.drawableResId) {
        BitmapFactory.decodeResource(context.resources, uiState.companion.drawableResId)?.asImageBitmap()
    }
    val landmarkBitmapCache = remember(context) {
        object : LruCache<Int, ImageBitmap>(48) {}
    }
    val getLandmarkBitmap: (Int) -> ImageBitmap? = remember(context, landmarkBitmapCache) {
        { resId ->
            landmarkBitmapCache.get(resId)
                ?: BitmapFactory.decodeResource(context.resources, resId)
                    ?.asImageBitmap()
                    ?.also { landmarkBitmapCache.put(resId, it) }
        }
    }
    val umigarasuFrames = remember(context) {
        listOf(
            R.drawable.sky_umigarasu_f1,
            R.drawable.sky_umigarasu_f2,
            R.drawable.sky_umigarasu_f3,
        ).mapNotNull { resId ->
            BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
        }
    }

    // Pre-parse official GSI Hokkaido coastline paths
    val mainlandPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.MAINLAND_D).toPath() }
    val okushiriPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.OKUSHIRI_D).toPath() }
    val rishiriPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.RISHIRI_D).toPath() }
    val rebunPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.REBUN_D).toPath() }
    val teuriPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.TEURI_D).toPath() }
    val yagishiriPath = remember { PathParser().parsePathString(HokkaidoSvgPaths.YAGISHIRI_D).toPath() }
    val hokkaidoPaths = remember { listOf(mainlandPath, okushiriPath, rishiriPath, rebunPath, teuriPath, yagishiriPath) }

    // Pre-parse official MLIT Hokkaido major inland lake paths
    val lakesPath = remember {
        PathParser().parsePathString(HokkaidoSvgPaths.LAKES_D).toPath().apply {
            fillType = PathFillType.EvenOdd
        }
    }

    // Pre-build spatially indexed road paths + pre-merged wide/mini-map SVG paths once
    val highDensityRoadSegments = remember(context) { HokkaidoRoadNetwork.getHighDensitySegments(context) }
    val worldScaleFactor = HokkaidoBoardTrack.WORLD_SCALE * density
    val roadPathCache = remember(highDensityRoadSegments, worldScaleFactor) {
        val grouped = HashMap<HighwayCategory, MutableList<CachedHighwayPath>>(4)
        val mergedWide = HashMap<HighwayCategory, Path>(4)
        for (cat in HighwayCategory.values()) {
            grouped[cat] = ArrayList()
            mergedWide[cat] = Path()
        }
        for (segment in highDensityRoadSegments) {
            val pts = segment.waypoints
            if (pts.isEmpty()) continue
            val segPath = Path()
            val merged = mergedWide.getValue(segment.category)
            var minX = Float.POSITIVE_INFINITY
            var minY = Float.POSITIVE_INFINITY
            var maxX = Float.NEGATIVE_INFINITY
            var maxY = Float.NEGATIVE_INFINITY
            val firstX = pts[0].x * worldScaleFactor
            val firstY = pts[0].y * worldScaleFactor
            segPath.moveTo(firstX, firstY)
            merged.moveTo(firstX, firstY)
            minX = minOf(minX, firstX)
            minY = minOf(minY, firstY)
            maxX = maxOf(maxX, firstX)
            maxY = maxOf(maxY, firstY)
            for (i in 1 until pts.size) {
                val px = pts[i].x * worldScaleFactor
                val py = pts[i].y * worldScaleFactor
                segPath.lineTo(px, py)
                merged.lineTo(px, py)
                if (px < minX) minX = px
                if (py < minY) minY = py
                if (px > maxX) maxX = px
                if (py > maxY) maxY = py
            }
            grouped.getValue(segment.category).add(
                CachedHighwayPath(
                    category = segment.category,
                    path = segPath,
                    minX = minX,
                    minY = minY,
                    maxX = maxX,
                    maxY = maxY,
                ),
            )
        }
        Pair(grouped, mergedWide)
    }
    val groupedHighwayPaths = roadPathCache.first
    val mergedWideHighwayPaths = roadPathCache.second

    val miniMapRoadPaths = remember(highDensityRoadSegments) {
        val nationalPath = Path()
        val scenicPath = Path()
        for (segment in highDensityRoadSegments) {
            val target = when (segment.category) {
                HighwayCategory.NATIONAL_HIGHWAY -> nationalPath
                HighwayCategory.SCENIC_ROAD -> scenicPath
                else -> continue
            }
            val pts = segment.waypoints
            if (pts.isEmpty()) continue
            target.moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) {
                target.lineTo(pts[i].x, pts[i].y)
            }
        }
        nationalPath to scenicPath
    }

    val renderCache = remember(worldScaleFactor, density) {
        SugorokuRenderCache(worldScaleFactor = worldScaleFactor, density = density)
    }
    val landmarkScratchPath = remember { Path() }

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
                            text = "旅路: ${SugorokuWalkingCalculator.formatDistance(uiState.totalDistanceMeters)}",
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
                    if (com.aidomari.app.BuildConfig.DEBUG) {
                        var showSpeedMenu by remember { mutableStateOf(false) }
                        Box {
                            TextButton(
                                onClick = { showSpeedMenu = true },
                                modifier = Modifier.testTag("DebugSpeedMultiplierButton"),
                            ) {
                                Text(
                                    text = "${uiState.speedMultiplier.toInt()}倍速",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                            ) {
                                SugorokuDebugPreferences.AVAILABLE_SPEED_MULTIPLIERS.forEach { mult ->
                                    DropdownMenuItem(
                                        text = { Text("${mult.toInt()}倍速") },
                                        onClick = {
                                            onSetSpeedMultiplier(mult)
                                            showSpeedMenu = false
                                        },
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onAddDistance(500f) },
                            modifier = Modifier.testTag("DebugAdd500mButton"),
                        ) {
                            Text(
                                text = "+500m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        IconButton(
                            onClick = onAdvanceToNextAnchor,
                            modifier = Modifier.testTag("DebugNextAnchorButton"),
                        ) {
                            Text(
                                text = "次へ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        IconButton(
                            onClick = onResetProgress,
                            modifier = Modifier.testTag("DebugResetButton"),
                        ) {
                            Text(
                                text = "初期化",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        IconButton(
                            onClick = { isDebugScrollUnlocked = !isDebugScrollUnlocked },
                            modifier = Modifier.testTag("DebugScrollLockButton"),
                        ) {
                            Text(
                                text = if (isDebugScrollUnlocked) "全域" else "制限",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebugScrollUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.oceanColor), // Calm blue ocean as base backdrop
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()

            val currentStep = uiState.progress.currentStepIndex
            val currentNode = uiState.currentNode

            // Clamp pan offset so camera cannot scroll too far outside Hokkaido
            fun clampPan(offset: Offset, scale: Float): Offset {
                if (tileCentersDp.isEmpty()) return offset
                val (minX, maxX, minY, maxY) = if (isDebugScrollUnlocked) {
                    listOf(
                        0f,
                        HokkaidoSvgPaths.SVG_WIDTH * HokkaidoBoardTrack.WORLD_SCALE * density,
                        0f,
                        HokkaidoSvgPaths.SVG_HEIGHT * HokkaidoBoardTrack.WORLD_SCALE * density,
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

                val margin = if (isDebugScrollUnlocked) 400f * density else 320f
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

            fun centerOnPlayer() {
                val targetWorldOffset = uiState.playerWorldOffset
                val targetX = targetWorldOffset.x * worldScaleFactor * zoomScale
                val targetY = targetWorldOffset.y * worldScaleFactor * zoomScale
                val newPan = Offset(
                    x = containerWidth / 2f - targetX,
                    y = containerHeight / 2f - targetY,
                )
                panOffset = clampPan(newPan, zoomScale)
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
                val minScale = if (isDebugScrollUnlocked) 0.003f else 0.40f
                val maxScale = 1.60f
                val newScale = (oldScale * factor).coerceIn(minScale, maxScale)
                val scaleRatio = newScale / oldScale
                val targetPan = pivot - (pivot - panOffset) * scaleRatio + panDelta
                zoomScale = newScale
                panOffset = clampPan(targetPan, newScale)
            }

            var birdPatrolCenter by remember { mutableStateOf(Offset.Unspecified) }

            // Centering on initial load
            LaunchedEffect(currentStep, containerWidth, containerHeight) {
                if (!hasCenteredInitially && containerWidth > 0 && containerHeight > 0) {
                    centerOnPlayer()
                    val centeredWorldX = (-panOffset.x + containerWidth / 2f) / zoomScale
                    val centeredWorldY = (-panOffset.y + containerHeight / 2f) / zoomScale
                    birdPatrolCenter = Offset(
                        x = centeredWorldX - 10f * density,
                        y = centeredWorldY - 95f * density,
                    )
                    hasCenteredInitially = true
                }
            }

            // Floating breathing animation & 12s cycle read ONLY inside draw scopes (zero 60fps recomposition!)
            val infiniteTransition = rememberInfiniteTransition(label = "player_piece_anim")
            val playerFloatYState = infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "player_float_y",
            )
            val animPhaseState = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 12000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "living_map_phase",
            )

            val coroutineScope = rememberCoroutineScope()
            var tappedLandmark by remember { mutableStateOf<RoadsideLandmark?>(null) }
            var tapSequence by remember { mutableIntStateOf(0) }
            val tapPopAnim = remember { Animatable(0f) }

            fun triggerLandmarkTap(landmark: RoadsideLandmark) {
                tappedLandmark = landmark
                val currentSeq = ++tapSequence
                coroutineScope.launch {
                    tapPopAnim.snapTo(0f)
                    tapPopAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 480),
                    )
                    tapPopAnim.snapTo(0f)
                    delay(1800)
                    if (tapSequence == currentSeq) {
                        tappedLandmark = null
                    }
                }
            }

            val visibleTrackStep = if (isDebugScrollUnlocked) {
                allNodes.lastIndex
            } else {
                (currentStep + 40).coerceAtMost(allNodes.lastIndex)
            }

            // Viewport in world coordinates for Frustum Culling & world-space Umigarasu patrol
            val viewLeft = -panOffset.x / zoomScale
            val viewTop = -panOffset.y / zoomScale
            val viewRight = (containerWidth - panOffset.x) / zoomScale
            val viewBottom = (containerHeight - panOffset.y) / zoomScale
            val viewportWorld = Rect(viewLeft, viewTop, viewRight, viewBottom)

            val activeBirdPatrolCenter = if (birdPatrolCenter == Offset.Unspecified) {
                Offset(
                    x = viewportWorld.center.x - 10f * density,
                    y = viewportWorld.center.y - 95f * density,
                )
            } else {
                birdPatrolCenter
            }

            LaunchedEffect(viewportWorld, hasCenteredInitially, containerWidth, containerHeight) {
                if (hasCenteredInitially && containerWidth > 0f && containerHeight > 0f) {
                    val zoomComp = if (zoomScale < 1.0f) {
                        zoomScale.coerceIn(0.10f, 1.0f).pow(-0.55f)
                    } else {
                        1.0f
                    }
                    val offscreenPad = 210f * density * zoomComp
                    val isFarOffscreen = birdPatrolCenter == Offset.Unspecified ||
                        birdPatrolCenter.x < viewportWorld.left - offscreenPad ||
                        birdPatrolCenter.x > viewportWorld.right + offscreenPad ||
                        birdPatrolCenter.y < viewportWorld.top - offscreenPad ||
                        birdPatrolCenter.y > viewportWorld.bottom + offscreenPad
                    if (isFarOffscreen) {
                        birdPatrolCenter = Offset(
                            x = viewportWorld.center.x - 10f * density,
                            y = viewportWorld.center.y - 95f * density,
                        )
                    }
                }
            }

            // Performance-isolated layered Canvas stack:
            // static terrain/roads & tiles never redraw during 60fps landmark/bird animations.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("SugorokuCanvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            zoomAt(centroid, zoom, pan)
                        }
                    }
                    .pointerInput(
                        tileCentersDp,
                        zoomScale,
                        panOffset,
                        currentStep,
                        isDebugScrollUnlocked,
                        uiState.roadsideLandmarks,
                    ) {
                        detectTapGestures { tapOffset ->
                            val worldX = (tapOffset.x - panOffset.x) / zoomScale
                            val worldY = (tapOffset.y - panOffset.y) / zoomScale
                            val threshold = 32.dp.toPx()

                            val maxVisibleTrackStep = if (isDebugScrollUnlocked) {
                                allNodes.lastIndex
                            } else {
                                (currentStep + 40).coerceAtMost(allNodes.lastIndex)
                            }
                            var hitTile = false
                            for (i in 0..maxVisibleTrackStep) {
                                val ptDp = tileCentersDp[i]
                                val sx = ptDp.x * density
                                val sy = ptDp.y * density
                                val dx = worldX - sx
                                val dy = worldY - sy
                                if (dx * dx + dy * dy <= threshold * threshold) {
                                    onSelectNode(allNodes[i])
                                    hitTile = true
                                    break
                                }
                            }

                            if (!hitTile && zoomScale >= 0.06f) {
                                val zoomComp = if (zoomScale < 1.0f) {
                                    zoomScale.coerceIn(0.10f, 1.0f).pow(-0.55f)
                                } else {
                                    1.0f
                                }
                                for (landmark in uiState.roadsideLandmarks.sortedByDescending { it.y }) {
                                    val cx = landmark.x * worldScaleFactor
                                    val cy = landmark.y * worldScaleFactor
                                    val halfW = landmark.type.baseDisplayWidthDp.dp.toPx() *
                                        landmark.scale * zoomComp * 0.5f
                                    val halfH = landmark.type.baseDisplayHeightDp.dp.toPx() *
                                        landmark.scale * zoomComp * 0.5f
                                    if (worldX in (cx - halfW)..(cx + halfW) &&
                                        worldY in (cy - halfH)..(cy + halfH)
                                    ) {
                                        triggerLandmarkTap(landmark)
                                        break
                                    }
                                }
                            }
                        }
                    },
            ) {
                // Layer 1 (Static Geography + Culled GIS Roads): only invalidates on pan/zoom
                Canvas(modifier = Modifier.fillMaxSize()) {
                    withTransform({
                        translate(panOffset.x, panOffset.y)
                        scale(zoomScale, zoomScale, Offset.Zero)
                    }) {
                        drawHokkaidoRealLandscape(
                            paths = hokkaidoPaths,
                            lakesPath = lakesPath,
                            renderCache = renderCache,
                            zoomScale = zoomScale,
                            theme = theme,
                        )
                        drawHokkaidoRoadNetwork(
                            groupedHighwayPaths = groupedHighwayPaths,
                            mergedWideHighwayPaths = mergedWideHighwayPaths,
                            viewportWorld = viewportWorld,
                            renderCache = renderCache,
                            zoomScale = zoomScale,
                            theme = theme,
                        )
                    }
                }

                // Layer 2 (Animated Roadside Landmarks & Ambient Effects): reads animPhase ONLY at draw time
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("SugorokuLandmarkAnimCanvas"),
                ) {
                    withTransform({
                        translate(panOffset.x, panOffset.y)
                        scale(zoomScale, zoomScale, Offset.Zero)
                    }) {
                        drawAnimatedRoadsideLandmarks(
                            landmarks = uiState.roadsideLandmarks,
                            getBitmap = getLandmarkBitmap,
                            ropewayGondolaResId = R.drawable.landmark_ropeway_gondola,
                            worldScaleFactor = worldScaleFactor,
                            viewportWorld = viewportWorld,
                            zoomScale = zoomScale,
                            animPhase = animPhaseState.value,
                            tappedLandmarkId = tappedLandmark?.id,
                            tapPopProgress = tapPopAnim.value,
                            scratchPath = landmarkScratchPath,
                        )
                    }
                }

                // Layer 3 (Sugoroku Square Tiles): static above landmarks, invalidates on pan/zoom/step
                Canvas(modifier = Modifier.fillMaxSize()) {
                    withTransform({
                        translate(panOffset.x, panOffset.y)
                        scale(zoomScale, zoomScale, Offset.Zero)
                    }) {
                        drawSugorokuTiles(
                            nodes = allNodes,
                            tileCentersDp = tileCentersDp,
                            currentStep = currentStep,
                            visibleStepCount = visibleTrackStep + 1,
                            density = density,
                            zoomScale = zoomScale,
                            viewportWorld = viewportWorld,
                            renderCache = renderCache,
                        )
                    }
                }

                // Layer 4 (Animated Player Marker, Speech Bubbles & World-Space 1.0x Umigarasu)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("SugorokuPlayerOverlayCanvas"),
                ) {
                    val playerFloatY = playerFloatYState.value
                    val animPhase = animPhaseState.value
                    withTransform({
                        translate(panOffset.x, panOffset.y)
                        scale(zoomScale, zoomScale, Offset.Zero)
                    }) {
                        val playerWorldOffset = uiState.playerWorldOffset
                        val curX = playerWorldOffset.x * worldScaleFactor
                        val curY = playerWorldOffset.y * worldScaleFactor

                        if (zoomScale < 0.05f) {
                            val beaconRadius = 7.dp.toPx() / zoomScale
                            val ringWidth = 2.dp.toPx() / zoomScale
                            val haloRadius = 12.dp.toPx() / zoomScale

                            drawCircle(
                                color = Color(0xFFFF9800).copy(alpha = 0.35f),
                                radius = haloRadius,
                                center = Offset(curX, curY),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = beaconRadius + ringWidth,
                                center = Offset(curX, curY),
                            )
                            drawCircle(
                                color = Color(0xFFE65100),
                                radius = beaconRadius,
                                center = Offset(curX, curY),
                            )
                            drawCircle(
                                color = Color(0xFFFFF3E0),
                                radius = beaconRadius * 0.45f,
                                center = Offset(curX, curY),
                            )
                        } else {
                            drawCompanionPiece(
                                x = curX,
                                y = curY + 12.dp.toPx() + playerFloatY * 1.5f,
                                shadowY = curY + 14.dp.toPx(),
                                companionBitmap = companionBitmap,
                                companion = uiState.companion,
                            )

                            uiState.nearbyLandmark?.let { landmark ->
                                drawSpeechBubble(
                                    text = landmark.reactionDialogue,
                                    x = curX,
                                    bubbleY = curY - 26.dp.toPx() + playerFloatY * 1.5f,
                                    renderCache = renderCache,
                                )
                            }

                            tappedLandmark?.let { tapped ->
                                if (tapped.id != uiState.nearbyLandmark?.id) {
                                    val zoomComp = if (zoomScale < 1.0f) {
                                        zoomScale.coerceIn(0.10f, 1.0f).pow(-0.55f)
                                    } else {
                                        1.0f
                                    }
                                    val tappedCx = tapped.x * worldScaleFactor
                                    val tappedCy = tapped.y * worldScaleFactor
                                    val halfH = tapped.type.baseDisplayHeightDp.dp.toPx() *
                                        tapped.scale * zoomComp * 0.5f
                                    drawSpeechBubble(
                                        text = tapped.reactionDialogue,
                                        x = tappedCx,
                                        bubbleY = tappedCy - halfH * 0.85f,
                                        renderCache = renderCache,
                                    )
                                }
                            }
                        }

                        // Single world-space Umigarasu gliding at 1.0x map rate with shadow directly underneath
                        drawWorldSpaceUmigarasu(
                            birdFrames = umigarasuFrames,
                            patrolCenterWorld = activeBirdPatrolCenter,
                            phase = animPhase,
                            zoomScale = zoomScale,
                        )
                    }
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
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = currentNode.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Text(
                            text = if (currentNode.roadName.isNotEmpty()) {
                                "${currentNode.municipality}・${currentNode.roadName}"
                            } else {
                                "${currentNode.municipality} を旅しています"
                            },
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
                                text = SugorokuWalkingCalculator.formatDistance(uiState.totalDistanceMeters),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // Bottom-Left Hokkaido Overall Mini-Map
            HokkaidoMiniMap(
                currentStepIndex = currentStep,
                totalSteps = allNodes.size,
                hokkaidoPaths = hokkaidoPaths,
                lakesPath = lakesPath,
                miniMapNationalRoadPath = miniMapRoadPaths.first,
                miniMapScenicRoadPath = miniMapRoadPaths.second,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 20.dp),
                onMiniMapClick = {
                    centerOnPlayer()
                },
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
                        centerOnPlayer()
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
                    val badge = when {
                        node.type == SugorokuNodeType.START_HAKODATE -> "[出発] "
                        node.type == SugorokuNodeType.GOAL_AIDOMARI -> "[終着] "
                        node.isAnchor -> "[アンカー] "
                        node.type == SugorokuNodeType.SCENIC_SPOT -> "[名所] "
                        node.type == SugorokuNodeType.TOWN_CENTER -> "[街] "
                        node.type == SugorokuNodeType.REST_AREA -> "[休憩] "
                        else -> ""
                    }
                    if (badge.isNotEmpty()) {
                        Text(
                            text = badge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 13.sp,
                        )
                    }
                    Text(
                        text = node.name,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (node.roadName.isNotEmpty()) {
                            "自治体: ${node.municipality} (${node.roadName})"
                        } else {
                            "自治体: ${node.municipality}"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isReached) "到達済み（足跡が刻まれています）" else "未踏の地（これから進む道）",
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

internal data class CachedHighwayPath(
    val category: HighwayCategory,
    val path: Path,
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
)

internal fun CachedHighwayPath.intersects(rect: Rect): Boolean =
    maxX >= rect.left && minX <= rect.right && maxY >= rect.top && minY <= rect.bottom

private class SugorokuRenderCache(
    val worldScaleFactor: Float,
    val density: Float,
) {
    private val cornerRadiusWorld = 3.5f * density / worldScaleFactor
    private val coastCornerEffect = PathEffect.cornerPathEffect(cornerRadiusWorld)

    val landBeachStroke = Stroke(width = 3.5f * density / worldScaleFactor, join = StrokeJoin.Round, pathEffect = coastCornerEffect)
    val landWaveStroke = Stroke(width = 1.5f * density / worldScaleFactor, join = StrokeJoin.Round, pathEffect = coastCornerEffect)
    val landBorderStroke = Stroke(width = 0.8f * density / worldScaleFactor, join = StrokeJoin.Round, pathEffect = coastCornerEffect)

    val lakeBeachStroke = Stroke(width = 2.0f * density / worldScaleFactor, join = StrokeJoin.Round, pathEffect = coastCornerEffect)
    val lakeWaveStroke = Stroke(width = 1.0f * density / worldScaleFactor, join = StrokeJoin.Round, pathEffect = coastCornerEffect)
    val lakeBorderStroke = Stroke(width = 0.6f * density / worldScaleFactor, join = StrokeJoin.Round, pathEffect = coastCornerEffect)

    val ridgePath = Path()

    val roadBorderStrokes: Map<HighwayCategory, Stroke> = mapOf(
        HighwayCategory.TRAVEL_ROUTE to Stroke(width = 18f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        HighwayCategory.NATIONAL_HIGHWAY to Stroke(width = 5.5f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        HighwayCategory.SCENIC_ROAD to Stroke(width = 4.8f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        HighwayCategory.PREFECTURAL_ROAD to Stroke(width = 4.0f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    val roadSurfaceStrokes: Map<HighwayCategory, Stroke> = mapOf(
        HighwayCategory.TRAVEL_ROUTE to Stroke(width = 14f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        HighwayCategory.NATIONAL_HIGHWAY to Stroke(width = 4.0f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        HighwayCategory.SCENIC_ROAD to Stroke(width = 3.3f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        HighwayCategory.PREFECTURAL_ROAD to Stroke(width = 2.7f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    val highwayDashStroke = Stroke(
        width = 0.6f * density,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * density, 4f * density)),
    )
    val travelDashStroke = Stroke(
        width = 2.0f * density,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * density, 8f * density)),
    )

    val labelPaint = Paint().apply {
        color = android.graphics.Color.rgb(33, 33, 33)
        textSize = 10f * density
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val stepNumberPaint = Paint().apply {
        color = android.graphics.Color.rgb(120, 144, 156)
        textSize = 8.5f * density
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val badgePaint = Paint().apply {
        color = android.graphics.Color.rgb(33, 33, 33)
        textSize = 10f * density
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    val bubbleTextPaint = Paint().apply {
        color = android.graphics.Color.rgb(33, 33, 33)
        textSize = 9.5f * density
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val bubbleFillPaint = Paint().apply {
        color = android.graphics.Color.rgb(255, 255, 255)
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    val bubbleBorderPaint = Paint().apply {
        color = android.graphics.Color.rgb(255, 183, 77)
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }
    val bubbleTextBounds = android.graphics.Rect()
    val bubbleRectF = android.graphics.RectF()
    val bubbleTailPath = android.graphics.Path()
}

/**
 * Draws real Hokkaido landmass and inland lakes traced directly from official GSI and MLIT data.
 * Outer area remains ocean backdrop; inner paths become lush land with sand beach & coastal surf.
 * Inland lakes are carved out with blue water, sandy beaches, wave foam, and coastline borders.
 */
private fun DrawScope.drawHokkaidoRealLandscape(
    paths: List<Path>,
    lakesPath: Path,
    renderCache: SugorokuRenderCache,
    zoomScale: Float,
    theme: SugorokuMapTheme = SugorokuMapTheme.Default,
) {
    val scaleFactor = renderCache.worldScaleFactor
    scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
        paths.forEach { path ->
            drawPath(path = path, color = theme.landColor)
        }

        paths.firstOrNull()?.let { mainland ->
            clipPath(mainland) {
                drawRegionalTerrainWashes(renderCache = renderCache, zoomScale = zoomScale)
            }
        }

        paths.forEach { path ->
            drawPath(path = path, color = theme.beachColor, style = renderCache.landBeachStroke)
            drawPath(path = path, color = theme.waveColor, style = renderCache.landWaveStroke)
            drawPath(path = path, color = theme.borderColor, style = renderCache.landBorderStroke)
        }

        drawPath(path = lakesPath, color = theme.lakeWaterColor)
        drawPath(path = lakesPath, color = theme.beachColor, style = renderCache.lakeBeachStroke)
        drawPath(path = lakesPath, color = theme.waveColor, style = renderCache.lakeWaveStroke)
        drawPath(path = lakesPath, color = theme.borderColor, style = renderCache.lakeBorderStroke)
    }
}

/**
 * Paints soft watercolor woodland and pastoral washes plus hand-drawn brush-pen mountain ridges
 * across Hokkaido's real geographic zones so zoomed-out maps keep rich picture-book texture.
 */
private fun DrawScope.drawRegionalTerrainWashes(
    renderCache: SugorokuRenderCache,
    zoomScale: Float,
) {
    val scaleFactor = renderCache.worldScaleFactor
    val woodlandWash = Color(0xFFC4ECA8).copy(alpha = 0.34f)
    val pastureWash = Color(0xFFDEF6C8).copy(alpha = 0.34f)
    val ridgeInk = Color(0xFF5FA374).copy(alpha = 0.25f)

    // Woodland & mountain zones (cx, cy, rx, ry in Hokkaido SVG space 0..612 x 0..387)
    val woodlandZones = listOf(
        // Local corridor hills & parks
        floatArrayOf(184.0f, 345.3f, 1.3f, 1.4f), // Mt. Hakodate
        floatArrayOf(187.1f, 341.1f, 1.2f, 1.1f), // Goryokaku Park
        floatArrayOf(180.5f, 323.5f, 6.5f, 5.5f), // Onuma / Komagatake woods
        // Broad regional mountain & forest belts
        floatArrayOf(155.0f, 322.0f, 17.0f, 24.0f), // Oshima Peninsula interior hills
        floatArrayOf(161.0f, 266.0f, 12.0f, 14.0f), // Kuromatsunai beech forest
        floatArrayOf(172.0f, 219.0f, 14.0f, 12.0f), // Shakotan Mountains
        floatArrayOf(190.0f, 250.0f, 20.0f, 17.0f), // Niseko / Mt. Yotei range
        floatArrayOf(212.0f, 240.0f, 16.0f, 14.0f), // Sapporo / Shikotsu massif
        floatArrayOf(316.0f, 178.0f, 36.0f, 30.0f), // Daisetsuzan central highlands
        floatArrayOf(318.0f, 266.0f, 20.0f, 38.0f), // Hidaka mountain spine
        floatArrayOf(302.0f, 94.0f, 26.0f, 32.0f), // Kitami Mountains
        floatArrayOf(412.0f, 186.0f, 24.0f, 20.0f), // Akan / Mashu highlands
        floatArrayOf(465.0f, 141.0f, 18.0f, 14.0f), // Shiretoko range
    )
    for (z in woodlandZones) {
        drawOval(
            color = woodlandWash,
            topLeft = Offset(z[0] - z[2], z[1] - z[3]),
            size = Size(z[2] * 2f, z[3] * 2f),
        )
    }

    // Pastoral & meadow plains
    val pastureZones = listOf(
        floatArrayOf(182.5f, 331.4f, 2.4f, 2.0f), // Nanae Dairy Ranch
        floatArrayOf(251.0f, 217.0f, 20.0f, 22.0f), // Ishikari Plain
        floatArrayOf(254.0f, 58.0f, 14.0f, 24.0f), // Teshio / Sarobetsu Plain
        floatArrayOf(348.0f, 245.0f, 30.0f, 24.0f), // Tokachi Plain
        floatArrayOf(440.0f, 214.0f, 30.0f, 22.0f), // Konsen Plateau
    )
    for (z in pastureZones) {
        drawOval(
            color = pastureWash,
            topLeft = Offset(z[0] - z[2], z[1] - z[3]),
            size = Size(z[2] * 2f, z[3] * 2f),
        )
    }

    // Subtle hand-drawn brush-pen mountain peak arcs on major highlands
    val ridgeStrokeWidth = (1.6.dp.toPx() / zoomScale.coerceIn(0.05f, 1.2f)) / scaleFactor
    val ridgeStroke = Stroke(width = ridgeStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val ridgePeaks = listOf(
        floatArrayOf(154f, 324f, 6.0f),
        floatArrayOf(189f, 251f, 7.0f), // Yotei
        floatArrayOf(312f, 174f, 10.0f), // Asahidake
        floatArrayOf(325f, 185f, 8.5f),
        floatArrayOf(318f, 256f, 8.0f), // Hidaka north
        floatArrayOf(322f, 276f, 7.5f), // Hidaka south
        floatArrayOf(301f, 92f, 8.0f), // Kitami
        floatArrayOf(414f, 185f, 7.5f), // Akan
        floatArrayOf(464f, 141f, 6.0f), // Rausudake
    )
    val ridgePath = renderCache.ridgePath
    for (pk in ridgePeaks) {
        val px = pk[0]
        val py = pk[1]
        val s = pk[2]
        ridgePath.reset()
        ridgePath.moveTo(px - s, py + s * 0.35f)
        ridgePath.quadraticTo(px, py - s * 0.55f, px + s, py + s * 0.35f)
        drawPath(path = ridgePath, color = ridgeInk, style = ridgeStroke)
    }
}

/**
 * Draws the real highway network across Hokkaido using pre-merged wide-view paths and
 * viewport frustum culling for detailed zoom so panning remains fast across all 18,981 segments.
 */
private fun DrawScope.drawHokkaidoRoadNetwork(
    groupedHighwayPaths: Map<HighwayCategory, List<CachedHighwayPath>>,
    mergedWideHighwayPaths: Map<HighwayCategory, Path>,
    viewportWorld: Rect,
    renderCache: SugorokuRenderCache,
    zoomScale: Float,
    theme: SugorokuMapTheme = SugorokuMapTheme.Default,
) {
    if (groupedHighwayPaths.isEmpty()) return

    val hierarchy = listOf(
        HighwayCategory.PREFECTURAL_ROAD,
        HighwayCategory.SCENIC_ROAD,
        HighwayCategory.NATIONAL_HIGHWAY,
        HighwayCategory.TRAVEL_ROUTE,
    )

    if (zoomScale < 0.05f) {
        // Wide overview: 4 single merged Path draw calls instead of 18,981 individual paths
        for (targetCat in hierarchy) {
            val merged = mergedWideHighwayPaths[targetCat] ?: continue
            val strokeWidth = when (targetCat) {
                HighwayCategory.TRAVEL_ROUTE -> 3.5.dp.toPx() / zoomScale
                HighwayCategory.NATIONAL_HIGHWAY -> 0.8.dp.toPx() / zoomScale
                HighwayCategory.SCENIC_ROAD -> 0.7.dp.toPx() / zoomScale
                HighwayCategory.PREFECTURAL_ROAD -> 0.5.dp.toPx() / zoomScale
            }
            drawPath(
                path = merged,
                color = theme.roadColor(targetCat),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        return
    }

    val pad = 32.dp.toPx()
    val culledRect = Rect(
        left = viewportWorld.left - pad,
        top = viewportWorld.top - pad,
        right = viewportWorld.right + pad,
        bottom = viewportWorld.bottom + pad,
    )

    val visibleByCategory = HashMap<HighwayCategory, List<CachedHighwayPath>>(4)
    for (targetCat in hierarchy) {
        val list = groupedHighwayPaths[targetCat].orEmpty()
        visibleByCategory[targetCat] = list.filter { it.intersects(culledRect) }
    }

    // PASS 1: Roadbeds / borders
    for (targetCat in hierarchy) {
        val borderStroke = renderCache.roadBorderStrokes.getValue(targetCat)
        val borderColor = theme.roadBorderColor(targetCat)
        for (seg in visibleByCategory.getValue(targetCat)) {
            drawPath(path = seg.path, color = borderColor, style = borderStroke)
        }
    }

    // PASS 2: Seamless asphalt surfaces over all borders
    for (targetCat in hierarchy) {
        val surfaceStroke = renderCache.roadSurfaceStrokes.getValue(targetCat)
        val surfaceColor = theme.roadBedColor(targetCat)
        for (seg in visibleByCategory.getValue(targetCat)) {
            drawPath(path = seg.path, color = surfaceColor, style = surfaceStroke)
        }
    }

    // PASS 3: Dashed centerlines for visible National Highways & Travel Route
    val dashColor = Color.White.copy(alpha = 0.85f)
    for (seg in visibleByCategory.getValue(HighwayCategory.NATIONAL_HIGHWAY)) {
        drawPath(path = seg.path, color = dashColor, style = renderCache.highwayDashStroke)
    }

    val travelDashColor = Color(0xFFFF9800).copy(alpha = 0.90f)
    for (seg in visibleByCategory.getValue(HighwayCategory.TRAVEL_ROUTE)) {
        drawPath(path = seg.path, color = travelDashColor, style = renderCache.travelDashStroke)
    }
}

/**
 * Draws the 46dp square Sugoroku tiles.
 * Supports frustum culling to render only visible tiles for optimal performance.
 * When zoomed out deeply, draws prominent landmark pins instead of microscopic tiles.
 */
private fun DrawScope.drawSugorokuTiles(
    nodes: List<SugorokuNode>,
    tileCentersDp: List<Offset>,
    currentStep: Int,
    visibleStepCount: Int,
    density: Float,
    zoomScale: Float,
    viewportWorld: Rect,
    renderCache: SugorokuRenderCache,
) {
    val count = visibleStepCount.coerceAtMost(tileCentersDp.size)

    // If zoomed out to whole prefecture overview, skip individual tile rendering
    if (zoomScale < 0.05f) {
        return
    }

    val tileSizePx = HokkaidoBoardTrack.TILE_SIZE_DP.dp.toPx()
    val halfTile = tileSizePx / 2f
    val cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx())

    val labelPaint = renderCache.labelPaint
    val stepNumberPaint = renderCache.stepNumberPaint
    val badgePaint = renderCache.badgePaint

    // Viewport bounds with padding
    val pad = tileSizePx * 2f
    val vLeft = viewportWorld.left - pad
    val vTop = viewportWorld.top - pad
    val vRight = viewportWorld.right + pad
    val vBottom = viewportWorld.bottom + pad

    for (i in 0 until count) {
        val ptDp = tileCentersDp[i]
        val cx = ptDp.x * density
        val cy = ptDp.y * density

        // Frustum Culling
        if (cx < vLeft || cx > vRight || cy < vTop || cy > vBottom) {
            continue
        }

        val node = nodes[i]
        val left = cx - halfTile
        val top = cy - halfTile

        val isVisited = i < currentStep
        val isCurrent = i == currentStep

        // 1. Soft drop shadow beneath tile
        drawRoundRect(
            color = Color(0x38000000),
            topLeft = Offset(left + 1.5.dp.toPx(), top + 2.5.dp.toPx()),
            size = Size(tileSizePx, tileSizePx),
            cornerRadius = cornerRadius,
        )

        // 2. Tile background & border colors
        val (bgColor, borderColor, borderWidth) = when {
            isCurrent -> Triple(Color(0xFFFFF176), Color(0xFFF57F17), 3.dp.toPx())
            isVisited && node.isAnchor -> Triple(Color(0xFFE1BEE7), Color(0xFF8E24AA), 2.5.dp.toPx())
            isVisited -> Triple(Color(0xFFFFE0B2), Color(0xFFFB8C00), 1.8.dp.toPx())
            node.isAnchor -> Triple(Color(0xFFEDE7F6), Color(0xFF7B1FA2), 2.5.dp.toPx())
            node.type == SugorokuNodeType.SCENIC_SPOT -> Triple(Color(0xFFFFF9C4), Color(0xFFFBC02D), 2.2.dp.toPx())
            else -> Triple(Color(0xFFFFFFFF), Color(0xFFB0BEC5), 1.5.dp.toPx())
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
            top + 12.dp.toPx(),
            stepNumberPaint,
        )

        if (isCurrent) {
            // For current tile: floating pill badge beneath tile
            val badgeText = node.name
            val textWidth = badgePaint.measureText(badgeText)
            val badgeWidth = textWidth + 12.dp.toPx()
            val badgeHeight = 17.dp.toPx()
            val badgeLeft = cx - badgeWidth / 2f
            val badgeTop = top + tileSizePx + 4.dp.toPx()

            // Badge shadow & white pill background
            drawRoundRect(
                color = Color(0x33000000),
                topLeft = Offset(badgeLeft + 1.dp.toPx(), badgeTop + 1.5.dp.toPx()),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(badgeLeft, badgeTop),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFF57F17),
                topLeft = Offset(badgeLeft, badgeTop),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx()),
            )
            drawContext.canvas.nativeCanvas.drawText(
                badgeText,
                cx,
                badgeTop + 12.dp.toPx(),
                badgePaint,
            )
        } else {
            // Normal tile: name cleanly centered vertically
            drawTileLabel(
                text = node.name,
                cx = cx,
                cy = top + 29.dp.toPx(),
                maxTextWidthPx = 48.dp.toPx(),
                baseTextSizePx = 10.dp.toPx(),
                paint = labelPaint,
            )

            // Subtle anchor indicator (elegant diamond/dot without preset emojis)
            if (node.isAnchor) {
                drawCircle(
                    color = if (isVisited) Color(0xFF8E24AA) else Color(0xFFAB47BC),
                    radius = 2.2.dp.toPx(),
                    center = Offset(cx, top + 41.5.dp.toPx()),
                )
            }
        }
    }
}

/**
 * Draws tile label text gracefully without truncation, wrapping into 2 lines if needed
 * and auto-scaling to strictly fit within maxTextWidthPx.
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
    } else {
        val (line1, line2) = splitJapaneseTileName(text)
        paint.textSize = (baseTextSizePx * 0.85f).coerceAtLeast(8.dp.toPx())
        val l1w = paint.measureText(line1)
        val l2w = paint.measureText(line2)
        val maxLw = maxOf(l1w, l2w)
        if (maxLw > maxTextWidthPx) {
            paint.textSize = (paint.textSize * (maxTextWidthPx / maxLw)).coerceAtLeast(7.dp.toPx())
        }
        val lineHeight = paint.fontSpacing * 0.88f
        drawContext.canvas.nativeCanvas.drawText(line1, cx, cy - lineHeight * 0.42f, paint)
        drawContext.canvas.nativeCanvas.drawText(line2, cx, cy + lineHeight * 0.58f, paint)
    }
}

/**
 * Gracefully splits Japanese landmark and street names at natural word boundaries.
 */
private fun splitJapaneseTileName(text: String): Pair<String, String> {
    if (text.startsWith("道の駅")) {
        return Pair("道の駅", text.removePrefix("道の駅"))
    }
    val customSplits = mapOf(
        "北緯45度モニュメント" to Pair("北緯45度", "モニュメント"),
        "ガリンコ号のりば" to Pair("ガリンコ号", "のりば"),
        "カニの爪オブジェ" to Pair("カニの爪", "オブジェ"),
        "常呂カーリング" to Pair("常呂", "カーリング"),
        "ウスタイベ千畳岩" to Pair("ウスタイベ", "千畳岩"),
        "小清水原生花園" to Pair("小清水", "原生花園"),
        "みなとま〜れ寿都" to Pair("みなとま〜れ", "寿都"),
        "よってけ！島牧" to Pair("よってけ！", "島牧"),
        "オスコイ！かもえない" to Pair("オスコイ！", "かもえない"),
        "あいろーど厚田" to Pair("あいろーど", "厚田"),
        "おびら鰊番屋" to Pair("おびら", "鰊番屋"),
        "風Wとままえ" to Pair("風W", "とままえ"),
        "三毛別羆事件跡" to Pair("三毛別", "羆事件跡"),
        "留萌 黄金岬" to Pair("留萌", "黄金岬"),
        "流氷街道網走" to Pair("流氷街道", "網走"),
        "愛ランド湧別" to Pair("愛ランド", "湧別"),
        "天に続く道" to Pair("天に", "続く道"),
        "大沼国定公園" to Pair("大沼", "国定公園"),
        "浜益海浜公園" to Pair("浜益", "海浜公園"),
        "雄冬岬展望台" to Pair("雄冬岬", "展望台"),
        "北見神威岬" to Pair("北見", "神威岬"),
        "キムアネップ岬" to Pair("キムアネップ", "岬"),
    )
    customSplits[text]?.let { return it }

    val keywords = listOf(
        "タワー前", "交差点", "公園前", "駅前通", "本町通", "温泉街", "市街地",
        "タワー", "公園", "駅前", "本町", "温泉", "案内所", "展望台", "灯台", "漁港", "海岸", "通り", "神社", "寺院", "岬", "峠", "湖", "運河", "線", "岩", "河口", "原野", "蒸溜所", "酒造",
    )
    for (kw in keywords) {
        val idx = text.indexOf(kw)
        if (idx in 2..5 && idx != text.length - 1) {
            return Pair(text.substring(0, idx), text.substring(idx))
        } else if (idx > 0 && idx == text.length - kw.length && text.length > 4) {
            return Pair(text.substring(0, idx), text.substring(idx))
        }
    }
    val mid = (text.length + 1) / 2
    return Pair(text.substring(0, mid), text.substring(mid))
}

/**
 * Draws the cute traveler companion piece (Shima-enaga etc.) with soft shadow.
 */
private fun DrawScope.drawCompanionPiece(
    x: Float,
    y: Float,
    shadowY: Float,
    companionBitmap: ImageBitmap?,
    companion: TravelerCompanion,
) {
    val sizeDp = 34.dp.toPx() * companion.defaultScale

    // 1. Soft oval shadow on the road
    drawOval(
        color = Color(0x38000000),
        topLeft = Offset(x - sizeDp * 0.42f, shadowY - 4.dp.toPx()),
        size = Size(sizeDp * 0.84f, 8.dp.toPx()),
    )

    // 2. Storybook companion sprite
    if (companionBitmap != null) {
        val wPx = (sizeDp * 0.88f).roundToInt()
        val hPx = sizeDp.roundToInt()
        drawImage(
            image = companionBitmap,
            dstOffset = IntOffset(
                x = (x - wPx / 2f).roundToInt(),
                y = (y - hPx).roundToInt(),
            ),
            dstSize = IntSize(wPx, hPx),
        )
    } else {
        // Fallback placeholder
        drawCircle(
            color = Color(0xFFFFF9C4),
            radius = sizeDp * 0.35f,
            center = Offset(x, y - sizeDp * 0.4f),
        )
    }
}

/**
 * Draws a charming storybook speech bubble above the companion's head when near a landmark.
 */
private fun DrawScope.drawSpeechBubble(
    text: String,
    x: Float,
    bubbleY: Float,
    renderCache: SugorokuRenderCache,
) {
    val textPaint = renderCache.bubbleTextPaint
    val bubblePaint = renderCache.bubbleFillPaint
    val borderPaint = renderCache.bubbleBorderPaint
    val textBounds = renderCache.bubbleTextBounds

    textPaint.getTextBounds(text, 0, text.length, textBounds)
    val textWidth = textBounds.width().toFloat()
    val textHeight = textBounds.height().toFloat()

    val padH = 8.dp.toPx()
    val padV = 5.dp.toPx()
    val bubbleWidth = textWidth + padH * 2
    val bubbleHeight = textHeight + padV * 2

    val left = x - bubbleWidth / 2f
    val top = bubbleY - bubbleHeight
    val right = x + bubbleWidth / 2f
    val bottom = bubbleY

    val bubbleRect = renderCache.bubbleRectF
    bubbleRect.set(left, top, right, bottom)
    drawContext.canvas.nativeCanvas.drawRoundRect(bubbleRect, 8.dp.toPx(), 8.dp.toPx(), bubblePaint)
    drawContext.canvas.nativeCanvas.drawRoundRect(bubbleRect, 8.dp.toPx(), 8.dp.toPx(), borderPaint)

    val tailPath = renderCache.bubbleTailPath
    tailPath.reset()
    tailPath.moveTo(x - 3.dp.toPx(), bottom)
    tailPath.lineTo(x, bottom + 4.dp.toPx())
    tailPath.lineTo(x + 3.dp.toPx(), bottom)
    tailPath.close()
    drawContext.canvas.nativeCanvas.drawPath(tailPath, bubblePaint)
    drawContext.canvas.nativeCanvas.drawPath(tailPath, borderPaint)

    val textY = top + padV + textHeight
    drawContext.canvas.nativeCanvas.drawText(text, x, textY, textPaint)
}
