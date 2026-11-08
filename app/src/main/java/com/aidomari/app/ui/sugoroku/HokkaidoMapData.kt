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

/**
 * Data model for fun illustrations placed across the Hokkaido map.
 */
data class MapIllustration(
    val id: String,
    val emoji: String,
    val label: String,
    val x: Float,
    val y: Float,
    val minVisibleStep: Int,
    val maxVisibleStep: Int,
)

object HokkaidoMapData {

    /**
     * Polygonal contour points representing Hokkaido island (North-up coordinates).
     * X range: ~380 to 1850, Y range: ~80 to 1920.
     * Route runs along this coastline from Hakodate (south) up to Soya (north) and Shiretoko (east).
     */
    val coastlinePoints: List<Offset> = listOf(
        // Matsumae / Shirakami Cape (Southmost tip of Oshima peninsula)
        Offset(380f, 1920f),
        Offset(420f, 1750f), // Esashi
        Offset(430f, 1500f), // Setana
        Offset(400f, 1370f), // Cape Mota
        Offset(420f, 1330f), // Suttsu
        Offset(410f, 1180f), // Cape Kamui (Shakotan peninsula)
        Offset(440f, 1140f), // Cape Shakotan
        Offset(530f, 1160f), // Yoichi
        Offset(600f, 1142f), // Otaru
        Offset(640f, 1130f), // Ishikari
        // Orolon Line (Sea of Japan coast)
        Offset(650f, 850f), // Mashike
        Offset(660f, 758f), // Rumoi
        Offset(640f, 520f), // Haboro
        Offset(605f, 303f), // Teshio
        Offset(575f, 130f), // Wakkanai / Cape Noshappu
        Offset(643.8f, 80f), // Cape Soya (Northmost point of Japan)
        // Okhotsk coast
        Offset(730f, 180f), // Sarufutsu
        Offset(780f, 240f), // Hamatonbetsu
        Offset(840f, 330f), // Esashi
        Offset(920f, 420f), // Oumu
        Offset(980f, 480f), // Okoppe
        Offset(1095f, 550f), // Mombetsu
        Offset(1240f, 610f), // Saroma Lake
        Offset(1340f, 640f), // Cape Notoro
        Offset(1370f, 680f), // Abashiri
        Offset(1440f, 720f), // Shari
        // Shiretoko Peninsula
        Offset(1530f, 670f), // Utoro
        Offset(1600f, 630f), // Shiretoko Pass
        Offset(1760f, 440f), // Cape Shiretoko tip
        Offset(1720f, 570f), // Aidomari
        Offset(1660f, 660f), // Rausu
        Offset(1640f, 730f), // Shibetsu
        // Nemuro Peninsula
        Offset(1850f, 880f), // Cape Nosappu
        // Pacific Coast
        Offset(1700f, 940f), // Hamanaka
        Offset(1550f, 990f), // Kushiro
        Offset(1420f, 1060f), // Tokachi
        Offset(1300f, 1160f), // Hiroo
        Offset(1280f, 1400f), // Cape Erimo
        Offset(1150f, 1330f), // Urakawa
        Offset(1050f, 1290f), // Shizunai
        Offset(880f, 1250f), // Tomakomai
        Offset(780f, 1390f), // Noboribetsu
        Offset(750f, 1460f), // Muroran
        Offset(660f, 1430f), // Date
        Offset(545f, 1420f), // Oshamambe (Inner Uchiura Bay curve)
        Offset(575f, 1550f), // Yakumo
        Offset(615f, 1680f), // Mori
        Offset(685f, 1820f), // Cape Esan
        Offset(550f, 1860f), // Hakodate
        Offset(480f, 1890f), // Kikonai
    )

