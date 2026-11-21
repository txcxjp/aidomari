package com.aidomari.app.core.server

import com.aidomari.app.domain.repository.SourceArticleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalIngestServerManager(
    private val repository: SourceArticleRepository,
    private val port: Int = LocalIngestServer.DEFAULT_PORT,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val server = LocalIngestServer(repository, port)

    private val _isServerActive = MutableStateFlow(false)
    val isServerActive: StateFlow<Boolean> = _isServerActive.asStateFlow()

    fun startServer() {
        if (!server.isRunning) {
            server.start(scope)
            _isServerActive.value = true
        }
    }

    fun stopServer() {
        if (server.isRunning) {
            server.stop()
            _isServerActive.value = false
        }
    }
}
