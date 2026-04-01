package com.nevis.service

import com.nevis.client.OpenAiClient.Companion.EMBEDDING_DIMENSIONS
import com.nevis.config.SearchSettings
import com.nevis.fakes.FakeClientRepository
import com.nevis.fakes.FakeDocumentRepository
import com.nevis.fakes.FakeEmbeddingClient
import com.nevis.fakes.FakeSummarizationClient
import com.nevis.model.ClientResult
import com.nevis.model.CreateClientRequest
import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentResult
import kotlin.test.*

class SearchServiceTest {

    private val clients = FakeClientRepository()
    private val documents = FakeDocumentRepository()
    private val embedder = FakeEmbeddingClient()
    private val summarizer = FakeSummarizationClient()
    private val service = SearchService(clients, documents, embedder, summarizer)

    private fun emptyChunkEmbeddings() = listOf("chunk" to FloatArray(EMBEDDING_DIMENSIONS))

    @Test
    fun `search returns matching clients and documents`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@neviswealth.com"))
        documents.create(client.id, CreateDocumentRequest("Neviswealth Report", "Annual report"), emptyChunkEmbeddings())

        val results = service.search("neviswealth", includeSummaries = false)

        val types = results.map { it.type }.toSet()
        assertTrue("client" in types)
        assertTrue("document" in types)
    }

    @Test
    fun `search with summaries populates summary field`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(client.id, CreateDocumentRequest("Passport", "ID document content"), emptyChunkEmbeddings())

        val results = service.search("passport", includeSummaries = true)

        val doc = results.first { it.type == "document" } as DocumentResult
        assertNotNull(doc.summary)
        assertTrue(doc.summary.orEmpty().startsWith("Summary of:"))
    }

    @Test
    fun `search without summaries leaves summary null`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(client.id, CreateDocumentRequest("Passport", "ID document content"), emptyChunkEmbeddings())

        val results = service.search("passport", includeSummaries = false)

        val doc = results.first { it.type == "document" } as DocumentResult
        assertNull(doc.summary)
    }

    @Test
    fun `search with no matches returns empty list`() {
        val results = service.search("xyznonexistent", includeSummaries = false)

        assertEquals(0, results.size)
    }

    @Test
    fun `all results include a score`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@neviswealth.com"))
        documents.create(client.id, CreateDocumentRequest("Report", "Annual report"), emptyChunkEmbeddings())

        val results = service.search("neviswealth", includeSummaries = false)

        assertTrue(results.all { it.score > 0.0 })
    }

    @Test
    fun `exact name match scores 0_95`() {
        clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val results = service.search("jane", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.95, client.score)
    }

    @Test
    fun `name prefix match scores 0_85`() {
        clients.create(CreateClientRequest("Alexandra", "Smith", "alex@example.com"))

        val results = service.search("alex", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.85, client.score)
    }

    @Test
    fun `email match scores 0_75`() {
        clients.create(CreateClientRequest("Bob", "Jones", "bob@neviswealth.com"))

        val results = service.search("neviswealth", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.75, client.score)
    }

    @Test
    fun `description match scores 0_60`() {
        clients.create(CreateClientRequest("Bob", "Jones", "bob@other.com", description = "wealth management client"))

        val results = service.search("wealth", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.60, client.score)
    }

    @Test
    fun `name contains match scores 0_70`() {
        clients.create(CreateClientRequest("Jane", "Doe", "j@example.com"))

        val results = service.search("ane", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.70, client.score)
    }

    @Test
    fun `best score wins when multiple fields match`() {
        clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val results = service.search("jane", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.95, client.score, "Exact name match should win over email contains")
    }

    @Test
    fun `client scoring is case insensitive`() {
        clients.create(CreateClientRequest("JANE", "DOE", "JANE@EXAMPLE.COM"))

        val results = service.search("jane", includeSummaries = false)
        val client = results.first { it.type == "client" }

        assertEquals(0.95, client.score)
    }

    @Test
    fun `document score reflects cosine distance`() {
        val threshold = 0.4f
        val nearDocuments = FakeDocumentRepository(defaultDistance = 0.1f)
        val serviceWithNearDocs = SearchService(clients, nearDocuments, embedder, summarizer, SearchSettings(documentThreshold = threshold))
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        nearDocuments.create(client.id, CreateDocumentRequest("Report", "Annual report"), emptyChunkEmbeddings())

        val results = serviceWithNearDocs.search("report", includeSummaries = false)
        val doc = results.first { it.type == "document" }

        // score = 1.0 - (distance / threshold) = 1.0 - (0.1 / 0.4) = 0.75
        assertEquals(0.75, doc.score)
    }

    @Test
    fun `threshold is passed through to repository filtering`() {
        val farDocuments = FakeDocumentRepository(defaultDistance = 0.8f)
        val serviceWithFarDocs = SearchService(clients, farDocuments, embedder, summarizer)
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        farDocuments.create(client.id, CreateDocumentRequest("Report", "Annual report"), emptyChunkEmbeddings())

        val results = serviceWithFarDocs.search("report", includeSummaries = false)

        assertTrue(results.none { it.type == "document" })
    }

    @Test
    fun `results are sorted by score descending`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(client.id, CreateDocumentRequest("Report", "Annual report"), emptyChunkEmbeddings())

        val results = service.search("jane", includeSummaries = false)

        val scores = results.map { it.score }
        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun `exact name match ranks above close document match`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))
        documents.create(client.id, CreateDocumentRequest("Report", "Annual report"), emptyChunkEmbeddings())

        val results = service.search("jane", includeSummaries = false)

        assertTrue(results.first() is ClientResult, "Client with exact name match should rank first")
    }

    // --- Comprehensive sorting with mixed result types ---

    @Test
    fun `multiple clients sort by their individual score tiers`() {
        clients.create(CreateClientRequest("Alexandra", "Morgan", "alex.morgan@example.com"))
        clients.create(CreateClientRequest("Alex", "Thompson", "alex.t@example.com"))

        val results = service.search("alex", includeSummaries = false)

        assertEquals(2, results.size)
        val scores = results.map { it.score }
        // "Alex" is exact match (0.95), "Alexandra" is prefix match (0.85)
        assertEquals(0.95, scores[0])
        assertEquals(0.85, scores[1])
    }

    @Test
    fun `documents at different distances produce different scores`() {
        val perDocDocs = FakeDocumentRepository()
        val svc = SearchService(clients, perDocDocs, embedder, summarizer)
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        perDocDocs.createWithDistance(client.id, CreateDocumentRequest("Close doc", "content"), 0.05f)
        perDocDocs.createWithDistance(client.id, CreateDocumentRequest("Far doc", "content"), 0.35f)

        val results = svc.search("anything", includeSummaries = false)
        val docs = results.filterIsInstance<DocumentResult>()

        assertEquals(2, docs.size)
        assertTrue(docs[0].score > docs[1].score, "Closer document should score higher")
    }

    @Test
    fun `interleaved client and document results sort correctly by score`() {
        val threshold = 0.5f
        val perDocDocs = FakeDocumentRepository()
        val svc = SearchService(clients, perDocDocs, embedder, summarizer, SearchSettings(documentThreshold = threshold))

        // Client: exact name match → 0.95
        val client1 = clients.create(CreateClientRequest("Morgan", "Stanley", "m.stanley@example.com"))
        // Client: email match only → 0.75
        val client2 = clients.create(CreateClientRequest("Bob", "Jones", "bob@morgan.com"))
        // Client: description match → 0.60
        clients.create(CreateClientRequest("Eve", "Wilson", "eve@other.com", description = "morgan advisory client"))

        // Document: distance 0.05 → score = 1.0 - (0.05/0.5) = 0.90 (between exact name 0.95 and email 0.75)
        perDocDocs.createWithDistance(client1.id, CreateDocumentRequest("Doc A", "content"), 0.05f)
        // Document: distance 0.20 → score = 1.0 - (0.20/0.5) = 0.60 (ties with description, but still in order)
        perDocDocs.createWithDistance(client2.id, CreateDocumentRequest("Doc B", "content"), 0.20f)

        val results = svc.search("morgan", includeSummaries = false)
        val scores = results.map { it.score }

        assertEquals(scores.sortedDescending(), scores)

        assertEquals(5, results.size)
        assertTrue(results[0] is ClientResult, "Exact name match should be first")
        assertTrue(results[1] is DocumentResult, "Close document should be second")
        assertTrue(results[2] is ClientResult, "Email match should be third")
    }

    @Test
    fun `multiple clients with same score maintain stable ordering`() {
        clients.create(CreateClientRequest("Jane", "Doe", "j.doe@example.com"))
        clients.create(CreateClientRequest("Jane", "Smith", "j.smith@example.com"))

        val results = service.search("jane", includeSummaries = false)
        val scores = results.map { it.score }

        // Both are exact name matches → 0.95
        assertEquals(2, results.size)
        assertTrue(scores.all { it == 0.95 })
    }

    @Test
    fun `documents at varied distances sort and filter correctly`() {
        val threshold = 0.4f
        val perDocDocs = FakeDocumentRepository()
        val svc = SearchService(clients, perDocDocs, embedder, summarizer, SearchSettings(documentThreshold = threshold))
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        perDocDocs.createWithDistance(client.id, CreateDocumentRequest("Doc A", "content"), 0.05f)
        perDocDocs.createWithDistance(client.id, CreateDocumentRequest("Doc B", "content"), 0.20f)
        perDocDocs.createWithDistance(client.id, CreateDocumentRequest("Doc C", "content"), 0.10f)
        perDocDocs.createWithDistance(client.id, CreateDocumentRequest("Doc D", "content"), 0.45f)

        val results = svc.search("anything", includeSummaries = false)
        val docs = results.filterIsInstance<DocumentResult>()

        assertEquals(3, docs.size, "Doc D (0.45) should be filtered out by threshold (0.4)")
        assertEquals("Doc A", docs.first().title, "Closest document should rank first")
        val scores = docs.map { it.score }
        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun `close documents rank above email-only client matches`() {
        val perDocDocs = FakeDocumentRepository()
        val svc = SearchService(clients, perDocDocs, embedder, summarizer)

        val client1 = clients.create(CreateClientRequest("Victoria", "Chen", "v.chen@neviswealth.com"))
        val client2 = clients.create(CreateClientRequest("Raj", "Patel", "raj.patel@neviswealth.com"))

        // Document distance 0.05 → score 0.875, above email match (0.75)
        perDocDocs.createWithDistance(client1.id, CreateDocumentRequest("Doc A", "content"), 0.05f)
        perDocDocs.createWithDistance(client2.id, CreateDocumentRequest("Doc B", "content"), 0.08f)

        val results = svc.search("neviswealth", includeSummaries = false)

        val scores = results.map { it.score }
        assertEquals(scores.sortedDescending(), scores, "Results should be sorted by score descending")

        val firstDocIndex = results.indexOfFirst { it is DocumentResult }
        val firstClientIndex = results.indexOfFirst { it is ClientResult }
        assertTrue(firstDocIndex < firstClientIndex,
            "Close document matches should rank above email-only client matches")
    }

    @Test
    fun `search does not treat LIKE wildcards as wildcards`() {
        clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        assertTrue(service.search("%", includeSummaries = false).isEmpty(),
            "% should not match everything")
        assertTrue(service.search("_", includeSummaries = false).isEmpty(),
            "_ should not match as single-character wildcard")
    }
}
