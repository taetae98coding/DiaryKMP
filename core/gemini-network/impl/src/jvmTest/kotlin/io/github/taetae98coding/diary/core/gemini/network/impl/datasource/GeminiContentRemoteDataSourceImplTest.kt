package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.GeminiNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClientEngine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
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

class GeminiContentRemoteDataSourceImplTest :
    FunSpec({
        test("TC-MEMO-GEMINI-DATA-001: 인증 정보와 모델을 담아 생성을 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.generateStructuredContent(
                apiKey = API_KEY,
                model = MODEL,
                systemInstruction = SYSTEM_INSTRUCTION,
                prompt = PROMPT,
                responseSchema = SCHEMA,
            )

            val request = engine.requestHistory.single()

            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldBe "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash:generateContent"
            request.headers["x-goog-api-key"] shouldBe API_KEY
        }

        test("요청에 지시문과 프롬프트, 받을 구조를 담는다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.generateStructuredContent(
                apiKey = API_KEY,
                model = MODEL,
                systemInstruction = SYSTEM_INSTRUCTION,
                prompt = PROMPT,
                responseSchema = SCHEMA,
            )

            val body = engine.requestHistory.single().bodyAsJsonObject()

            body.textOf("systemInstruction") shouldBe SYSTEM_INSTRUCTION
            body["contents"]!!
                .jsonArray
                .single()
                .jsonObject
                .textOf() shouldBe PROMPT
            body["generationConfig"]!!.jsonObject["responseMimeType"]!!.jsonPrimitive.content shouldBe "application/json"
            body["generationConfig"]!!.jsonObject["responseSchema"] shouldBe SCHEMA
        }

        test("TC-MEMO-GEMINI-DATA-003: 지시문이 비어 있으면 요청에 담지 않는다") {
            listOf("", "   ").forEach { blank ->
                val engine = createEngine()
                val dataSource = createDataSource(engine)

                dataSource.generateStructuredContent(
                    apiKey = API_KEY,
                    model = MODEL,
                    systemInstruction = blank,
                    prompt = PROMPT,
                    responseSchema = SCHEMA,
                )

                engine.requestHistory.single().bodyAsJsonObject() shouldNotContainKey "systemInstruction"
            }
        }

        test("구조를 지킨 응답을 그대로 전달한다") {
            val content = generateContentResponse("""{"title":"제목","description":"설명"}""")
            val dataSource = createDataSource(createEngine(content = content))

            val actual =
                dataSource.generateStructuredContent(
                    apiKey = API_KEY,
                    model = MODEL,
                    systemInstruction = SYSTEM_INSTRUCTION,
                    prompt = PROMPT,
                    responseSchema = SCHEMA,
                )

            actual["title"]!!.jsonPrimitive.content shouldBe "제목"
            actual["description"]!!.jsonPrimitive.content shouldBe "설명"
        }

        test("여러 조각으로 나뉘어 온 응답을 이어 붙여 전달한다") {
            val content =
                buildJsonObject {
                    putJsonArray("candidates") {
                        add(
                            buildJsonObject {
                                putJsonObject("content") {
                                    putJsonArray("parts") {
                                        add(buildJsonObject { put("text", """{"title":"제""") })
                                        add(buildJsonObject { put("text", """목"}""") })
                                    }
                                }
                            },
                        )
                    }
                }.toString()
            val dataSource = createDataSource(createEngine(content = content))

            val actual =
                dataSource.generateStructuredContent(
                    apiKey = API_KEY,
                    model = MODEL,
                    systemInstruction = SYSTEM_INSTRUCTION,
                    prompt = PROMPT,
                    responseSchema = SCHEMA,
                )

            actual["title"]!!.jsonPrimitive.content shouldBe "제목"
        }

        test("TC-MEMO-GEMINI-DATA-005: 구조를 지키지 않은 응답을 인증 실패와 구분해 알린다") {
            listOf(
                generateContentResponse(""),
                generateContentResponse("생성된 문장입니다"),
                generateContentResponse("[1, 2, 3]"),
                buildJsonObject { putJsonArray("candidates") {} }.toString(),
            ).forEach { content ->
                val dataSource = createDataSource(createEngine(content = content))

                val actual =
                    shouldThrow<GeminiException.InvalidContent> {
                        dataSource.generateStructuredContent(
                            apiKey = API_KEY,
                            model = MODEL,
                            systemInstruction = SYSTEM_INSTRUCTION,
                            prompt = PROMPT,
                            responseSchema = SCHEMA,
                        )
                    }

                actual shouldNotBe null
            }
        }

        test("인증 정보가 유효하지 않은 실패를 구분해 알린다") {
            listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden).forEach { status ->
                val dataSource = createDataSource(MockEngine { respondError(status) })

                shouldThrow<GeminiException.InvalidApiKey> {
                    dataSource.generateStructuredContent(
                        apiKey = API_KEY,
                        model = MODEL,
                        systemInstruction = SYSTEM_INSTRUCTION,
                        prompt = PROMPT,
                        responseSchema = SCHEMA,
                    )
                }
            }
        }

        test("TC-MEMO-GEMINI-DATA-006: 그 밖의 실패를 실패로 알린다") {
            listOf(
                HttpStatusCode.BadRequest,
                HttpStatusCode.TooManyRequests,
                HttpStatusCode.InternalServerError,
            ).forEach { status ->
                val dataSource = createDataSource(MockEngine { respondError(status) })

                shouldThrow<ResponseException> {
                    dataSource.generateStructuredContent(
                        apiKey = API_KEY,
                        model = MODEL,
                        systemInstruction = SYSTEM_INSTRUCTION,
                        prompt = PROMPT,
                        responseSchema = SCHEMA,
                    )
                }
            }
        }
    }) {
    private companion object {
        private const val API_KEY = "testApiKey"
        private const val MODEL = "models/gemini-flash"
        private const val SYSTEM_INSTRUCTION = "testSystemInstruction"
        private const val PROMPT = """{"prompt":"회고를 써 줘"}"""

        private val json = Json

        private val SCHEMA: JsonObject =
            buildJsonObject {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("title") { put("type", "STRING") }
                }
            }

        private infix fun JsonObject.shouldNotContainKey(key: String) {
            containsKey(key) shouldBe false
        }

        private fun HttpRequestData.bodyAsJsonObject(): JsonObject = json.parseToJsonElement((body as TextContent).text).jsonObject

        private fun JsonObject.textOf(key: String? = null): String {
            val content = if (key == null) this else this[key]!!.jsonObject

            return content["parts"]!!
                .jsonArray
                .single()
                .jsonObject["text"]!!
                .jsonPrimitive.content
        }

        private fun generateContentResponse(text: String): String =
            buildJsonObject {
                putJsonArray("candidates") {
                    add(
                        buildJsonObject {
                            putJsonObject("content") {
                                putJsonArray("parts") {
                                    add(buildJsonObject { put("text", text) })
                                }
                            }
                        },
                    )
                }
            }.toString()

        private fun createEngine(content: String = generateContentResponse("""{"title":"제목"}""")): MockEngine =
            MockEngine {
                respond(
                    content = content,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): GeminiContentRemoteDataSource =
            koinApplication<GeminiNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<GeminiHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
