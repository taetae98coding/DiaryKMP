package io.github.taetae98coding.diary.core.gemini.network.impl

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

// Gemini API 오류 응답 형식. https://ai.google.dev/gemini-api/docs/generate-content/api-errors
internal fun badRequestEngine(reason: String): MockEngine =
    MockEngine {
        respond(
            content = badRequestErrorResponse(reason = reason),
            status = HttpStatusCode.BadRequest,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

private fun badRequestErrorResponse(reason: String): String =
    buildJsonObject {
        putJsonObject("error") {
            put("code", HttpStatusCode.BadRequest.value)
            put("message", "Bad request")
            put("status", "INVALID_ARGUMENT")
            putJsonArray("details") {
                addJsonObject {
                    put("@type", "type.googleapis.com/google.rpc.ErrorInfo")
                    put("reason", reason)
                    put("domain", "googleapis.com")
                    putJsonObject("metadata") { put("service", "generativelanguage.googleapis.com") }
                }
                addJsonObject {
                    put("@type", "type.googleapis.com/google.rpc.LocalizedMessage")
                    put("locale", "en-US")
                    put("message", "Bad request")
                }
            }
        }
    }.toString()

internal const val API_KEY_INVALID_REASON: String = "API_KEY_INVALID"
