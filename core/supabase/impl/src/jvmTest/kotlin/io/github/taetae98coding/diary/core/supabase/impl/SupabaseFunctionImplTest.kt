package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.util.reflect.typeInfo
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.time.Duration.Companion.minutes

class SupabaseFunctionImplTest :
    FunSpec({
        test("실패 상태의 응답은 상태 코드를 담은 예외로 알린다") {
            listOf(HttpStatusCode.PayloadTooLarge, HttpStatusCode.BadRequest, HttpStatusCode.InternalServerError).forEach { status ->
                val client = createSupabaseClient(MockEngine { respond(content = "{}", status = status) })
                val function = SupabaseFunctionImpl(client = client)

                val exception =
                    shouldThrow<SupabaseFunctionException> {
                        function(
                            function = "v1-test",
                            body = ByteArrayContent(bytes = byteArrayOf(), contentType = ContentType.Application.OctetStream),
                            typeInfo = typeInfo<ByteArrayContent>(),
                        )
                    }

                exception.statusCode shouldBe status.value
                client.close()
            }
        }

        test("요청 시간 제한을 따로 정해도 성공 응답을 그대로 돌려준다") {
            val client = createSupabaseClient(MockEngine { respondOk() })
            val function = SupabaseFunctionImpl(client = client)

            val response =
                function(
                    function = "v1-test",
                    body = ByteArrayContent(bytes = byteArrayOf(), contentType = ContentType.Application.OctetStream),
                    typeInfo = typeInfo<ByteArrayContent>(),
                    requestTimeout = 5.minutes,
                )

            response.status shouldBe HttpStatusCode.OK
            client.close()
        }
    }) {
    companion object {
        private fun createSupabaseClient(engine: HttpClientEngine): SupabaseClient =
            koinApplication<SupabaseTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<SupabaseHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
