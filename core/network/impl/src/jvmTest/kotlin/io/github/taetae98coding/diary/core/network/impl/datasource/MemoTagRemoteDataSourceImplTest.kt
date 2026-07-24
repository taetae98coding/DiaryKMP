package io.github.taetae98coding.diary.core.network.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.MemoTagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.MemoTagPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

private const val PUSH_FUNCTION: String = "v1-sync-push-memo-tag"
private const val PULL_FUNCTION: String = "v1-sync-pull-memo-tag"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoTagRemoteDataSourceImplTest :
    FunSpec({
        test("메모 태그 관계의 전체 상태를 Supabase 관계 push 함수에 전달한다") {
            val memoTagList = listOf(memoTag(), memoTag(isDeleted = true))
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<MemoTagPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = MemoTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(memoTagList = memoTagList)

            requestSlot.captured.memoTagList shouldBe memoTagList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<MemoTagPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("메모 태그 관계의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = fixtureMonkey.giveMeOne<Long>()
            val memoTagPullList =
                listOf(
                    MemoTagPullRemoteEntity(
                        memoTag = memoTag(),
                        usn = fixtureMonkey.giveMeOne<Long>(),
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
                            MemoTagPullResponseRemoteEntity(memoTagList = memoTagPullList),
                        ),
                )
            val dataSource = MemoTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe memoTagPullList
        }

        test("메모 태그 pull 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PULL_FUNCTION,
                    body = any<PullRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = MemoTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = fixtureMonkey.giveMeOne<Long>())
            }
        }
    }) {
    public companion object {
        private fun memoTag(isDeleted: Boolean = false): MemoTagRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoTagRemoteEntity>()
                .setExp(MemoTagRemoteEntity::isDeleted, isDeleted)
                .setExp(MemoTagRemoteEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(MemoTagRemoteEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

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
