package io.github.taetae98coding.diary.core.network.impl.qr.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrPullRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.qr.entity.QrPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.qr.entity.QrPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.qr.remoteQr
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

private const val PUSH_FUNCTION: String = "v1-sync-push-qr"
private const val PULL_FUNCTION: String = "v1-sync-pull-qr"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class QrRemoteDataSourceImplTest :
    FunSpec({
        test("QR의 전체 상태를 Supabase push 함수에 전달한다") {
            val qrList = listOf(fixtureMonkey.remoteQr(isDeleted = false), fixtureMonkey.remoteQr(isDeleted = true))
            val response = mockk<HttpResponse>()
            every { response.status } returns HttpStatusCode.OK
            val requestSlot = slot<QrPushRequestRemoteEntity>()
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = capture(requestSlot),
                    typeInfo = any(),
                    headers = any(),
                )
            } returns response
            val dataSource = QrRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            dataSource.push(qrList = qrList)

            requestSlot.captured.qrList shouldBe qrList
            coVerify(exactly = 1) {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<QrPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            }
        }

        test("TC-DATA-SYNC-DATA-019 QR의 최신 전체 상태와 서버 변경 순번을 응답한다") {
            val usn = fixtureMonkey.giveMeOne<Long>()
            val qrPullList =
                listOf(
                    QrPullRemoteEntity(
                        qr = fixtureMonkey.remoteQr(isDeleted = false),
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
            } returns httpResponse(body = Json.encodeToString(QrPullResponseRemoteEntity(qrList = qrPullList)))
            val dataSource = QrRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            val actual = dataSource.pull(usn = usn)

            requestSlot.captured.usn shouldBe usn
            actual shouldBe qrPullList
        }

        test("QR push 함수가 실패하면 오류를 그대로 전달한다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val supabaseFunction = mockk<SupabaseFunction>()
            coEvery {
                supabaseFunction(
                    function = PUSH_FUNCTION,
                    body = any<QrPushRequestRemoteEntity>(),
                    typeInfo = any(),
                    headers = any(),
                )
            } throws failure
            val dataSource = QrRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

            shouldThrow<TestException> {
                dataSource.push(qrList = listOf(fixtureMonkey.remoteQr(isDeleted = false)))
            }
        }

        test("QR pull 함수가 실패하면 오류를 그대로 전달한다") {
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
            val dataSource = QrRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

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
