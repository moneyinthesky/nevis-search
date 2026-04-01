package com.nevis.routes

import com.nevis.config.Json
import com.nevis.service.SearchService
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.lens.nonBlankString
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

private val queryLens = Query.nonBlankString().required("q")
private val summaryLens = Query.boolean().defaulted("summary", false)

fun searchRoutes(searchService: SearchService): RoutingHttpHandler =
    "/search" bind GET to { request ->
        val query = queryLens(request).lowercase()
        val includeSummaries = summaryLens(request)

        val results = searchService.search(query, includeSummaries)
        Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.asFormatString(results))
    }
