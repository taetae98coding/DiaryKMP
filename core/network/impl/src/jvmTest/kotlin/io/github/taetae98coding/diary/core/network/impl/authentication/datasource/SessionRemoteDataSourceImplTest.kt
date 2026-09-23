package io.github.taetae98coding.diary.core.network.impl.authentication.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.authentication.entity.SessionRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.authentication.entity.GoogleAuthorizationCodeRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
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
import kotlinx.serialization.json.jsonObject

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SessionRemoteDataSourceImplTest :
    FunSpec({
        test("TC-LOGIN-DATA-001 JVM authorization code 인증 정보를 서버에 전달한다") {
            val authorizationCode = fixtureMonkey.giveMeOne<String>()
            val clientId = fixtureMonkey.giveMeOne<String>()
            val redirectUri = fixtureMonkey.giveMeOne<String>()
            val codeVerifier = fixtureMonkey.giveMeOne<String>()
            val response = fixtureMonkey.giveMeOne<SessionRemoteEntity>()
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

        test("verifier가 없는 authorization code 요청은 codeVerifier 필드를 생략한다") {
            val request =
                GoogleAuthorizationCodeRequestRemoteEntity(
                    authorizationCode = fixtureMonkey.giveMeOne<String>(),
                    clientId = fixtureMonkey.giveMeOne<String>(),
                    redirectUri = fixtureMonkey.giveMeOne<String>(),
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
