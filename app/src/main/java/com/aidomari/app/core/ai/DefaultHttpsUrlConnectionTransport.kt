package com.aidomari.app.core.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DefaultHttpsUrlConnectionTransport(
    private val connectTimeoutMs: Int = 15_000,
    private val readTimeoutMs: Int = 45_000,
) : HttpTransport {

    override suspend fun post(
        url: String,
        jsonBody: String,
        headers: Map<String, String>,
    ): HttpResponse = withContext(Dispatchers.IO) {
        val targetUrl = URL(url)
        val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }

        try {
            conn.outputStream.use { os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
                os.flush()
            }

            val statusCode = conn.responseCode
            val isSuccess = statusCode in 200..299
            val stream = if (isSuccess) conn.inputStream else (conn.errorStream ?: conn.inputStream)

            val body = stream?.use { s ->
                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { br ->
                    br.readText()
                }
            } ?: ""

            HttpResponse(statusCode = statusCode, body = body)
        } finally {
            conn.disconnect()
        }
    }

    override suspend fun get(
        url: String,
        headers: Map<String, String>,
    ): HttpResponse = withContext(Dispatchers.IO) {
        val targetUrl = URL(url)
        val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            instanceFollowRedirects = true
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }

        try {
            val statusCode = conn.responseCode
            val isSuccess = statusCode in 200..299
            val stream = if (isSuccess) conn.inputStream else (conn.errorStream ?: conn.inputStream)

            val body = stream?.use { s ->
                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { br ->
                    br.readText()
                }
            } ?: ""

            HttpResponse(statusCode = statusCode, body = body)
        } finally {
            conn.disconnect()
        }
    }
}
