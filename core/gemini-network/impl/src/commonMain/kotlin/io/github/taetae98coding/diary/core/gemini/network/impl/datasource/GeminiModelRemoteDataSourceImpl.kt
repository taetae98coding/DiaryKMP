package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiModelRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.ListModelsResponseRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.toGeminiExceptionOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import org.koin.core.annotation.Factory

@Factory
internal class GeminiModelRemoteDataSourceImpl(
    @GeminiHttpClient
    private val httpClient: HttpClient,
) : GeminiModelRemoteDataSource {
    override suspend fun getAvailableModel(apiKey: String): List<GeminiModelRemoteEntity> {
        val response =
            try {
                httpClient
                    .get(MODELS_PATH) {
                        header(API_KEY_HEADER, apiKey)
                        parameter(PAGE_SIZE_PARAMETER, MAX_PAGE_SIZE)
                    }.body<ListModelsResponseRemoteEntity>()
            } catch (cause: ResponseException) {
                throw cause.toGeminiExceptionOrNull() ?: cause
            }

        return response.models.filter { model -> model.isAvailable() }
    }

    private fun GeminiModelRemoteEntity.isAvailable(): Boolean = GENERATE_CONTENT in supportedGenerationMethods

    private companion object {
        private const val MODELS_PATH = "models"
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val PAGE_SIZE_PARAMETER = "pageSize"
        private const val MAX_PAGE_SIZE = 1000
        private const val GENERATE_CONTENT = "generateContent"
    }
}
