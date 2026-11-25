package com.aidomari.app.core.ai

data class HttpResponse(
    val statusCode: Int,
    val body: String,
) {
    val isSuccessful: Boolean get() = statusCode in 200..299
}

interface HttpTransport {
    suspend fun post(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpResponse
}
