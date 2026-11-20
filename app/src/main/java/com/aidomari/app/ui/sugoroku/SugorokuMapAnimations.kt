package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.LandmarkMotionStyle
import com.aidomari.app.domain.model.RoadsideLandmark
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private const val TWO_PI: Float = (Math.PI * 2.0).toFloat()

// Normalized Mt. Hakodate ropeway cable endpoints & pulley attachment ratios
private const val ROPEWAY_CABLE_START_X = 0.7233f
private const val ROPEWAY_CABLE_START_Y = 0.3269f
private const val ROPEWAY_CABLE_END_X = 0.8218f
private const val ROPEWAY_CABLE_END_Y = 0.2608f
private const val ROPEWAY_GONDOLA_ATTACH_X = 0.4887f
private const val ROPEWAY_GONDOLA_ATTACH_Y = 0.1487f
private const val ROPEWAY_GONDOLA_WIDTH_RATIO = 0.1855f
private const val ROPEWAY_GONDOLA_HEIGHT_RATIO = 0.4197f

/**
 * Per-frame transform and frame-toggle state for a roadside landmark or character.
 */
data class LandmarkMotionOffset(
    val translateXDp: Float = 0f,
    val translateYDp: Float = 0f,
    val rotationDeg: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val useSecondFrame: Boolean = false,
    val flipHorizontal: Boolean = false,
)

/**
 * Per-frame slide progress and gentle cabin swing for the Mt. Hakodate ropeway gondola.
 */
data class RopewayGondolaState(
    val cableProgress: Float,
    val swingDeg: Float,
)

/**
 * World-space state for the single soaring Umigarasu seabird relative to its patrol center.
 */
data class WorldUmigarasuState(
    val offsetXDp: Float,
    val offsetYDp: Float,
    val headingDeg: Float,
    val flapFrame: Int,
    val sizeDp: Float,
    val altitudeShadowDp: Float,
)

/**
 * Placement target for re-entering the soaring Umigarasu smoothly from outside the screen edge.
 */
data class BirdPatrolPlacement(
    val patrolCenterWorld: Offset,
    val phaseOffset: Float,
)

/**
 * Computes seamless periodic motion offsets across a normalized 0..1 animation phase.
 * Integer harmonic frequencies ensure phase 0f and phase 1f wrap identically without jumps.
 */
