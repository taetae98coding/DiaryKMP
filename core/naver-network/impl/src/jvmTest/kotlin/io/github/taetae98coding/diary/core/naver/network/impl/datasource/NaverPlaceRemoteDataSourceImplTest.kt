package io.github.taetae98coding.diary.core.naver.network.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.naver.network.api.datasource.NaverPlaceRemoteDataSource
import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import io.github.taetae98coding.diary.core.naver.network.impl.NaverNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.naver.network.impl.NaverNetworkTestKoinModule
import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClientEngine
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.ResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class NaverPlaceRemoteDataSourceImplTest :
    FunSpec({
        test("TC-NAVER-PLACE-SEARCH-DOMAIN-001: 검색은 최대 개수인 5개를 요청한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>())

            engine.requestHistory
                .single()
                .url
                .parameters["display"] shouldBe "5"
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-001: 검색어와 인증 정보를 담아 기본 정렬로 요청한다") {
            val config = NaverNetworkTestKoinModule.config
            val query = fixtureMonkey.giveMeOne<String>()
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = query)

            val request = engine.requestHistory.single()
            request.url.parameters["query"] shouldBe query
            request.url.parameters["sort"] shouldBe null
            request.headers["X-Naver-Client-Id"] shouldBe config.clientId
            request.headers["X-Naver-Client-Secret"] shouldBe config.clientSecret
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-002: 성공 응답의 장소 목록을 그대로 전달한다") {
            val places = fixtureMonkey.giveMe<NaverPlaceRemoteEntity>(5)
            val engine = createSuccessEngine(places)
            val dataSource = createDataSource(engine)

            val actual = dataSource.search(query = fixtureMonkey.giveMeOne<String>())

            actual shouldBe places
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-003: 결과가 없으면 빈 목록을 성공으로 전달한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            val actual = dataSource.search(query = fixtureMonkey.giveMeOne<String>())

            actual shouldBe emptyList()
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-004: 원격 조회 실패를 그대로 알린다") {
            listOf(
                HttpStatusCode.Unauthorized,
                HttpStatusCode.BadRequest,
                HttpStatusCode.InternalServerError,
            ).forEach { status ->
                val engine = MockEngine { respondError(status) }
                val dataSource = createDataSource(engine)

                shouldThrow<ResponseException> {
                    dataSource.search(query = fixtureMonkey.giveMeOne<String>())
                }
            }
        }

        test("TC-NAVER-PLACE-SEARCH-DATA-006: 좌표가 빠진 장소가 섞여 있어도 검색은 실패하지 않고 그 장소를 좌표 없이 받은 순서대로 전달한다") {
            val places = fixtureMonkey.giveMe<NaverPlaceRemoteEntity>(3)
            val items =
                JsonArray(
                    listOf(
                        Json.encodeToJsonElement(places[0]),
                        JsonObject(Json.encodeToJsonElement(places[1]).jsonObject - "mapx" - "mapy"),
                        JsonObject(Json.encodeToJsonElement(places[2]).jsonObject - "mapy"),
                    ),
                )
            val dataSource = createDataSource(createEngine(content = successContent(items = items, count = places.size)))

            val actual = dataSource.search(query = fixtureMonkey.giveMeOne<String>())

            actual shouldBe
                listOf(
                    places[0],
                    places[1].copy(mapx = "", mapy = ""),
                    places[2].copy(mapy = ""),
                )
        }

        test("네이버 지역 검색 주소로 요청한다") {
            val engine = createSuccessEngine(emptyList())
            val dataSource = createDataSource(engine)

            dataSource.search(query = fixtureMonkey.giveMeOne<String>())

            engine.requestHistory
                .single()
                .url
                .toString() shouldStartWith "https://openapi.naver.com/v1/search/local.json"
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun createSuccessEngine(places: List<NaverPlaceRemoteEntity>): MockEngine = createEngine(content = successContent(items = Json.encodeToJsonElement(places), count = places.size))

        private fun createEngine(content: String): MockEngine =
            MockEngine {
                respond(
                    content = content,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun successContent(
            items: JsonElement,
            count: Int,
        ): String =
            buildJsonObject {
                put("lastBuildDate", "Mon, 01 Jan 2026 00:00:00 +0900")
                put("total", count)
                put("start", 1)
                put("display", count)
                put("items", items)
            }.toString()

        private fun createDataSource(engine: HttpClientEngine): NaverPlaceRemoteDataSource =
            koinApplication<NaverNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<NaverHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()
    }
}
