package io.github.taetae98coding.diary.core.google.network.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.google.network.api.datasource.GooglePlaceRemoteDataSource
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceDisplayNameRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBias
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.GoogleNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.google.network.impl.GoogleNetworkTestKoinModule
import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClientEngine
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class GooglePlaceRemoteDataSourceImplTest :
    FunSpec({
        test("TC-GOOGLE-PLACE-SEARCH-DOMAIN-001: 검색은 최대 개수인 20개를 요청한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            engine.requestHistory
                .single()
                .jsonBody()["pageSize"]
                ?.jsonPrimitive
                ?.int shouldBe 20
        }

        test("TC-GOOGLE-PLACE-SEARCH-DOMAIN-002: 검색 결과를 한국어 기준으로 요청한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            engine.requestHistory
                .single()
                .jsonBody()["languageCode"]
                ?.jsonPrimitive
                ?.content shouldBe "ko"
        }

        test("TC-GOOGLE-PLACE-SEARCH-DOMAIN-003: 기준 위치를 지정하면 좌표와 반경이 요청에 포함된다") {
            val locationBias = locationBias(radiusMeters = WITHIN_LIMIT_RADIUS_METERS)
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = locationBias)

            val body = engine.requestHistory.single().jsonBody()
            val circle = body["locationBias"]?.jsonObject?.get("circle")?.jsonObject

            circle
                ?.get("center")
                ?.jsonObject
                ?.get("latitude")
                ?.jsonPrimitive
                ?.double shouldBe locationBias.latitude
            circle
                ?.get("center")
                ?.jsonObject
                ?.get("longitude")
                ?.jsonPrimitive
                ?.double shouldBe locationBias.longitude
            circle?.get("radius")?.jsonPrimitive?.double shouldBe locationBias.radiusMeters
            body["locationRestriction"] shouldBe null
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-005: 기준 위치의 반경이 상한을 넘으면 상한으로 요청한다") {
            val locationBias = locationBias(radiusMeters = MAX_RADIUS_METERS + WITHIN_LIMIT_RADIUS_METERS)
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = locationBias)

            val body = engine.requestHistory.single().jsonBody()

            body["locationBias"]
                ?.jsonObject
                ?.get("circle")
                ?.jsonObject
                ?.get("radius")
                ?.jsonPrimitive
                ?.double shouldBe MAX_RADIUS_METERS
        }

        test("TC-GOOGLE-PLACE-SEARCH-DOMAIN-004: 기준 위치를 지정하지 않으면 요청에 위치 조건이 없다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            val body = engine.requestHistory.single().jsonBody()

            body["locationBias"] shouldBe null
            body["locationRestriction"] shouldBe null
        }

        test("TC-GOOGLE-PLACE-SEARCH-DOMAIN-005: 정의된 장소 정보만 요청한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            engine.requestHistory
                .single()
                .headers["X-Goog-FieldMask"]
                .orEmpty()
                .split(",") shouldContainExactlyInAnyOrder
                listOf(
                    "places.id",
                    "places.displayName",
                    "places.location",
                    "places.types",
                    "places.formattedAddress",
                    "places.shortFormattedAddress",
                    "places.googleMapsUri",
                )
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-001: 검색어와 인증 정보를 담아 기본 정렬로 요청한다") {
            val config = GoogleNetworkTestKoinModule.config
            val query = fixtureMonkey.giveMeOne<String>()
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = query, locationBias = null)

            val request = engine.requestHistory.single()

            request.jsonBody()["textQuery"]?.jsonPrimitive?.content shouldBe query
            request.jsonBody()["rankPreference"] shouldBe null
            request.headers["X-Goog-Api-Key"] shouldBe config.apiKey
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-002: 성공 응답의 장소 목록을 그대로 전달한다") {
            val places = fixtureMonkey.giveMe<GooglePlaceRemoteEntity>(20)
            val engine = createSuccessEngine(places)
            val dataSource = createDataSource(engine)

            val actual = dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            actual shouldBe places
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-003: 결과가 없으면 빈 목록을 성공으로 전달한다") {
            val engine = createEngine(content = "{}")
            val dataSource = createDataSource(engine)

            val actual = dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            actual shouldBe emptyList()
        }

        test("TC-GOOGLE-PLACE-SEARCH-DATA-004: 원격 조회 실패를 그대로 알린다") {
            listOf(
                HttpStatusCode.Unauthorized,
                HttpStatusCode.BadRequest,
                HttpStatusCode.TooManyRequests,
                HttpStatusCode.InternalServerError,
            ).forEach { status ->
                val engine = MockEngine { respondError(status) }
                val dataSource = createDataSource(engine)

                shouldThrow<ResponseException> {
                    dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)
                }
            }
        }

        test("Google 장소 텍스트 검색 주소로 요청한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null)

            val request = engine.requestHistory.single()

            request.method shouldBe HttpMethod.Post
            request.url.toString() shouldStartWith "https://places.googleapis.com/v1/places:searchText"
            request.body.contentType?.withoutParameters() shouldBe ContentType.Application.Json
        }

        test("응답에 없는 선택 정보는 빈 값으로 전달한다") {
            val place =
                GooglePlaceRemoteEntity(
                    id = fixtureMonkey.giveMeOne<String>(),
                    displayName = fixtureMonkey.giveMeOne<GooglePlaceDisplayNameRemoteEntity>(),
                    location = fixtureMonkey.giveMeOne<GooglePlaceLocationRemoteEntity>(),
                )
            val engine = createSuccessEngine(listOf(place))
            val dataSource = createDataSource(engine)

            val actual = dataSource.search(query = fixtureMonkey.giveMeOne<String>(), locationBias = null).single()

            actual.types shouldBe emptyList()
            actual.formattedAddress shouldBe ""
            actual.shortFormattedAddress shouldBe ""
            actual.googleMapsUri shouldBe ""
        }
    }) {
    private companion object {
        private const val MAX_RADIUS_METERS = 50_000.0
        private const val WITHIN_LIMIT_RADIUS_METERS = 1_000.0

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun locationBias(radiusMeters: Double): GooglePlaceLocationBias =
            fixtureMonkey
                .giveMeKotlinBuilder<GooglePlaceLocationBias>()
                .setExp(GooglePlaceLocationBias::radiusMeters, radiusMeters)
                .sample()

        private fun HttpRequestData.jsonBody(): JsonObject = Json.parseToJsonElement((body as TextContent).text).jsonObject

        private fun createSuccessEngine(places: List<GooglePlaceRemoteEntity>): MockEngine = createEngine(content = successContent(places))

        private fun createEngine(content: String): MockEngine =
            MockEngine {
                respond(
                    content = content,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun successContent(places: List<GooglePlaceRemoteEntity>): String =
            buildJsonObject {
                put("places", Json.encodeToJsonElement(places))
            }.toString()

        private fun createDataSource(engine: HttpClientEngine): GooglePlaceRemoteDataSource =
            koinApplication<GoogleNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<GoogleHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
