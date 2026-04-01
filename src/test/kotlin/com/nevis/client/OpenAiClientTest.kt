package com.nevis.client

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OpenAiClientTest {

    @Test
    fun `embed throws when no embeddings returned`() {
        val http = { _: Request ->
            Response(OK)
                .header("Content-Type", "application/json")
                .body("""{"data":[]}""")
        }
        val client = OpenAiClient(http, "fake-key")

        val error = assertFailsWith<IllegalStateException> {
            client.embed("hello")
        }
        assertTrue(error.message!!.contains("No embedding returned"))
    }

    @Test
    fun `embed throws OpenAiException on non-success response`() {
        val http = { _: Request ->
            Response(INTERNAL_SERVER_ERROR).body("Service unavailable")
        }
        val client = OpenAiClient(http, "fake-key")

        val error = assertFailsWith<OpenAiException> {
            client.embed("hello")
        }
        assertEquals(500, error.statusCode)
        assertTrue(error.message!!.contains("Service unavailable"))
    }

    @Test
    fun `summarize throws when no choices returned`() {
        val http = { _: Request ->
            Response(OK)
                .header("Content-Type", "application/json")
                .body("""{"choices":[]}""")
        }
        val client = OpenAiClient(http, "fake-key")

        val error = assertFailsWith<IllegalStateException> {
            client.summarize("some document content")
        }
        assertTrue(error.message!!.contains("No response returned"))
    }

    @Test
    fun `summarize throws OpenAiException on non-success response`() {
        val http = { _: Request ->
            Response(INTERNAL_SERVER_ERROR).body("Service unavailable")
        }
        val client = OpenAiClient(http, "fake-key")

        val error = assertFailsWith<OpenAiException> {
            client.summarize("some document content")
        }
        assertEquals(500, error.statusCode)
    }
}
