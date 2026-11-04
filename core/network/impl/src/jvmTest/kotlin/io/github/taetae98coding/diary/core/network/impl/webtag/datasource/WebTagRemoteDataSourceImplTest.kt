package io.github.taetae98coding.diary.core.network.impl.webtag.datasource

import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.webtag.entity.WebTagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.webtag.entity.WebTagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.webtag.webTagRemoteEntity
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

private const val PUSH_FUNCTION: String = "v1-sync-push-web-tag"
private const val PULL_FUNCTION: String = "v1-sync-pull-web-tag"

class WebTagRemoteDataSourceImplTest :
    FunSpec({
        test("TC-fbebf0be-a83a-43c9-8c7b-0c47f25e1e00 TC-fb383a98-cf0e-4086-af3d-4e557b8edd75 웹 항목과 태그의 연결의 전체 상태를 Supabase push 함수에 전달한다") {
            val webTagList = listOf(webTag(), webTag(isDeleted = true))
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<WebTagPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = WebTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(webTagList = webTagList)

            requestSlot.captured.webTagList shouldBe webTagList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<WebTagPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-3de3f780-f097-4009-b2ff-da214d0d3328 연결의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = Arb.long().next()
            val webTagPullList =
                listOf(
                    WebTagPullRemoteEntity(
                        webTag = webTag(),
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
                            WebTagPullResponseRemoteEntity(webTagList = webTagPullList),
                        ),
                )
            val dataSource = WebTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe webTagPullList
        }

        test("웹 항목과 태그의 연결 pull 함수가 실패하면 오류를 그대로 전달한다") {
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
            val dataSource = WebTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = Arb.long().next())
            }
        }
    }) {
    public companion object {
        private fun webTag(isDeleted: Boolean = false): WebTagRemoteEntity =
            Arb
                .webTagRemoteEntity()
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
