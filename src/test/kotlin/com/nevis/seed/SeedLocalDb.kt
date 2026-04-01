package com.nevis.seed

import com.nevis.config.Json
import com.nevis.model.ClientResponse
import org.http4k.client.OkHttp
import org.http4k.core.Credentials
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.then
import org.http4k.filter.ClientFilters

private val clientResponseLens = Json.autoBody<ClientResponse>().toLens()

fun main() {
    val baseUrl = System.getenv("SEED_URL") ?: "http://localhost:8080"
    val username = System.getenv("BASIC_AUTH_USERNAME") ?: "admin"
    val password = System.getenv("BASIC_AUTH_PASSWORD") ?: "admin"
    val http = ClientFilters.BasicAuth(Credentials(username, password)).then(OkHttp())

    println("Seeding $baseUrl with ${SeedData.all.size} clients...")

    SeedData.all.forEach { seed ->
        val clientResponse = http(
            Request(POST, "$baseUrl/clients")
                .header("Content-Type", "application/json")
                .body(Json.asFormatString(seed.client))
        )
        check(clientResponse.status == CREATED) {
            "Failed to create client ${seed.client.firstName} ${seed.client.lastName}: ${clientResponse.status}"
        }

        val client = clientResponseLens(clientResponse)
        println("  ${seed.client.firstName} ${seed.client.lastName}: ${client.id}")

        seed.documents.forEach { doc ->
            val docResponse = http(
                Request(POST, "$baseUrl/clients/${client.id}/documents")
                    .header("Content-Type", "application/json")
                    .body(Json.asFormatString(doc))
            )
            check(docResponse.status == CREATED) {
                "Failed to create document '${doc.title}': ${docResponse.status}"
            }
            println("    ${doc.title}")
        }
    }

    println("Done — ${SeedData.all.size} clients, ${SeedData.all.sumOf { it.documents.size }} documents")
}
