package com.nevis.routes

import com.nevis.config.Json
import com.nevis.model.CreateDocumentRequest
import com.nevis.model.DocumentResponse
import com.nevis.service.DocumentService
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import java.util.UUID

private val requestLens = Json.autoBody<CreateDocumentRequest>().toLens()
private val responseLens = Json.autoBody<DocumentResponse>().toLens()

fun documentRoutes(documentService: DocumentService): RoutingHttpHandler =
    "/clients/{clientId}/documents" bind POST to { request ->
        val clientId = UUID.fromString(request.path("clientId"))
        val body = requestLens(request)
        val document = documentService.create(clientId, body)
        responseLens(document, Response(CREATED))
    }
