package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionRequest
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.util.reflect.TypeInfo
import org.koin.core.annotation.Factory
import kotlin.time.Duration

@Factory
internal class SupabaseFunctionImpl(
    private val client: SupabaseClient,
) : SupabaseFunction {
    override suspend fun <T : Any> invoke(
        function: String,
        body: T,
        typeInfo: TypeInfo,
        headers: Headers,
        requestTimeout: Duration?,
    ): HttpResponse {
        val headers =
            Headers.build {
                headers.forEach { key, value -> appendAll(key, value) }
                if (body !is OutgoingContent && !contains(HttpHeaders.ContentType)) {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }

        // RestException은 supabase-kt의 타입이라 호출하는 core 모듈이 상태 코드로 실패를 가를 수 있게 이 모듈의 예외로 바꾼다.
        return try {
            client.functions(function) {
                this.headers.appendAll(headers)
                if (requestTimeout != null) {
                    timeout {
                        requestTimeoutMillis = requestTimeout.toTimeoutMillis()
                        // 엔진은 소켓 제한이 없으면 자기 기본값(OkHttp는 10초)을 쓰므로, 제한을 풀 때는 소켓 제한도 함께 푼다.
                        if (requestTimeout.isInfinite()) {
                            socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                        }
                    }
                }
                setBody(body, typeInfo)
            }
        } catch (exception: RestException) {
            throw SupabaseFunctionException(statusCode = exception.statusCode, message = exception.message, cause = exception)
        }
    }

    // 로그인하지 않았으면 함수 호출과 같이 프로젝트 키로 인증한다.
    override suspend fun createRequest(function: String): SupabaseFunctionRequest {
        val accessToken = client.auth.currentAccessTokenOrNull() ?: client.supabaseKey

        return SupabaseFunctionRequest(
            url = client.functions.resolveUrl(function),
            headers =
                mapOf(
                    API_KEY_HEADER to client.supabaseKey,
                    HttpHeaders.Authorization to "Bearer $accessToken",
                ),
        )
    }

    private companion object {
        const val API_KEY_HEADER: String = "apikey"
    }
}

private fun Duration.toTimeoutMillis(): Long = if (isInfinite()) HttpTimeoutConfig.INFINITE_TIMEOUT_MS else inWholeMilliseconds
