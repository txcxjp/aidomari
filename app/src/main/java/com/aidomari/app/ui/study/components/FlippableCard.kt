package com.aidomari.app.ui.study.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DRAG_THRESHOLD = 220f

@Composable
fun FlippableCard(
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp * density
    val offscreenTarget = (screenWidthPx * 1.2f).coerceAtLeast(1200f)

    var isDismissing by remember { mutableStateOf(false) }
    val dragOffsetX = remember { Animatable(0f) }

    // Subtle scale entrance from stack (0.94 -> 1.0)
    val entranceScale = remember { Animatable(0.94f) }
    LaunchedEffect(Unit) {
        entranceScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        )
    }

    // 3D Flip rotation driven by gesture / isFlipped
    val rotation = remember { Animatable(if (isFlipped) 180f else 0f) }
    LaunchedEffect(isFlipped) {
        val target = if (isFlipped) 180f else 0f
        if (rotation.value != target) {
            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            )
        }
    }

    val isBackFace = rotation.value > 90f

    val gestureModifier = if (isDismissing) {
        Modifier
    } else {
        Modifier.pointerInput(isFlipped) {
            val touchSlop = viewConfiguration.touchSlop
            val flipDistance = (screenWidthPx * 0.45f).coerceIn(300f, 600f)

            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var totalDragX = 0f
                var totalDragY = 0f
                var isHorizontalDrag = false
                var isVerticalDrag = false

                while (true) {
                    val event = awaitPointerEvent()
                    val pointer = event.changes.find { it.id == down.id } ?: break

                    if (!pointer.pressed) {
                        // Pointer lifted
                        if (!isHorizontalDrag && !isVerticalDrag) {
                            if (!pointer.isConsumed) {
                                // Tap: flip card
                                coroutineScope.launch {
                                    val target = if (!isFlipped) 180f else 0f
                                    rotation.animateTo(
                                        targetValue = target,
                                        animationSpec = tween(
                                            durationMillis = 260,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    )
                                    onFlip()
                                }
                            }
                        } else if (isHorizontalDrag) {
                            if (!isFlipped) {
                                // Front face drag end: snap to back or bounce to front
                                val progress = (abs(totalDragX) / flipDistance).coerceIn(0f, 1f)
                                coroutineScope.launch {
                                    if (progress >= 0.38f) {
                                        rotation.animateTo(
                                            targetValue = 180f,
                                            animationSpec = tween(
                                                durationMillis = 180,
                                                easing = FastOutSlowInEasing,
                                            ),
                                        )
                                        onFlip()
                                    } else {
                                        rotation.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            ),
                                        )
                                    }
                                }
                            } else {
                                // Back face drag end: Tinder swipe decision
                                val offset = totalDragX
                                coroutineScope.launch {
                                    when {
                                        offset > DRAG_THRESHOLD -> {
                                            isDismissing = true
                                            dragOffsetX.animateTo(
                                                targetValue = offscreenTarget,
                                                animationSpec = tween(
                                                    durationMillis = 180,
                                                    easing = FastOutSlowInEasing,
                                                ),
                                            )
                                            onDone()
                                        }
                                        offset < -DRAG_THRESHOLD -> {
                                            isDismissing = true
                                            dragOffsetX.animateTo(
                                                targetValue = -offscreenTarget,
                                                animationSpec = tween(
                                                    durationMillis = 180,
                                                    easing = FastOutSlowInEasing,
                                                ),
                                            )
                                            onRetry()
                                        }
                                        else -> {
                                            dragOffsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        break
                    }

                    val dx = pointer.position.x - pointer.previousPosition.x
                    val dy = pointer.position.y - pointer.previousPosition.y
                    totalDragX += dx
                    totalDragY += dy

                    if (!isHorizontalDrag && !isVerticalDrag) {
                        if (abs(totalDragX) > touchSlop && abs(totalDragX) > abs(totalDragY)) {
                            isHorizontalDrag = true
                            pointer.consume()
                        } else if (abs(totalDragY) > touchSlop) {
                            isVerticalDrag = true
                        }
                    }

                    if (isHorizontalDrag) {
                        pointer.consume()
                        if (!isFlipped) {
                            val progress = (abs(totalDragX) / flipDistance).coerceIn(0f, 1f)
                            val targetRot = progress * 180f
                            coroutineScope.launch {
                                rotation.snapTo(targetRot)
                            }
                        } else {
                            coroutineScope.launch {
                                dragOffsetX.snapTo(totalDragX)
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
            .graphicsLayer {
                scaleX = entranceScale.value
                scaleY = entranceScale.value
                // Drag rotation for Tinder card physics on back face
                if (isFlipped) {
                    rotationZ = (dragOffsetX.value / 25f).coerceIn(-15f, 15f)
                }
            }
            .then(gestureModifier),
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 16f * density
                },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            if (isBackFace) {
                // Un-mirror back content so text is right side up
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(24.dp),
                ) {
                    backContent()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                ) {
                    frontContent()
                }
            }
        }

        // Overlay badges for swipe feedback
        if (isFlipped && abs(dragOffsetX.value) > 20f) {
            val progress = (abs(dragOffsetX.value) / DRAG_THRESHOLD).coerceIn(0f, 1f)

            if (dragOffsetX.value > 0) {
                // Done indicator (Green overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .background(
                            color = Color(0xFF2E7D32).copy(alpha = progress * 0.9f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = Color.White,
                        )
                        Spacer(
                            modifier = Modifier.padding(start = 6.dp),
                        )
                        Text(
                            text = "覚えた",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                }
            } else {
                // Retry indicator (Red overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(
                            color = Color(0xFFC62828).copy(alpha = progress * 0.9f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = Color.White,
                        )
                        Spacer(
                            modifier = Modifier.padding(start = 6.dp),
                        )
                        Text(
                            text = "もう一度",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }
    }
}
