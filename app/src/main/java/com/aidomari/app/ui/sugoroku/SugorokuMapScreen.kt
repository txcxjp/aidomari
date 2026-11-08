package com.aidomari.app.ui.sugoroku

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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.model.SugorokuNodeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SugorokuMapScreen(
    viewModel: SugorokuMapViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val allNodes = HokkaidoAct1Route.nodes

    var zoomScale by remember { mutableFloatStateOf(0.7f) }
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
                            text = "北海道すごろく旅（第1幕）",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "日本海・最北端・オホーツク編",
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
                        text = "🗺️",
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()

            LaunchedEffect(uiState.currentNode, containerWidth, containerHeight) {
                if (!hasCenteredInitially && containerWidth > 0 && containerHeight > 0) {
                    centerOnNode(uiState.currentNode, containerWidth, containerHeight)
                    hasCenteredInitially = true
                }
            }

            // Pulse animation for current player token
            val infiniteTransition = rememberInfiniteTransition(label = "player_pulse")
            val pulseRadius by infiniteTransition.animateFloat(
                initialValue = 12f,
                targetValue = 28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "pulse_radius",
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "pulse_alpha",
            )

            // Primary colors
            val routeColor = MaterialTheme.colorScheme.primary
            val remainingRouteColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            val completedNodeColor = MaterialTheme.colorScheme.primary
            val uncompletedNodeColor = MaterialTheme.colorScheme.outlineVariant
            val landmarkColor = Color(0xFFE65100) // Deep Orange
            val startColor = Color(0xFF2E7D32) // Forest Green
            val goalColor = Color(0xFFC62828) // Deep Red

            // 2D Canvas Map
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("SugorokuCanvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.25f, 2.5f)
                            panOffset += pan
                        }
                    }
                    .pointerInput(allNodes, zoomScale, panOffset) {
                        detectTapGestures { tapOffset ->
                            // Find nearest node to tap
                            val worldX = (tapOffset.x - panOffset.x) / zoomScale
                            val worldY = (tapOffset.y - panOffset.y) / zoomScale
                            val threshold = 40f / zoomScale

                            val nearest = allNodes.minByOrNull { node ->
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
                val currentStep = uiState.progress.currentStepIndex

                // 1. Draw route path
                if (allNodes.size > 1) {
                    val completedPath = Path()
                    val remainingPath = Path()

                    allNodes.forEachIndexed { index, node ->
                        val screenX = node.x * zoomScale + panOffset.x
                        val screenY = node.y * zoomScale + panOffset.y

                        if (index == 0) {
                            completedPath.moveTo(screenX, screenY)
                        } else if (index <= currentStep) {
                            completedPath.lineTo(screenX, screenY)
                        }

                        if (index == currentStep) {
                            remainingPath.moveTo(screenX, screenY)
                        } else if (index > currentStep) {
                            remainingPath.lineTo(screenX, screenY)
                        }
                    }

                    // Remaining route
                    drawPath(
                        path = remainingPath,
                        color = remainingRouteColor,
                        style = Stroke(
                            width = 3.5f * zoomScale,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )

                    // Completed route
                    drawPath(
                        path = completedPath,
                        color = routeColor,
                        style = Stroke(
                            width = 5.0f * zoomScale,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }

                // 2. Draw nodes
                allNodes.forEach { node ->
                    val screenX = node.x * zoomScale + panOffset.x
                    val screenY = node.y * zoomScale + panOffset.y
                    val center = Offset(screenX, screenY)
                    val isVisited = node.stepIndex <= currentStep

                    when (node.type) {
                        SugorokuNodeType.START_HAKODATE -> {
                            drawCircle(
                                color = startColor,
                                radius = 10f * zoomScale,
                                center = center,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f * zoomScale,
                                center = center,
                            )
                        }
                        SugorokuNodeType.GOAL_AIDOMARI -> {
                            drawCircle(
                                color = goalColor,
                                radius = 12f * zoomScale,
                                center = center,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6f * zoomScale,
                                center = center,
                            )
                        }
                        SugorokuNodeType.SCENIC_SPOT,
                        SugorokuNodeType.TOWN_CENTER,
                        SugorokuNodeType.REST_AREA,
                        -> {
                            val color = if (isVisited) landmarkColor else landmarkColor.copy(alpha = 0.5f)
                            drawCircle(
                                color = color,
                                radius = 7.5f * zoomScale,
                                center = center,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f * zoomScale,
                                center = center,
                            )
                        }
                        SugorokuNodeType.NORMAL -> {
                            // Only draw normal nodes when zoomed in enough or sampled to keep performance high
                            if (zoomScale >= 0.5f || node.stepIndex % 4 == 0) {
                                val color = if (isVisited) completedNodeColor else uncompletedNodeColor
                                drawCircle(
                                    color = color,
                                    radius = 3.0f * zoomScale,
                                    center = center,
                                )
                            }
                        }
                    }
                }

                // 3. Draw Player Token at current node
                val currentNode = uiState.currentNode
                val playerScreenX = currentNode.x * zoomScale + panOffset.x
                val playerScreenY = currentNode.y * zoomScale + panOffset.y
                val playerCenter = Offset(playerScreenX, playerScreenY)

                // Pulse aura
                drawCircle(
                    color = routeColor.copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = playerCenter,
                )

                // Player icon core
                drawCircle(
                    color = routeColor,
                    radius = 9f * zoomScale,
                    center = playerCenter,
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5f * zoomScale,
                    center = playerCenter,
                )
            }

            // Top Status Overlay Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
                    .testTag("SugorokuStatusCard"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "📍", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = uiState.currentNode.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "自治体: ${uiState.currentNode.municipality}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = "残り ${uiState.remainingSteps} マス",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progressFraction = if (uiState.totalNodes > 1) {
                        (uiState.progress.currentStepIndex.toFloat() / (uiState.totalNodes - 1)).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "起点: 函館",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            text = "到達: ${(progressFraction * 100).toInt()}% (${uiState.progress.currentStepIndex + 1}/${uiState.totalNodes})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "終着: 相泊",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }

            // Bottom-Right Controls (Recenter, Zoom In, Zoom Out)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FloatingActionButton(
                    onClick = {
                        centerOnNode(uiState.currentNode, containerWidth, containerHeight)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("RecenterButton"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "現在地にセンタリング",
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f)
                    },
                    modifier = Modifier.size(44.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ズームイン",
                    )
                }

                FloatingActionButton(
                    onClick = {
                        zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.25f)
                    },
                    modifier = Modifier.size(44.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "ズームアウト",
                    )
                }
            }
        }
    }

    // Node Detail Dialog
    if (uiState.selectedNode != null) {
        val node = uiState.selectedNode!!
        val isCurrent = node.stepIndex == uiState.progress.currentStepIndex
        val isVisited = node.stepIndex <= uiState.progress.currentStepIndex

        AlertDialog(
            onDismissRequest = { viewModel.selectNode(null) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (node.type) {
                            SugorokuNodeType.START_HAKODATE -> "🏁 "
                            SugorokuNodeType.GOAL_AIDOMARI -> "🎉 "
                            SugorokuNodeType.SCENIC_SPOT -> "✨ "
                            SugorokuNodeType.TOWN_CENTER -> "🏛️ "
                            SugorokuNodeType.REST_AREA -> "☕ "
                            SugorokuNodeType.NORMAL -> "📍 "
                        },
                    )
                    Text(text = node.name, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "自治体: ${node.municipality}（マス ${node.stepIndex + 1} / ${uiState.totalNodes}）",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            isCurrent -> "【現在地】ここにいます！"
                            isVisited -> "【到達済み】通過した場所です。"
                            else -> "【これからの目的地】相泊へ向けて進みましょう！"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.selectNode(null) },
                    modifier = Modifier.testTag("CloseNodeDetailButton"),
                ) {
                    Text(text = "閉じる")
                }
            },
        )
    }
}
