package io.github.taetae98coding.diary.core.gemini.network.api.datasource

import kotlinx.serialization.json.JsonObject

public interface GeminiContentRemoteDataSource {
    public suspend fun generateStructuredContent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        prompt: String,
        responseSchema: JsonObject,
    ): JsonObject
}