fun computeLandmarkMotion(
    style: LandmarkMotionStyle,
    phase: Float,
    seedPhase: Float = 0f,
    tapPopProgress: Float = 0f,
): LandmarkMotionOffset {
    val pNorm = phase.mod(1f)
    val seedRad = seedPhase * TWO_PI

    var tx = 0f
    var ty = 0f
    var rot = 0f
    var sx = 1f
    var sy = 1f
    var secondFrame = false
    var flipHoriz = false

    when (style) {
        LandmarkMotionStyle.NONE,
        LandmarkMotionStyle.SAKURA_PETALS,
        LandmarkMotionStyle.ONSEN_STEAM,
        LandmarkMotionStyle.LIGHTHOUSE_BEAM,
        LandmarkMotionStyle.ROPEWAY_SPARKLE,
        LandmarkMotionStyle.RIVER_SHIMMER,
        LandmarkMotionStyle.CAMPFIRE_FLAME,
        LandmarkMotionStyle.COASTAL_WAVE_CRASH,
        LandmarkMotionStyle.WIND_TURBINE_SPIN,
        -> Unit

        LandmarkMotionStyle.DEER_GRAZE -> {
            // Calm forest pause interspersed with a gentle grazing dip
            val grazeWave = cos(TWO_PI * 4f * pNorm + seedRad)
            val dip = ((grazeWave - 0.10f) / 0.90f).coerceAtLeast(0f)
            val subNibble = 0.5f + 0.5f * sin(TWO_PI * 16f * pNorm + seedRad)
            val activeDip = dip * (0.65f + 0.35f * subNibble)
            ty = activeDip * 2.2f
            rot = -activeDip * 2.4f
            sx = 1f + activeDip * 0.015f
            sy = 1f - activeDip * 0.03f
        }

        LandmarkMotionStyle.SQUIRREL_NIBBLE -> {
            val burstEnv = (0.5f + 0.5f * sin(TWO_PI * 3f * pNorm + seedRad)).coerceIn(0f, 1f)
            val chew = abs(sin(TWO_PI * 18f * pNorm + seedRad))
            ty = -burstEnv * chew * 1.8f
            rot = sin(TWO_PI * 9f * pNorm + seedRad) * burstEnv * 2.2f
            val squash = burstEnv * chew * 0.04f
            sx = 1f + squash * 0.5f
            sy = 1f - squash
        }

        LandmarkMotionStyle.SWAN_GLIDE -> {
            val glideAngle = TWO_PI * 1f * pNorm + seedRad
            val rippleAngle = TWO_PI * 3f * pNorm + seedRad
            tx = sin(glideAngle) * 2.0f
            ty = sin(rippleAngle) * 0.9f
        }

        LandmarkMotionStyle.WHALE_SPOUT -> {
            val swell = TWO_PI * 2f * pNorm + seedRad
            ty = sin(swell) * 2.4f
            rot = cos(swell) * 2.0f
        }

        LandmarkMotionStyle.WAVE_DANCE_2FRAME -> {
            val flipCycle = (pNorm * 18f + seedPhase * 2f).mod(2f)
            secondFrame = flipCycle >= 1f
            val danceAngle = TWO_PI * 9f * pNorm + seedRad
            ty = -abs(sin(danceAngle)) * 4.5f
            rot = sin(danceAngle) * 3.2f
            val squash = cos(danceAngle) * 0.03f
            sx = 1f - squash
            sy = 1f + squash
        }

        LandmarkMotionStyle.WILDLIFE_WALK_2FRAME -> {
            val patrolAngle = TWO_PI * 1f * pNorm + seedRad
            tx = sin(patrolAngle) * 16f
            val stepAngle = TWO_PI * 12f * pNorm + seedRad
            ty = -abs(sin(stepAngle)) * 2.2f
            rot = sin(stepAngle) * 1.8f
            secondFrame = (pNorm * 24f + seedPhase * 2f).mod(2f) >= 1f
            flipHoriz = cos(patrolAngle) < 0f
        }

        LandmarkMotionStyle.DANCE_BOUNCE -> {
            val bounceAngle = TWO_PI * 6f * pNorm + seedRad
            val tiltAngle = TWO_PI * 3f * pNorm + seedRad
            ty = -abs(sin(bounceAngle)) * 3.2f
            rot = sin(tiltAngle) * 2.8f
            val squash = cos(bounceAngle) * 0.03f
            sx = 1f - squash
            sy = 1f + squash
        }

        LandmarkMotionStyle.BOAT_BOB -> {
            val waveAngle = TWO_PI * 3f * pNorm + seedRad
            ty = sin(waveAngle) * 2.5f
            rot = cos(waveAngle) * 1.8f
        }

        LandmarkMotionStyle.TREE_SWAY -> {
            val breezeAngle = TWO_PI * 2f * pNorm + seedRad
            rot = sin(breezeAngle) * 1.5f
        }

        LandmarkMotionStyle.TRAM_CHUG -> {
            val glideAngle = TWO_PI * 2f * pNorm + seedRad
            val chugAngle = TWO_PI * 8f * pNorm + seedRad
            tx = sin(glideAngle) * 2.8f
            ty = -abs(sin(chugAngle)) * 1.2f
        }

        LandmarkMotionStyle.SHINKANSEN_GLIDE -> {
            val glideAngle = TWO_PI * 2f * pNorm + seedRad
            tx = sin(glideAngle) * 4.5f
        }
    }

    if (tapPopProgress > 0f) {
        val popT = tapPopProgress.coerceIn(0f, 1f)
        val env = sin(PI.toFloat() * popT)
        val springBoost = 1f + env * 0.16f * (1f - 0.3f * popT)
        ty -= env * 7f
        sx *= springBoost
        sy *= springBoost
    }

    return LandmarkMotionOffset(
        translateXDp = tx,
        translateYDp = ty,
        rotationDeg = rot,
        scaleX = sx,
        scaleY = sy,
        useSecondFrame = secondFrame,
        flipHorizontal = flipHoriz,
    )
}

/**
 * Maps a normalized 0..1 loop phase to a ping-pong wingbeat index (0 -> 1 -> 2 -> 1).
 */
