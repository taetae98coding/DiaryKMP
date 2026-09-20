package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.data.memo.mapper.memoDraftResponseSchema
import io.github.taetae98coding.diary.data.memo.mapper.toMemoDraft
import io.github.taetae98coding.diary.data.memo.mapper.toPrompt
import io.github.taetae98coding.diary.domain.memo.repository.MemoDraftRepository
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import org.koin.core.annotation.Factory

@Factory
internal class MemoDraftRepositoryImpl(
    private val geminiContentRemoteDataSource: GeminiContentRemoteDataSource,
) : MemoDraftRepository {
    override suspend fun fetch(
        setting: GeminiSetting,
        request: MemoDraftRequest,
    ): MemoDraft =
        try {
            geminiContentRemoteDataSource
                .generateStructuredContent(
                    apiKey = setting.apiKey,
                    model = setting.model,
                    systemInstruction = setting.systemPrompt,
                    prompt = request.toPrompt(),
                    responseSchema = memoDraftResponseSchema,
                ).toMemoDraft()
        } catch (exception: GeminiException.InvalidApiKey) {
            throw GeminiApiKeyInvalidException(cause = exception)
        }
}
