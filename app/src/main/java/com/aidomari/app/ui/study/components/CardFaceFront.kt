package com.aidomari.app.ui.study.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews
import com.aidomari.app.domain.model.Card as DomainCard

@Composable
fun CardFaceFront(
    card: DomainCard,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
    isReverse: Boolean = false,
    onEditClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Bookmarked indicator icon at top start if bookmarked
        if (card.isBookmarked) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Bookmarked",
                tint = Color(0xFFFFB300),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
            )
        }

        // Speaker icon at top end to replay front face audio
        IconButton(
            onClick = onSpeak,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Speak",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        // Center content: prompt word/meaning and question
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (isReverse) card.back else card.front,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isReverse) 28.sp else 32.sp,
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (card.userQuestion.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = if (onEditClick != null) Modifier.clickable { onEditClick() } else Modifier,
                ) {
                    Text(
                        text = card.userQuestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Bottom hint
        Text(
            text = "左右スワイプまたは音量キーでめくる",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun CardFaceFrontPreview() {
    AidomariTheme {
        CardFaceFront(
            card = PreviewSampleData.sampleCard,
            onSpeak = {},
            isReverse = false,
            onEditClick = {},
        )
    }
}