fun birdFlapFrameIndex(
    phase: Float,
    cyclesPerLoop: Int = 20,
    offset: Float = 0f,
): Int {
    val totalSteps = (cyclesPerLoop.coerceAtLeast(1) * 4).toFloat()
    val step = floor((phase + offset).mod(1f) * totalSteps).toInt() % 4
    return when (step) {
        0 -> 0
        1 -> 1
        2 -> 2
        else -> 1
    }
}

/**
 * Computes seamless periodic ropeway gondola slide progress and gentle cabin pendulum tilt.
 */
fun computeRopewayGondolaState(phase: Float): RopewayGondolaState {
    val pNorm = phase.mod(1f)
    val angle = TWO_PI * pNorm
    val cableProgress = (0.5f + 0.5f * sin(angle)).coerceIn(0f, 1f)
    val swingDeg = -cos(angle) * 2.2f
    return RopewayGondolaState(
        cableProgress = cableProgress,
        swingDeg = swingDeg,
    )
}

/**
 * Computes deterministic world-space soaring state for a single Umigarasu seabird
 * orbiting smoothly around its world patrol anchor without clinging to scroll gestures.
 */
fun computeWorldUmigarasu(phase: Float): WorldUmigarasuState {
    val pNorm = phase.mod(1f)
    val theta = TWO_PI * pNorm - 1.95f
    val rx = 150f
    val ry = 85f
    val tiltRad = -10.0 * (Math.PI / 180.0)
    val cosT = cos(tiltRad).toFloat()
    val sinT = sin(tiltRad).toFloat()

    val lx = rx * cos(theta)
    val ly = ry * sin(theta)
    val ox = lx * cosT - ly * sinT
    val oy = lx * sinT + ly * cosT

    val dlx = -rx * sin(theta)
    val dly = ry * cos(theta)
    val vx = dlx * cosT - dly * sinT
    val vy = dlx * sinT + dly * cosT
    val headingDeg = Math.toDegrees(atan2(vy.toDouble(), vx.toDouble())).toFloat()

    return WorldUmigarasuState(
        offsetXDp = ox,
        offsetYDp = oy,
        headingDeg = headingDeg,
        flapFrame = birdFlapFrameIndex(phase = pNorm, cyclesPerLoop = 18),
        sizeDp = 50f,
        altitudeShadowDp = 24f,
    )
}

/**
 * Computes a world-space patrol center and phase offset so the Umigarasu smoothly glides in
 * from outside the left or right viewport edge instead of materializing mid-screen.
 */
fun computeEdgeEntryPatrolCenter(
    viewportWorld: Rect,
    currentPhase: Float,
    density: Float,
    zoomScale: Float,
    enterFromLeft: Boolean = true,
): BirdPatrolPlacement {
    val targetStartPhase = if (enterFromLeft) 0.90f else 0.40f
    val startBird = computeWorldUmigarasu(targetStartPhase)
    val zoomComp = if (zoomScale < 1.0f) {
        zoomScale.coerceIn(0.10f, 1.0f).pow(-0.55f)
    } else {
        1.0f
    }
    val edgeMarginPx = (startBird.sizeDp * 0.75f + 22f) * density * zoomComp
    val targetBirdWorldX = if (enterFromLeft) {
        viewportWorld.left - edgeMarginPx
    } else {
        viewportWorld.right + edgeMarginPx
    }
    val targetBirdWorldY = viewportWorld.top + viewportWorld.height * 0.28f
    val patrolCenter = Offset(
        x = targetBirdWorldX - startBird.offsetXDp * density,
        y = targetBirdWorldY - startBird.offsetYDp * density,
    )
    val phaseOffset = (targetStartPhase - currentPhase.mod(1f)).mod(1f)
    return BirdPatrolPlacement(
        patrolCenterWorld = patrolCenter,
        phaseOffset = phaseOffset,
    )
}

/**
 * Draws all visible roadside landmarks with gentle idle life motion, ambient effects,
 * and interactive spring-pop reactions without mutating or invalidating terrain Canvas layers.
 * Accepts [getBitmap] backed by an LruCache so off-screen landmark assets are never eagerly decoded.
 */
