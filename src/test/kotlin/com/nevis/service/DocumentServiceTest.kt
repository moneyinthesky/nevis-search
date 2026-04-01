package com.nevis.service

import com.nevis.client.EmbeddingClient
import com.nevis.fakes.FakeClientRepository
import com.nevis.fakes.FakeDocumentRepository
import com.nevis.fakes.FakeEmbeddingClient
import com.nevis.fakes.NoOpTransactional
import com.nevis.model.CreateClientRequest
import com.nevis.model.CreateDocumentRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DocumentServiceTest {

    private val clients = FakeClientRepository()
    private val documents = FakeDocumentRepository()
    private val embedder = FakeEmbeddingClient()
    private val service = DocumentService(clients, documents, embedder, transactional = NoOpTransactional)

    @Test
    fun `create returns document for valid client`() {
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        val document = service.create(client.id, CreateDocumentRequest("Passport", "Scanned passport"))

        assertEquals("Passport", document.title)
        assertEquals("Scanned passport", document.content)
        assertEquals(client.id, document.clientId)
    }

    @Test
    fun `create throws ClientNotFoundException for missing client`() {
        val missingId = java.util.UUID.randomUUID()

        val exception = assertFailsWith<ClientNotFoundException> {
            service.create(missingId, CreateDocumentRequest("Passport", "Scanned passport"))
        }

        assertEquals(missingId, exception.clientId)
    }

    @Test
    fun `create propagates LLM failure`() {
        val failingEmbedder = object : EmbeddingClient {
            override fun embed(text: String): FloatArray = throw RuntimeException("LLM unavailable")
        }
        val failService = DocumentService(clients, documents, failingEmbedder, transactional = NoOpTransactional)
        val client = clients.create(CreateClientRequest("Jane", "Doe", "jane@example.com"))

        assertFailsWith<RuntimeException> {
            failService.create(client.id, CreateDocumentRequest("Passport", "Scanned passport"))
        }
    }
}
