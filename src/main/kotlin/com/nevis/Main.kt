package com.nevis

import com.nevis.client.OpenAiClient
import com.nevis.config.AppConfig
import com.nevis.config.DatabaseConfig
import com.nevis.config.errorHandler
import com.nevis.db.DatabaseClientRepository
import com.nevis.db.DatabaseDocumentRepository
import com.nevis.routes.clientRoutes
import com.nevis.routes.documentRoutes
import com.nevis.routes.documentationRoutes
import com.nevis.routes.searchRoutes
import com.nevis.service.DocumentService
import com.nevis.service.SearchService
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Credentials
import org.http4k.core.then
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("com.nevis.Main")

fun main() {
    val config = AppConfig.fromEnvironment()

    DatabaseConfig.init(config.database)
    val clients = DatabaseClientRepository()
    val documents = DatabaseDocumentRepository()
    val llm = OpenAiClient(http = OkHttp(), apiKey = config.openAi.apiKey)

    val documentService = DocumentService(clients, documents, llm, config.chunking)
    val searchService = SearchService(clients, documents, llm, llm, config.search)

    val requestLogger = ResponseFilters.ReportHttpTransaction { tx ->
        log.info("{} {} -> {} ({}ms)", tx.request.method, tx.request.uri, tx.response.status.code, tx.duration.toMillis())
    }

    val basicAuth = ServerFilters.BasicAuth("nevis", Credentials(config.auth.username, config.auth.password))

    val appRoutes = routes(
        "/status" bind GET to { Response(OK).body("OK") },
        documentationRoutes(),
        basicAuth.then(routes(
            clientRoutes(clients),
            documentRoutes(documentService),
            searchRoutes(searchService),
        )),
    )

    val app = requestLogger.then(errorHandler).then(appRoutes)
    val server = app.asServer(Undertow(config.serverPort)).start()
    log.info("Nevis Search API started on port {}", config.serverPort)
    server.block()
}
