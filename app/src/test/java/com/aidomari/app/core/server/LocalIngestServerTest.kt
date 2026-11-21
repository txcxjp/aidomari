package com.aidomari.app.core.server

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.database.AidomariDatabase
import com.aidomari.app.core.database.SourceArticleRepositoryImpl
import com.aidomari.app.domain.model.article.ArticleStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

@RunWith(RobolectricTestRunner::class)
class LocalIngestServerTest {

    private lateinit var db: AidomariDatabase
    private lateinit var repository: SourceArticleRepositoryImpl
    private lateinit var server: LocalIngestServer
    private lateinit var scope: CoroutineScope
    private var testPort: Int = 0

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SourceArticleRepositoryImpl(db.sourceArticleDao())

        // Find an open port dynamically
        val tempSocket = ServerSocket(0)
        testPort = tempSocket.localPort
        tempSocket.close()

        server = LocalIngestServer(repository, testPort)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        server.start(scope)
    }

    @After
    fun tearDown() {
        server.stop()
        scope.cancel()
        db.close()
    }

    @Test
    fun server_handlesCorsPreflight_returns204() = runBlocking {
        delay(100) // Wait for server to bind

        Socket("127.0.0.1", testPort).use { socket ->
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))

            writer.print("OPTIONS /api/ingest HTTP/1.1\r\nHost: 127.0.0.1\r\n\r\n")
            writer.flush()

            val statusLine = reader.readLine()
            assertTrue(statusLine != null && statusLine.contains("204"))

            val headers = mutableListOf<String>()
            var line = reader.readLine()
            while (!line.isNullOrBlank()) {
                headers.add(line)
                line = reader.readLine()
            }
            assertTrue(headers.any { it.contains("Access-Control-Allow-Origin: *") })
        }
    }

    @Test
    fun server_handlesStatusEndpoint_returns200() = runBlocking {
        delay(100)

        Socket("127.0.0.1", testPort).use { socket ->
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))

            writer.print("GET /api/status HTTP/1.1\r\nHost: 127.0.0.1\r\n\r\n")
            writer.flush()

            val statusLine = reader.readLine()
            assertTrue(statusLine != null && statusLine.contains("200"))

            var line = reader.readLine()
            var contentLength = 0
            while (!line.isNullOrBlank()) {
                if (line.lowercase().startsWith("content-length:")) {
                    contentLength = line.substringAfter(":").trim().toInt()
                }
                line = reader.readLine()
            }

            val bodyChars = CharArray(contentLength)
            reader.read(bodyChars, 0, contentLength)
            val body = String(bodyChars)
            assertTrue(body.contains("\"status\":\"ok\""))
            assertTrue(body.contains("\"app\":\"aidomari\""))
        }
    }

    @Test
    fun server_handlesIngestPost_persistsArticleInDatabase() = runBlocking {
        delay(100)

        val jsonPayload = """{"url":"https://example.com/test-article","title":"テスト記事","content":"記事の本文内容です。"}"""
        val payloadBytes = jsonPayload.toByteArray(Charsets.UTF_8)

        Socket("127.0.0.1", testPort).use { socket ->
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))

            writer.print("POST /api/ingest HTTP/1.1\r\n")
            writer.print("Host: 127.0.0.1\r\n")
            writer.print("Content-Type: application/json\r\n")
            writer.print("Content-Length: ${payloadBytes.size}\r\n")
            writer.print("\r\n")
            writer.print(jsonPayload)
            writer.flush()

            val statusLine = reader.readLine()
            assertTrue(statusLine != null && statusLine.contains("200"))
        }

        // Verify stored in DB via repository
        val pendingArticles = repository.getPendingArticles().first()
        assertEquals(1, pendingArticles.size)
        assertEquals("テスト記事", pendingArticles[0].title)
        assertEquals("https://example.com/test-article", pendingArticles[0].url)
        assertEquals("example.com", pendingArticles[0].domain)
        assertEquals(ArticleStatus.PENDING, pendingArticles[0].status)
    }

    @Test
    fun server_handlesMissingUrlOrContent_returns400() = runBlocking {
        delay(100)

        val invalidPayload = """{"url":"","title":"No URL","content":""}"""
        val payloadBytes = invalidPayload.toByteArray(Charsets.UTF_8)

        Socket("127.0.0.1", testPort).use { socket ->
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))

            writer.print("POST /api/ingest HTTP/1.1\r\n")
            writer.print("Host: 127.0.0.1\r\n")
            writer.print("Content-Type: application/json\r\n")
            writer.print("Content-Length: ${payloadBytes.size}\r\n")
            writer.print("\r\n")
            writer.print(invalidPayload)
            writer.flush()

            val statusLine = reader.readLine()
            assertTrue(statusLine != null && statusLine.contains("400"))
        }
    }
}