internal fun DrawScope.drawAnimatedRoadsideLandmarks(
    landmarks: List<RoadsideLandmark>,
    getBitmap: (Int) -> ImageBitmap?,
    ropewayGondolaResId: Int? = null,
    worldScaleFactor: Float,
    viewportWorld: Rect,
    zoomScale: Float,
    animPhase: Float,
    tappedLandmarkId: String?,
    tapPopProgress: Float,
    scratchPath: Path = Path(),
) {
    if (landmarks.isEmpty() || zoomScale < 0.06f) return

    val zoomCompensation = if (zoomScale < 1.0f) {
        zoomScale.coerceIn(0.10f, 1.0f).pow(-0.55f)
    } else {
        1.0f
    }
    val motionDamp = ((zoomScale - 0.12f) / 0.25f).coerceIn(0f, 1f)

    val pad = 220.dp.toPx() * zoomCompensation
    val vLeft = viewportWorld.left - pad
    val vTop = viewportWorld.top - pad
    val vRight = viewportWorld.right + pad
    val vBottom = viewportWorld.bottom + pad

    val visible = landmarks.filter { lm ->
        val cx = lm.x * worldScaleFactor
        val cy = lm.y * worldScaleFactor
        cx in vLeft..vRight && cy in vTop..vBottom
    }.sortedBy { it.y }

    for (landmark in visible) {
        val cx = landmark.x * worldScaleFactor
        val cy = landmark.y * worldScaleFactor
        val seedPhase = ((landmark.id.hashCode() and 0x7FFF) % 1000) / 1000f
        val isTapped = landmark.id == tappedLandmarkId && tapPopProgress > 0f
        val pop = if (isTapped) tapPopProgress else 0f

        val motion = computeLandmarkMotion(
            style = landmark.type.motionStyle,
            phase = animPhase,
            seedPhase = seedPhase,
            tapPopProgress = pop,
        )
        val resId = if (motion.useSecondFrame && landmark.type.animFrame2DrawableResId != null) {
            landmark.type.animFrame2DrawableResId
        } else {
            landmark.type.drawableResId
        }
        val bitmap = getBitmap(resId) ?: continue

        val wPx = landmark.type.baseDisplayWidthDp.dp.toPx() * landmark.scale * zoomCompensation
        val hPx = landmark.type.baseDisplayHeightDp.dp.toPx() * landmark.scale * zoomCompensation
        val unitScale = landmark.scale * zoomCompensation * motionDamp
        val txPx = motion.translateXDp.dp.toPx() * unitScale
        val tyPx = motion.translateYDp.dp.toPx() * unitScale
        val pivot = Offset(cx, cy + hPx * 0.45f)
        val effectiveFlipX = if (landmark.flipX xor motion.flipHorizontal) -1f else 1f
        val gondolaBitmap = if (landmark.type.motionStyle == LandmarkMotionStyle.ROPEWAY_SPARKLE) {
            ropewayGondolaResId?.let { getBitmap(it) }
        } else {
            null
        }

        withTransform({
            translate(txPx, tyPx)
            rotate(degrees = motion.rotationDeg * motionDamp, pivot = pivot)
            scale(
                scaleX = effectiveFlipX * (1f + (motion.scaleX - 1f) * motionDamp.coerceAtLeast(if (isTapped) 1f else 0f)),
                scaleY = 1f + (motion.scaleY - 1f) * motionDamp.coerceAtLeast(if (isTapped) 1f else 0f),
                pivot = pivot,
            )
        }) {
            drawImage(
                image = bitmap,
                dstOffset = IntOffset(
                    x = (cx - wPx / 2f).roundToInt(),
                    y = (cy - hPx / 2f).roundToInt(),
                ),
                dstSize = IntSize(wPx.roundToInt(), hPx.roundToInt()),
            )

            if (motionDamp > 0.05f || landmark.type.motionStyle == LandmarkMotionStyle.ROPEWAY_SPARKLE) {
                drawLandmarkAmbientOverlay(
                    style = landmark.type.motionStyle,
                    cx = cx,
                    cy = cy,
                    wPx = wPx,
                    hPx = hPx,
                    phase = animPhase,
                    seedPhase = seedPhase,
                    alphaScale = motionDamp,
                    ropewayGondolaBitmap = gondolaBitmap,
                    scratchPath = scratchPath,
                )
            }
        }

        if (isTapped) {
            drawTapSparkleBurst(
                cx = cx,
                cy = cy - hPx * 0.15f,
                radius = wPx * 0.46f,
                progress = pop,
                scratchPath = scratchPath,
            )
        }
    }
}

