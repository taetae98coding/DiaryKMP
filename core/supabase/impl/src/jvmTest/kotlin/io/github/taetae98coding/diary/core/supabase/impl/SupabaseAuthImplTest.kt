package io.github.taetae98coding.diary.core.supabase.impl

import app.cash.turbine.test
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.Uuid

class SupabaseAuthImplTest :
    FunSpec({
        test("인증된 세션을 알리면 저장소가 아니라 알림에 담긴 프로필 이미지를 사용한다") {
            val storedUser = user(profileImage = "https://example.com/stored.jpg")
            val client = createClient(sessionManager = MemorySessionManager(session(storedUser)))

            client.auth.awaitInitialization()
            client.auth.importSession(session(storedUser.withProfileImage("https://example.com/notified.jpg")))

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem()?.profileImage shouldBe "https://example.com/notified.jpg"
            }

            client.close()
        }

        test("인증되지 않은 세션을 알리면 저장된 프로필 이미지를 사용한다") {
            val storedUser = user(profileImage = "https://example.com/stored.jpg")
            val client = createClient(sessionManager = MemorySessionManager(session(storedUser)))

            client.auth.awaitInitialization()

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem()?.profileImage shouldBe "https://example.com/stored.jpg"
            }

            client.close()
        }

        test("앱 메타데이터에 모인 연결된 로그인 수단을 사용자 정보에 담는다") {
            val storedUser =
                user(profileImage = "https://example.com/stored.jpg")
                    .copy(appMetadata = JsonObject(mapOf("providers" to JsonArray(listOf(JsonPrimitive("google"), JsonPrimitive("apple"))))))
            val client = createClient(sessionManager = MemorySessionManager(session(storedUser)))

            client.auth.awaitInitialization()

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem()?.providerSet shouldBe setOf("google", "apple")
            }

            client.close()
        }

        test("저장된 세션도 인증된 세션도 없으면 사용자 정보를 알리지 않는다") {
            val client = createClient(sessionManager = MemorySessionManager())

            client.auth.awaitInitialization()

            SupabaseAuthImpl(client).getUserFlow().test {
                awaitItem() shouldBe null
            }

            client.close()
        }

        test("TC-fcce04af-8204-49c6-8248-420b10fad20c 연결이 끊겨도 서버 요청 없이 세션을 지운다") {
            val engine = MockEngine { throw IOException("연결 실패") }
            val sessionManager = MemorySessionManager()
            val client = createClient(sessionManager = sessionManager, engine = engine)

            client.auth.awaitInitialization()
            client.auth.importSession(session(user(profileImage = "https://example.com/stored.jpg")), autoRefresh = false)

            SupabaseAuthImpl(client).signOut()

            engine.requestHistory.shouldBeEmpty()
            sessionManager.loadSessionOrNull().shouldBeNull()
            client.auth.sessionStatus.value
                .shouldBeInstanceOf<SessionStatus.NotAuthenticated>()

            client.close()
        }

        test("TC-dff9f5e6-6bd3-4396-8de3-c99bde0dc40b TC-cf52b49b-0abb-4be5-9993-df80f5a89d42 사용자 정보를 다시 받아 저장된 사용자 정보를 바꾼다") {
            val storedUser = user(profileImage = "https://example.com/stored.jpg")
            val storedSession = session(storedUser, token = Arb.kotlinUuid().next().toString())
            val retrievedProfileImage = "https://example.com/${Arb.kotlinUuid().next()}.jpg"
            val retrievedUser =
                storedUser
                    .copy(email = "retrieved-${Arb.kotlinUuid().next()}@example.com")
                    .withProfileImage(retrievedProfileImage)
            val engine = MockEngine { respondJson(content = Json.encodeToString(UserInfo.serializer(), retrievedUser)) }
            val sessionManager = MemorySessionManager()
            val client = createClient(sessionManager = sessionManager, engine = engine, autoSaveToStorage = true)
            client.auth.awaitInitialization()
            client.auth.importSession(storedSession, autoRefresh = false)
            val supabaseAuth = SupabaseAuthImpl(client)

            supabaseAuth.getUserFlow().test {
                awaitItem()?.email shouldBe storedUser.email

                supabaseAuth.retrieveUserForCurrentSession()

                val user = expectMostRecentItem()
                user?.email shouldBe retrievedUser.email
                user?.profileImage shouldBe retrievedProfileImage
            }

            engine.requestHistory shouldHaveSize 1
            engine.requestHistory
                .single()
                .url.encodedPath shouldBe "/auth/v1/user"
            engine.requestHistory.single().headers[HttpHeaders.Authorization] shouldBe "Bearer ${storedSession.accessToken}"
            sessionManager.loadSessionOrNull()?.user?.email shouldBe retrievedUser.email

            client.close()
        }

        test("TC-06635dcf-adbd-4d7c-ae3b-c510d3a97ee4 자동 갱신이 멈춘 세션을 한 번 갱신하고 자동 갱신 없이 설정한다") {
            val storedSession = session(user(profileImage = "https://example.com/stored.jpg"), token = Arb.kotlinUuid().next().toString())
            val refreshedSession = storedSession.copy(accessToken = "access-${Arb.kotlinUuid().next()}", refreshToken = "refresh-${Arb.kotlinUuid().next()}")
            val engine = MockEngine { respondJson(content = refreshedSession.toTokenResponse()) }
            val sessionManager = MemorySessionManager(storedSession)
            val client = createClient(sessionManager = sessionManager, engine = engine, autoSaveToStorage = true)

            SupabaseAuthImpl(client).refreshStoppedSession()

            engine.requestHistory.map { request -> request.url.parameters["grant_type"] } shouldContainExactly listOf("refresh_token")
            (client.auth.sessionStatus.value as SessionStatus.Authenticated).session.accessToken shouldBe refreshedSession.accessToken
            sessionManager.loadSessionOrNull()?.refreshToken shouldBe refreshedSession.refreshToken
            client.auth.isAutoRefreshRunning shouldBe false

            client.close()
        }

        listOf(
            "refresh_token_not_found" to HttpStatusCode.BadRequest,
            "refresh_token_already_used" to HttpStatusCode.BadRequest,
            "validation_failed" to HttpStatusCode.BadRequest,
            "session_not_found" to HttpStatusCode.BadRequest,
            "session_expired" to HttpStatusCode.BadRequest,
            "user_not_found" to HttpStatusCode.NotFound,
        ).forEach { (errorCode, status) ->
            test("TC-4aec434b-fd3a-4df7-9881-046bf3795d1e 폐기 응답($errorCode)이면 세션을 지운다") {
                val storedSession = session(user(profileImage = "https://example.com/stored.jpg"), token = Arb.kotlinUuid().next().toString())
                val engine = MockEngine { respondError(status = status, errorCode = errorCode) }
                val sessionManager = MemorySessionManager(storedSession)
                val client = createClient(sessionManager = sessionManager, engine = engine)

                SupabaseAuthImpl(client).refreshStoppedSession()

                engine.requestHistory shouldHaveSize 1
                sessionManager.loadSessionOrNull().shouldBeNull()
                client.auth.sessionStatus.value
                    .shouldBeInstanceOf<SessionStatus.NotAuthenticated>()

                client.close()
            }
        }

        listOf<Pair<String, MockRequestHandler>>(
            "네트워크 연결 실패" to { throw IOException("연결 실패") },
            "서버의 일시적인 오류 응답" to { respondJson(content = "{}", status = HttpStatusCode.ServiceUnavailable) },
            "요청이 너무 많음" to { respondError(status = HttpStatusCode.TooManyRequests, errorCode = "over_request_rate_limit") },
            "갱신 겹침" to { respondError(status = HttpStatusCode.Conflict, errorCode = "conflict") },
            "사용자 정지" to { respondError(status = HttpStatusCode.BadRequest, errorCode = "user_banned") },
            "처리 시간 초과" to { respondError(status = HttpStatusCode.GatewayTimeout, errorCode = "request_timeout") },
            "목록에 없는 종류" to { respondError(status = HttpStatusCode.BadRequest, errorCode = "bad_json") },
            "모르는 종류" to { respondError(status = HttpStatusCode.BadRequest, errorCode = "code-${Arb.kotlinUuid().next()}") },
            "종류 없는 응답" to { respondJson(content = "{}", status = HttpStatusCode.BadRequest) },
            "읽을 수 없는 응답" to { respond(content = "<html></html>", status = HttpStatusCode.TooManyRequests) },
        ).forEach { (failure, handler) ->
            test("TC-c97615f3-df76-4736-97cf-66e30f37908d 갱신 실패($failure) 시 세션을 남긴 채 실패를 알린다") {
                val storedSession = session(user(profileImage = "https://example.com/stored.jpg"), token = Arb.kotlinUuid().next().toString())
                // 처리기가 예외를 던진 요청은 MockEngine의 요청 기록에 남지 않으므로 직접 센다.
                var requestCount = 0
                val engine =
                    MockEngine { request ->
                        requestCount++
                        handler(request)
                    }
                val sessionManager = MemorySessionManager(storedSession)
                val client = createClient(sessionManager = sessionManager, engine = engine)

                shouldThrowAny { SupabaseAuthImpl(client).refreshStoppedSession() }

                requestCount shouldBe 1
                sessionManager.loadSessionOrNull() shouldBe storedSession

                client.close()
            }
        }

        test("TC-501338e8-2224-4bd8-b3c4-4dc55bd8df11 자동 갱신 중이면 서버에 갱신을 요청하지 않는다") {
            val storedSession = session(user(profileImage = "https://example.com/stored.jpg"), token = Arb.kotlinUuid().next().toString())
            val engine = MockEngine { respondOk() }
            val client = createClient(sessionManager = MemorySessionManager(storedSession), engine = engine)
            client.auth.importSession(storedSession, autoRefresh = true)

            SupabaseAuthImpl(client).refreshStoppedSession()

            engine.requestHistory.shouldBeEmpty()
            (client.auth.sessionStatus.value as SessionStatus.Authenticated).session shouldBe storedSession

            client.close()
        }

        test("저장된 세션이 없으면 서버에 갱신을 요청하지 않는다") {
            val engine = MockEngine { respondOk() }
            val client = createClient(sessionManager = MemorySessionManager(), engine = engine)

            SupabaseAuthImpl(client).refreshStoppedSession()

            engine.requestHistory.shouldBeEmpty()

            client.close()
        }
    }) {
    companion object {
        // 저장소에 쓰기 전에 상태를 먼저 알리는 순서를 테스트에서 그대로 만들기 위해, 자동 저장을 끄고
        // 저장소의 세션과 알림의 세션을 서로 다르게 둔다.
        private fun createClient(
            sessionManager: SessionManager,
            engine: MockEngine = MockEngine { respondOk() },
            autoSaveToStorage: Boolean = false,
        ): SupabaseClient =
            createSupabaseClient(
                supabaseUrl = "https://example.supabase.co",
                supabaseKey = "test-key",
            ) {
                httpEngine = engine
                install(Auth) {
                    minimalConfig()
                    this.sessionManager = sessionManager
                    this.autoSaveToStorage = autoSaveToStorage
                }
            }

        private fun user(profileImage: String): UserInfo =
            UserInfo(
                aud = "authenticated",
                id = Uuid.random().toString(),
                email = "diary@example.com",
            ).withProfileImage(profileImage)

        private fun UserInfo.withProfileImage(profileImage: String): UserInfo = copy(userMetadata = JsonObject(mapOf("avatar_url" to JsonPrimitive(profileImage))))

        private fun session(
            user: UserInfo,
            token: String = "token",
        ): UserSession =
            UserSession(
                accessToken = "access-$token",
                refreshToken = "refresh-$token",
                expiresIn = 3600,
                tokenType = "bearer",
                user = user,
            )

        private fun UserSession.toTokenResponse(): String = Json.encodeToString(UserSession.serializer(), this)

        private fun MockRequestHandleScope.respondError(
            status: HttpStatusCode,
            errorCode: String,
        ): HttpResponseData =
            respondJson(
                content =
                    JsonObject(
                        mapOf(
                            "code" to JsonPrimitive(status.value),
                            "error_code" to JsonPrimitive(errorCode),
                            "msg" to JsonPrimitive(status.description),
                        ),
                    ).toString(),
                status = status,
            )

        private fun MockRequestHandleScope.respondJson(
            content: String,
            status: HttpStatusCode = HttpStatusCode.OK,
        ): HttpResponseData = respond(content = content, status = status, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
    }
}
