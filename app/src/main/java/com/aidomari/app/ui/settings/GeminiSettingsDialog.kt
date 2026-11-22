package com.aidomari.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidomari.app.core.ai.GeminiConfig
import kotlinx.coroutines.launch

sealed interface ConnectionTestState {
    data object Idle : ConnectionTestState
    data object Testing : ConnectionTestState
    data object Success : ConnectionTestState
    data class Error(val message: String) : ConnectionTestState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSettingsDialog(
    config: GeminiConfig,
    onConfigChanged: (GeminiConfig) -> Unit,
    onTestConnection: suspend (apiKey: String, model: String) -> Result<Boolean>,
    onDismissRequest: () -> Unit,
    onFetchModels: (suspend (apiKey: String) -> Result<List<String>>)? = null,
    modifier: Modifier = Modifier,
) {
    var apiKey by remember(config.apiKey) { mutableStateOf(config.apiKey) }
    var selectedModel by remember(config.modelName) { mutableStateOf(config.modelName) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var testState by remember { mutableStateOf<ConnectionTestState>(ConnectionTestState.Idle) }
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    var availableModels by remember { mutableStateOf(GeminiConfig.AVAILABLE_MODELS) }
    var isFetchingModels by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(apiKey) {
        if (apiKey.isNotBlank() && onFetchModels != null) {
            isFetchingModels = true
            val result = onFetchModels(apiKey.trim())
            if (result.isSuccess) {
                val fetched = result.getOrDefault(emptyList())
                if (fetched.isNotEmpty()) {
                    availableModels = fetched
                }
            }
            isFetchingModels = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("GeminiSettingsDialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        },
        title = {
            Text(
                text = "AIデッキ化設定 (Gemini)",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Google AI Studio で取得したAPIキーを入力すると、Webブラウザで閲覧したページから自動で重要知識を暗記カード化できます。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // API Key Input
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        testState = ConnectionTestState.Idle
                    },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "キーを隠す" else "キーを表示",
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("GeminiApiKeyInput"),
                )

                // Model Selector (Dropdown Only, Read-Only)
                ExposedDropdownMenuBox(
                    expanded = isModelMenuExpanded,
                    onExpandedChange = { isModelMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("使用モデル") },
                        trailingIcon = {
                            if (isFetchingModels) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelMenuExpanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("GeminiModelSelector"),
                    )
                    ExposedDropdownMenu(
                        expanded = isModelMenuExpanded,
                        onDismissRequest = { isModelMenuExpanded = false },
                    ) {
                        availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = {
                                    val note = if (model == GeminiConfig.DEFAULT_MODEL) " (推奨・高速)" else ""
                                    Text("$model$note")
                                },
                                onClick = {
                                    selectedModel = model
                                    isModelMenuExpanded = false
                                    testState = ConnectionTestState.Idle
                                },
                            )
                        }
                    }
                }

                // Test Connection Button & Status Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedButton(
                        onClick = {
                            if (apiKey.isBlank()) {
                                testState = ConnectionTestState.Error("APIキーを入力してください")
                                return@OutlinedButton
                            }
                            testState = ConnectionTestState.Testing
                            coroutineScope.launch {
                                val result = onTestConnection(apiKey.trim(), selectedModel)
                                testState = if (result.isSuccess) {
                                    if (onFetchModels != null) {
                                        val modelsResult = onFetchModels(apiKey.trim())
                                        val fetched = modelsResult.getOrDefault(emptyList())
                                        if (fetched.isNotEmpty()) {
                                            availableModels = fetched
                                        }
                                    }
                                    ConnectionTestState.Success
                                } else {
                                    val error = result.exceptionOrNull()?.message ?: "接続に失敗しました"
                                    ConnectionTestState.Error(error)
                                }
                            }
                        },
                        enabled = apiKey.isNotBlank() && testState != ConnectionTestState.Testing,
                        modifier = Modifier.testTag("GeminiTestConnectionButton"),
                    ) {
                        if (testState == ConnectionTestState.Testing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("接続テスト")
                    }

                    // Test Status Badge
                    when (val state = testState) {
                        ConnectionTestState.Testing -> {
                            Text(
                                text = "検証中...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        ConnectionTestState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "接続成功",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        is ConnectionTestState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "接続失敗",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        ConnectionTestState.Idle -> {
                            // No status
                        }
                    }
                }

                // Error message detail box
                if (testState is ConnectionTestState.Error) {
                    val errMsg = (testState as ConnectionTestState.Error).message
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = errMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }

                // Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(8.dp),
                ) {
                    Text(
                        text = "※ Google AI Studioの個人用無料枠（1日1,500回）の範囲内で完全無料で利用可能です。入力されたAPIキーは端末内にのみ保存されます。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfigChanged(
                        config.copy(
                            apiKey = apiKey.trim(),
                            modelName = selectedModel.trim().ifBlank { GeminiConfig.DEFAULT_MODEL },
                        ),
                    )
                    onDismissRequest()
                },
                modifier = Modifier.testTag("GeminiSaveButton"),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("GeminiCancelButton"),
            ) {
                Text("キャンセル")
            }
        },
    )
}
