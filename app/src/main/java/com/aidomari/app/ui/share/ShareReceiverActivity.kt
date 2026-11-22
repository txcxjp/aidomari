package com.aidomari.app.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aidomari.app.AidomariApp
import com.aidomari.app.MainActivity
import com.aidomari.app.core.privacy.PrivacyFilter
import com.aidomari.app.core.share.DefaultShareArticleFetcher
import com.aidomari.app.core.share.ParsedShareContent
import com.aidomari.app.core.share.ShareArticleFetcher
import com.aidomari.app.core.share.ShareIntentParser
import com.aidomari.app.domain.repository.SourceArticleRepository
import com.aidomari.app.ui.theme.AidomariTheme
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {

    internal var articleFetcher: ShareArticleFetcher = DefaultShareArticleFetcher()
    internal var repositoryOverride: SourceArticleRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
        val extraSubject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)
        val parsed = ShareIntentParser.parse(extraText, extraSubject)

        val repository = repositoryOverride ?: (application as AidomariApp).sourceArticleRepository

        setContent {
            AidomariTheme {
                ShareReceiverScreen(
                    parsed = parsed,
                    fetcher = articleFetcher,
                    onSave = { url, title, content, navigateToDigest ->
                        lifecycleScopeLaunchSave(repository, url, title, content, navigateToDigest)
                    },
                    onDismiss = { finish() },
                )
            }
        }
    }

    private fun lifecycleScopeLaunchSave(
        repository: SourceArticleRepository,
        url: String,
        title: String,
        content: String,
        navigateToDigest: Boolean,
    ) {
        val app = application as? AidomariApp
        val scope = app?.let { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main) }
            ?: kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)

        scope.launch {
            val result = repository.ingestArticle(url, title, content)
            if (result.isSuccess) {
                Toast.makeText(this@ShareReceiverActivity, "愛泊に保存しました", Toast.LENGTH_SHORT).show()
                if (navigateToDigest) {
                    val mainIntent = Intent(this@ShareReceiverActivity, MainActivity::class.java).apply {
                        putExtra("navigate_to", "browsing_digest")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(mainIntent)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "保存に失敗しました"
                Toast.makeText(this@ShareReceiverActivity, error, Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }
}

@Composable
fun ShareReceiverScreen(
    parsed: ParsedShareContent,
    fetcher: ShareArticleFetcher,
    onSave: (url: String, title: String, content: String, navigateToDigest: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(parsed.title) }
    var content by remember { mutableStateOf(parsed.content) }
    var isLoading by remember { mutableStateOf(parsed.isUrlOnly) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val targetUrl = parsed.url ?: "text://share/${System.currentTimeMillis()}"
    val domain = if (parsed.url != null) {
        parsed.url.substringAfter("://").substringBefore("/")
    } else {
        "text.share"
    }
    val isExcluded = remember(targetUrl, domain) {
        parsed.url != null && PrivacyFilter.isExcluded(targetUrl, domain)
    }

    LaunchedEffect(parsed) {
        if (parsed.isUrlOnly && parsed.url != null) {
            isLoading = true
            val result = fetcher.fetchArticle(parsed.url)
            if (result.isSuccess) {
                val fetched = result.getOrNull()
                if (fetched != null) {
                    if (!fetched.title.isNullOrBlank() && (title.isBlank() || title == "共有記事" || title == domain)) {
                        title = fetched.title
                    }
                    content = fetched.content
                }
            } else {
                fetchError = result.exceptionOrNull()?.message
                if (content.isBlank()) {
                    content = "(URL共有: ${parsed.url})"
                }
            }
            isLoading = false
        }
    }

    // Semi-transparent background scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* stop propagation */ },
                )
                .testTag("ShareReceiverCard"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "愛泊に保存",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("ShareCancelButton"),
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる")
                    }
                }

                // Privacy Warning if excluded
                if (isExcluded) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "プライバシー保護のため、除外対象のドメイン/ページです（保存されません）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                // Domain / Source Info
                if (parsed.url != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("記事タイトル") },
                    singleLine = true,
                    enabled = !isLoading && !isExcluded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ShareTitleInput"),
                )

                // Content Preview / Loading State
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "記事本文を取得中...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "本文プレビュー (${content.length} 文字)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = content.take(300) + if (content.length > 300) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .heightIn(max = 100.dp)
                                    .verticalScroll(rememberScrollState()),
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            if (content.isNotBlank() && !isExcluded) {
                                onSave(targetUrl, title.trim().ifBlank { "共有記事" }, content.trim(), false)
                            }
                        },
                        enabled = !isLoading && !isExcluded && content.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ShareSaveButton"),
                    ) {
                        Text("保存（後で）")
                    }

                    Button(
                        onClick = {
                            if (content.isNotBlank() && !isExcluded) {
                                onSave(targetUrl, title.trim().ifBlank { "共有記事" }, content.trim(), true)
                            }
                        },
                        enabled = !isLoading && !isExcluded && content.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ShareDeckifyButton"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("今すぐデッキ化")
                    }
                }
            }
        }
    }
}
