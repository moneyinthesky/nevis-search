package com.nevis.routes

import com.nevis.client.EmbeddingClient
import com.nevis.config.Json
import com.nevis.config.errorHandler
import com.nevis.fakes.FakeClientRepository
import com.nevis.fakes.FakeDocumentRepository
import com.nevis.fakes.FakeEmbeddingClient
import com.nevis.model.CreateClientRequest
import com.nevis.model.DocumentResponse
import com.nevis.service.DocumentService
import com.nevis.fakes.NoOpTransactional
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.then
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentRoutesTest {

    private val clients = FakeClientRepository()
    private val documents = FakeDocumentRepository()
    private val embedder = FakeEmbeddingClient()
    private val service = DocumentService(clients, documents, embedder, transactional = NoOpTransactional)
    private val handler = errorHandler.then(documentRoutes(service))
    private val responseLens = Json.autoBody<DocumentResponse>().toLens()

    @Test
    fun `POST creates a document and returns 201`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"Passport","content":"Scanned passport document"}""")
        )

        assertEquals(CREATED, response.status)
        val body = responseLens(response)
        assertEquals("Passport", body.title)
        assertEquals(client.id, body.clientId)
    }

    @Test
    fun `POST returns 404 for non-existent client`() {
        val response = handler(
            Request(POST, "/clients/00000000-0000-0000-0000-000000000000/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"Passport","content":"Scanned passport"}""")
        )

        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `POST with blank title returns 400`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"  ","content":"Some content"}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("title must not be blank"))
    }

    @Test
    fun `POST with blank content returns 400`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"Passport","content":" "}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("content must not be blank"))
    }

    @Test
    fun `POST with empty body returns 400`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("\"error\""))
    }

    @Test
    fun `POST with malformed JSON returns 400`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("\"error\""))
    }

    @Test
    fun `POST returns 500 when LLM embedding fails`() {
        val failingEmbedder = object : EmbeddingClient {
            override fun embed(text: String): FloatArray = throw RuntimeException("LLM unavailable")
        }
        val failService = DocumentService(clients, documents, failingEmbedder, transactional = NoOpTransactional)
        val failHandler = errorHandler.then(documentRoutes(failService))
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = failHandler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"Passport","content":"Scanned passport"}""")
        )

        assertEquals(INTERNAL_SERVER_ERROR, response.status)
        assertTrue(response.bodyString().contains("Internal server error"))
    }

    @Test
    fun `POST with content exceeding max length returns 400`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"Report","content":"${"x".repeat(100_001)}"}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("content must not exceed"))
    }

    @Test
    fun `POST with title exceeding max length returns 400`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val response = handler(
            Request(POST, "/clients/${client.id}/documents")
                .header("Content-Type", "application/json")
                .body("""{"title":"${"x".repeat(501)}","content":"Some content"}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("title must not exceed"))
    }
}
