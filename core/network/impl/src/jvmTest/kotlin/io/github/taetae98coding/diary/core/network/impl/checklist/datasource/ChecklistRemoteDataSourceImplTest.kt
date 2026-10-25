package io.github.taetae98coding.diary.core.network.impl.checklist.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistPullRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.checklist.entity.ChecklistPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.checklist.entity.ChecklistPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.checklist.remoteChecklist
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

private const val PUSH_FUNCTION: String = "v1-sync-push-checklist"
private const val PULL_FUNCTION: String = "v1-sync-pull-checklist"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ChecklistRemoteDataSourceImplTest :
    FunSpec({
        test("체크리스트의 전체 상태를 Supabase push 함수에 전달한다") {
            val checklistList = listOf(fixtureMonkey.remoteChecklist(isDeleted = false), fixtureMonkey.remoteChecklist(isDeleted = true))
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<ChecklistPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = ChecklistRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(checklistList = checklistList)

            requestSlot.captured.checklistList shouldBe checklistList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<ChecklistPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-c368908b-b191-4ceb-9b5d-e82025cf6da8 체크리스트의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = fixtureMonkey.giveMeOne<Long>()
            val pullList =
                listOf(
                    ChecklistPullRemoteEntity(
                        checklist = fixtureMonkey.remoteChecklist(isDeleted = false),
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
            } returns httpResponse(body = Json.encodeToString(ChecklistPullResponseRemoteEntity(checklistList = pullList)))
            val dataSource = ChecklistRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe pullList
        }

        test("체크리스트 push 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<ChecklistPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = ChecklistRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.push(checklistList = listOf(fixtureMonkey.remoteChecklist(isDeleted = false)))
            }
        }

        test("체크리스트 pull 함수가 실패하면 오류를 그대로 전달한다") {
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
            val dataSource = ChecklistRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = fixtureMonkey.giveMeOne<Long>())
            }
        }
    }) {
    public companion object {
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
    }
}
