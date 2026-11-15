package io.github.taetae98coding.diary.core.network.impl.authentication.datasource

import io.github.taetae98coding.diary.core.network.api.authentication.entity.SessionRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.AppleIdTokenRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.GoogleAuthorizationCodeRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.GoogleIdTokenRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.authentication.sessionRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class SessionRemoteDataSourceImplTest :
    FunSpec({
        test("TC-e15946a0-04bc-498d-a34f-30ef35e7f825 JVM authorization code 인증 정보를 서버에 전달한다") {
            val authorizationCode = Arb.string().next()
            val clientId = Arb.string().next()
            val redirectUri = Arb.string().next()
            val codeVerifier = Arb.string().next()
            val response = Arb.sessionRemoteEntity().next()
            val requestSlot = slot<GoogleAuthorizationCodeRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-session-google-authorization-code",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns httpResponse(body = Json.encodeToString(response))
            val dataSource = SessionRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual =
                dataSource.createWithGoogle(
                    code = authorizationCode,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    codeVerifier = codeVerifier,
                )

            requestSlot.captured shouldBe
                GoogleAuthorizationCodeRequestRemoteEntity(
                    authorizationCode = authorizationCode,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    codeVerifier = codeVerifier,
                )
            actual shouldBe response
        }

        test("TC-63cac31c-7605-4cf2-8d07-827a51c41bbc Google 신원 증명과 시도 확인 값을 Google 신원 증명 로그인 요청으로 서버에 전달한다") {
            val idToken = Arb.string().next()
            val nonce = Arb.string().next()
            val response = Arb.sessionRemoteEntity().next()
            val requestSlot = slot<GoogleIdTokenRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-session-google-id-token", body = capture(requestSlot), typeInfo = any(), headers = any())
            } returns httpResponse(body = Json.encodeToString(response))
            val dataSource = SessionRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.createWithGoogle(idToken = idToken, nonce = nonce)

            requestSlot.captured shouldBe GoogleIdTokenRequestRemoteEntity(idToken = idToken, nonce = nonce)
            Json.parseToJsonElement(Json.encodeToString(requestSlot.captured)).jsonObject shouldBe
                JsonObject(mapOf("idToken" to JsonPrimitive(idToken), "nonce" to JsonPrimitive(nonce)))
            actual shouldBe response
        }

        test("TC-6e664879-ccd1-4045-8836-4373dbf33fe3 Apple 신원 증명과 시도 확인 값을 Apple 신원 증명 로그인 요청으로 서버에 전달한다") {
            val idToken = Arb.string().next()
            val nonce = Arb.string().next()
            val response = Arb.sessionRemoteEntity().next()
            val requestSlot = slot<AppleIdTokenRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-session-apple-id-token", body = capture(requestSlot), typeInfo = any(), headers = any())
            } returns httpResponse(body = Json.encodeToString(response))
            val dataSource = SessionRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.createWithApple(idToken = idToken, nonce = nonce)

            requestSlot.captured shouldBe AppleIdTokenRequestRemoteEntity(idToken = idToken, nonce = nonce)
            Json.parseToJsonElement(Json.encodeToString(requestSlot.captured)).jsonObject shouldBe
                JsonObject(mapOf("idToken" to JsonPrimitive(idToken), "nonce" to JsonPrimitive(nonce)))
            actual shouldBe response
        }

        test("TC-e15946a0-04bc-498d-a34f-30ef35e7f825 코드 확인 값이 없는 authorization code 요청은 codeVerifier 필드를 생략한다") {
            val request =
                GoogleAuthorizationCodeRequestRemoteEntity(
                    authorizationCode = Arb.string().next(),
                    clientId = Arb.string().next(),
                    redirectUri = Arb.string().next(),
                )

            val actual = Json.parseToJsonElement(Json.encodeToString(request)).jsonObject

            actual.shouldNotContainKey("codeVerifier")
        }
    })

private suspend fun httpResponse(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponse {
    val client =
        HttpClient(
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        ) {
            install(ContentNegotiation) {
                json()
            }
        }

    return client.get("/")
}
