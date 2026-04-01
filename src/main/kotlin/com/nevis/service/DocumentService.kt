package com.nevis.service

import com.nevis.client.EmbeddingClient
import com.nevis.config.ChunkingSettings
import com.nevis.db.ClientRepository
import com.nevis.db.DocumentRepository
import com.nevis.db.ExposedTransactional
import com.nevis.db.Transactional
import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentResponse
import com.nevis.util.chunkText
import com.nevis.util.mapConcurrently
import java.util.UUID

class DocumentService(
    private val clients: ClientRepository,
    private val documents: DocumentRepository,
    private val embedder: EmbeddingClient,
    private val chunking: ChunkingSettings = ChunkingSettings(),
    private val transactional: Transactional = ExposedTransactional,
) {
    fun create(clientId: UUID, request: CreateDocumentRequest): DocumentResponse {
        val textToEmbed = "${request.title} ${request.content}"
        val chunks = chunkText(textToEmbed, chunking.chunkSize, chunking.overlap)
        val chunkEmbeddings = chunks.mapConcurrently(batchSize = chunking.embeddingBatchSize) { chunk ->
            chunk to embedder.embed(chunk)
        }

        return transactional.execute {
            if (!clients.existsById(clientId))
                throw ClientNotFoundException(clientId)
            documents.create(clientId, request, chunkEmbeddings)
        }
    }
}

class ClientNotFoundException(val clientId: UUID) : RuntimeException("Client not found: $clientId")
