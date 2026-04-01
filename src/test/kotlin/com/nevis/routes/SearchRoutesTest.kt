package com.nevis.routes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nevis.client.OpenAiClient.Companion.EMBEDDING_DIMENSIONS
import com.nevis.config.errorHandler
import com.nevis.fakes.FakeClientRepository
import com.nevis.fakes.FakeDocumentRepository
import com.nevis.fakes.FakeEmbeddingClient
import com.nevis.fakes.FakeSummarizationClient
import com.nevis.model.CreateClientRequest
import com.nevis.model.CreateDocumentRequest
import com.nevis.service.SearchService
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchRoutesTest {

    private val clients = FakeClientRepository()
    private val documents = FakeDocumentRepository()
    private val embedder = FakeEmbeddingClient()
    private val summarizer = FakeSummarizationClient()
    private val service = SearchService(clients, documents, embedder, summarizer)
    private val handler = errorHandler.then(searchRoutes(service))
    private val mapper = jacksonObjectMapper()

    @Test
    fun `GET search returns matching clients`() {
        clients.create(CreateClientRequest("Jane", "Doe", "jane@neviswealth.com"))
        clients.create(CreateClientRequest("John", "Smith", "john@other.com"))

        val response = handler(Request(GET, "/search?q=neviswealth"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any>>>(response.bodyString())
        assertEquals(1, results.size)
        assertEquals("client", results[0]["type"])
    }

    @Test
    fun `GET search returns documents`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(
            client.id,
            CreateDocumentRequest("Passport", "ID document"),
            listOf("chunk" to FloatArray(EMBEDDING_DIMENSIONS))
        )

        val response = handler(Request(GET, "/search?q=anything"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any>>>(response.bodyString())
        assertTrue(results.any { it["type"] == "document" })
    }

    @Test
    fun `GET search with summary=true includes summaries`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(
            client.id,
            CreateDocumentRequest("Passport", "ID document content"),
            listOf("chunk" to FloatArray(EMBEDDING_DIMENSIONS)
            )
        )

        val response = handler(Request(GET, "/search?q=passport&summary=true"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any?>>>(response.bodyString())
        val doc = results.first { it["type"] == "document" }
        assertTrue(doc["summary"].toString().startsWith("Summary of:"))
    }

    @Test
    fun `GET search without q returns 400`() {
        val response = handler(Request(GET, "/search"))
        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `GET search returns both clients and documents in one response`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@neviswealth.com"))
        documents.create(
            client.id,
            CreateDocumentRequest("Neviswealth Report", "Annual report"),
            listOf("chunk" to FloatArray(EMBEDDING_DIMENSIONS))
        )

        val response = handler(Request(GET, "/search?q=neviswealth"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any>>>(response.bodyString())
        val types = results.map { it["type"] }.toSet()
        assertTrue(types.contains("client"), "Expected client results")
        assertTrue(types.contains("document"), "Expected document results")
    }

    @Test
    fun `GET search with empty query string returns 400`() {
        val response = handler(Request(GET, "/search?q="))
        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `GET search with no matching results returns empty list`() {
        val response = handler(Request(GET, "/search?q=xyznonexistent"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any>>>(response.bodyString())
        assertEquals(0, results.size)
    }

    @Test
    fun `GET search with special characters returns 200`() {
        clients.create(CreateClientRequest("Jane", "O'Brien", "jane@example.com"))

        val response = handler(Request(GET, "/search?q=%25DROP%20TABLE"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any>>>(response.bodyString())
        assertTrue(results.none { it["type"] == "client" })
    }

    @Test
    fun `GET search results do not expose score`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(
            client.id,
            CreateDocumentRequest("Report", "Annual report"),
            listOf("chunk" to FloatArray(EMBEDDING_DIMENSIONS))
        )

        val response = handler(Request(GET, "/search?q=jane"))

        assertEquals(OK, response.status)
        val results = mapper.readValue<List<Map<String, Any>>>(response.bodyString())
        assertTrue(results.isNotEmpty())
        assertTrue(results.none { it.containsKey("score") }, "Score should not be exposed in the response")
    }
}
