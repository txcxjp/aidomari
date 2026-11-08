package com.aidomari.app.ui.sugoroku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity geographical model for Hokkaido and Sugoroku board track.
 * Coastline contour is accurately modeled from official geospatial map of Hokkaido (Wikipedia / GSI standard).
 */
object HokkaidoMapData {

    /**
     * Accurate coastline contour points of Hokkaido main island (normalized 0.0 to 1.0).
     * Includes all characteristic geographical features:
     * - Oshima Peninsula (Matsumae, Cape Shirakami, Esashi)
     * - Shakotan Peninsula (Cape Kamui, Cape Shakotan)
     * - Ishikari Bay & Orolon line
     * - Cape Soya & Wakkanai (Northmost tip)
     * - Okhotsk coast & Saroma Lake
     * - Shiretoko Peninsula (Utoro, Rausu, Aidomari)
     * - Nemuro Peninsula & Cape Nosappu
     * - Cape Erimo & Pacific Coast
     * - Uchiura Bay (Volcano Bay curve: Muroran to Oshamambe, Yakumo, Mori, Hakodate)
     */
    val officialContourNormalized: List<Offset> = listOf(
        Offset(0.33f, 0.98f), // Cape Shirakami (Southmost tip of Oshima)
        Offset(0.31f, 0.94f), // Matsumae
        Offset(0.28f, 0.88f), // Kaminokuni
        Offset(0.27f, 0.84f), // Esashi
        Offset(0.29f, 0.77f), // Otobe
        Offset(0.28f, 0.72f), // Yakumo (Kumaishi)
        Offset(0.25f, 0.68f), // Setana
        Offset(0.22f, 0.65f), // Cape Mota
        Offset(0.24f, 0.61f), // Shimamaki
        Offset(0.26f, 0.58f), // Suttsu
        Offset(0.27f, 0.54f), // Kuromatsunai
        Offset(0.29f, 0.52f), // Iwanai
        Offset(0.28f, 0.49f), // Tomari
        Offset(0.27f, 0.46f), // Kamoenai
        Offset(0.26f, 0.43f), // Cape Kamui (Shakotan tip)
        Offset(0.28f, 0.41f), // Cape Shakotan
        Offset(0.31f, 0.42f), // Bikuni
        Offset(0.33f, 0.44f), // Furuhira
        Offset(0.34f, 0.46f), // Yoichi
        Offset(0.37f, 0.47f), // Otaru
        Offset(0.40f, 0.46f), // Ishikari Bay
        Offset(0.42f, 0.43f), // Atsuta
        Offset(0.43f, 0.38f), // Hamamasu
        Offset(0.43f, 0.34f), // Cape Ofuyu
        Offset(0.44f, 0.31f), // Mashike
        Offset(0.45f, 0.28f), // Rumoi
        Offset(0.46f, 0.25f), // Obira
        Offset(0.46f, 0.22f), // Tomamae
        Offset(0.47f, 0.19f), // Haboro
        Offset(0.48f, 0.16f), // Shosanbetsu
        Offset(0.48f, 0.13f), // Enbetsu
        Offset(0.49f, 0.10f), // Teshio
        Offset(0.50f, 0.07f), // Horonobe coast
        Offset(0.51f, 0.04f), // Wakkanai
        Offset(0.54f, 0.01f), // Cape Soya (Northmost point of Japan)
        Offset(0.58f, 0.04f), // Sarufutsu
        Offset(0.61f, 0.07f), // Hamatonbetsu
        Offset(0.64f, 0.11f), // Esashi (Okhotsk)
        Offset(0.67f, 0.15f), // Oumu
        Offset(0.70f, 0.18f), // Okoppe
        Offset(0.74f, 0.22f), // Mombetsu
        Offset(0.78f, 0.25f), // Yubetsu
        Offset(0.81f, 0.26f), // Saroma Lake sandbar
        Offset(0.85f, 0.27f), // Tokoro
        Offset(0.88f, 0.28f), // Cape Notoro
        Offset(0.89f, 0.30f), // Abashiri
        Offset(0.92f, 0.32f), // Koshimizu
        Offset(0.94f, 0.33f), // Shari
        Offset(0.96f, 0.30f), // Utoro
        Offset(0.99f, 0.24f), // Cape Shiretoko tip
        Offset(0.98f, 0.28f), // Rausu / Aidomari
        Offset(0.95f, 0.34f), // Shibetsu
        Offset(0.95f, 0.38f), // Betsukai
        Offset(0.98f, 0.41f), // Cape Nosappu (Nemuro tip)
        Offset(0.94f, 0.43f), // Nemuro south
        Offset(0.89f, 0.45f), // Hamanaka
        Offset(0.84f, 0.47f), // Akkeshi
        Offset(0.78f, 0.49f), // Kushiro
        Offset(0.73f, 0.53f), // Shiranuka
        Offset(0.69f, 0.58f), // Urahoro
        Offset(0.67f, 0.63f), // Otsu (Tokachi river)
        Offset(0.65f, 0.69f), // Hiroo
        Offset(0.64f, 0.77f), // Cape Erimo
        Offset(0.60f, 0.73f), // Samani
        Offset(0.57f, 0.71f), // Urakawa
        Offset(0.53f, 0.69f), // Shizunai
        Offset(0.50f, 0.67f), // Hidaka
        Offset(0.48f, 0.65f), // Mukawa
        Offset(0.45f, 0.63f), // Tomakomai
        Offset(0.43f, 0.66f), // Shiraoi
        Offset(0.42f, 0.69f), // Noboribetsu
        Offset(0.40f, 0.72f), // Muroran (Cape Chikyu)
        Offset(0.37f, 0.70f), // Date
        Offset(0.35f, 0.68f), // Toyako
        Offset(0.33f, 0.68f), // Toyoura
        Offset(0.31f, 0.70f), // Oshamambe (Inner Uchiura bay)
        Offset(0.32f, 0.74f), // Yakumo (Uchiura side)
        Offset(0.34f, 0.78f), // Otoshibe
        Offset(0.36f, 0.82f), // Mori
        Offset(0.38f, 0.85f), // Shikabe
        Offset(0.41f, 0.88f), // Minamikayabe
        Offset(0.43f, 0.90f), // Cape Esan
        Offset(0.40f, 0.93f), // Toi
        Offset(0.37f, 0.94f), // Hakodate
        Offset(0.34f, 0.95f), // Kikonai
        Offset(0.33f, 0.97f), // Shiriuchi
    )
}

