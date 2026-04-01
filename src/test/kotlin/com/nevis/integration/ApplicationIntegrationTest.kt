package com.nevis.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nevis.config.DatabaseConfig
import com.nevis.config.DatabaseSettings
import com.nevis.config.Json
import com.nevis.config.errorHandler
import com.nevis.db.DatabaseClientRepository
import com.nevis.db.DatabaseDocumentRepository
import com.nevis.fakes.FakeEmbeddingClient
import com.nevis.fakes.FakeSummarizationClient
import com.nevis.model.ClientResponse
import com.nevis.model.CreateClientRequest
import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentResponse
import com.nevis.routes.clientRoutes
import com.nevis.routes.documentRoutes
import com.nevis.routes.searchRoutes
import com.nevis.seed.ClientSeed
import com.nevis.seed.SeedData
import com.nevis.service.DocumentService
import com.nevis.service.SearchService
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.routes
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Testcontainers
class ApplicationIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("pgvector/pgvector:pg17")
            .withDatabaseName("nevis_test")
            .withUsername("test")
            .withPassword("test")

        @BeforeAll
        @JvmStatic
        fun setupDatabase() {
            DatabaseConfig.init(
                DatabaseSettings(
                    url = postgres.jdbcUrl,
                    user = postgres.username,
                    password = postgres.password,
                )
            )
        }
    }

    private val clients = DatabaseClientRepository()
    private val documents = DatabaseDocumentRepository()
    private val embedder = FakeEmbeddingClient()
    private val summarizer = FakeSummarizationClient()
    private val documentService = DocumentService(clients, documents, embedder)
    private val searchService = SearchService(clients, documents, embedder, summarizer)
    private val clientResponseLens = Json.autoBody<ClientResponse>().toLens()
    private val documentResponseLens = Json.autoBody<DocumentResponse>().toLens()
    private val mapper = jacksonObjectMapper()

    private val handler = errorHandler.then(
        routes(
            clientRoutes(clients),
            documentRoutes(documentService),
            searchRoutes(searchService),
        )
    )

    @BeforeEach
    fun cleanDatabase() {
        transaction {
            exec("TRUNCATE document_chunks, documents, clients CASCADE")
        }
    }

    @Test
    fun `POST clients creates a client with generated id and timestamps`() {
        val response = makeCreateClientCall(SeedData.johnDoe.client)

        assertEquals(CREATED, response.status)
        val body = clientResponseLens(response)
        assertNotNull(body.id)
        assertEquals("John", body.firstName)
        assertEquals("Doe", body.lastName)
        assertNotNull(body.createdAt)
        assertNotNull(body.updatedAt)
    }

    @Test
    fun `POST documents creates a document linked to existing client`() {
        val client = createClient(SeedData.johnDoe.client)
        val doc = SeedData.johnDoe.documents.first()

        val response = makeCreateDocumentCall(client.id.toString(), doc)

        assertEquals(CREATED, response.status)
        val body = documentResponseLens(response)
        assertNotNull(body.id)
        assertEquals(client.id, body.clientId)
        assertEquals(doc.title, body.title)
        assertNotNull(body.createdAt)
    }

    @Test
    fun `POST documents returns 404 for non-existent client`() {
        val response = makeCreateDocumentCall(
            "00000000-0000-0000-0000-000000000000", // non-existent client
            SeedData.johnDoe.documents.first(),
        )

        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `GET search returns matching clients by text`() {
        createClient(SeedData.johnDoe.client)       // neviswealth email
        createClient(SeedData.aliceSmith.client)     // goldmansachs email

        val results = search("neviswealth")
        assertEquals(1, results.size)
        assertEquals("client", results[0]["type"])
        assertEquals("John", results[0]["first_name"])
    }

    @Test
    fun `GET search returns documents by semantic similarity`() {
        seedClient(SeedData.johnDoe)

        // FakeEmbeddingClient returns the same embedding (0.1f) for all inputs,
        // so the query embedding will match the stored document embedding
        val results = search("utility bill")
        val docTitles = results.filter { it["type"] == "document" }.map { it["title"] }
        assertTrue("Utility Bill - March 2026" in docTitles)
        assertTrue("Investment Policy Statement" in docTitles)
    }

    @Test
    fun `GET search returns both clients and documents`() {
        seedClient(SeedData.johnDoe) // neviswealth email + documents

        val results = search("neviswealth")
        val types = results.map { it["type"] }.toSet()
        assertTrue("client" in types, "Expected client results")
        assertTrue("document" in types, "Expected document results")
    }

    @Test
    fun `GET search with summary=true returns summaries on documents`() {
        seedClient(SeedData.johnDoe)

        val results = search("investment", summary = true)
        val doc = results.first { it["type"] == "document" }
        assertNotNull(doc["summary"], "Document should have a summary")
    }

    @Test
    fun `GET search with mixed case query returns results`() {
        createClient(SeedData.johnDoe.client)

        val results = search("JOHN")
        assertTrue(results.any { it["type"] == "client" })
    }

    @Test
    fun `GET search with no matches returns empty list`() {
        val results = search("xyznonexistent")
        assertEquals(0, results.size)
    }

    @Test
    fun `search across multiple clients with neviswealth emails`() {
        createClient(SeedData.johnDoe.client)         // neviswealth
        createClient(SeedData.bobWilliams.client)     // neviswealth
        createClient(SeedData.alexandraMorgan.client)  // neviswealth

        val results = search("neviswealth")
        val clientResults = results.filter { it["type"] == "client" }
        assertEquals(3, clientResults.size, "All three clients should match via email")
    }

    @Test
    fun `exact name match ranks above prefix match`() {
        createClient(SeedData.alexThompson.client)     // "Alex"
        createClient(SeedData.alexandraMorgan.client)   // "Alexandra"

        val results = search("alex")
        assertTrue(results.size >= 2, "Should match multiple clients")
        assertEquals("Alex", results.first()["first_name"])
    }

    @Test
    fun `client with documents returns both types`() {
        seedClient(SeedData.jamesChen)

        val results = search("chen")
        assertTrue(results.any { it["type"] == "client" }, "Client Chen should appear")
        assertTrue(results.any { it["type"] == "document" }, "Documents should appear via semantic search")
    }

    @Test
    fun `documents with summaries return summary field`() {
        seedClient(SeedData.johnDoe)

        val results = search("investment policy", summary = true)
        val docs = results.filter { it["type"] == "document" }
        docs.forEach { doc ->
            assertNotNull(doc["summary"], "Each document should have a summary")
        }
    }

    @Test
    fun `search returns only matching client when multiple clients exist`() {
        seedClient(SeedData.jamesChen)
        seedClient(SeedData.bobWilliams)

        val results = search("chen")
        val clientResults = results.filter { it["type"] == "client" }
        assertEquals(1, clientResults.size)
        assertEquals("Chen", clientResults.first()["last_name"])
    }

    @Test
    fun `description match returns client result`() {
        createClient(SeedData.johnDoe.client) // description: "equities and fixed income"

        val results = search("equities")
        assertTrue(results.any { it["type"] == "client" && it["first_name"] == "John" },
            "Client should match via description containing 'equities'")
    }

    @Test
    fun `creating a document produces chunks in the database`() {
        val client = createClient(SeedData.johnDoe.client)
        createDocument(client.id.toString(), SeedData.johnDoe.documents.first())

        val chunkCount = transaction {
            exec("SELECT COUNT(*) FROM document_chunks") { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        assertTrue(chunkCount!! > 0, "Document creation should produce at least one chunk")
    }

    @Test
    fun `large document produces multiple chunks`() {
        val client = createClient(SeedData.johnDoe.client)
        val largeDoc = CreateDocumentRequest(
            title = "Large Document",
            content = "x".repeat(5000),
        )
        createDocument(client.id.toString(), largeDoc)

        val chunkCount = transaction {
            exec("SELECT COUNT(*) FROM document_chunks") { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        assertTrue(chunkCount!! > 1, "Large document should produce multiple chunks")
    }

    private fun makeCreateClientCall(request: CreateClientRequest) = handler(
        Request(POST, "/clients")
            .header("Content-Type", "application/json")
            .body(Json.asFormatString(request))
    )

    private fun createClient(request: CreateClientRequest): ClientResponse =
        clientResponseLens(makeCreateClientCall(request))

    private fun makeCreateDocumentCall(clientId: String, request: CreateDocumentRequest) = handler(
        Request(POST, "/clients/$clientId/documents")
            .header("Content-Type", "application/json")
            .body(Json.asFormatString(request))
    )

    private fun createDocument(clientId: String, request: CreateDocumentRequest): DocumentResponse =
        documentResponseLens(makeCreateDocumentCall(clientId, request))

    private fun seedClient(seed: ClientSeed): ClientResponse {
        val client = createClient(seed.client)
        seed.documents.forEach { createDocument(client.id.toString(), it) }
        return client
    }

    private fun search(query: String, summary: Boolean = false): List<Map<String, Any?>> {
        val params = buildString {
            append("q=$query")
            if (summary) append("&summary=true")
        }
        val response = handler(Request(GET, "/search?$params"))
        assertEquals(OK, response.status)
        return mapper.readValue(response.bodyString())
    }
}
