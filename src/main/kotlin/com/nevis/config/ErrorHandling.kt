package com.nevis.config

import com.nevis.model.ErrorResponse
import com.nevis.service.ClientNotFoundException
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.filter.ServerFilters
import org.http4k.lens.LensFailure

val errorHandler = ServerFilters.CatchAll {
    when (it) {
        is LensFailure -> jsonError(BAD_REQUEST, it.message.orEmpty())
        is IllegalArgumentException -> jsonError(BAD_REQUEST, it.message.orEmpty())
        is ClientNotFoundException -> jsonError(NOT_FOUND, "Client ${it.clientId} not found")
        else -> jsonError(INTERNAL_SERVER_ERROR, "Internal server error")
    }
}

fun jsonError(status: org.http4k.core.Status, message: String): Response =
    Response(status)
        .header("Content-Type", "application/json")
        .body(Json.asFormatString(ErrorResponse(message)))