    /**
     * Illustrations placed at key locations on Hokkaido.
     * Revealed progressively as player travels through Act 1.
     */
    val illustrations: List<MapIllustration> = listOf(
        // Hakodate & South
        MapIllustration("goryokaku", "⭐", "五稜郭", 562f, 1835f, 0, 150),
        MapIllustration("hakodate_mountain", "⛰️", "函館山", 530f, 1865f, 0, 100),
        MapIllustration("squid_boat", "🦑", "イカ釣り船", 575f, 1875f, 0, 80),
        MapIllustration("seagull_south", "🕊️", "カモメ", 525f, 1820f, 0, 120),
        // Oshima & Volcano Bay
        MapIllustration("komagatake", "🌋", "駒ヶ岳", 605f, 1710f, 10, 220),
        MapIllustration("cows_yakumo", "🐄", "八雲牧場", 545f, 1560f, 50, 300),
        MapIllustration("scallop_bay", "🌊", "噴火湾の波", 610f, 1590f, 40, 280),
        MapIllustration("crab_oshamambe", "🦀", "かにめし", 525f, 1420f, 100, 360),
        // Japan Sea Coast
        MapIllustration("windmills_suttsu", "🌬️", "寿都風車", 400f, 1335f, 160, 420),
        MapIllustration("dolphin_shakotan", "🐬", "積丹ブルー", 375f, 1220f, 220, 500),
        MapIllustration("lighthouse_kamui", "🗼", "神威岬灯台", 395f, 1165f, 240, 520),
        MapIllustration("gaslamp_otaru", "🏮", "小樽ガス灯", 618f, 1150f, 350, 600),
        // Orolon Line
        MapIllustration("hamanasu_ishikari", "🌸", "ハマナス", 665f, 1115f, 400, 680),
        MapIllustration("shrimp_mashike", "🦐", "甘エビ", 625f, 840f, 500, 780),
        MapIllustration("orolon_bird", "🐧", "オロロン鳥", 610f, 530f, 650, 950),
        MapIllustration("sarobetsu_flower", "🌼", "サロベツ原野", 635f, 340f, 850, 1100),
        MapIllustration("rishiri_fuji", "🗻", "利尻富士", 500f, 230f, 900, 1150),
        MapIllustration("reindeer_horonobe", "🦌", "トナカイ", 625f, 220f, 950, 1200),
        // North & Okhotsk
        MapIllustration("monument_soya", "🚩", "日本最北端", 643f, 60f, 1150, 1300),
        MapIllustration("soya_cow", "🐂", "宗谷黒牛", 680f, 95f, 1180, 1350),
        MapIllustration("scallop_sarufutsu", "🐚", "猿払ホタテ", 755f, 170f, 1220, 1400),
        MapIllustration("crab_mombetsu", "🦀", "紋別カニの爪", 1115f, 535f, 1400, 1650),
        MapIllustration("seal_abashiri", "🦭", "アザラシ", 1355f, 630f, 1550, 1800),
        // Shiretoko
        MapIllustration("killer_whale", "🐋", "知床のクジラ", 1700f, 510f, 1750, 1999),
        MapIllustration("brown_bear", "🐻", "ヒグマ", 1585f, 630f, 1800, 1999),
    )
}

/**
 * Detachable small silhouette of Hokkaido placed in the corner of the map.
 * Shows only the contour of Hokkaido and a pulsing red dot at the player's current location.
 * Does NOT reveal route path, goal, or distant destinations.
 */
@Composable
fun HokkaidoMiniMap(
    playerWorldX: Float,
    playerWorldY: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8F5E9).copy(alpha = 0.92f))
            .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(10.dp))
            .padding(6.dp)
            .size(width = 72.dp, height = 86.dp),
    ) {
        val landColor = Color(0xFFA5D6A7)
        val currentDotColor = Color(0xFFD32F2F)

        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Coordinate mapping:
            // World X: 350 to 1850 -> Canvas X
            // World Y: 60 to 1950  -> Canvas Y
            val minX = 350f
            val maxX = 1880f
            val minY = 60f
            val maxY = 1950f

            fun toCanvasX(wx: Float): Float = ((wx - minX) / (maxX - minX)) * canvasW
            fun toCanvasY(wy: Float): Float = ((wy - minY) / (maxY - minY)) * canvasH

            // 1. Draw Hokkaido contour polygon
            val path = Path()
            HokkaidoMapData.coastlinePoints.forEachIndexed { index, point ->
                val cx = toCanvasX(point.x)
                val cy = toCanvasY(point.y)
                if (index == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
            }
            path.close()
            drawPath(path = path, color = landColor)

            // 2. Draw current location pulsing red dot
            val playerCx = toCanvasX(playerWorldX)
            val playerCy = toCanvasY(playerWorldY)

            drawCircle(
                color = currentDotColor,
                radius = 3.5.dp.toPx(),
                center = Offset(playerCx, playerCy),
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(playerCx, playerCy),
            )
        }
    }
}
