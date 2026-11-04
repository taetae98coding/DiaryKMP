package io.github.taetae98coding.diary.core.network.impl.placetag.datasource

import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.placetag.entity.PlaceTagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.placetag.entity.PlaceTagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.placetag.placeTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

private const val PUSH_FUNCTION: String = "v1-sync-push-place-tag"
private const val PULL_FUNCTION: String = "v1-sync-pull-place-tag"

class PlaceTagRemoteDataSourceImplTest :
    FunSpec({
        test("TC-7304e71b-b36f-423d-9efa-52d3d75bcdd9 TC-a7ac9999-59d1-445b-bc5b-312a87bad444 장소와 태그의 연결의 전체 상태를 Supabase push 함수에 전달한다") {
            val placeTagList = listOf(placeTag(), placeTag(isDeleted = true))
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<PlaceTagPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = PlaceTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(placeTagList = placeTagList)

            requestSlot.captured.placeTagList shouldBe placeTagList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<PlaceTagPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-96b8e102-0188-48ac-83f6-9f2651aaee11 연결의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = Arb.long().next()
            val placeTagPullList =
                listOf(
                    PlaceTagPullRemoteEntity(
                        placeTag = placeTag(),
                        usn = Arb.long().next(),
                    ),
                )
            val requestSlot = slot<PullRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PULL_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns
                httpResponse(
                    body =
                        Json.encodeToString(
                            PlaceTagPullResponseRemoteEntity(placeTagList = placeTagPullList),
                        ),
                )
            val dataSource = PlaceTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe placeTagPullList
        }

        test("장소와 태그의 연결 pull 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(Arb.string().next())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PULL_FUNCTION,
                    body = any<PullRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = PlaceTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = Arb.long().next())
            }
        }
    }) {
    public companion object {
        private fun placeTag(isDeleted: Boolean = false): PlaceTagRemoteEntity =
            Arb
                .placeTagRemoteEntity()
                .next()
                .copy(
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private suspend fun httpResponse(
            body: String = """{"error":"sync_pull_failed"}""",
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
    }
}
