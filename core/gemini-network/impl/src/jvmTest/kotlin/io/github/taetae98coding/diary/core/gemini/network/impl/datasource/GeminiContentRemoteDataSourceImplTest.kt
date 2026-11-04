package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.API_KEY_INVALID_REASON
import io.github.taetae98coding.diary.core.gemini.network.impl.GeminiNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.gemini.network.impl.badRequestEngine
import io.github.taetae98coding.diary.core.gemini.network.impl.createGeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.request.HttpRequestData
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class GeminiContentRemoteDataSourceImplTest :
    FunSpec({
        test("TC-9b073a1f-64e9-4bb1-be62-c93c520d2f8b: 인증 정보와 모델을 담아 생성을 요청한다") {
            val request = request()
            val engine = createEngine()

            createDataSource(engine).stream(request = request)

            val sent = engine.requestHistory.single()
            sent.method shouldBe HttpMethod.Post
            sent.url.encodedPath shouldBe "/v1beta/${request.model}:streamGenerateContent"
            sent.headers["x-goog-api-key"] shouldBe request.apiKey
        }

        test("TC-92a73c1f-ebce-4681-985c-bdc1c684ee34: 결과를 조각으로 나누어 받는 요청을 보낸다") {
            val engine = createEngine()

            createDataSource(engine).stream(request = request())

            val sent = engine.requestHistory.single()
            sent.url.encodedPath shouldContain ":streamGenerateContent"
            sent.url.parameters["alt"] shouldBe "sse"
        }

        test("요청에 지시문과 프롬프트, 받을 구조를 담는다") {
            val request = request()
            val engine = createEngine()

            createDataSource(engine).stream(request = request)

            val body = engine.requestHistory.single().bodyAsJsonObject()

            body.textOf("systemInstruction") shouldBe request.systemInstruction
            body["contents"]!!
                .jsonArray
                .single()
                .jsonObject
                .textOf() shouldBe request.prompt
            body["generationConfig"]!!.jsonObject["responseMimeType"]!!.jsonPrimitive.content shouldBe "application/json"
            body["generationConfig"]!!.jsonObject["responseJsonSchema"] shouldBe SCHEMA
            body["generationConfig"]!!.jsonObject.containsKey("responseSchema") shouldBe false
        }

        test("TC-3a3766f6-143b-423d-a9ad-bc65853f66ef: 지시문을 더하거나 다듬지 않고 받은 그대로 한 조각으로 담는다") {
            val request = request().copy(systemInstruction = "  첫 줄 지시\n\n둘째 줄 지시  ")
            val engine = createEngine()

            createDataSource(engine).stream(request = request)

            engine.requestHistory
                .single()
                .bodyAsJsonObject()
                .textOf("systemInstruction") shouldBe request.systemInstruction
        }

        test("TC-0ea03cb6-5be1-4857-915b-b2aaf75e2102: 지시문이 비어 있으면 요청에 담지 않는다") {
            listOf("", "   ").forEach { blank ->
                val engine = createEngine()

                createDataSource(engine).stream(request = request().copy(systemInstruction = blank))

                engine.requestHistory
                    .single()
                    .bodyAsJsonObject()
                    .containsKey("systemInstruction") shouldBe false
            }
        }

        test("TC-7fbc6869-e13c-4bd8-9965-0e2292c8d0ba: 받은 대기 한도를 조각 사이 대기에만 걸고 요청 전체 시간은 제한하지 않는다") {
            val engine = createEngine()

            createDataSource(engine).stream(request = request(), idleTimeout = 2.minutes)

            val timeout = engine.requestHistory.single().getCapabilityOrNull(HttpTimeoutCapability)!!
            timeout.socketTimeoutMillis shouldBe 2.minutes.inWholeMilliseconds
            timeout.requestTimeoutMillis shouldBe HttpTimeoutConfig.INFINITE_TIMEOUT_MS
        }

        test("TC-b2c73e80-0370-41e4-849b-c578276c14f2: 받은 조각의 글을 차례로 전달한다") {
            val head = "제${Arb.kotlinUuid().next()}"
            val tail = "목${Arb.kotlinUuid().next()}"
            val body = sse(chunk(text = """{"title":"$head"""), chunk(text = """$tail"}""", finishReason = "STOP"))

            createDataSource(createEngine(body = body)).stream(request = request()) shouldContainExactly listOf("""{"title":"$head""", """$tail"}""")
        }

        test("한 조각이 여러 부분으로 나뉘어 오면 이어 붙여 한 번에 전달한다") {
            val content =
                buildJsonObject {
                    putJsonArray("candidates") {
                        addJsonObject {
                            putJsonObject("content") {
                                putJsonArray("parts") {
                                    addJsonObject { put("text", "앞") }
                                    addJsonObject { put("text", "뒤") }
                                }
                            }
                        }
                    }
                }

            createDataSource(createEngine(body = sse(content))).stream(request = request()) shouldContainExactly listOf("앞뒤")
        }

        test("모델이 생각하는 과정으로 보낸 부분은 전달하지 않는다") {
            val content =
                buildJsonObject {
                    putJsonArray("candidates") {
                        addJsonObject {
                            putJsonObject("content") {
                                putJsonArray("parts") {
                                    addJsonObject {
                                        put("text", "생각")
                                        put("thought", true)
                                    }
                                    addJsonObject { put("text", "결과") }
                                }
                            }
                        }
                    }
                }

            createDataSource(createEngine(body = sse(content))).stream(request = request()) shouldContainExactly listOf("결과")
        }

        test("CRLF 줄바꿈과 여러 줄의 data 필드를 한 조각으로 읽는다") {
            val event = chunk(text = "조각").toString()
            val half = event.length / 2
            val body = "data: ${event.substring(0, half)}" + CRLF + "data: ${event.substring(half)}" + CRLF + CRLF

            createDataSource(createEngine(body = body)).stream(request = request()) shouldContainExactly listOf("조각")
        }

        test("TC-cafafa06-f52a-4988-ab6e-164c6d4fd04b: Gemini의 실패 응답을 실패 이유로 나누어 알린다") {
            listOf<Pair<HttpClientEngine, (Throwable) -> Unit>>(
                errorEngine(HttpStatusCode.TooManyRequests, status = "RESOURCE_EXHAUSTED") to { it.shouldBeInstanceOf<GeminiException.QuotaExceeded>() },
                errorEngine(HttpStatusCode.NotFound, status = "NOT_FOUND") to { it.shouldBeInstanceOf<GeminiException.ModelNotFound>() },
                errorEngine(HttpStatusCode.InternalServerError, status = "INTERNAL") to { it.shouldBeInstanceOf<GeminiException.Server>() },
                errorEngine(HttpStatusCode.ServiceUnavailable, status = "UNAVAILABLE") to { it.shouldBeInstanceOf<GeminiException.Server>() },
                createEngine(body = sse(blocked())) to { it.shouldBeInstanceOf<GeminiException.Blocked>() },
                createEngine(body = sse(chunk(text = """{"title":"제""", finishReason = "SAFETY"))) to { it.shouldBeInstanceOf<GeminiException.Blocked>() },
                createEngine(body = sse(chunk(text = """{"title":"제""", finishReason = "MAX_TOKENS"))) to { it.shouldBeInstanceOf<GeminiException.Truncated>() },
                MockEngine { throw IOException("connection reset") } to { it.shouldBeInstanceOf<GeminiException.Network>() },
                MockEngine { throw ConnectTimeoutException("connect timeout") } to { it.shouldBeInstanceOf<GeminiException.Network>() },
            ).forEach { (engine, assertFailure) ->
                val actual = shouldThrow<GeminiException> { createDataSource(engine).stream(request = request()) }

                assertFailure(actual)
            }
        }

        test("TC-cafafa06-f52a-4988-ab6e-164c6d4fd04b: 조각이 대기 한도 안에 오지 않으면 응답 시간 초과로 알린다") {
            // 조각 사이 대기는 엔진이 지키므로, 엔진이 대기 한도를 넘겼다고 알리는 경우를 그대로 흉내 낸다.
            listOf<Throwable>(
                SocketTimeoutException("socket timeout"),
                HttpRequestTimeoutException(url = "https://generativelanguage.googleapis.com", timeoutMillis = null),
            ).forEach { timeout ->
                val engine = MockEngine { throw timeout }

                shouldThrow<GeminiException.Timeout> { createDataSource(engine).stream(request = request()) }
            }
        }

        test("TC-cafafa06-f52a-4988-ab6e-164c6d4fd04b: 위에 해당하지 않는 실패는 Gemini의 설명 문장과 함께 알린다") {
            val message = "Request contains an invalid argument. ${Arb.kotlinUuid().next()}"

            val actual =
                shouldThrow<GeminiException.Other> {
                    createDataSource(errorEngine(HttpStatusCode.BadRequest, status = "INVALID_ARGUMENT", message = message)).stream(request = request())
                }

            actual.description shouldBe message
        }

        test("인증 정보가 유효하지 않은 실패를 구분해 알린다") {
            listOf(
                errorEngine(HttpStatusCode.Unauthorized, status = "UNAUTHENTICATED"),
                errorEngine(HttpStatusCode.Forbidden, status = "PERMISSION_DENIED"),
                badRequestEngine(reason = API_KEY_INVALID_REASON),
            ).forEach { engine ->
                shouldThrow<GeminiException.InvalidApiKey> { createDataSource(engine).stream(request = request()) }
            }
        }

        test("TC-659c06e0-8cc6-4e57-8cbc-be3ed3159758: 읽을 수 없는 조각이나 비정상 종료는 구조화된 형태가 아닌 응답으로 알린다") {
            listOf(
                "data: {not json\n\n",
                sse(chunk(text = """{"title":"제""", finishReason = "RECITATION")),
            ).forEach { body ->
                shouldThrow<GeminiException.InvalidContent> { createDataSource(createEngine(body = body)).stream(request = request()) }
            }
        }

        test("TC-78cd381e-2737-42c3-b920-1a6b404d2d69: 실패에 생성된 내용과 보낸 내용을 담지 않고 종료 사유만 담는다") {
            val request = request()
            val generated = "생성된 문장 ${Arb.kotlinUuid().next()}"

            listOf(
                sse(chunk(text = generated, finishReason = "MAX_TOKENS")) to "MAX_TOKENS",
                sse(chunk(text = generated, finishReason = "SAFETY")) to "SAFETY",
                """data: {"candidates": [$generated""" + "\n\n" to "",
            ).forEach { (body, reason) ->
                val actual = shouldThrow<GeminiException> { createDataSource(createEngine(body = body)).stream(request = request) }

                actual.cause shouldBe null
                actual.message!! shouldContain reason
                listOf(generated, request.apiKey, request.systemInstruction, request.prompt).forEach { secret -> actual.message!! shouldNotContain secret }
            }
        }

        test("TC-78cd381e-2737-42c3-b920-1a6b404d2d69: 인증 실패에 인증 정보와 보낸 내용을 담지 않는다") {
            val request = request()
            listOf(
                errorEngine(HttpStatusCode.Unauthorized, status = "UNAUTHENTICATED"),
                badRequestEngine(reason = API_KEY_INVALID_REASON),
            ).forEach { engine ->
                val actual = shouldThrow<GeminiException.InvalidApiKey> { createDataSource(engine).stream(request = request) }

                actual.allMessages().forEach { message ->
                    listOf(request.apiKey, request.systemInstruction, request.prompt).forEach { secret -> message shouldNotContain secret }
                }
            }
        }

        test("TC-12677c0d-ad0b-4684-b74b-a79692f1ee8a 받은 조각을 읽지 못하면 원격 응답 실패 보고가 한 번 남는다") {
            val reportList = recordCrashlyticsLog()
            val request = request()

            shouldThrow<GeminiException.InvalidContent> { createDataSource(createEngine(body = "data: {not json\n\n")).stream(request = request) }

            val report = reportList.single()
            report.message shouldContain "POST generativelanguage.googleapis.com/v1beta/${request.model}:streamGenerateContent 200"
            report.message shouldContain "JsonDecodingException"
        }

        test("TC-78cd381e-2737-42c3-b920-1a6b404d2d69: 오류 보고에 생성된 내용과 보낸 내용을 담지 않는다") {
            val request = request()
            val generated = "생성된 문장 ${Arb.kotlinUuid().next()}"

            listOf(
                createEngine(body = """data: {"candidates": [$generated""" + "\n\n"),
                errorEngine(HttpStatusCode.ServiceUnavailable, status = "UNAVAILABLE", message = generated),
            ).forEach { engine ->
                val reportList = recordCrashlyticsLog()

                shouldThrow<GeminiException> { createDataSource(engine).stream(request = request) }

                val report = reportList.single()
                listOf(report.message, report.throwable.message.orEmpty(), report.issueKey).forEach { text ->
                    listOf(generated, request.apiKey, request.systemInstruction, request.prompt).forEach { secret -> text shouldNotContain secret }
                }
            }
        }
    }) {
    private companion object {
        private val json = Json

        private const val CRLF = "\r\n"

        private val SSE_HEADERS = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())

        private val SCHEMA: JsonObject =
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("title") { put("type", "string") }
                }
            }

        private data class Request(
            val apiKey: String,
            val model: String,
            val systemInstruction: String,
            val prompt: String,
        )

        private fun request(): Request =
            Request(
                apiKey = "apiKey${Arb.kotlinUuid().next()}",
                model = "models/gemini-${Arb.kotlinUuid().next()}",
                systemInstruction = "지시문 ${Arb.kotlinUuid().next()}",
                prompt = """{"prompt":"${Arb.kotlinUuid().next()}"}""",
            )

        private suspend fun GeminiContentRemoteDataSource.stream(
            request: Request,
            idleTimeout: Duration = 2.minutes,
        ): List<String> =
            streamStructuredContent(
                apiKey = request.apiKey,
                model = request.model,
                systemInstruction = request.systemInstruction,
                prompt = request.prompt,
                responseJsonSchema = SCHEMA,
                idleTimeout = idleTimeout,
            ).toList()

        private fun Throwable.allMessages(): List<String> = generateSequence(this) { throwable -> throwable.cause }.mapNotNull { throwable -> throwable.message }.toList()

        private fun HttpRequestData.bodyAsJsonObject(): JsonObject = json.parseToJsonElement((body as TextContent).text).jsonObject

        private fun JsonObject.textOf(key: String? = null): String {
            val content = if (key == null) this else this[key]!!.jsonObject

            return content["parts"]!!
                .jsonArray
                .single()
                .jsonObject["text"]!!
                .jsonPrimitive.content
        }

        private fun chunk(
            text: String,
            finishReason: String? = null,
        ): JsonObject =
            buildJsonObject {
                putJsonArray("candidates") {
                    addJsonObject {
                        putJsonObject("content") {
                            putJsonArray("parts") {
                                add(buildJsonObject { put("text", text) })
                            }
                        }
                        finishReason?.let { reason -> put("finishReason", reason) }
                    }
                }
            }

        private fun blocked(): JsonObject =
            buildJsonObject {
                putJsonArray("candidates") {}
                putJsonObject("promptFeedback") { put("blockReason", "SAFETY") }
            }

        private fun sse(vararg event: JsonObject): String = event.joinToString(separator = "") { value -> "data: $value\n\n" }

        private fun createEngine(body: String = sse(chunk(text = """{"title":"제목"}""", finishReason = "STOP"))): MockEngine =
            MockEngine {
                respond(content = body, status = HttpStatusCode.OK, headers = SSE_HEADERS)
            }

        private fun errorEngine(
            code: HttpStatusCode,
            status: String,
            message: String = code.description,
        ): MockEngine =
            MockEngine {
                respond(
                    content =
                        buildJsonObject {
                            putJsonObject("error") {
                                put("code", code.value)
                                put("message", message)
                                put("status", status)
                            }
                        }.toString(),
                    status = code,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
            val reportList = mutableListOf<CrashlyticsLog>()
            val delegate = mockk<DiaryLoggerDelegate>()
            every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
            DiaryLogger.add(delegate = delegate)
            return reportList
        }

        private fun createDataSource(engine: HttpClientEngine): GeminiContentRemoteDataSource =
            koinApplication<GeminiNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClient>(qualifier = named<GeminiHttpClient>()) { createGeminiHttpClient(json = get(named<GeminiJson>()), engine = engine) }
                    },
                )
            }.koin.get()
    }
}
