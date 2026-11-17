package com.aidomari.app.domain.model

import android.content.Context
import androidx.compose.ui.geometry.Offset
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A single travel route leg connecting consecutive Act 1 anchors along verified real roads.
 */
data class Act1RouteLeg(
    val legIndex: Int,
    val fromAnchorId: String,
    val toAnchorId: String,
    val distanceMeters: Float,
    val waypoints: List<Offset>,
)

/**
 * Repository for the authoritative player travel route across Hokkaido Act 1.
 * Contains 447 exact road legs connecting all 448 Act 1 anchors.
 */
object Act1TravelRoute {
    private var cachedLegs: List<Act1RouteLeg>? = null

    /**
     * Loads 447 walking path legs connecting adjacent anchors along real roads.
     */
    fun getLegs(context: Context? = null): List<Act1RouteLeg> {
        cachedLegs?.let { return it }

        val inputStream: InputStream? = try {
            context?.assets?.open("act1_travel_route.bin")
                ?: javaClass.classLoader?.getResourceAsStream("assets/act1_travel_route.bin")
                ?: javaClass.classLoader?.getResourceAsStream("act1_travel_route.bin")
        } catch (_: Exception) {
            null
        }

        if (inputStream != null) {
            try {
                val parsed = parseBinaryLegs(inputStream)
                if (parsed.isNotEmpty()) {
                    cachedLegs = parsed
                    return parsed
                }
            } catch (_: Exception) {
                // Ignore fallback
            }
        }
        return emptyList()
    }

    /**
     * Finds the road walking path between two anchor IDs.
     */
    fun getLeg(fromAnchorId: String, toAnchorId: String, context: Context? = null): Act1RouteLeg? {
        val legs = getLegs(context)
        return legs.firstOrNull { it.fromAnchorId == fromAnchorId && it.toAnchorId == toAnchorId }
    }

    private fun parseBinaryLegs(inputStream: InputStream): List<Act1RouteLeg> {
        val bytes = inputStream.use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val magic = ByteArray(8)
        buffer.get(magic)
        if (String(magic) != "ACT1ROUT") return emptyList()

        val numLegs = buffer.short.toInt() and 0xFFFF
        val result = ArrayList<Act1RouteLeg>(numLegs)

        for (i in 0 until numLegs) {
            val fromLen = buffer.get().toInt() and 0xFF
            val fromBytes = ByteArray(fromLen)
            buffer.get(fromBytes)
            val fromId = String(fromBytes, Charsets.UTF_8)

            val toLen = buffer.get().toInt() and 0xFF
            val toBytes = ByteArray(toLen)
            buffer.get(toBytes)
            val toId = String(toBytes, Charsets.UTF_8)

            val distanceMeters = buffer.float

            val numPoints = buffer.short.toInt() and 0xFFFF
            val pts = ArrayList<Offset>(numPoints)
            for (p in 0 until numPoints) {
                val x = buffer.float
                val y = buffer.float
                pts.add(Offset(x, y))
            }

            result.add(
                Act1RouteLeg(
                    legIndex = i,
                    fromAnchorId = fromId,
                    toAnchorId = toId,
                    distanceMeters = distanceMeters,
                    waypoints = pts,
                ),
            )
        }
        return result
    }
}
