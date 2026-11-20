package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.graphics.Color
import com.aidomari.app.domain.model.HighwayCategory

/**
 * Visual styling theme for the Hokkaido Sugoroku Map.
 * Provides unified color palettes with gentle storybook watercolor tones,
 * and allows clean switching for future day/night or seasonal variations.
 */
data class SugorokuMapTheme(
    val landColor: Color = Color(0xFFF7FAF5), // Bright airy Hokkaido daylight land
    val beachColor: Color = Color(0xFFF5F1E6), // Clean sunny light sand shore
    val waveColor: Color = Color(0xFFEDF8FE), // Bright coastal foam
    val borderColor: Color = Color(0xFF8BC0B2), // Clear fresh seafoam-sage coastline
    val lakeWaterColor: Color = Color(0xFF82CEF7), // Brighter livelier Hokkaido sky-blue lake
    val oceanColor: Color = Color(0xFF82CEF7), // Brighter livelier Hokkaido sky-blue sea
    val travelRouteColor: Color = Color(0xFFFF9800), // Vibrant orange walking route
    val travelRouteBorderColor: Color = Color(0xFFE65100), // Deep warm orange route border
    val travelRouteBedColor: Color = Color(0xFFFFF3E0), // Warm ivory inner route bed
    val nationalHighwayColor: Color = Color(0xFFD4A373), // Warm ochre highway
    val scenicRoadColor: Color = Color(0xFFA3B18A), // Soft sage scenic road
    val prefecturalRoadColor: Color = Color(0xFFD6C7B2), // Warm sand-taupe road
    val standardRoadBedColor: Color = Color(0xFFFFFFFF), // Pure white roads
    val standardRoadBorderColor: Color = Color(0xFFD3DFD8), // Lighter airy road border
) {
    fun roadColor(category: HighwayCategory): Color = when (category) {
        HighwayCategory.TRAVEL_ROUTE -> travelRouteColor
        HighwayCategory.NATIONAL_HIGHWAY -> nationalHighwayColor
        HighwayCategory.SCENIC_ROAD -> scenicRoadColor
        HighwayCategory.PREFECTURAL_ROAD -> prefecturalRoadColor
    }

    fun roadBorderColor(category: HighwayCategory): Color = when (category) {
        HighwayCategory.TRAVEL_ROUTE -> travelRouteBorderColor
        else -> standardRoadBorderColor
    }

    fun roadBedColor(category: HighwayCategory): Color = when (category) {
        HighwayCategory.TRAVEL_ROUTE -> travelRouteBedColor
        else -> standardRoadBedColor
    }

    companion object {
        val Default = SugorokuMapTheme()

        val Evening = SugorokuMapTheme(
            landColor = Color(0xFF9EBA8D),
            oceanColor = Color(0xFF78909C),
            lakeWaterColor = Color(0xFF78909C),
            travelRouteColor = Color(0xFFFFB74D),
            travelRouteBorderColor = Color(0xFFD84315),
        )

        val Night = SugorokuMapTheme(
            landColor = Color(0xFF37474F),
            oceanColor = Color(0xFF263238),
            lakeWaterColor = Color(0xFF263238),
            borderColor = Color(0xFF455A64),
            beachColor = Color(0xFF546E7A),
            waveColor = Color(0xFF37474F),
            travelRouteColor = Color(0xFFFFD54F),
            travelRouteBorderColor = Color(0xFFFF8F00),
        )
    }
}
