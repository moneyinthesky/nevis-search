package com.nevis.client

interface EmbeddingClient {
    fun embed(text: String): FloatArray
}

interface SummarizationClient {
    fun summarize(content: String): String
}