private fun DrawScope.drawLandmarkAmbientOverlay(
    style: LandmarkMotionStyle,
    cx: Float,
    cy: Float,
    wPx: Float,
    hPx: Float,
    phase: Float,
    seedPhase: Float,
    alphaScale: Float,
    ropewayGondolaBitmap: ImageBitmap?,
    scratchPath: Path,
) {
    when (style) {
        LandmarkMotionStyle.LIGHTHOUSE_BEAM -> {
            val sweepAngle = sin(TWO_PI * 2f * phase + seedPhase * TWO_PI) * 32f
            val lanternX = cx - wPx * 0.03f
            val lanternY = cy - hPx * 0.34f
            val beamLength = wPx * 0.52f
            withTransform({
                rotate(degrees = sweepAngle, pivot = Offset(lanternX, lanternY))
            }) {
                scratchPath.reset()
                scratchPath.moveTo(lanternX, lanternY)
                scratchPath.lineTo(lanternX - beamLength, lanternY - beamLength * 0.20f)
                scratchPath.lineTo(lanternX - beamLength, lanternY + beamLength * 0.16f)
                scratchPath.close()
                drawPath(
                    path = scratchPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x00FFF176),
                            Color(0xFFFFF176).copy(alpha = 0.40f * alphaScale),
                        ),
                        startX = lanternX - beamLength,
                        endX = lanternX,
                    ),
                )
            }
        }

        LandmarkMotionStyle.ONSEN_STEAM -> {
            val baseX = cx - wPx * 0.24f
            val baseY = cy - hPx * 0.10f
            for (i in 0..2) {
                val t = (phase * 2f + seedPhase + i * 0.33f).mod(1f)
                val sx = baseX + (i - 1) * wPx * 0.08f + sin(TWO_PI * t + i) * wPx * 0.035f
                val sy = baseY - t * hPx * 0.46f
                val alpha = sin(PI.toFloat() * t) * 0.55f * alphaScale
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = wPx * (0.032f + t * 0.028f),
                    center = Offset(sx, sy),
                )
            }
        }

        LandmarkMotionStyle.TREE_SWAY,
        LandmarkMotionStyle.SAKURA_PETALS,
        -> {
            for (i in 0..3) {
                val t = (phase * 2f + seedPhase + i * 0.25f).mod(1f)
                val px = cx + (-0.28f + i * 0.19f) * wPx + sin(TWO_PI * t + i) * wPx * 0.06f
                val py = cy - hPx * 0.30f + t * hPx * 0.52f
                val alpha = sin(PI.toFloat() * t) * 0.85f * alphaScale
                drawOval(
                    color = Color(0xFFFF8FC2).copy(alpha = alpha),
                    topLeft = Offset(px, py),
                    size = Size(wPx * 0.026f, wPx * 0.017f),
                )
            }
        }

        LandmarkMotionStyle.CAMPFIRE_FLAME -> {
            // Exact campfire position on highland_camp: right-bottom stone ring (0.77, 0.76)
            val fireX = cx + wPx * 0.270f
            val fireBaseY = cy + hPx * 0.265f
            val sway = sin(TWO_PI * 10f * phase + seedPhase * TWO_PI)
            val flicker = 0.85f + 0.15f * cos(TWO_PI * 14f * phase + seedPhase * TWO_PI)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFB300).copy(alpha = 0.34f * flicker * alphaScale),
                        Color.Transparent,
                    ),
                    center = Offset(fireX, fireBaseY - hPx * 0.08f),
                    radius = wPx * 0.13f,
                ),
                radius = wPx * 0.13f,
                center = Offset(fireX, fireBaseY - hPx * 0.08f),
            )

            // Outer flickering flame tongue
            val flameH = hPx * (0.145f * flicker)
            val flameW = wPx * 0.048f
            scratchPath.reset()
            scratchPath.moveTo(fireX - flameW, fireBaseY)
            scratchPath.quadraticTo(
                fireX - flameW * 0.9f,
                fireBaseY - flameH * 0.55f,
                fireX + sway * flameW * 0.45f,
                fireBaseY - flameH,
            )
            scratchPath.quadraticTo(
                fireX + flameW * 0.9f,
                fireBaseY - flameH * 0.55f,
                fireX + flameW,
                fireBaseY,
            )
            scratchPath.close()
            drawPath(
                path = scratchPath,
                color = Color(0xFFFF7043).copy(alpha = 0.82f * alphaScale),
            )

            // Inner sunny core flame
            val coreH = flameH * 0.65f
            val coreW = flameW * 0.58f
            scratchPath.reset()
            scratchPath.moveTo(fireX - coreW, fireBaseY)
            scratchPath.quadraticTo(
                fireX - coreW * 0.8f,
                fireBaseY - coreH * 0.5f,
                fireX - sway * coreW * 0.35f,
                fireBaseY - coreH,
            )
            scratchPath.quadraticTo(
                fireX + coreW * 0.8f,
                fireBaseY - coreH * 0.5f,
                fireX + coreW,
                fireBaseY,
            )
            scratchPath.close()
            drawPath(
                path = scratchPath,
                color = Color(0xFFFFEE58).copy(alpha = 0.92f * alphaScale),
            )

            // Rising glowing embers
            for (i in 0..2) {
                val t = (phase * 4f + seedPhase + i * 0.33f).mod(1f)
                val ex = fireX + sin(TWO_PI * 2f * t + i * 2.1f) * wPx * 0.042f
                val ey = fireBaseY - hPx * (0.08f + t * 0.25f)
                val alpha = sin(PI.toFloat() * t) * 0.88f * alphaScale
                drawCircle(
                    color = if (i % 2 == 0) Color(0xFFFFCA28).copy(alpha = alpha) else Color(0xFFFF5722).copy(alpha = alpha),
                    radius = wPx * (0.011f * (1f - 0.35f * t)),
                    center = Offset(ex, ey),
                )
            }

            // Delicate white smoke wisps rising higher above the fire
            for (i in 0..1) {
                val t = (phase * 2f + seedPhase + i * 0.5f).mod(1f)
                val sx = fireX + sin(TWO_PI * t + i) * wPx * 0.032f
                val sy = fireBaseY - hPx * (0.15f + t * 0.34f)
                val alpha = sin(PI.toFloat() * t) * 0.44f * alphaScale
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = wPx * (0.022f + t * 0.020f),
                    center = Offset(sx, sy),
                )
            }
        }

        LandmarkMotionStyle.SWAN_GLIDE -> {
            // Centered beneath the swan boat at bottom-right of ONUMA_SWAN_BOAT (0.835, 0.855)
            val swanWaterX = cx + wPx * 0.335f
            val swanWaterY = cy + hPx * 0.355f
            for (i in 0..1) {
                val t = (phase * 3f + seedPhase + i * 0.5f).mod(1f)
                val rx = wPx * (0.075f + t * 0.115f)
                val ry = hPx * (0.032f + t * 0.045f)
                val alpha = sin(PI.toFloat() * t) * 0.72f * alphaScale
                drawOval(
                    color = Color.White.copy(alpha = alpha),
                    topLeft = Offset(swanWaterX - rx, swanWaterY - ry),
                    size = Size(rx * 2f, ry * 2f),
                    style = Stroke(width = wPx * 0.008f),
                )
            }
            for (i in 0..2) {
                val t = (phase * 2f + seedPhase + i * 0.33f).mod(1f)
                val sx = cx + (-0.28f + i * 0.24f - t * 0.06f) * wPx
                val sy = cy + (0.28f + (i % 2) * 0.11f) * hPx
                val alpha = sin(PI.toFloat() * t) * 0.65f * alphaScale
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(sx, sy),
                    end = Offset(sx + wPx * 0.055f, sy),
                    strokeWidth = wPx * 0.010f,
                    cap = StrokeCap.Round,
                )
            }
        }

        LandmarkMotionStyle.COASTAL_WAVE_CRASH -> {
            for (w in 0..1) {
                val t = (phase * 3f + seedPhase + w * 0.5f).mod(1f)
                val crashX = cx + (if (w == 0) -0.18f else 0.18f) * wPx
                val baseY = cy + hPx * 0.33f
                val surgeUp = sin(PI.toFloat() * t) * hPx * 0.11f
                val crestAlpha = sin(PI.toFloat() * t) * 0.78f * alphaScale
                val foamWidth = wPx * (0.14f + 0.14f * t)
                val foamHeight = hPx * (0.045f + 0.045f * t)

                drawOval(
                    color = Color(0xFFE0F7FA).copy(alpha = crestAlpha),
                    topLeft = Offset(crashX - foamWidth * 0.5f, baseY - surgeUp - foamHeight * 0.5f),
                    size = Size(foamWidth, foamHeight),
                )

                // Splash droplets arcing away as the wave strikes the sea-stack rock
                for (d in -1..1) {
                    val dt = (t * 1.15f).coerceIn(0f, 1f)
                    val dx = d * wPx * (0.045f + 0.075f * dt)
                    val arcY = baseY - surgeUp - sin(PI.toFloat() * dt) * hPx * (0.14f + (1 - abs(d)) * 0.04f)
                    val dropAlpha = sin(PI.toFloat() * dt) * 0.90f * alphaScale
                    drawCircle(
                        color = Color.White.copy(alpha = dropAlpha),
                        radius = wPx * (0.014f * (1f - 0.3f * dt)),
                        center = Offset(crashX + dx, arcY),
                    )
                }
            }
        }

        LandmarkMotionStyle.WIND_TURBINE_SPIN,
        LandmarkMotionStyle.WHALE_SPOUT,
        -> Unit

        LandmarkMotionStyle.ROPEWAY_SPARKLE -> {
            val state = computeRopewayGondolaState(phase)
            val left = cx - wPx / 2f
            val top = cy - hPx / 2f
            val attachNormX = ROPEWAY_CABLE_START_X +
                (ROPEWAY_CABLE_END_X - ROPEWAY_CABLE_START_X) * state.cableProgress
            val attachNormY = ROPEWAY_CABLE_START_Y +
                (ROPEWAY_CABLE_END_Y - ROPEWAY_CABLE_START_Y) * state.cableProgress
            val attachX = left + attachNormX * wPx
            val attachY = top + attachNormY * hPx

            ropewayGondolaBitmap?.let { gondola ->
                val gw = wPx * ROPEWAY_GONDOLA_WIDTH_RATIO
                val gh = hPx * ROPEWAY_GONDOLA_HEIGHT_RATIO
                withTransform({
                    rotate(degrees = state.swingDeg * alphaScale, pivot = Offset(attachX, attachY))
                }) {
                    drawImage(
                        image = gondola,
                        dstOffset = IntOffset(
                            x = (attachX - gw * ROPEWAY_GONDOLA_ATTACH_X).roundToInt(),
                            y = (attachY - gh * ROPEWAY_GONDOLA_ATTACH_Y).roundToInt(),
                        ),
                        dstSize = IntSize(gw.roundToInt(), gh.roundToInt()),
                    )
                }
            }

            if (alphaScale > 0.05f) {
                for (i in 0..1) {
                    val t = (phase * 3f + seedPhase + i * 0.5f).mod(1f)
                    val pulse = 0.45f + 0.55f * abs(sin(TWO_PI * t))
                    val sx = cx + (if (i == 0) 0.09f else -0.20f) * wPx
                    val sy = cy - (if (i == 0) 0.25f else 0.31f) * hPx
                    val r = wPx * 0.025f * pulse
                    val color = Color(0xFFFFE04D).copy(alpha = 0.85f * pulse * alphaScale)
                    drawFourPointStar(
                        center = Offset(sx, sy),
                        radius = r,
                        color = color,
                        scratchPath = scratchPath,
                    )
                }
            }
        }

        LandmarkMotionStyle.RIVER_SHIMMER -> {
            for (i in 0..2) {
                val t = (phase * 2f + seedPhase + i * 0.33f).mod(1f)
                val sx = cx + (0.04f - t * 0.16f) * wPx
                val sy = cy + (-0.02f + i * 0.11f + t * 0.16f) * hPx
                val alpha = sin(PI.toFloat() * t) * 0.72f * alphaScale
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(sx, sy),
                    end = Offset(sx + wPx * 0.055f, sy - hPx * 0.02f),
                    strokeWidth = wPx * 0.012f,
                    cap = StrokeCap.Round,
                )
            }
        }

        else -> Unit
    }
}

