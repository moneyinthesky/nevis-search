package com.nevis.config

data class AppConfig(
    val serverPort: Int,
    val database: DatabaseSettings,
    val openAi: OpenAiSettings,
    val auth: AuthSettings,
    val chunking: ChunkingSettings = ChunkingSettings(),
    val search: SearchSettings = SearchSettings(),
) {
    companion object {
        fun fromEnvironment() = AppConfig(
            serverPort = env("SERVER_PORT", "8080").toInt(),
            database = DatabaseSettings(
                url = env("DB_URL", "jdbc:postgresql://localhost:5432/nevis"),
                user = env("DB_USER", "nevis"),
                password = env("DB_PASSWORD", "nevis"),
            ),
            openAi = OpenAiSettings(
                apiKey = env("OPENAI_API_KEY"),
            ),
            auth = AuthSettings(
                username = env("BASIC_AUTH_USERNAME"),
                password = env("BASIC_AUTH_PASSWORD"),
            ),
            chunking = ChunkingSettings(
                chunkSize = env("CHUNK_SIZE", "500").toInt(),
                overlap = env("CHUNK_OVERLAP", "100").toInt(),
                embeddingBatchSize = env("EMBEDDING_BATCH_SIZE", "10").toInt(),
            ),
            search = SearchSettings(
                documentThreshold = env("DOCUMENT_THRESHOLD", "0.6").toFloat(),
                summaryBatchSize = env("SUMMARY_BATCH_SIZE", "10").toInt(),
            ),
        )

        private fun env(key: String, default: String): String =
            System.getenv(key) ?: default

        private fun env(key: String): String =
            System.getenv(key) ?: error("Required environment variable $key is not set")
    }
}

data class DatabaseSettings(
    val url: String,
    val user: String,
    val password: String,
)

data class OpenAiSettings(
    val apiKey: String,
)

data class AuthSettings(
    val username: String,
    val password: String,
)

data class ChunkingSettings(
    val chunkSize: Int = 500,
    val overlap: Int = 100,
    val embeddingBatchSize: Int = 10,
)

data class SearchSettings(
    val documentThreshold: Float = 0.6f,
    val summaryBatchSize: Int = 10,
)
