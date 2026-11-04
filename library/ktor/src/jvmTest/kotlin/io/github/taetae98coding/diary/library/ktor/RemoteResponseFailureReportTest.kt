package io.github.taetae98coding.diary.library.ktor

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.writeStringUtf8
import io.ktor.utils.io.writer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonObject

class RemoteResponseFailureReportTest :
    FunSpec({
        test("TC-cac4f65d-b545-4e9e-894f-4de2f2416d6c 401, 403, 500번대 응답을 받으면 오류 보고가 한 번 남는다") {
            listOf(401, 403, 500, 503, 599).forEach { statusCode ->
                val reportList = recordCrashlyticsLog()
                val client = createJsonHttpClient(baseUrl = BASE_URL, engine = MockEngine { respond(content = "", status = HttpStatusCode.fromValue(statusCode)) })

                shouldThrowAny { client.get(PATH) }

                reportList shouldHaveSize 1
                reportList.single().message shouldContain "$statusCode"
            }
        }

        test("TC-4763d874-aadf-4f3d-8e13-9d10b3a2e348 보고 기준에 맞지 않는 응답은 오류 보고를 남기지 않는다") {
            listOf(
                HttpStatusCode.OK to VALID_JSON,
                HttpStatusCode.NoContent to "",
                HttpStatusCode.BadRequest to fixtureMonkey.giveMeOne<String>(),
                HttpStatusCode.NotFound to fixtureMonkey.giveMeOne<String>(),
                HttpStatusCode.Conflict to fixtureMonkey.giveMeOne<String>(),
                HttpStatusCode.TooManyRequests to fixtureMonkey.giveMeOne<String>(),
                HttpStatusCode.NotFound to INVALID_JSON,
            ).forEach { (status, content) ->
                val reportList = recordCrashlyticsLog()
                val client = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = status, content = content), expectSuccess = false)

                // 내용이 없는 응답은 내용을 읽지 않는다.
                runCatching { client.get(PATH).also { response -> if (content.isNotEmpty()) response.body<JsonObject>() } }

                reportList.shouldBeEmpty()
            }
        }

        test("TC-736f7b6b-267f-4e01-9b85-70a53caa72ff 성공 응답의 내용을 기대한 형식으로 읽지 못하면 오류 보고가 한 번 남는다") {
            listOf(
                MockEngine { respond(content = JSON_ARRAY, status = HttpStatusCode.OK, headers = JSON_HEADERS) } to "JsonConvertException",
                MockEngine { respond(content = "<html></html>", status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString())) } to "NoTransformationFoundException",
            ).forEach { (engine, reason) ->
                val reportList = recordCrashlyticsLog()
                val client = createJsonHttpClient(baseUrl = BASE_URL, engine = engine)

                shouldThrowAny { client.get(PATH).body<JsonObject>() }

                reportList shouldHaveSize 1
                reportList.single().message shouldContain reason
            }
        }

        test("TC-95e226d5-51c8-4e9f-aeec-5e83f3cfbf0f 실패 응답의 내용을 읽지 못해도 응답 상태 기준의 보고 하나만 남는다") {
            val reportList = recordCrashlyticsLog()
            val client = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.InternalServerError, content = INVALID_JSON), expectSuccess = false)

            shouldThrowAny { client.get(PATH).body<JsonObject>() }

            reportList shouldHaveSize 1
            reportList.single().message shouldContain "500"
        }

        test("TC-882f8352-2646-4203-baff-a125ca4fd172 응답을 받지 못하면 오류 보고를 남기지 않는다") {
            listOf<MockRequestHandler>(
                { throw IOException("connect failed") },
                { request -> throw HttpRequestTimeoutException(request.url.toString(), null) },
                {
                    val channel =
                        CoroutineScope(Dispatchers.IO)
                            .writer {
                                channel.writeStringUtf8("""{"value":""")
                                throw IOException("connection cut")
                            }.channel
                    respond(content = channel, status = HttpStatusCode.OK, headers = JSON_HEADERS)
                },
            ).forEach { handler ->
                val reportList = recordCrashlyticsLog()
                val client = createJsonHttpClient(baseUrl = BASE_URL, engine = MockEngine(handler))

                shouldThrowAny { client.get(PATH).body<JsonObject>() }

                reportList.shouldBeEmpty()
            }
        }

        test("TC-e5e56743-5bb1-44c4-b363-af6f5adbfd1d 요청이 취소되면 오류 보고를 남기지 않는다") {
            val reportList = recordCrashlyticsLog()
            val started = CompletableDeferred<Unit>()
            val client =
                createJsonHttpClient(
                    baseUrl = BASE_URL,
                    engine =
                        MockEngine {
                            started.complete(Unit)
                            awaitCancellation()
                        },
                )

            runBlocking {
                val job = launch { client.get(PATH) }
                started.await()
                job.cancelAndJoin()
            }

            reportList.shouldBeEmpty()
        }

        test("TC-c8c0bb6b-0a6e-4e54-8c79-aaf8cd688a3f 보고에는 요청 방식, 서버 이름, 주소 경로, 응답 상태가 담긴다") {
            val reportList = recordCrashlyticsLog()
            val query = "query${fixtureMonkey.giveMeOne<Int>()}"
            val requestContent = "request${fixtureMonkey.giveMeOne<Int>()}"
            val responseContent = "response${fixtureMonkey.giveMeOne<Int>()}"
            val client = createJsonHttpClient(baseUrl = BASE_URL, engine = MockEngine { respond(content = responseContent, status = HttpStatusCode.ServiceUnavailable) })

            shouldThrowAny {
                client.post("$PATH?key=$query") { setBody(requestContent) }
            }

            val report = reportList.single()
            listOf(report.message, report.throwable.message.orEmpty()).forEach { text ->
                text shouldContain "POST $HOST/$PATH"
                text shouldContain "503"
                listOf(query, requestContent, responseContent).forEach { secret -> text shouldNotContain secret }
            }
        }

        test("경로를 담지 않도록 설치한 요청의 보고에는 서버 이름만 담긴다") {
            val reportList = recordCrashlyticsLog()
            val client =
                HttpClient(MockEngine { respond(content = "", status = HttpStatusCode.Forbidden) }) {
                    install(RemoteResponseFailureReport) { reportsPath = false }
                }

            client.get("$BASE_URL$PATH?token=${fixtureMonkey.giveMeOne<Int>()}")

            val report = reportList.single()
            report.message shouldContain "GET $HOST 403"
            report.message shouldNotContain PATH
            report.message shouldNotContain "token"
            report.issueKey shouldBe "GET $HOST"
        }

        test("TC-07746aa2-d200-46dd-8436-fb64ed1b3c18 요청 방식, 서버 이름, 주소 경로가 같은 보고만 같은 오류로 묶인다") {
            listOf(
                Triple(TestCall(url = statusUrl(status = 401)), TestCall(url = statusUrl(status = 500)), true),
                Triple(TestCall(url = "${statusUrl(status = 500)}&q=a"), TestCall(url = "${statusUrl(status = 500)}&q=b"), true),
                Triple(TestCall(url = statusUrl(status = 200), readsBody = true), TestCall(url = statusUrl(status = 500)), true),
                Triple(TestCall(url = statusUrl(status = 500)), TestCall(url = statusUrl(status = 500), method = HttpMethod.Put), false),
                Triple(TestCall(url = statusUrl(status = 500)), TestCall(url = statusUrl(status = 500, host = "other.test")), false),
                Triple(TestCall(url = statusUrl(status = 500)), TestCall(url = statusUrl(status = 500, path = "other")), false),
            ).forEach { (first, second, isSame) ->
                val reportList = recordCrashlyticsLog()
                val client = plainJsonClient(engine = statusEngine()).config { install(RemoteResponseFailureReport) }

                runCatching { client.send(call = first) }
                runCatching { client.send(call = second) }

                reportList shouldHaveSize 2
                if (isSame) {
                    reportList[0].issueKey shouldBe reportList[1].issueKey
                } else {
                    reportList[0].issueKey shouldNotBe reportList[1].issueKey
                }
            }
        }

        test("TC-11116101-46d7-47d6-af90-68fd1130b4f6 보고를 남겨도 요청의 결과는 바뀌지 않는다") {
            val content = fixtureMonkey.giveMeOne<String>()
            val reported = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.InternalServerError, content = content), expectSuccess = false)
            val plain = plainJsonClient(engine = jsonEngine(status = HttpStatusCode.InternalServerError, content = content))

            val reportedResponse = reported.get(PATH)
            val plainResponse = plain.get(PATH)
            reportedResponse.status shouldBe plainResponse.status
            reportedResponse.bodyAsText() shouldBe plainResponse.bodyAsText()

            val reportedFailure = shouldThrowAny { createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.OK, content = JSON_ARRAY)).get(PATH).body<JsonObject>() }
            val plainFailure = shouldThrowAny { plainJsonClient(engine = jsonEngine(status = HttpStatusCode.OK, content = JSON_ARRAY)).get(PATH).body<JsonObject>() }
            reportedFailure::class shouldBe plainFailure::class
            reportedFailure.message shouldBe plainFailure.message
        }

        test("TC-a750cb56-7ff8-46b9-9224-862a1d1a164c 같은 요청을 다시 보내면 보고 기준에 맞는 응답마다 보고가 남는다") {
            val reportList = recordCrashlyticsLog()
            val client = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.InternalServerError, content = ""), expectSuccess = false)

            client.get(PATH)
            client.get(PATH)

            reportList shouldHaveSize 2
        }

        test("TC-a03f08d5-1c4f-4bf2-bcf0-c166ec3f0baa JSON 요청 클라이언트는 보고 수단을 갖춘다") {
            val reportList = recordCrashlyticsLog()
            val client = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.InternalServerError, content = ""), expectSuccess = false)

            client.get(PATH)

            reportList shouldHaveSize 1
        }

        test("응답 내용을 직접 읽는 호출처의 보고는 보고 수단을 설치한 요청에서만 남는다") {
            val reportList = recordCrashlyticsLog()
            val installed = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.OK, content = VALID_JSON))
            val notInstalled = HttpClient(jsonEngine(status = HttpStatusCode.OK, content = VALID_JSON))

            installed.get(PATH).reportUnreadableContent(cause = IllegalStateException("unreadable"))
            notInstalled.get("$BASE_URL$PATH").reportUnreadableContent(cause = IllegalStateException("unreadable"))

            reportList.single().message shouldContain "IllegalStateException"
        }

        test("실패 응답에서는 응답 내용을 직접 읽는 호출처의 보고를 남기지 않는다") {
            val reportList = recordCrashlyticsLog()
            val client = createJsonHttpClient(baseUrl = BASE_URL, engine = jsonEngine(status = HttpStatusCode.BadRequest, content = ""), expectSuccess = false)

            client.get(PATH).reportUnreadableContent(cause = IllegalStateException("unreadable"))

            reportList.shouldBeEmpty()
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private const val HOST = "example.test"
        private const val BASE_URL = "https://$HOST/"
        private const val PATH = "items"
        private const val VALID_JSON = """{"value":1}"""
        private const val INVALID_JSON = "{not json"
        private const val JSON_ARRAY = "[1,2]"
        private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

        private fun jsonEngine(
            status: HttpStatusCode,
            content: String,
        ): MockEngine = MockEngine { respond(content = content, status = status, headers = JSON_HEADERS) }

        // 주소의 status 값을 응답 상태로 돌려준다. 성공 응답은 기대한 형식이 아닌 내용을 담는다.
        private fun statusEngine(): MockEngine =
            MockEngine { request ->
                val status =
                    HttpStatusCode.fromValue(
                        request.url.parameters["status"]
                            .orEmpty()
                            .toInt(),
                    )
                respond(content = JSON_ARRAY, status = status, headers = JSON_HEADERS)
            }

        private fun statusUrl(
            status: Int,
            host: String = HOST,
            path: String = PATH,
        ): String = "https://$host/$path?status=$status"

        private data class TestCall(
            val url: String,
            val method: HttpMethod = HttpMethod.Get,
            val readsBody: Boolean = false,
        )

        private suspend fun HttpClient.send(call: TestCall) {
            val response = request(call.url) { method = call.method }
            if (call.readsBody) response.body<JsonObject>()
        }

        private fun plainJsonClient(engine: MockEngine): HttpClient =
            HttpClient(engine) {
                install(ContentNegotiation) { json(DefaultHttpClientJson) }
            }

        private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
            val reportList = mutableListOf<CrashlyticsLog>()
            val delegate = mockk<DiaryLoggerDelegate>()
            every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
            DiaryLogger.add(delegate = delegate)
            return reportList
        }
    }
}
