package io.github.taetae98coding.diary.core.gemini.network.impl

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode

internal fun ResponseException.toGeminiExceptionOrNull(): GeminiException? =
    when (response.status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> GeminiException.InvalidApiKey(cause = this)
        else -> null
    }
