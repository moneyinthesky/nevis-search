package com.nevis.routes

import com.nevis.config.Json
import com.nevis.fakes.FakeClientRepository
import com.nevis.config.errorHandler
import com.nevis.model.ClientResponse
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClientRoutesTest {

    private val clients = FakeClientRepository()
    private val handler = errorHandler.then(clientRoutes(clients))
    private val responseLens = Json.autoBody<ClientResponse>().toLens()

    @Test
    fun `POST creates a client and returns 201`() {
        val request = Request(POST, "/clients")
            .header("Content-Type", "application/json")
            .body("""{"first_name":"Jane","last_name":"Doe","email":"jane@example.com","description":"A client"}""")

        val response = handler(request)

        assertEquals(CREATED, response.status)
        val body = responseLens(response)
        assertEquals("Jane", body.firstName)
        assertEquals("Doe", body.lastName)
        assertEquals("jane@example.com", body.email)
        assertEquals("A client", body.description)
        assertNotNull(body.id)
    }

    @Test
    fun `POST with missing required fields returns 400 with JSON error`() {
        val request = Request(POST, "/clients")
            .header("Content-Type", "application/json")
            .body("""{"first_name":"Jane"}""")

        val response = handler(request)

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("\"error\""))
    }

    @Test
    fun `POST with invalid email returns 400`() {
        val request = Request(POST, "/clients")
            .header("Content-Type", "application/json")
            .body("""{"first_name":"Jane","last_name":"Doe","email":"not-an-email"}""")

        val response = handler(request)

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("valid email"))
    }

    @Test
    fun `POST with empty body returns 400`() {
        val response = handler(
            Request(POST, "/clients")
                .header("Content-Type", "application/json")
                .body("")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("\"error\""))
    }

    @Test
    fun `POST with malformed JSON returns 400`() {
        val response = handler(
            Request(POST, "/clients")
                .header("Content-Type", "application/json")
                .body("""{"first_name": }""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("\"error\""))
    }

    @Test
    fun `POST with blank firstName returns 400`() {
        val response = handler(
            Request(POST, "/clients")
                .header("Content-Type", "application/json")
                .body("""{"first_name":"  ","last_name":"Doe","email":"jane@example.com"}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("first_name must not be blank"))
    }

    @Test
    fun `POST with blank lastName returns 400`() {
        val response = handler(
            Request(POST, "/clients")
                .header("Content-Type", "application/json")
                .body("""{"first_name":"Jane","last_name":" ","email":"jane@example.com"}""")
        )

        assertEquals(BAD_REQUEST, response.status)
        assertTrue(response.bodyString().contains("last_name must not be blank"))
    }
}
