package com.nevis.routes

import com.nevis.config.Json
import com.nevis.db.ClientRepository
import com.nevis.model.ClientResponse
import com.nevis.model.CreateClientRequest
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

private val requestLens = Json.autoBody<CreateClientRequest>().toLens()
private val responseLens = Json.autoBody<ClientResponse>().toLens()

fun clientRoutes(clients: ClientRepository): RoutingHttpHandler =
    "/clients" bind POST to { request ->
        val client = clients.create(requestLens(request))
        responseLens(client, Response(CREATED))
    }
