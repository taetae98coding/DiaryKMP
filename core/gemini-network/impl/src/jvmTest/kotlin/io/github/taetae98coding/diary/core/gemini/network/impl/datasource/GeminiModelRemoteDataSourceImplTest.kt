package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiModelRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.GeminiNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClientEngine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class GeminiModelRemoteDataSourceImplTest :
    FunSpec({
        test("TC-GEMINI-MODEL-LIST-DOMAIN-001: 조회는 최대 개수인 1000개를 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = API_KEY)

            engine.requestHistory
                .single()
                .url
                .parameters["pageSize"] shouldBe "1000"
        }

        test("TC-GEMINI-MODEL-LIST-DOMAIN-002: 내용 생성에 쓸 수 있는 모델만 결과에 포함한다") {
            val content =
                listModelsResponse(
                    model(name = "models/generate-only", methods = listOf("generateContent")),
                    model(name = "models/generate-and-count", methods = listOf("generateContent", "countTokens")),
                    model(name = "models/embed-only", methods = listOf("embedContent")),
                    model(name = "models/count-only", methods = listOf("countTokens")),
                    model(name = "models/image-only", methods = listOf("predict")),
                    model(name = "models/no-method", methods = emptyList()),
                )
            val dataSource = createDataSource(createEngine(content = content))

            dataSource.getAvailableModel(apiKey = API_KEY).map { model -> model.id } shouldContainExactly
                listOf("models/generate-only", "models/generate-and-count")
        }

        test("TC-GEMINI-MODEL-LIST-DOMAIN-003: 받은 순서를 그대로 유지한다") {
            val order = listOf("models/third", "models/first", "models/second")
            val content = listModelsResponse(*order.map { name -> model(name = name) }.toTypedArray())
            val engine = createEngine(content = content)
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = API_KEY).map { model -> model.id } shouldContainExactly order

            val parameters =
                engine.requestHistory
                    .single()
                    .url.parameters
            parameters.names() shouldBe setOf("pageSize")
        }

        test("TC-GEMINI-MODEL-LIST-DATA-001: 인증 정보를 담아 조회한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = API_KEY)

            val request = engine.requestHistory.single()
            request.method shouldBe HttpMethod.Get
            request.url.toString() shouldStartWith "https://generativelanguage.googleapis.com/v1beta/models"
            request.headers["x-goog-api-key"] shouldBe API_KEY
        }

        test("TC-GEMINI-MODEL-LIST-DATA-002: 성공 응답의 모델 정보를 그대로 전달한다") {
            val content =
                listModelsResponse(
                    model(name = "models/gemini-flash", displayName = "Gemini Flash", description = "빠른 범용 모델"),
                    model(name = "models/gemini-pro", displayName = "Gemini Pro", description = "정확한 범용 모델"),
                )
            val dataSource = createDataSource(createEngine(content = content))

            val actual = dataSource.getAvailableModel(apiKey = API_KEY)

            actual.size shouldBe 2
            actual[0].id shouldBe "models/gemini-flash"
            actual[0].displayName shouldBe "Gemini Flash"
            actual[0].description shouldBe "빠른 범용 모델"
            actual[1].id shouldBe "models/gemini-pro"
            actual[1].displayName shouldBe "Gemini Pro"
            actual[1].description shouldBe "정확한 범용 모델"
        }

        test("TC-GEMINI-MODEL-LIST-DATA-003: 설명이 없는 모델도 전달한다") {
            val content =
                listModelsResponse(
                    buildJsonObject {
                        put("name", "models/gemini-flash")
                        put("displayName", "Gemini Flash")
                        putJsonArray("supportedGenerationMethods") { add("generateContent") }
                    },
                )
            val dataSource = createDataSource(createEngine(content = content))

            val actual = dataSource.getAvailableModel(apiKey = API_KEY).single()

            actual.id shouldBe "models/gemini-flash"
            actual.displayName shouldBe "Gemini Flash"
            actual.description shouldBe ""
        }

        test("TC-GEMINI-MODEL-LIST-DATA-004: 기준에 맞는 모델이 없으면 빈 목록을 성공으로 전달한다") {
            val contents =
                listOf(
                    listModelsResponse(),
                    listModelsResponse(model(name = "models/embed-only", methods = listOf("embedContent"))),
                )

            contents.forEach { content ->
                val dataSource = createDataSource(createEngine(content = content))

                dataSource.getAvailableModel(apiKey = API_KEY).shouldBeEmpty()
            }
        }

        test("TC-GEMINI-MODEL-LIST-DATA-005: 인증 정보가 유효하지 않은 실패를 구분해 알린다") {
            listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden).forEach { status ->
                val dataSource = createDataSource(MockEngine { respondError(status) })

                val actual = shouldThrow<GeminiException.InvalidApiKey> { dataSource.getAvailableModel(apiKey = API_KEY) }

                actual.cause.shouldBeInstanceOf<ResponseException>()
            }
        }

        test("TC-GEMINI-MODEL-LIST-DATA-006: 그 밖의 실패를 실패로 알린다") {
            listOf(
                HttpStatusCode.BadRequest,
                HttpStatusCode.TooManyRequests,
                HttpStatusCode.InternalServerError,
            ).forEach { status ->
                val dataSource = createDataSource(MockEngine { respondError(status) })

                shouldThrow<ResponseException> { dataSource.getAvailableModel(apiKey = API_KEY) }
            }

            val brokenDataSource = createDataSource(createEngine(content = "{"))

            shouldThrow<Throwable> { brokenDataSource.getAvailableModel(apiKey = API_KEY) }
        }

        test("TC-GEMINI-MODEL-LIST-DATA-007: 조회할 때마다 새로 조회한다") {
            val contents =
                mutableListOf(
                    listModelsResponse(model(name = "models/first")),
                    listModelsResponse(model(name = "models/second")),
                )
            val engine =
                MockEngine {
                    respond(
                        content = contents.removeFirst(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = API_KEY).map { model -> model.id } shouldContainExactly listOf("models/first")
            dataSource.getAvailableModel(apiKey = API_KEY).map { model -> model.id } shouldContainExactly listOf("models/second")

            engine.requestHistory.size shouldBe 2
        }

        test("TC-GEMINI-MODEL-LIST-DOMAIN-004: 조회는 모델 정보만 받아 오고 내용을 생성하지 않는다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = API_KEY)

            val request = engine.requestHistory.single()

            request.method shouldBe HttpMethod.Get
            request.body.contentLength shouldBe 0L
            request.url.encodedPath shouldEndWith "/models"
        }
    }) {
    private companion object {
        private const val API_KEY = "testApiKey"

        private fun model(
            name: String,
            displayName: String = "Display $name",
            description: String = "Description $name",
            methods: List<String> = listOf("generateContent"),
        ) = buildJsonObject {
            put("name", name)
            put("displayName", displayName)
            put("description", description)
            putJsonArray("supportedGenerationMethods") {
                methods.forEach { method -> add(method) }
            }
        }

        private fun listModelsResponse(vararg models: kotlinx.serialization.json.JsonObject): String =
            buildJsonObject {
                putJsonArray("models") {
                    models.forEach { model -> add(model) }
                }
            }.toString()

        private fun createEngine(content: String = listModelsResponse(model(name = "models/gemini-flash"))): MockEngine =
            MockEngine {
                respond(
                    content = content,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): GeminiModelRemoteDataSource =
            koinApplication<GeminiNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<GeminiHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
