package com.aidomari.app.ui.study.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

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
    val dragOffsetX = remember { Animatable(0f) }

    // 3D Flip rotation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "cardFlipRotation",
    )

    val isBackFace = rotation > 90f

    val gestureModifier = if (!isFlipped) {
        // Front face: tap or swipe flips card
        Modifier.pointerInput(Unit) {
            detectTapGestures {
                onFlip()
            }
        }
    } else {
        // Back face: Tinder swipe left (Retry) or right (Done) + tap to flip back
        Modifier.pointerInput(isFlipped) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    coroutineScope.launch {
                        val currentOffset = dragOffsetX.value
                        when {
                            currentOffset > DRAG_THRESHOLD -> {
                                // Animate right and complete as Done
                                dragOffsetX.animateTo(
                                    targetValue = 1000f,
                                    animationSpec = tween(durationMillis = 180),
                                )
                                dragOffsetX.snapTo(0f)
                                onDone()
                            }
                            currentOffset < -DRAG_THRESHOLD -> {
                                // Animate left and complete as Retry
                                dragOffsetX.animateTo(
                                    targetValue = -1000f,
                                    animationSpec = tween(durationMillis = 180),
                                )
                                dragOffsetX.snapTo(0f)
                                onRetry()
                            }
                            else -> {
                                // Spring back to center
                                dragOffsetX.animateTo(0f)
                            }
                        }
                    }
                },
                onHorizontalDrag = { _, dragAmount ->
                    coroutineScope.launch {
                        dragOffsetX.snapTo(dragOffsetX.value + dragAmount)
                    }
                },
            )
        }.pointerInput(Unit) {
            detectTapGestures {
                onFlip()
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
            .graphicsLayer {
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
                    rotationY = rotation
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
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = Color.White,
                        )
                        androidx.compose.foundation.layout.Spacer(
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
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = Color.White,
                        )
                        androidx.compose.foundation.layout.Spacer(
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
