package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.util.reflect.TypeInfo
import org.koin.core.annotation.Factory

@Factory
internal class SupabaseFunctionImpl(
    private val client: SupabaseClient,
) : SupabaseFunction {
    override suspend fun <T : Any> invoke(
        function: String,
        body: T,
        typeInfo: TypeInfo,
        headers: Headers,
    ): HttpResponse {
        val headers =
            Headers.build {
                headers.forEach { key, value -> appendAll(key, value) }
                if (body !is OutgoingContent && !contains(HttpHeaders.ContentType)) {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }

        return client.functions(function) {
            this.headers.appendAll(headers)
            setBody(body, typeInfo)
        }
    }
}
