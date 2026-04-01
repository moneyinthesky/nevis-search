package com.nevis.service

import com.nevis.client.EmbeddingClient
import com.nevis.client.SummarizationClient
import com.nevis.config.SearchSettings
import com.nevis.db.ClientRepository
import com.nevis.db.DocumentRepository
import com.nevis.model.ClientResult
import com.nevis.model.DocumentHit
import com.nevis.model.SearchResult
import com.nevis.util.mapConcurrently
import com.nevis.util.runConcurrently

class SearchService(
    private val clients: ClientRepository,
    private val documents: DocumentRepository,
    private val embedder: EmbeddingClient,
    private val summarizer: SummarizationClient,
    private val settings: SearchSettings = SearchSettings(),
) {
    fun search(query: String, includeSummaries: Boolean): List<SearchResult> {
        val embedding = embedder.embed(query)

        val (clientResults, documentHits) = runConcurrently(
            { clients.search(query) },
            { documents.search(embedding, includeContent = includeSummaries, threshold = settings.documentThreshold) },
        )

        val scoredClients = clientResults.map { it.withScore(query) }
        val scoredDocuments = documentHits
            .map { it.withScore() }
            .let { if (includeSummaries) it.withSummaries() else it.map { hit -> hit.result } }

        return (scoredClients + scoredDocuments).sortedByDescending { it.score }
    }

    private fun ClientResult.withScore(query: String): ClientResult {
        // Using the scoring strategy that results in the best score
        val bestScore = clientScoringRules(this, query)
            .filter { (matched, _) -> matched }
            .maxOfOrNull { (_, score) -> score } ?: 0.0
        return this.copy(score = bestScore)
    }

    private fun DocumentHit.withScore(): DocumentHit =
        copy(result = result.copy(score = scoreDocument(this)))

    // Calculating document score for ordering using cosine distance, then normalizing for comparison with clients
    private fun scoreDocument(hit: DocumentHit): Double =
        (1.0 - (hit.cosineDistance / settings.documentThreshold)).coerceIn(0.0, 1.0)

    // Calling the summarizer in batches to improve performance without overwhelming the LLM
    private fun List<DocumentHit>.withSummaries() = mapConcurrently(batchSize = settings.summaryBatchSize) { hit ->
        hit.result.copy(summary = hit.content?.let { summarizer.summarize(it) })
    }

    companion object {

        private fun nameExactMatch(r: ClientResult, q: String) = r.firstName.equals(q, ignoreCase = true)
                || r.lastName.equals(q, ignoreCase = true)
        private fun namePrefixMatch(r: ClientResult, q: String) = r.firstName.startsWith(q, ignoreCase = true)
                || r.lastName.startsWith(q, ignoreCase = true)
        private fun nameContainsMatch(r: ClientResult, q: String) = r.firstName.contains(q, ignoreCase = true)
                || r.lastName.contains(q, ignoreCase = true)
        private fun emailMatch(r: ClientResult, q: String) = r.email.contains(q, ignoreCase = true)
        private fun descriptionMatch(r: ClientResult, q: String) = r.description?.contains(q, ignoreCase = true) == true

        // For client scoring, giving exact matches and name matches a higher score
        private fun clientScoringRules(clientResult: ClientResult, query: String) = listOf(
            nameExactMatch(clientResult, query) to 0.95,
            namePrefixMatch(clientResult, query) to 0.85,
            nameContainsMatch(clientResult, query) to 0.70,
            emailMatch(clientResult, query) to 0.75,
            descriptionMatch(clientResult, query) to 0.60,
        )
    }
}
