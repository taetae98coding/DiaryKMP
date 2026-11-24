package io.github.taetae98coding.diary.data.setting.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiModelRemoteDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.data.setting.mapper.toDomain
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.repository.GeminiModelRepository

@Inject
@ContributesBinding(AppScope::class)
internal class GeminiModelRepositoryImpl(
    private val geminiModelRemoteDataSource: GeminiModelRemoteDataSource,
) : GeminiModelRepository {
    override suspend fun fetch(apiKey: String): List<GeminiModel> =
        try {
            geminiModelRemoteDataSource
                .readAvailableModel(apiKey = apiKey)
                .map { remote -> remote.toDomain() }
        } catch (exception: GeminiException.InvalidApiKey) {
            throw GeminiApiKeyInvalidException(cause = exception)
        }
}
