package io.github.taetae98coding.diary.core.gemini.network.api.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration

public interface GeminiContentRemoteDataSource {
    public fun streamStructuredContent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        prompt: String,
        responseJsonSchema: JsonObject,
        idleTimeout: Duration,
    ): Flow<String>
}