private fun DrawScope.drawTapSparkleBurst(
    cx: Float,
    cy: Float,
    radius: Float,
    progress: Float,
    scratchPath: Path,
) {
    val alpha = sin(PI.toFloat() * progress).coerceIn(0f, 1f)
    val reach = radius * (0.45f + 0.55f * progress)
    for (i in 0..5) {
        val angle = TWO_PI * (i / 6f) + progress * 0.5f
        val sx = cx + cos(angle) * reach
        val sy = cy + sin(angle) * reach
        val starColor = if (i % 2 == 0) Color(0xFFFFD54F) else Color(0xFFFF80AB)
        drawFourPointStar(
            center = Offset(sx, sy),
            radius = radius * 0.09f * (1f - 0.3f * progress),
            color = starColor.copy(alpha = alpha),
            scratchPath = scratchPath,
        )
    }
}

private fun DrawScope.drawFourPointStar(
    center: Offset,
    radius: Float,
    color: Color,
    scratchPath: Path,
) {
    val inner = radius * 0.32f
    scratchPath.reset()
    scratchPath.moveTo(center.x, center.y - radius)
    scratchPath.lineTo(center.x + inner, center.y - inner)
    scratchPath.lineTo(center.x + radius, center.y)
    scratchPath.lineTo(center.x + inner, center.y + inner)
    scratchPath.lineTo(center.x, center.y + radius)
    scratchPath.lineTo(center.x - inner, center.y + inner)
    scratchPath.lineTo(center.x - radius, center.y)
    scratchPath.lineTo(center.x - inner, center.y - inner)
    scratchPath.close()
    drawPath(path = scratchPath, color = color)
}

