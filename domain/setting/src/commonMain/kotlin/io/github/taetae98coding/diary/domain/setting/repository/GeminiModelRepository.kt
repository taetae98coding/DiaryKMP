package io.github.taetae98coding.diary.domain.setting.repository

import io.github.taetae98coding.diary.core.model.gemini.GeminiModel

public interface GeminiModelRepository {
    public suspend fun fetch(apiKey: String): List<GeminiModel>
}
