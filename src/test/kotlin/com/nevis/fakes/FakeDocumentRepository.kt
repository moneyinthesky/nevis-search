package com.nevis.fakes

import com.nevis.db.DocumentRepository
import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentHit
import com.nevis.model.DocumentResponse
import com.nevis.model.DocumentResult
import java.time.OffsetDateTime
import java.util.UUID

class FakeDocumentRepository(private val defaultDistance: Float = 0.1f) : DocumentRepository {

    private val documents = mutableMapOf<UUID, DocumentResponse>()
    private val distances = mutableMapOf<UUID, Float>()

    override fun create(clientId: UUID, request: CreateDocumentRequest, chunkEmbeddings: List<Pair<String, FloatArray>>): DocumentResponse =
        createWithDistance(clientId, request, defaultDistance)

    fun createWithDistance(clientId: UUID, request: CreateDocumentRequest, distance: Float): DocumentResponse {
        val doc = DocumentResponse(
            id = UUID.randomUUID(),
            clientId = clientId,
            title = request.title,
            content = request.content,
            createdAt = OffsetDateTime.now(),
        )
        documents[doc.id] = doc
        distances[doc.id] = distance
        return doc
    }

    override fun search(queryEmbedding: FloatArray, includeContent: Boolean, threshold: Float): List<DocumentHit> =
        documents.values
            .filter { (distances[it.id] ?: defaultDistance) < threshold }
            .map {
                DocumentHit(
                    result = DocumentResult(
                        id = it.id,
                        clientId = it.clientId,
                        title = it.title,
                        createdAt = it.createdAt,
                    ),
                    content = if (includeContent) it.content else null,
                    cosineDistance = distances[it.id] ?: defaultDistance,
                )
            }
}
