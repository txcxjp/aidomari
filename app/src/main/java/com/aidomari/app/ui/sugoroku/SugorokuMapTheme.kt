package com.aidomari.app.ui.sugoroku

import androidx.compose.ui.graphics.Color
import com.aidomari.app.domain.model.HighwayCategory

/**
 * Visual styling theme for the Hokkaido Sugoroku Map.
 * Provides unified color palettes with gentle storybook watercolor tones,
 * and allows clean switching for future day/night or seasonal variations.
 */
data class SugorokuMapTheme(
    val landColor: Color = Color(0xFFA8D5A2), // Soft watercolor meadow green
    val beachColor: Color = Color(0xFFFFF0B2), // Gentle sandy shore
    val waveColor: Color = Color(0xFFE0F7FA), // Soft shoreline foam
    val borderColor: Color = Color(0xFF81C784), // Natural coastline contour
    val lakeWaterColor: Color = Color(0xFF81D4FA), // Calm blue lake water
    val oceanColor: Color = Color(0xFF81D4FA), // Calm blue ocean backdrop
    val travelRouteColor: Color = Color(0xFFFF9800), // Vibrant orange walking route
    val travelRouteBorderColor: Color = Color(0xFFE65100), // Deep warm orange route border
    val travelRouteBedColor: Color = Color(0xFFFFF3E0), // Warm ivory inner route bed
    val nationalHighwayColor: Color = Color(0xFF1565C0), // Crisp blue highway
    val scenicRoadColor: Color = Color(0xFF00897B), // Scenic teal road
    val prefecturalRoadColor: Color = Color(0xFF546E7A), // Slate grey prefectural road
    val standardRoadBedColor: Color = Color(0xFFCFD8DC), // Cool grey asphalt
    val standardRoadBorderColor: Color = Color(0xFF37474F), // Slate roadbed border
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
