package com.nevis.routes

import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

val swaggerHtml = resource("/static/swagger-ui.html")
val openapiYaml = resource("/openapi-spec.yaml")

fun documentationRoutes(): RoutingHttpHandler = routes(
    "/docs" bind GET to {
        Response(OK)
            .header("Content-Type", TEXT_HTML.value)
            .body(swaggerHtml)
    },
    "/docs/openapi.yaml" bind GET to {
        Response(OK)
            .header("Content-Type", "text/yaml")
            .body(openapiYaml)
    },
)

private fun resource(path: String): String =
    object {}.javaClass.getResource(path)?.readText()
        ?: error("Resource not found: $path")
