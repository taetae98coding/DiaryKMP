package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.ContentRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.GenerateContentRequestRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.GenerateContentResponseRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.GenerationConfigRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.PartRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.toGeminiExceptionOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.koin.core.annotation.Factory

@Factory
internal class GeminiContentRemoteDataSourceImpl(
    @GeminiHttpClient
    private val httpClient: HttpClient,
    @GeminiJson
    private val json: Json,
) : GeminiContentRemoteDataSource {
    override suspend fun generateStructuredContent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        prompt: String,
        responseSchema: JsonObject,
    ): JsonObject {
        val response =
            try {
                httpClient
                    .post("$model:$GENERATE_CONTENT_METHOD") {
                        header(API_KEY_HEADER, apiKey)
                        setBody(
                            GenerateContentRequestRemoteEntity(
                                contents = listOf(prompt.toContent()),
                                generationConfig =
                                    GenerationConfigRemoteEntity(
                                        responseMimeType = JSON_MIME_TYPE,
                                        responseSchema = responseSchema,
                                    ),
                                systemInstruction = systemInstruction.takeIf { it.isNotBlank() }?.toContent(),
                            ),
                        )
                    }.body<GenerateContentResponseRemoteEntity>()
            } catch (cause: ResponseException) {
                throw cause.toGeminiExceptionOrNull() ?: cause
            }

        return response.toStructuredContent()
    }

    private fun String.toContent(): ContentRemoteEntity = ContentRemoteEntity(parts = listOf(PartRemoteEntity(text = this)))

    private fun GenerateContentResponseRemoteEntity.toStructuredContent(): JsonObject {
        val text =
            candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.joinToString(separator = "") { part -> part.text }
                .orEmpty()

        // 모델이 요청한 스키마를 지키지 않으면 빈 본문이나 JSON이 아닌 본문이 오므로 파싱 실패를 그대로 구분해 알린다.
        return try {
            json.parseToJsonElement(text).jsonObject
        } catch (exception: IllegalArgumentException) {
            throw GeminiException.InvalidContent(cause = exception)
        }
    }

    private companion object {
        private const val GENERATE_CONTENT_METHOD = "generateContent"
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val JSON_MIME_TYPE = "application/json"
    }
}
