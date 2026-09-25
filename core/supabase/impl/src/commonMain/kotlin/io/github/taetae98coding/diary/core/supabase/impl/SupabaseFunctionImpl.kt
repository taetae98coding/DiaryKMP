package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.functions
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
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
                    timeout { requestTimeoutMillis = requestTimeout.inWholeMilliseconds }
                }
                setBody(body, typeInfo)
            }
        } catch (exception: RestException) {
            throw SupabaseFunctionException(statusCode = exception.statusCode, message = exception.message, cause = exception)
        }
    }
}
