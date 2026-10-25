package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiModelRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.API_KEY_INVALID_REASON
import io.github.taetae98coding.diary.core.gemini.network.impl.GeminiNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.gemini.network.impl.badRequestEngine
import io.github.taetae98coding.diary.core.gemini.network.impl.createGeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.ktor.client.HttpClient
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
import kotlin.uuid.Uuid

class GeminiModelRemoteDataSourceImplTest :
    FunSpec({
        test("TC-412278da-bcc1-4b72-96cb-742895a3aca1: 조회는 최대 개수인 1000개를 요청한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = apiKey)

            engine.requestHistory
                .single()
                .url
                .parameters["pageSize"] shouldBe "1000"
        }

        test("TC-f818e1e6-d876-497c-9979-4f3afa182843: 내용 생성에 쓸 수 있는 모델만 결과에 포함한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val generateOnly = modelName()
            val generateAndCount = modelName()
            val content =
                listModelsResponse(
                    model(name = generateOnly, methods = listOf("generateContent")),
                    model(name = generateAndCount, methods = listOf("generateContent", "countTokens")),
                    model(name = modelName(), methods = listOf("embedContent")),
                    model(name = modelName(), methods = listOf("countTokens")),
                    model(name = modelName(), methods = listOf("predict")),
                    model(name = modelName(), methods = emptyList()),
                )
            val dataSource = createDataSource(createEngine(content = content))

            dataSource.getAvailableModel(apiKey = apiKey).map { model -> model.id } shouldContainExactly
                listOf(generateOnly, generateAndCount)
        }

        test("TC-d663817b-9698-4ecf-82b0-8c82d5a750f3: 받은 순서를 그대로 유지한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val order = List(3) { modelName() }
            val content = listModelsResponse(*order.map { name -> model(name = name) }.toTypedArray())
            val engine = createEngine(content = content)
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = apiKey).map { model -> model.id } shouldContainExactly order

            val parameters =
                engine.requestHistory
                    .single()
                    .url.parameters
            parameters.names() shouldBe setOf("pageSize")
        }

        test("TC-aa003166-63bd-4035-a7ad-ebd30f51f83a: 인증 정보를 담아 조회한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = apiKey)

            val request = engine.requestHistory.single()
            request.method shouldBe HttpMethod.Get
            request.url.toString() shouldStartWith "https://generativelanguage.googleapis.com/v1beta/models"
            request.headers["x-goog-api-key"] shouldBe apiKey
        }

        test("TC-0f8409dd-24ce-4374-b402-7f86d29d4f0c: 성공 응답의 모델 정보를 그대로 전달한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val expected =
                List(2) {
                    Triple(modelName(), "Gemini ${fixtureMonkey.giveMeOne<Uuid>()}", "설명 ${fixtureMonkey.giveMeOne<Uuid>()}")
                }
            val content =
                listModelsResponse(
                    *expected.map { (name, displayName, description) -> model(name = name, displayName = displayName, description = description) }.toTypedArray(),
                )
            val dataSource = createDataSource(createEngine(content = content))

            val actual = dataSource.getAvailableModel(apiKey = apiKey)

            actual.map { model -> Triple(model.id, model.displayName, model.description) } shouldContainExactly expected
        }

        test("TC-c3829ff3-9290-4b6b-89f1-53b81d343d87: 설명이 없는 모델도 전달한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val name = modelName()
            val displayName = "Gemini ${fixtureMonkey.giveMeOne<Uuid>()}"
            val content =
                listModelsResponse(
                    buildJsonObject {
                        put("name", name)
                        put("displayName", displayName)
                        putJsonArray("supportedGenerationMethods") { add("generateContent") }
                    },
                )
            val dataSource = createDataSource(createEngine(content = content))

            val actual = dataSource.getAvailableModel(apiKey = apiKey).single()

            actual.id shouldBe name
            actual.displayName shouldBe displayName
            actual.description shouldBe ""
        }

        test("TC-15f7886d-ad40-4789-b8d8-83e02bfd6897: 기준에 맞는 모델이 없으면 빈 목록을 성공으로 전달한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val contents =
                listOf(
                    listModelsResponse(),
                    listModelsResponse(model(name = modelName(), methods = listOf("embedContent"))),
                )

            contents.forEach { content ->
                val dataSource = createDataSource(createEngine(content = content))

                dataSource.getAvailableModel(apiKey = apiKey).shouldBeEmpty()
            }
        }

        test("TC-dea588b7-4afa-480c-83dc-5124ef512c60: 인증 정보가 유효하지 않은 실패를 구분해 알린다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            listOf(
                MockEngine { respondError(HttpStatusCode.Unauthorized) },
                MockEngine { respondError(HttpStatusCode.Forbidden) },
                badRequestEngine(reason = API_KEY_INVALID_REASON),
            ).forEach { engine ->
                val dataSource = createDataSource(engine)

                val actual = shouldThrow<GeminiException.InvalidApiKey> { dataSource.getAvailableModel(apiKey = apiKey) }

                actual.cause.shouldBeInstanceOf<ResponseException>()
            }
        }

        test("TC-20ea17d1-f65b-44dc-a808-189c1bffa637: 그 밖의 실패를 실패로 알린다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            listOf(
                MockEngine { respondError(HttpStatusCode.BadRequest) },
                badRequestEngine(reason = OTHER_BAD_REQUEST_REASON),
                MockEngine { respondError(HttpStatusCode.TooManyRequests) },
                MockEngine { respondError(HttpStatusCode.InternalServerError) },
            ).forEach { engine ->
                val dataSource = createDataSource(engine)

                shouldThrow<ResponseException> { dataSource.getAvailableModel(apiKey = apiKey) }.shouldNotBeInstanceOf<GeminiException.InvalidApiKey>()
            }

            val brokenDataSource = createDataSource(createEngine(content = "{"))

            shouldThrow<Throwable> { brokenDataSource.getAvailableModel(apiKey = apiKey) }.shouldNotBeInstanceOf<GeminiException.InvalidApiKey>()
        }

        test("TC-977b79c7-04bc-4bde-a1dc-271cf67cfb8e: 조회할 때마다 새로 조회한다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val first = modelName()
            val second = modelName()
            val contents =
                mutableListOf(
                    listModelsResponse(model(name = first)),
                    listModelsResponse(model(name = second)),
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

            dataSource.getAvailableModel(apiKey = apiKey).map { model -> model.id } shouldContainExactly listOf(first)
            dataSource.getAvailableModel(apiKey = apiKey).map { model -> model.id } shouldContainExactly listOf(second)

            engine.requestHistory.size shouldBe 2
        }

        test("TC-50450572-6788-4784-8fbf-264fcf89cf61: 조회는 모델 정보만 받아 오고 내용을 생성하지 않는다") {
            val apiKey = "apiKey${fixtureMonkey.giveMeOne<Uuid>()}"
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getAvailableModel(apiKey = apiKey)

            val request = engine.requestHistory.single()

            request.method shouldBe HttpMethod.Get
            request.body.contentLength shouldBe 0L
            request.url.encodedPath shouldEndWith "/models"
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private const val OTHER_BAD_REQUEST_REASON = "INVALID_ARGUMENT"

        private fun modelName(): String = "models/gemini-${fixtureMonkey.giveMeOne<Uuid>()}"

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

        private fun createEngine(content: String = listModelsResponse(model(name = modelName()))): MockEngine =
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
                        single<HttpClient>(qualifier = named<GeminiHttpClient>()) { createGeminiHttpClient(json = get(named<GeminiJson>()), engine = engine) }
                    },
                )
            }.koin.get()
    }
}
