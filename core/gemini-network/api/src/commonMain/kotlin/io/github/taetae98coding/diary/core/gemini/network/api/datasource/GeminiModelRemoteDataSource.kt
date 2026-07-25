package io.github.taetae98coding.diary.core.gemini.network.api.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity

public interface GeminiModelRemoteDataSource {
    public suspend fun getAvailableModel(apiKey: String): List<GeminiModelRemoteEntity>
}
