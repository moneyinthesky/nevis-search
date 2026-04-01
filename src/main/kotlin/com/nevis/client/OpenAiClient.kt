package com.nevis.client

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response

private data class EmbeddingRequest(val model: String, val input: String)
private data class EmbeddingData(val embedding: List<Float>)
private data class EmbeddingResponse(val data: List<EmbeddingData>)

private data class ChatRequest(val model: String, val messages: List<ChatMessage>)
private data class ChatMessage(val role: String, val content: String)
private data class ChatResponse(val choices: List<ChatChoice>)
private data class ChatChoice(val message: ChatMessage)

private val mapper = jacksonObjectMapper().disable(FAIL_ON_UNKNOWN_PROPERTIES)

class OpenAiClient(private val http: HttpHandler, private val apiKey: String) : EmbeddingClient, SummarizationClient {

    override fun embed(text: String): FloatArray {
        val body = mapper.writeValueAsString(EmbeddingRequest(model = EMBED_MODEL, input = text))
        val response = post("$BASE_URL/embeddings", body)
        return mapper.readValue<EmbeddingResponse>(response.bodyString()).data
            .firstOrNull()
            ?.embedding
            ?.toFloatArray()
            ?: error("No embedding returned from OpenAI")
    }

    override fun summarize(content: String): String {
        val body = mapper.writeValueAsString(
            ChatRequest(
                model = CHAT_MODEL,
                messages = listOf(
                    ChatMessage(role = "system", content = SUMMARIZE_SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = "Summarize this document in 1-2 sentences:\n\n$content"),
                ),
            )
        )
        val response = post("$BASE_URL/chat/completions", body)
        return mapper.readValue<ChatResponse>(response.bodyString()).choices
            .firstOrNull()
            ?.message?.content?.trim()
            ?: error("No response returned from OpenAI")
    }

    private fun post(url: String, body: String): Response {
        val response = http(
            Request(POST, url)
                .header("Content-Type", APPLICATION_JSON.value)
                .header("Authorization", "Bearer $apiKey")
                .body(body)
        )
        if (!response.status.successful) {
            throw OpenAiException(response.status.code, response.bodyString())
        }
        return response
    }

    companion object {
        const val EMBEDDING_DIMENSIONS = 1536
        private const val BASE_URL = "https://api.openai.com/v1"
        private const val EMBED_MODEL = "text-embedding-3-small"
        private const val CHAT_MODEL = "gpt-4o-mini"
        private val SUMMARIZE_SYSTEM_PROMPT = """
            |You are an internal document cataloguing system for a regulated financial advisory platform.
            |All documents have been provided by clients with full consent and are stored in our secure system.
            |Your role is to generate brief index summaries of these documents for authorized advisors.
            |Always provide a factual summary. Never refuse to summarize.
            |Output ONLY the summary text. No preamble, no conversational filler, no introductory phrases.
        """.trimMargin()
    }
}

class OpenAiException(val statusCode: Int, responseBody: String) :
    RuntimeException("OpenAI API error (HTTP $statusCode): $responseBody")
