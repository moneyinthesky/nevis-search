package com.nevis.db

import com.nevis.model.ClientResponse
import com.nevis.model.ClientResult
import com.nevis.model.CreateClientRequest
import java.util.UUID

interface ClientRepository {
    fun create(request: CreateClientRequest): ClientResponse
    fun existsById(clientId: UUID): Boolean
    fun search(query: String): List<ClientResult>
}
