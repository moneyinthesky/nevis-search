package com.nevis.fakes

import com.nevis.db.ClientRepository
import com.nevis.model.ClientResponse
import com.nevis.model.ClientResult
import com.nevis.model.CreateClientRequest
import java.time.OffsetDateTime
import java.util.UUID

class FakeClientRepository : ClientRepository {

    private val clients = mutableMapOf<UUID, ClientResponse>()

    override fun create(request: CreateClientRequest): ClientResponse {
        val now = OffsetDateTime.now()
        val client = ClientResponse(
            id = UUID.randomUUID(),
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            description = request.description,
            socialLinks = request.socialLinks,
            createdAt = now,
            updatedAt = now,
        )
        clients[client.id] = client
        return client
    }

    override fun existsById(clientId: UUID): Boolean = clients.containsKey(clientId)

    override fun search(query: String): List<ClientResult> {
        val searchableFields: List<(ClientResponse) -> String?> = listOf(
            { it.firstName },
            { it.lastName },
            { it.email },
            { it.description },
        )
        return clients.values
            .filter { client -> searchableFields.any { field -> field(client)?.lowercase()?.contains(query) == true } }
            .map {
                ClientResult(
                    id = it.id,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email,
                    description = it.description,
                    socialLinks = it.socialLinks,
                )
            }
    }
}
