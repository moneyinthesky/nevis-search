package com.nevis.db

import com.nevis.client.OpenAiClient.Companion.EMBEDDING_DIMENSIONS
import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentHit
import com.nevis.model.DocumentResponse
import com.nevis.model.DocumentResult
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

internal object DocumentsTable : Table("documents") {
    val id = uuid("id").autoGenerate()
    val clientId = uuid("client_id").references(ClientsTable.id)
    val title = varchar("title", 500)
    val content = text("content")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}

internal object DocumentChunksTable : Table("document_chunks") {
    val id = uuid("id").autoGenerate()
    val documentId = uuid("document_id").references(DocumentsTable.id)
    val chunkIndex = integer("chunk_index")
    val content = text("content")
    val embedding = vector("embedding", EMBEDDING_DIMENSIONS).nullable()
    val createdAt = timestampWithTimeZone("created_at")
        .defaultExpression(CurrentTimestampWithTimeZone)

    override val primaryKey = PrimaryKey(id)
}

class DatabaseDocumentRepository : DocumentRepository {

    override fun create(clientId: UUID, request: CreateDocumentRequest, chunkEmbeddings: List<Pair<String, FloatArray>>): DocumentResponse = transaction {
        val document = DocumentsTable.insertReturning {
            it[DocumentsTable.clientId] = clientId
            it[title] = request.title
            it[content] = request.content
        }.single().let { row ->
            DocumentResponse(
                id = row[DocumentsTable.id],
                clientId = row[DocumentsTable.clientId],
                title = row[DocumentsTable.title],
                content = row[DocumentsTable.content],
                createdAt = row[DocumentsTable.createdAt],
            )
        }

        DocumentChunksTable.batchInsert(
            chunkEmbeddings.withIndex().toList(),
            shouldReturnGeneratedValues = false,
        ) { (index, chunkPair) ->
            val (chunkContent, embedding) = chunkPair
            this[DocumentChunksTable.documentId] = document.id
            this[DocumentChunksTable.chunkIndex] = index
            this[DocumentChunksTable.content] = chunkContent
            this[DocumentChunksTable.embedding] = embedding
        }

        document
    }

    override fun search(queryEmbedding: FloatArray, includeContent: Boolean, threshold: Float): List<DocumentHit> = transaction {
        val distance = cosineDistance(DocumentChunksTable.embedding, queryEmbedding)

        val resultColumns = listOfNotNull(
            DocumentsTable.id, DocumentsTable.clientId, DocumentsTable.title, DocumentsTable.createdAt,
            distance,
            if (includeContent) DocumentsTable.content else null,
        )

        (DocumentChunksTable innerJoin DocumentsTable)
            .select(resultColumns)
            .where { distance less threshold }
            .withDistinctOn(DocumentsTable.id)
            .orderBy(DocumentsTable.id to ASC, distance to ASC)
            .map { row ->
                DocumentHit(
                    result = DocumentResult(
                        id = row[DocumentsTable.id],
                        clientId = row[DocumentsTable.clientId],
                        title = row[DocumentsTable.title],
                        createdAt = row[DocumentsTable.createdAt],
                    ),
                    content = if (includeContent) row[DocumentsTable.content] else null,
                    cosineDistance = row[distance],
                )
            }
            .sortedBy { it.cosineDistance }
    }
}
