package com.aidomari.app.domain.util

import java.util.Locale

/**
 * Utility calculator for converting card study time into realistic walking distances.
 * Based on standard adult walking speed (80 meters per minute / 4.8 km/h).
 * Caps study time at 20 seconds per question to prevent infinite walking on idle screens.
 */
object SugorokuWalkingCalculator {

    /** Standard pedestrian walking speed (不動産広告等で用いられる標準歩行速度: 分速80m / 時速4.8km) */
    const val BASE_WALKING_METERS_PER_MINUTE: Float = 80.0f

    /** Base walking speed in meters per second (80 / 60 ≈ 1.333 m/s) */
    const val BASE_WALKING_METERS_PER_SECOND: Float = BASE_WALKING_METERS_PER_MINUTE / 60.0f

    /** Maximum allowed study time per question to convert into walking distance (20 seconds) */
    const val MAX_QUESTION_DURATION_SECONDS: Float = 20.0f

    /**
     * Calculates walking distance in meters from the elapsed study time on a question.
     *
     * @param durationSeconds Seconds spent on the question (clamped to [0.0, 20.0])
     * @param speedMultiplier Speed multiplier (default 1.0, used for debug acceleration)
     * @return Distance walked in meters
     */
    fun calculateDistanceMeters(
        durationSeconds: Float,
        speedMultiplier: Float = 1.0f,
    ): Float {
        val clampedSeconds = durationSeconds.coerceIn(0.0f, MAX_QUESTION_DURATION_SECONDS)
        val effectiveMultiplier = speedMultiplier.coerceAtLeast(0.1f)
        return clampedSeconds * BASE_WALKING_METERS_PER_SECOND * effectiveMultiplier
    }

    /**
     * Formats distance in meters into human-readable string (e.g. "450 m", "12.34 km").
     */
    fun formatDistance(meters: Float): String {
        val safeMeters = meters.coerceAtLeast(0.0f)
        return if (safeMeters < 1000.0f) {
            String.format(Locale.US, "%.1f m", safeMeters)
        } else {
            String.format(Locale.US, "%.2f km", safeMeters / 1000.0f)
        }
    }
}
