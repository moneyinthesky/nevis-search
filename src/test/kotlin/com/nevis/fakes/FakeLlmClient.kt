package com.nevis.fakes

import com.nevis.client.EmbeddingClient
import com.nevis.client.OpenAiClient.Companion.EMBEDDING_DIMENSIONS
import com.nevis.client.SummarizationClient

class FakeEmbeddingClient : EmbeddingClient {
    override fun embed(text: String): FloatArray = FloatArray(EMBEDDING_DIMENSIONS) { 0.1f }
}

class FakeSummarizationClient : SummarizationClient {
    override fun summarize(content: String): String = "Summary of: ${content.take(50)}"
}
