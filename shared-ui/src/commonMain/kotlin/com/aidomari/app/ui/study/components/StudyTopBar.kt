package com.aidomari.app.ui.study.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.aidomari.app.ui.theme.AidomariTheme
import com.aidomari.app.ui.theme.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTopBar(
    title: String,
    subtitle: String,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
    onEditNotes: (() -> Unit)? = null,
    autoSpeakEnabled: Boolean = false,
    onOpenTtsSettings: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onDeleteCard: (() -> Unit)? = null,
    onSnoozeCard: (() -> Unit)? = null,
    snoozeDurationLabel: String? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            if (onSearchClick != null) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Words",
                    )
                }
            }

            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isBookmarked) {
                        Icons.Default.Star
                    } else {
                        Icons.Outlined.StarBorder
                    },
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) {
                        Color(0xFFFFB300)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    if (onOpenTtsSettings != null) {
                        DropdownMenuItem(
                            text = { Text("音声読み上げ設定") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = if (autoSpeakEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            },
                            onClick = {
                                showMenu = false
                                onOpenTtsSettings()
                            },
                        )
                    }

                    if (onEditNotes != null) {
                        DropdownMenuItem(
                            text = { Text("メモ・疑問点を編集") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEditNotes()
                            },
                        )
                    }

                    if (onSnoozeCard != null && snoozeDurationLabel != null) {
                        DropdownMenuItem(
                            text = { Text("しばらく表示しない ($snoozeDurationLabel)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onSnoozeCard()
                            },
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("最初からやり直す") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRestart()
                        },
                    )

                    if (onDeleteCard != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "カードを削除",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteCard()
                            },
                        )
                    }
                }
            }
        },
    )
}

@ThemePreviews
@Composable
private fun StudyTopBarPreview() {
    AidomariTheme {
        StudyTopBar(
            title = "日常英会話・基本単語",
            subtitle = "Part 1",
            isBookmarked = true,
            onBack = {},
            onToggleBookmark = {},
            onRestart = {},
            autoSpeakEnabled = true,
            onOpenTtsSettings = {},
            onSearchClick = {},
            onDeleteCard = {},
            onEditNotes = {},
        )
    }
}
