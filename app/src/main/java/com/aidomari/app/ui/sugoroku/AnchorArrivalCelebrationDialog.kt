package com.aidomari.app.ui.sugoroku

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aidomari.app.domain.model.Act1TravelRoute
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.SugorokuNode
import com.aidomari.app.domain.model.SugorokuNodeType
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Celebration presentation shown when the traveler arrives at an anchor node (Issue #76).
 * Displays celebratory particle confetti, spot rank badge, step progress,
 * description, and distance/preview to the next goal anchor.
 */
@Composable
fun AnchorArrivalCelebrationDialog(
    node: SugorokuNode,
    totalDistanceMeters: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Dismissible background scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )

            // Animated celebratory confetti particles
            ConfettiParticleCanvas(
                modifier = Modifier.fillMaxSize(),
            )

            // Central celebration card
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFB300).copy(alpha = 0.75f)),
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}, // Catch clicks inside card
                    )
                    .testTag("AnchorArrivalCelebrationCard"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Celebration Header Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = if (node.type == SugorokuNodeType.GOAL_AIDOMARI) {
                                    "🏆 第1部 堂々踏破！"
                                } else {
                                    "🎉 アンカー到達！"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100),
                            )
                        }
                    }

                    Text(
                        text = if (node.type == SugorokuNodeType.GOAL_AIDOMARI) {
                            "北海道一周すごろく 第1部ゴールに到達しました！"
                        } else {
                            "おめでとうございます！新たな名所に足跡を刻みました。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    // Spot Rank Badge & Municipality
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val (badgeText, badgeBg, badgeFg) = when {
                            node.type == SugorokuNodeType.START_HAKODATE -> Triple("出発地", Color(0xFFFFE082), Color(0xFF5D4037))
                            node.type == SugorokuNodeType.GOAL_AIDOMARI -> Triple("終着地", Color(0xFFFFD54F), Color(0xFFE65100))
                            node.rank == 1 -> Triple("★1 国家級名所", Color(0xFFFFE082), Color(0xFFBF360C))
                            node.rank == 2 -> Triple("★2 地域代表名所", Color(0xFFFFF9C4), Color(0xFFE65100))
                            node.rank == 3 -> Triple("★3 景勝地", Color(0xFFE0F2F1), Color(0xFF004D40))
                            node.type == SugorokuNodeType.REST_AREA -> Triple("温泉・道の駅", Color(0xFFE8F5E9), Color(0xFF1B5E20))
                            node.type == SugorokuNodeType.TOWN_CENTER -> Triple("駅・街", Color(0xFFE1F5FE), Color(0xFF01579B))
                            else -> Triple("名所アンカー", Color(0xFFECEFF1), Color(0xFF37474F))
                        }
                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeFg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }

                        Text(
                            text = if (node.roadName.isNotEmpty()) {
                                "${node.municipality} (${node.roadName})"
                            } else {
                                node.municipality
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    // Main Spot Name
                    Text(
                        text = node.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    // Step Progress Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val totalNodes = HokkaidoAct1Route.totalNodes
                        val currentStep = node.stepIndex + 1
                        val percent = (currentStep * 100f / totalNodes).roundToInt()
                        val km = totalDistanceMeters / 1000f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "第 $currentStep マス / 全 $totalNodes マス",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "$percent% 走破",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (currentStep.toFloat() / totalNodes).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF9800),
                            trackColor = Color(0xFFFFE0B2),
                        )

                        if (km > 0.05f) {
                            Text(
                                text = "累計踏破距離: ${String.format(Locale.JAPAN, "%.1f", km)} km",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Spot Description
                    if (node.description.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFDF5),
                            border = BorderStroke(1.dp, Color(0xFFEFEBE9)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = node.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    // Next Anchor Preview
                    val nextNode = HokkaidoAct1Route.getNodeByStepIndex(node.stepIndex + 1)
                    if (nextNode != null) {
                        val leg = remember(node.stepIndex) { Act1TravelRoute.getLegs().getOrNull(node.stepIndex) }
                        val legKm = (leg?.distanceMeters ?: 0f) / 1000f

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(text = "🚩", fontSize = 15.sp)
                                Column {
                                    Text(
                                        text = "次の目標: ${nextNode.name} (${nextNode.municipality})",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "区間距離: 約 ${String.format(Locale.JAPAN, "%.1f", legKm)} km",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Continue Journey Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("CelebrationContinueButton"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "旅を続ける",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class CelebrationParticle(
    val seed: Float,
    val xRatio: Float,
    val startYRatio: Float,
    val fallDistance: Float,
    val driftAmp: Float,
    val rotationSpeed: Float,
    val sizeDp: Float,
    val color: Color,
    val isStar: Boolean,
)

@Composable
private fun ConfettiParticleCanvas(
    modifier: Modifier = Modifier,
) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD54F), // Gold
            Color(0xFFFF7043), // Coral / Orange
            Color(0xFF42A5F5), // Sky Blue
            Color(0xFF66BB6A), // Spring Green
            Color(0xFFAB47BC), // Lavender Purple
            Color(0xFFFF4081), // Sakura Pink
            Color(0xFFFFB300), // Amber
        )
        val random = kotlin.random.Random(1337)
        List(42) { index ->
            CelebrationParticle(
                seed = random.nextFloat() * 10f,
                xRatio = 0.05f + random.nextFloat() * 0.90f,
                startYRatio = -0.15f + random.nextFloat() * 0.35f,
                fallDistance = 0.70f + random.nextFloat() * 0.60f,
                driftAmp = 14f + random.nextFloat() * 22f,
                rotationSpeed = if (random.nextBoolean()) 360f + random.nextFloat() * 360f else -(360f + random.nextFloat() * 360f),
                sizeDp = 6f + random.nextFloat() * 8f,
                color = colors[index % colors.size],
                isStar = index % 3 == 0,
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ConfettiTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ConfettiProgress",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            val phase = (progress + p.seed * 0.1f) % 1.0f
            val px = p.xRatio * w + sin(phase * 4f * PI.toFloat() + p.seed) * p.driftAmp.dp.toPx()
            val py = (p.startYRatio + phase * p.fallDistance) * h
            val rotation = (phase * p.rotationSpeed) % 360f
            val alpha = when {
                phase < 0.10f -> phase / 0.10f
                phase > 0.85f -> (1.0f - phase) / 0.15f
                else -> 1.0f
            }

            val pSize = p.sizeDp.dp.toPx()
            val drawColor = p.color.copy(alpha = (alpha * 0.85f).coerceIn(0f, 1f))

            withTransform({
                rotate(rotation, Offset(px, py))
            }) {
                if (p.isStar) {
                    drawCelebrationStar(
                        center = Offset(px, py),
                        radius = pSize * 0.8f,
                        color = drawColor,
                    )
                } else {
                    drawRoundRect(
                        color = drawColor,
                        topLeft = Offset(px - pSize / 2f, py - pSize * 0.4f),
                        size = Size(pSize, pSize * 0.8f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCelebrationStar(
    center: Offset,
    radius: Float,
    color: Color,
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}
