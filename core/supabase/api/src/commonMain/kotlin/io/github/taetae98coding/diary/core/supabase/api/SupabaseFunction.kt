package io.github.taetae98coding.diary.core.supabase.api

import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlin.time.Duration

public interface SupabaseFunction {
    // 실패 상태의 응답은 구현이 SupabaseFunctionException으로 던지므로, 반환된 응답은 항상 성공 상태다.
    public suspend operator fun <T : Any> invoke(
        function: String,
        body: T,
        typeInfo: TypeInfo,
        headers: Headers = Headers.Empty,
        requestTimeout: Duration? = null,
    ): HttpResponse
}

public suspend inline operator fun <reified T : Any> SupabaseFunction.invoke(
    function: String,
    body: T,
    headers: Headers = Headers.Empty,
    requestTimeout: Duration? = null,
): HttpResponse =
    invoke(
        function = function,
        body = body,
        typeInfo = typeInfo<T>(),
        headers = headers,
        requestTimeout = requestTimeout,
    )
