package io.github.taetae98coding.diary.core.network.impl.memo.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.memo.entity.MemoPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.memo.entity.MemoPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
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

private const val MAX_TEST_EPOCH_MILLISECONDS: Long = 4_102_444_800_000

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MemoRemoteDataSourceImplTest :
    FunSpec({
        test("메모 전체 상태를 Supabase 메모 push 함수에 전달한다") {
            val memoList = listOf(memo())
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<MemoPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-sync-push-memo",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = MemoRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(memoList = memoList)

            requestSlot.captured.memoList shouldBe memoList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = "v1-sync-push-memo",
                    body = any<MemoPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-DATA-SYNC-DATA-019 메모의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = fixtureMonkey.giveMeOne<Long>()
            val memoPullList =
                listOf(
                    MemoPullRemoteEntity(
                        memo = memo(),
                        usn = fixtureMonkey.giveMeOne<Long>(),
                    ),
                )
            val requestSlot = slot<PullRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-sync-pull-memo",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns
                httpResponse(
                    body =
                        Json.encodeToString(
                            MemoPullResponseRemoteEntity(memoList = memoPullList),
                        ),
                )
            val dataSource = MemoRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe memoPullList
        }

        test("메모 pull 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-sync-pull-memo",
                    body = any<PullRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = MemoRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = fixtureMonkey.giveMeOne<Long>())
            }
        }
    }) {
    public companion object {
        private fun memo(): MemoRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoRemoteEntity>()
                .setExp(MemoRemoteEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(MemoRemoteEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun instant(): Instant =
            Instant.fromEpochMilliseconds(
                fixtureMonkey.giveMeOne<Long>() % MAX_TEST_EPOCH_MILLISECONDS,
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
