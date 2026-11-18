package com.aidomari.app.domain.model

import com.aidomari.app.R

/**
 * Unique identifiers for selectable traveler companions.
 * Enables seamless future expansion to multiple companions (e.g. Fox, Bear, Chibi-explorer).
 */
enum class TravelerCompanionId(val serialName: String) {
    SHIMA_ENAGA("shima_enaga"),
}

/**
 * Traveler companion data model for the Hokkaido Sugoroku journey.
 *
 * @param id Unique companion identifier.
 * @param name Display name of the companion.
 * @param title Short subtitle / epithet.
 * @param drawableResId Resource ID for the transparent sprite asset.
 * @param defaultScale Base scaling factor for rendering.
 */
data class TravelerCompanion(
    val id: TravelerCompanionId,
    val name: String,
    val title: String,
    val drawableResId: Int,
    val defaultScale: Float = 1.0f,
) {
    companion object {
        val SHIMA_ENAGA = TravelerCompanion(
            id = TravelerCompanionId.SHIMA_ENAGA,
            name = "シマエナガ探検隊",
            title = "雪の妖精",
            drawableResId = R.drawable.traveler_companion_shima_enaga,
            defaultScale = 1.0f,
        )

        val ALL_COMPANIONS: List<TravelerCompanion> = listOf(
            SHIMA_ENAGA,
        )

        val DEFAULT: TravelerCompanion = SHIMA_ENAGA

        fun fromId(id: String?): TravelerCompanion {
            return ALL_COMPANIONS.firstOrNull { it.id.serialName == id } ?: DEFAULT
        }
    }
}