/**
 * Draws the single hand-drawn Umigarasu seabird gliding gracefully in 1.0x world space
 * above roads, landmarks, and Sugoroku tiles with its soft shadow directly underneath.
 */
internal fun DrawScope.drawWorldSpaceUmigarasu(
    birdFrames: List<ImageBitmap>,
    patrolCenterWorld: Offset,
    phase: Float,
    zoomScale: Float,
) {
    if (birdFrames.size < 3 || zoomScale < 0.08f) return

    val alphaFactor = ((zoomScale - 0.08f) / 0.12f).coerceIn(0f, 1f)
    if (alphaFactor <= 0.01f) return

    val zoomCompensation = if (zoomScale < 1.0f) {
        zoomScale.coerceIn(0.10f, 1.0f).pow(-0.55f)
    } else {
        1.0f
    }

    val bird = computeWorldUmigarasu(phase)
    val bx = patrolCenterWorld.x + bird.offsetXDp.dp.toPx()
    val by = patrolCenterWorld.y + bird.offsetYDp.dp.toPx()
    val sizePx = bird.sizeDp.dp.toPx() * zoomCompensation
    val shadowOffsetX = 12.dp.toPx() * zoomCompensation
    val shadowOffsetY = bird.altitudeShadowDp.dp.toPx() * zoomCompensation

    // Soft aerial ground shadow offset down-right for altitude depth
    drawOval(
        color = Color(0x28000000).copy(alpha = 0.16f * alphaFactor),
        topLeft = Offset(bx + shadowOffsetX - sizePx * 0.28f, by + shadowOffsetY - sizePx * 0.09f),
        size = Size(sizePx * 0.56f, sizePx * 0.18f),
    )

    val isLeftward = abs(bird.headingDeg) > 90f
    val visualTiltDeg = if (isLeftward) {
        val signed = if (bird.headingDeg > 0f) 180f - bird.headingDeg else -180f - bird.headingDeg
        signed.coerceIn(-28f, 28f)
    } else {
        bird.headingDeg.coerceIn(-28f, 28f)
    }

    val frameBitmap = birdFrames[bird.flapFrame.coerceIn(0, birdFrames.lastIndex)]
    withTransform({
        if (isLeftward) {
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(bx, by))
        }
        rotate(degrees = visualTiltDeg, pivot = Offset(bx, by))
    }) {
        drawImage(
            image = frameBitmap,
            dstOffset = IntOffset(
                x = (bx - sizePx / 2f).roundToInt(),
                y = (by - sizePx / 2f).roundToInt(),
            ),
            dstSize = IntSize(sizePx.roundToInt(), sizePx.roundToInt()),
            alpha = alphaFactor,
        )
    }
}
