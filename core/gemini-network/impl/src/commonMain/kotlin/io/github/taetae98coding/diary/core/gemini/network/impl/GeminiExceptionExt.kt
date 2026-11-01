package io.github.taetae98coding.diary.core.gemini.network.impl

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.ErrorResponseRemoteEntity
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

internal suspend fun ResponseException.toGeminiExceptionOrNull(json: Json): GeminiException? = toGeminiException(status = response.status, body = response.bodyAsText(), json = json, cause = this) as? GeminiException.InvalidApiKey

internal fun toGeminiException(
    status: HttpStatusCode,
    body: String,
    json: Json,
    cause: Throwable?,
): GeminiException {
    val error = json.decodeErrorOrNull(body)

    return when {
        status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden -> GeminiException.InvalidApiKey(cause = cause)

        // Gemini는 유효하지 않은 API 키를 400 INVALID_ARGUMENT로 돌려주고 ErrorInfo의 reason으로만 구분한다.
        status == HttpStatusCode.BadRequest && error?.details.orEmpty().any { detail -> detail.reason == API_KEY_INVALID_REASON } -> GeminiException.InvalidApiKey(cause = cause)

        status == HttpStatusCode.TooManyRequests || error?.status == RESOURCE_EXHAUSTED_STATUS -> GeminiException.QuotaExceeded(cause = cause)

        status == HttpStatusCode.NotFound -> GeminiException.ModelNotFound(cause = cause)

        status.value in SERVER_ERROR_RANGE -> GeminiException.Server(cause = cause)

        else -> GeminiException.Other(description = error?.message.orEmpty(), cause = cause)
    }
}

private fun Json.decodeErrorOrNull(body: String) =
    try {
        decodeFromString<ErrorResponseRemoteEntity>(body).error
    } catch (_: IllegalArgumentException) {
        null
    }

private const val API_KEY_INVALID_REASON = "API_KEY_INVALID"
private const val RESOURCE_EXHAUSTED_STATUS = "RESOURCE_EXHAUSTED"
private val SERVER_ERROR_RANGE = 500..599
