package com.nevis.db

import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentHit
import com.nevis.model.DocumentResponse
import java.util.UUID

interface DocumentRepository {
    fun create(clientId: UUID, request: CreateDocumentRequest, chunkEmbeddings: List<Pair<String, FloatArray>>): DocumentResponse
    fun search(queryEmbedding: FloatArray, includeContent: Boolean = false, threshold: Float): List<DocumentHit>
}
