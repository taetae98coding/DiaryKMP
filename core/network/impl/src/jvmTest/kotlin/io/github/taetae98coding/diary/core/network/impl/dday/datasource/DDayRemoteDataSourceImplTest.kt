package io.github.taetae98coding.diary.core.network.impl.dday.datasource

import io.github.taetae98coding.diary.core.data.testing.dday.ddayRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayPullRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.dday.entity.DDayPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.dday.entity.DDayPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PUSH_FUNCTION: String = "v2-sync-push-dday"

private const val PULL_FUNCTION: String = "v1-sync-pull-dday"

class DDayRemoteDataSourceImplTest :
    FunSpec({
        test("TC-26760beb-c86c-4349-bbe2-61ba56517e9f 디데이는 대표 태그를 반드시 담는 새 방식으로 업로드된다") {
            val ddayList =
                listOf(
                    Arb.ddayRemoteEntity().next().copy(primaryTagId = null),
                    Arb.ddayRemoteEntity().next().copy(primaryTagId = Arb.kotlinUuid().next()),
                )
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<DDayPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = DDayRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(ddayList = ddayList)

            requestSlot.captured.ddayList shouldBe ddayList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<DDayPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("디데이 내려받기는 대표 태그를 담은 응답을 내려받기 함수에서 읽는다") {
            val usn = Arb.long().next()
            val pullList = listOf(DDayPullRemoteEntity(dday = Arb.ddayRemoteEntity().next().copy(primaryTagId = Arb.kotlinUuid().next()), usn = Arb.long().next()))
            val requestSlot = slot<PullRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PULL_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns httpResponse(body = Json.encodeToString(DDayPullResponseRemoteEntity(ddayList = pullList)))
            val dataSource = DDayRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe pullList
        }
    }) {
    private companion object {
        private suspend fun httpResponse(body: String): HttpResponse {
            val client =
                HttpClient(
                    MockEngine {
                        respond(
                            content = body,
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
    }
}
