package io.github.taetae98coding.diary.core.network.impl.routinerecord.datasource

import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordPullRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.routinerecord.entity.RoutineRecordPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.routinerecord.entity.RoutineRecordPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.routine.remoteRoutineRecord
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
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

private const val PUSH_FUNCTION: String = "v1-sync-push-routine-record"
private const val PULL_FUNCTION: String = "v1-sync-pull-routine-record"

class RoutineRecordRemoteDataSourceImplTest :
    FunSpec({
        test("루틴 기록의 전체 상태를 Supabase push 함수에 전달한다") {
            val routineRecordList = listOf(Arb.remoteRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = false).next(), Arb.remoteRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = true).next())
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<RoutineRecordPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = RoutineRecordRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(routineRecordList = routineRecordList)

            requestSlot.captured.routineRecordList shouldBe routineRecordList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<RoutineRecordPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-c368908b-b191-4ceb-9b5d-e82025cf6da8 루틴 기록의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = Arb.long().next()
            val pullList =
                listOf(
                    RoutineRecordPullRemoteEntity(
                        routineRecord = Arb.remoteRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = false).next(),
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
            } returns httpResponse(body = Json.encodeToString(RoutineRecordPullResponseRemoteEntity(routineRecordList = pullList)))
            val dataSource = RoutineRecordRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe pullList
        }

        test("루틴 기록 push 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(Arb.string().next())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<RoutineRecordPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = RoutineRecordRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.push(routineRecordList = listOf(Arb.remoteRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = false).next()))
            }
        }

        test("루틴 기록 pull 함수가 실패하면 오류를 그대로 전달한다") {
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
            val dataSource = RoutineRecordRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.pull(usn = Arb.long().next())
            }
        }
    }) {
    private companion object {
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
