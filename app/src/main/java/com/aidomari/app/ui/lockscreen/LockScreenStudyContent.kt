package com.aidomari.app.ui.lockscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.ui.preview.PreviewSampleData
import com.aidomari.app.ui.study.components.CardFaceBack
import com.aidomari.app.ui.study.components.CardFaceFront
import com.aidomari.app.ui.study.components.FlippableCard
import com.aidomari.app.ui.theme.AidomariTheme

@Composable
fun LockScreenStudyContent(
    uiState: LockScreenUiState,
    onFlip: () -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    onSpeak: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Upward swipe threshold (dragAmount < -30px)
                    if (dragAmount < -30f) {
                        onDismiss()
                    }
                }
            }
            .testTag("LockScreenStudyRoot"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "相泊 ロック画面学習",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                    Text(
                        text = "1日1問 習慣化",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f),
                        ),
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("LockScreenDismissButton"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "閉じる",
                        tint = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Card Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(color = Color.White)
                    }

                    uiState.card != null -> {
                        val card = uiState.card
                        FlippableCard(
                            isFlipped = uiState.isFlipped,
                            onFlip = onFlip,
                            onDone = onDone,
                            onRetry = onRetry,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("LockScreenFlippableCard"),
                            frontContent = {
                                CardFaceFront(
                                    card = card,
                                    onSpeak = { onSpeak(card.front) },
                                )
                            },
                            backContent = {
                                CardFaceBack(
                                    card = card,
                                    onDone = onDone,
                                    onRetry = onRetry,
                                    onSpeak = { onSpeak(card.back) },
                                )
                            },
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp),
                        ) {
                            Text(
                                text = "出題できる単語がありません",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "アプリで単語を追加するか、出題設定を確認してください。",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                ),
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(text = "画面を閉じる")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Swipe Guide
            AnimatedVisibility(
                visible = uiState.card != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "上へスワイプでスキップしてスマホを使う",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Preview(name = "Lock Screen Study Preview", showBackground = true)
@Composable
private fun LockScreenStudyContentPreview() {
    AidomariTheme {
        LockScreenStudyContent(
            uiState = LockScreenUiState(
                isLoading = false,
                card = PreviewSampleData.sampleCards.first(),
                isFlipped = false,
            ),
            onFlip = {},
            onDone = {},
            onRetry = {},
            onSpeak = {},
            onDismiss = {},
        )
    }
}
