package com.aidomari.app.core.server

import com.aidomari.app.domain.repository.SourceArticleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

/**
 * Lightweight embedded HTTP server receiving article payloads from browser extensions.
 * Listens on 127.0.0.1 (localhost) and supports CORS preflight (OPTIONS).
 */
class LocalIngestServer(
    private val repository: SourceArticleRepository,
    private val port: Int = DEFAULT_PORT,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    val isRunning: Boolean get() = serverSocket?.isClosed == false && serverJob?.isActive == true

    fun start(scope: CoroutineScope) {
        if (isRunning) return

        serverJob = scope.launch(Dispatchers.IO) {
            try {
                val ss = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
                serverSocket = ss

                while (isActive && !ss.isClosed) {
                    try {
                        val clientSocket = ss.accept()
                        launch(Dispatchers.IO) {
                            handleClient(clientSocket)
                        }
                    } catch (_: SocketException) {
                        break
                    } catch (_: Exception) {
                        if (!isActive) break
                    }
                }
            } catch (_: Exception) {
                // Port binding error or termination
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        socket.use { s ->
            s.soTimeout = 10_000
            val inputStream = s.getInputStream()
            val outputStream = s.getOutputStream()

            // Read headers byte-by-byte until \r\n\r\n
            val headerBuffer = ByteArrayOutputStream()
            var lastFour = 0
            var b = inputStream.read()
            while (b != -1) {
                headerBuffer.write(b)
                lastFour = (lastFour shl 8) or (b and 0xFF)
                if (lastFour == 0x0D0A0D0A) { // \r\n\r\n
                    break
                }
                if (headerBuffer.size() > 64 * 1024) { // 64KB max headers guard
                    return@use
                }
                b = inputStream.read()
            }

            if (lastFour != 0x0D0A0D0A) return@use

            val headerText = headerBuffer.toString(Charsets.ISO_8859_1.name())
            val headerLines = headerText.split("\r\n")
            val requestLine = headerLines.firstOrNull() ?: return@use
            val parts = requestLine.split(" ")
            if (parts.size < 2) return@use

            val method = parts[0].uppercase()
            val path = parts[1]

            val headers = mutableMapOf<String, String>()
            for (i in 1 until headerLines.size) {
                val line = headerLines[i]
                if (line.isBlank()) continue
                val colon = line.indexOf(':')
                if (colon > 0) {
                    val k = line.substring(0, colon).trim().lowercase()
                    val v = line.substring(colon + 1).trim()
                    headers[k] = v
                }
            }

            // CORS Preflight
            if (method == "OPTIONS") {
                sendCorsPreflightResponse(outputStream)
                return@use
            }

            when {
                path == "/api/status" && method == "GET" -> {
                    val body = buildJsonObject {
                        put("status", "ok")
                        put("app", "aidomari")
                        put("version", "1.0")
                    }.toString()
                    sendJsonResponse(outputStream, statusCode = 200, statusText = "OK", body = body)
                }

                path == "/api/ingest" && method == "POST" -> {
                    val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                    if (contentLength <= 0 || contentLength > MAX_PAYLOAD_BYTES) {
                        val errorBody = buildJsonObject {
                            put("status", "error")
                            put("message", "Invalid Content-Length")
                        }.toString()
                        sendJsonResponse(outputStream, statusCode = 400, statusText = "Bad Request", body = errorBody)
                        return@use
                    }

                    // Read exact content bytes
                    val bodyBytes = ByteArray(contentLength)
                    var totalRead = 0
                    while (totalRead < contentLength) {
                        val read = inputStream.read(bodyBytes, totalRead, contentLength - totalRead)
                        if (read == -1) break
                        totalRead += read
                    }

                    if (totalRead < contentLength) {
                        val errorBody = buildJsonObject {
                            put("status", "error")
                            put("message", "Incomplete body read")
                        }.toString()
                        sendJsonResponse(outputStream, statusCode = 400, statusText = "Bad Request", body = errorBody)
                        return@use
                    }

                    val requestBody = String(bodyBytes, Charsets.UTF_8)
                    try {
                        val root = json.parseToJsonElement(requestBody).jsonObject
                        val url = root["url"]?.jsonPrimitive?.content ?: ""
                        val title = root["title"]?.jsonPrimitive?.content ?: ""
                        val content = root["content"]?.jsonPrimitive?.content ?: ""

                        if (url.isBlank() || content.isBlank()) {
                            val errorBody = buildJsonObject {
                                put("status", "error")
                                put("message", "url and content are required")
                            }.toString()
                            sendJsonResponse(outputStream, statusCode = 400, statusText = "Bad Request", body = errorBody)
                            return@use
                        }

                        val ingestResult = repository.ingestArticle(url = url, title = title, content = content)
                        if (ingestResult.isSuccess) {
                            val article = ingestResult.getOrThrow()
                            val successBody = buildJsonObject {
                                put("status", "success")
                                put("articleId", article.id)
                                put("title", article.title)
                            }.toString()
                            sendJsonResponse(outputStream, statusCode = 200, statusText = "OK", body = successBody)
                        } else {
                            val errorBody = buildJsonObject {
                                put("status", "error")
                                put("message", ingestResult.exceptionOrNull()?.message ?: "Failed to ingest")
                            }.toString()
                            sendJsonResponse(outputStream, statusCode = 500, statusText = "Internal Server Error", body = errorBody)
                        }
                    } catch (e: Exception) {
                        val errorBody = buildJsonObject {
                            put("status", "error")
                            put("message", "Invalid JSON payload: ${e.message}")
                        }.toString()
                        sendJsonResponse(outputStream, statusCode = 400, statusText = "Bad Request", body = errorBody)
                    }
                }

                else -> {
                    val notFoundBody = buildJsonObject {
                        put("status", "error")
                        put("message", "Not Found")
                    }.toString()
                    sendJsonResponse(outputStream, statusCode = 404, statusText = "Not Found", body = notFoundBody)
                }
            }
        }
    }

    private fun sendCorsPreflightResponse(outputStream: OutputStream) {
        val response = (
            "HTTP/1.1 204 No Content\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With\r\n" +
                "Access-Control-Max-Age: 86400\r\n" +
                "Content-Length: 0\r\n\r\n"
            ).toByteArray(Charsets.US_ASCII)
        outputStream.write(response)
        outputStream.flush()
    }

    private fun sendJsonResponse(
        outputStream: OutputStream,
        statusCode: Int,
        statusText: String,
        body: String,
    ) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $statusCode $statusText\r\n" +
            "Content-Type: application/json; charset=UTF-8\r\n" +
            "Content-Length: ${bytes.size}\r\n" +
            "Access-Control-Allow-Origin: *\r\n" +
            "Connection: close\r\n\r\n"
        outputStream.write(header.toByteArray(Charsets.US_ASCII))
        outputStream.write(bytes)
        outputStream.flush()
    }

    companion object {
        const val DEFAULT_PORT = 8877
        private const val MAX_PAYLOAD_BYTES = 10 * 1024 * 1024 // 10MB
    }
}
