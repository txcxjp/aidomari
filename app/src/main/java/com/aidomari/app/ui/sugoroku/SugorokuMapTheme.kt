package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.graphics.Color
import com.aidomari.app.domain.model.HighwayCategory

/**
 * Visual styling theme for the Hokkaido Sugoroku Map.
 * Provides unified color palettes with gentle storybook watercolor tones,
 * and allows clean switching for future day/night or seasonal variations.
 */
data class SugorokuMapTheme(
    val landColor: Color = Color(0xFFF3F6F1), // Clean daylight Hokkaido off-white land
    val beachColor: Color = Color(0xFFF1EFE9), // Clean pale sand shore without sepia cast
    val waveColor: Color = Color(0xFFEAF6FD), // Crisp coastal foam
    val borderColor: Color = Color(0xFF8FB5A8), // Fresh blue-sage coastline contour
    val lakeWaterColor: Color = Color(0xFF8ECDF0), // Clear fresh Hokkaido sky-blue lake
    val oceanColor: Color = Color(0xFF8ECDF0), // Clear fresh Hokkaido sky-blue sea
    val travelRouteColor: Color = Color(0xFFFF9800), // Vibrant orange walking route
    val travelRouteBorderColor: Color = Color(0xFFE65100), // Deep warm orange route border
    val travelRouteBedColor: Color = Color(0xFFFFF3E0), // Warm ivory inner route bed
    val nationalHighwayColor: Color = Color(0xFFD4A373), // Warm ochre highway
    val scenicRoadColor: Color = Color(0xFFA3B18A), // Soft sage scenic road
    val prefecturalRoadColor: Color = Color(0xFFD6C7B2), // Warm sand-taupe road
    val standardRoadBedColor: Color = Color(0xFFFFFFFF), // Pure clean white roads
    val standardRoadBorderColor: Color = Color(0xFFCFD8D2), // Clean sage-grey road outline
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
