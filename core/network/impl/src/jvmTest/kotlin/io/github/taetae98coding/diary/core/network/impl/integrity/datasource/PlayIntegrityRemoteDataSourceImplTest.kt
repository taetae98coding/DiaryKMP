package io.github.taetae98coding.diary.core.network.impl.integrity.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.impl.integrity.entity.PlayIntegrityDecodeRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlayIntegrityRemoteDataSourceImplTest :
    FunSpec({
        test("TC-PLAY-INTEGRITY-LOGGING-DATA-001 서버에 토큰과 패키지 이름을 보내고 받은 판정 결과를 구조 그대로 돌려준다") {
            val token = fixtureMonkey.giveMeOne<String>()
            val packageName = fixtureMonkey.giveMeOne<String>()
            val verdict =
                buildJsonObject {
                    putJsonObject("requestDetails") { put("timestampMillis", fixtureMonkey.giveMeOne<Long>().toString()) }
                    putJsonObject("deviceIntegrity") { putJsonObject("deviceAttributes") { put("sdkVersion", fixtureMonkey.giveMeOne<Int>()) } }
                }
            val bodySlot = slot<Any>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(function = "v1-play-integrity-decode", body = capture(bodySlot), typeInfo = any(), headers = any())
            } returns httpResponse(content = verdict.toString())
            val dataSource = PlayIntegrityRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val result = dataSource.decode(token = token, packageName = packageName)

            bodySlot.captured shouldBe PlayIntegrityDecodeRequestRemoteEntity(token = token, packageName = packageName)
            result shouldBe verdict
        }
    })

private suspend fun httpResponse(content: String): HttpResponse {
    val client =
        HttpClient(
            MockEngine {
                respond(
                    content = content,
                    status = HttpStatusCode.OK,
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
