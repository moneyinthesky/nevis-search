package com.nevis.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.OffsetDateTime
import java.util.UUID

data class CreateClientRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val description: String? = null,
    val socialLinks: List<String> = emptyList(),
) {
    init {
        require(firstName.isNotBlank()) { "first_name must not be blank" }
        require(lastName.isNotBlank()) { "last_name must not be blank" }
        require(email.matches(Regex(".+@.+\\..+"))) { "email must be a valid email address" }
    }
}

data class ClientResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val description: String?,
    val socialLinks: List<String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

data class CreateDocumentRequest(
    val title: String,
    val content: String,
) {
    init {
        require(title.isNotBlank()) { "title must not be blank" }
        require(title.length <= MAX_TITLE_LENGTH) { "title must not exceed $MAX_TITLE_LENGTH characters" }
        require(content.isNotBlank()) { "content must not be blank" }
        require(content.length <= MAX_CONTENT_LENGTH) { "content must not exceed $MAX_CONTENT_LENGTH characters" }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 500
        const val MAX_CONTENT_LENGTH = 100_000
    }
}

data class DocumentResponse(
    val id: UUID,
    val clientId: UUID,
    val title: String,
    val content: String,
    val createdAt: OffsetDateTime,
)

sealed interface SearchResult {
    val type: String
    @get:JsonIgnore
    val score: Double
}

data class ClientResult(
    override val type: String = "client",
    override val score: Double = 0.0,
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val description: String?,
    val socialLinks: List<String>,
) : SearchResult

data class DocumentResult(
    override val type: String = "document",
    override val score: Double = 0.0,
    val id: UUID,
    val clientId: UUID,
    val title: String,
    val createdAt: OffsetDateTime,
    val summary: String? = null,
) : SearchResult

data class DocumentHit(val result: DocumentResult, val content: String?, val cosineDistance: Float)

data class ErrorResponse(val error: String)
