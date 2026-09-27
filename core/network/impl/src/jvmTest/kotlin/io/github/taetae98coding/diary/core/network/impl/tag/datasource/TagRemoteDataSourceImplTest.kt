package io.github.taetae98coding.diary.core.network.impl.tag.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.tag.entity.TagPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.tag.entity.TagPushRequestRemoteEntity
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

class TagRemoteDataSourceImplTest :
    FunSpec({
        test("태그 전체 상태를 Supabase 태그 push 함수에 전달한다") {
            val tagList = listOf(tag())
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<TagPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-sync-push-tag",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = TagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(tagList = tagList)

            requestSlot.captured.tagList shouldBe tagList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = "v1-sync-push-tag",
                    body = any<TagPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-DATA-SYNC-DATA-019 태그의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = fixtureMonkey.giveMeOne<Long>()
            val tagPullList =
                listOf(
                    TagPullRemoteEntity(
                        tag = tag(),
                        usn = fixtureMonkey.giveMeOne<Long>(),
                    ),
                )
            val requestSlot = slot<PullRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-sync-pull-tag",
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns
                httpResponse(
                    body =
                        Json.encodeToString(
                            TagPullResponseRemoteEntity(tagList = tagPullList),
                        ),
                )
            val dataSource = TagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe tagPullList
        }

        test("태그 pull 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = "v1-sync-pull-tag",
                    body = any<PullRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = TagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = fixtureMonkey.giveMeOne<Long>())
            }
        }
    }) {
    public companion object {
        private fun tag(): TagRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagRemoteEntity>()
                .setExp(TagRemoteEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(TagRemoteEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
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
