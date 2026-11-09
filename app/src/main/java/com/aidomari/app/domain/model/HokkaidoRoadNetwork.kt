package com.aidomari.app.domain.model

import android.content.Context
import androidx.compose.ui.geometry.Offset
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Classification of Hokkaido Highway network.
 */
enum class HighwayCategory {
    NATIONAL_HIGHWAY, // 国道（主要幹線）
    PREFECTURAL_ROAD, // 主要道道
    SCENIC_ROAD, // 絶景・観光主要ルート（知床横断道路等）
}

/**
 * A real road segment connecting continuous points across Hokkaido.
 */
data class HighwaySegment(
    val id: String,
    val name: String,
    val category: HighwayCategory,
    val routeNumber: String,
    val waypoints: List<Offset>,
)

/**
 * High-fidelity verified Hokkaido Highway Network model.
 * Contains over 34,000 real geometry points extracted from OpenStreetMap GIS data
 * and projected into the Wikipedia Hokkaido standard coordinate space using Thin Plate Spline.
 */
object HokkaidoRoadNetwork {

    private var cachedHighDensitySegments: List<HighwaySegment>? = null

    /**
     * Loads high-density road network containing 34,000+ real GIS coordinates.
     * Caches in memory once loaded.
     */
    fun getHighDensitySegments(context: Context? = null): List<HighwaySegment> {
        cachedHighDensitySegments?.let { return it }

        val inputStream: InputStream? = try {
            context?.assets?.open("hokkaido_roads_high_density.bin")
                ?: javaClass.classLoader?.getResourceAsStream("assets/hokkaido_roads_high_density.bin")
                ?: javaClass.classLoader?.getResourceAsStream("hokkaido_roads_high_density.bin")
        } catch (_: Exception) {
            null
        }

        if (inputStream != null) {
            try {
                val parsed = parseBinaryRoads(inputStream)
                if (parsed.isNotEmpty()) {
                    cachedHighDensitySegments = parsed
                    return parsed
                }
            } catch (_: Exception) {
                // fallback to key skeleton segments
            }
        }

        return fallbackSkeletonSegments
    }

    private fun parseBinaryRoads(inputStream: InputStream): List<HighwaySegment> {
        val bytes = inputStream.use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val magic = ByteArray(8)
        buffer.get(magic)
        if (String(magic) != "HKDROAD1") return emptyList()

        val numSegments = buffer.short.toInt() and 0xFFFF
        val result = ArrayList<HighwaySegment>(numSegments)

        for (i in 0 until numSegments) {
            val idLen = buffer.get().toInt() and 0xFF
            val idBytes = ByteArray(idLen)
            buffer.get(idBytes)
            val id = String(idBytes, Charsets.UTF_8)

            val nameLen = buffer.get().toInt() and 0xFF
            val nameBytes = ByteArray(nameLen)
            buffer.get(nameBytes)
            val name = String(nameBytes, Charsets.UTF_8)

            val catByte = buffer.get().toInt() and 0xFF
            val category = when (catByte) {
                0 -> HighwayCategory.NATIONAL_HIGHWAY
                1 -> HighwayCategory.PREFECTURAL_ROAD
                else -> HighwayCategory.SCENIC_ROAD
            }

            val numPoints = buffer.int
            val pts = ArrayList<Offset>(numPoints)
            for (p in 0 until numPoints) {
                val x = buffer.float
                val y = buffer.float
                pts.add(Offset(x, y))
            }

            result.add(
                HighwaySegment(
                    id = id,
                    name = name,
                    category = category,
                    routeNumber = id.split("_").firstOrNull() ?: "",
                    waypoints = pts,
                ),
            )
        }
        return result
    }

    /**
     * Fallback skeleton segments in case asset binary is unavailable.
     */
    val fallbackSkeletonSegments: List<HighwaySegment> = listOf(
        HighwaySegment(
            id = "route_5",
            name = "国道5号",
            category = HighwayCategory.NATIONAL_HIGHWAY,
            routeNumber = "5",
            waypoints = listOf(
                Offset(105.0f, 328.0f),
                Offset(87.5f, 307.5f),
                Offset(76.0f, 295.5f),
                Offset(76.0f, 283.0f),
                Offset(85.0f, 272.5f),
            ),
        ),
    )
}
