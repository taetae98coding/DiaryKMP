package io.github.taetae98coding.diary.core.gemini.network.impl

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.ErrorResponseRemoteEntity
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

internal suspend fun ResponseException.toGeminiExceptionOrNull(json: Json): GeminiException? =
    when {
        response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden -> GeminiException.InvalidApiKey(cause = this)

        // Gemini는 유효하지 않은 API 키를 400 INVALID_ARGUMENT로 돌려주고 ErrorInfo의 reason으로만 구분한다.
        response.status == HttpStatusCode.BadRequest && hasInvalidApiKeyReason(json) -> GeminiException.InvalidApiKey(cause = this)

        else -> null
    }

private suspend fun ResponseException.hasInvalidApiKeyReason(json: Json): Boolean {
    val errorResponse =
        try {
            json.decodeFromString<ErrorResponseRemoteEntity>(response.bodyAsText())
        } catch (_: IllegalArgumentException) {
            return false
        }

    return errorResponse.error.details.any { detail -> detail.reason == API_KEY_INVALID_REASON }
}

private const val API_KEY_INVALID_REASON = "API_KEY_INVALID"