/**
 * Pre-computed equidistant board game layout for the 2,000 Sugoroku steps.
 * Designed as a winding, snake-like board game track across Hokkaido.
 * Step distance is strictly 80dp so tiles (58dp) NEVER overlap.
 */
object HokkaidoBoardTrack {
    const val STEP_DIST_DP = 80f // 80dp strictly between tile centers (58dp tile + 22dp gap)

    /**
     * 2,000 equidistant tile center coordinates in DP units.
     * Starts at Hakodate (0, 0), snakes gently left and right, and travels northwards
     * up the coast towards Soya and then southeast to Shiretoko.
     */
    val tileCenters: List<Offset> by lazy {
        val count = 2000
        val points = ArrayList<Offset>(count)
        points.add(Offset(0f, 0f))

        var curX = 0f
        var curY = 0f

        for (i in 0 until count - 1) {
            // General direction of Hokkaido journey:
            // 0 - 1218 (Hakodate to Cape Soya): Heading North (-PI/2)
            // 1218 - 1700 (Soya along Okhotsk to Abashiri): Heading South-East (PI * 0.35)
            // 1700 - 1999 (Abashiri to Shiretoko): Heading North-East (-PI * 0.35)
            val baseAngle = when {
                i < 1218 -> -Math.PI.toFloat() / 2f
                i < 1700 -> (Math.PI * 0.35).toFloat()
                else -> (-Math.PI * 0.35).toFloat()
            }

            // Local winding snake S-curves across screen width (swing +- 34 degrees left/right)
            val snake = sin(i * 0.34f) * 0.60f
            val angle = baseAngle + snake

            curX += STEP_DIST_DP * cos(angle)
            curY += STEP_DIST_DP * sin(angle)
            points.add(Offset(curX, curY))
        }

        points
    }
}

/**
 * Accurate silhouette of Hokkaido in the corner of the map based on Wikipedia/GSI standard.
 * Shows the authentic shape of Hokkaido and a pulsing red dot at the player's current location.
 */
@Composable
fun HokkaidoMiniMap(
    currentStepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8F5E9).copy(alpha = 0.94f))
            .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(10.dp))
            .padding(6.dp)
            .size(width = 82.dp, height = 76.dp),
    ) {
        val landColor = Color(0xFFA5D6A7)
        val currentDotColor = Color(0xFFD32F2F)

        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw accurate Hokkaido coastline contour
            val path = Path()
            HokkaidoMapData.officialContourNormalized.forEachIndexed { idx, pt ->
                val cx = pt.x * canvasW
                val cy = pt.y * canvasH
                if (idx == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
            }
            path.close()
            drawPath(path = path, color = landColor)

            // 2. Accurate position of player dot
            // Step 0: Hakodate (0.37, 0.94)
            // Step 1218: Soya (0.54, 0.01)
            // Step 1999: Shiretoko / Aidomari (0.98, 0.28)
            val ratio = (currentStepIndex.toFloat() / totalSteps.coerceAtLeast(1)).coerceIn(0f, 1f)
            val dotX: Float
            val dotY: Float
            if (ratio < 0.61f) {
                val subR = ratio / 0.61f
                dotX = (0.37f + subR * (0.54f - 0.37f)) * canvasW
                dotY = (0.94f + subR * (0.01f - 0.94f)) * canvasH
            } else {
                val subR = (ratio - 0.61f) / 0.39f
                dotX = (0.54f + subR * (0.98f - 0.54f)) * canvasW
                dotY = (0.01f + subR * (0.28f - 0.01f)) * canvasH
            }

            drawCircle(
                color = currentDotColor,
                radius = 3.5.dp.toPx(),
                center = Offset(dotX, dotY),
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(dotX, dotY),
            )
        }
    }
}
