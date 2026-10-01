package io.github.taetae98coding.diary.core.supabase.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.TextContent
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun functionName(): String = "v1-${fixtureMonkey.giveMeOne<Int>().absoluteValue}"

class SupabaseFunctionImplTest :
    FunSpec({
        test("실패 상태의 응답은 상태 코드를 담은 예외로 알린다") {
            listOf(HttpStatusCode.PayloadTooLarge, HttpStatusCode.BadRequest, HttpStatusCode.InternalServerError).forEach { status ->
                val client = createSupabaseClient(MockEngine { respond(content = "{}", status = status) })
                val function = SupabaseFunctionImpl(client = client)

                val exception =
                    shouldThrow<SupabaseFunctionException> {
                        function(
                            function = functionName(),
                            body = ByteArrayContent(bytes = byteArrayOf(), contentType = ContentType.Application.OctetStream),
                            typeInfo = typeInfo<ByteArrayContent>(),
                        )
                    }

                exception.statusCode shouldBe status.value
                client.close()
            }
        }

        test("TC-FILE-STORAGE-DATA-013 요청 시간 제한을 풀면 기본 시간 제한보다 늦은 응답도 기다려 돌려준다") {
            runTest {
                val dispatcher = StandardTestDispatcher(testScheduler)
                val client = createSupabaseClient(slowEngine(dispatcher = dispatcher, delay = 30.minutes))
                val function = SupabaseFunctionImpl(client = client)

                val response =
                    function(
                        function = functionName(),
                        body = ByteArrayContent(bytes = byteArrayOf(), contentType = ContentType.Application.OctetStream),
                        typeInfo = typeInfo<ByteArrayContent>(),
                        requestTimeout = Duration.INFINITE,
                    )

                response.status shouldBe HttpStatusCode.OK
                client.close()
            }
        }

        test("TC-FILE-STORAGE-DATA-013 요청 시간 제한을 풀면 엔진의 소켓 시간 제한도 함께 푼다") {
            var capability: HttpTimeoutConfig? = null
            val client =
                createSupabaseClient(
                    MockEngine { request ->
                        capability = request.getCapabilityOrNull(HttpTimeoutCapability)
                        respondOk()
                    },
                )
            val function = SupabaseFunctionImpl(client = client)

            function(
                function = functionName(),
                body = ByteArrayContent(bytes = byteArrayOf(), contentType = ContentType.Application.OctetStream),
                typeInfo = typeInfo<ByteArrayContent>(),
                requestTimeout = Duration.INFINITE,
            )

            capability?.requestTimeoutMillis shouldBe HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            capability?.socketTimeoutMillis shouldBe HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            client.close()
        }

        test("요청 시간 제한을 따로 정하지 않으면 기본 시간 제한을 넘긴 응답은 기다리지 않는다") {
            runTest {
                val dispatcher = StandardTestDispatcher(testScheduler)
                val client = createSupabaseClient(slowEngine(dispatcher = dispatcher, delay = 30.minutes))
                val function = SupabaseFunctionImpl(client = client)

                shouldThrowAny {
                    function(
                        function = functionName(),
                        body = ByteArrayContent(bytes = byteArrayOf(), contentType = ContentType.Application.OctetStream),
                        typeInfo = typeInfo<ByteArrayContent>(),
                    )
                }
                client.close()
            }
        }

        test("로그인했으면 세션의 접근 정보로 인증하고 본문을 JSON으로 보낸다") {
            var request: HttpRequestData? = null
            val client =
                createSupabaseClient(
                    supabaseUrl = "https://example.supabase.co",
                    supabaseKey = "test-key",
                ) {
                    httpEngine =
                        MockEngine { data ->
                            request = data
                            respondOk()
                        }
                    install(Auth) {
                        minimalConfig()
                        sessionManager = MemorySessionManager()
                    }
                    install(Functions)
                }
            client.auth.awaitInitialization()
            client.auth.importSession(
                session = UserSession(accessToken = "access-token", refreshToken = "refresh-token", expiresIn = 3600, tokenType = "bearer"),
                autoRefresh = false,
            )
            val function = SupabaseFunctionImpl(client = client)

            function(
                function = functionName(),
                body = JsonObject(mapOf("token" to JsonPrimitive("token-a"))),
                typeInfo = typeInfo<JsonObject>(),
            )

            request?.headers?.get(HttpHeaders.Authorization) shouldBe "Bearer access-token"
            (request?.body as? TextContent)?.text shouldBe """{"token":"token-a"}"""
            client.close()
        }

        test("로그인하지 않았으면 함수 주소와 프로젝트 키로 인증하는 헤더를 만든다") {
            val client = createSupabaseClient(MockEngine { respondOk() })
            val function = SupabaseFunctionImpl(client = client)

            val functionName = functionName()
            val request = function.createRequest(function = functionName)

            request.url shouldBe "https://example.supabase.co/functions/v1/$functionName"
            request.headers shouldBe
                mapOf(
                    "apikey" to "test-key",
                    HttpHeaders.Authorization to "Bearer test-key",
                )
            client.close()
        }
    }) {
    companion object {
        private fun slowEngine(
            dispatcher: CoroutineDispatcher,
            delay: Duration,
        ): HttpClientEngine =
            MockEngine(
                MockEngineConfig().apply {
                    this.dispatcher = dispatcher
                    addHandler {
                        delay(delay)
                        respondOk()
                    }
                },
            )

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
