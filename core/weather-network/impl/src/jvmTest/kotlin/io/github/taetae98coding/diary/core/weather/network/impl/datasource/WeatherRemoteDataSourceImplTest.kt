package io.github.taetae98coding.diary.core.weather.network.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherLanguage
import io.github.taetae98coding.diary.core.weather.network.impl.WeatherNetworkTestKoinApplication
import io.github.taetae98coding.diary.core.weather.network.impl.WeatherNetworkTestKoinModule
import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClientEngine
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
import net.jqwik.api.Arbitraries
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import kotlin.time.Instant

class WeatherRemoteDataSourceImplTest :
    FunSpec({
        test("TC-WEATHER-FETCH-DOMAIN-001: 섭씨 단위로 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getCurrentWeather(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())
            dataSource.getForecast(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            engine.requestHistory.forEach { request ->
                request.url.parameters["units"] shouldBe "metric"
            }
            engine.requestHistory.size shouldBe 2
        }

        test("TC-WEATHER-FETCH-DOMAIN-007: 날씨 설명을 한국어로 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getCurrentWeather(latitude = randomLatitude(), longitude = randomLongitude(), language = WeatherLanguage.KOREAN)
            dataSource.getForecast(latitude = randomLatitude(), longitude = randomLongitude(), language = WeatherLanguage.KOREAN)

            engine.requestHistory.forEach { request ->
                request.url.parameters["lang"] shouldBe "kr"
            }
            engine.requestHistory.size shouldBe 2
        }

        test("TC-WEATHER-FETCH-DATA-001: 위치와 인증 정보를 담아 현재 날씨를 요청한다") {
            val config = WeatherNetworkTestKoinModule.config
            val latitude = randomLatitude()
            val longitude = randomLongitude()
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getCurrentWeather(latitude = latitude, longitude = longitude, language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            val request = engine.requestHistory.single()
            request.url.parameters["lat"] shouldBe latitude.toString()
            request.url.parameters["lon"] shouldBe longitude.toString()
            request.url.parameters["appid"] shouldBe config.appId
        }

        test("TC-WEATHER-FETCH-DATA-002: 성공 응답의 현재 날씨를 그대로 전달한다") {
            val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val engine = createEngine(currentWeatherContent = Json.encodeToString(currentWeather))
            val dataSource = createDataSource(engine)

            val actual = dataSource.getCurrentWeather(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            actual shouldBe currentWeather
        }

        test("TC-WEATHER-FETCH-DATA-004: 위치와 인증 정보를 담아 시간대별 예보를 요청한다") {
            val config = WeatherNetworkTestKoinModule.config
            val latitude = randomLatitude()
            val longitude = randomLongitude()
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getForecast(latitude = latitude, longitude = longitude, language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            val request = engine.requestHistory.single()
            request.url.parameters["lat"] shouldBe latitude.toString()
            request.url.parameters["lon"] shouldBe longitude.toString()
            request.url.parameters["appid"] shouldBe config.appId
        }

        test("TC-WEATHER-FETCH-DATA-005: 성공 응답의 예보 항목을 시각 순서대로 그대로 전달한다") {
            val forecast =
                fixtureMonkey.giveMeOne<ForecastRemoteEntity>().let { entity ->
                    entity.copy(list = List(5) { fixtureMonkey.giveMeOne<ForecastWeatherRemoteEntity>() })
                }
            val engine = createEngine(forecastContent = Json.encodeToString(forecast))
            val dataSource = createDataSource(engine)

            val actual = dataSource.getForecast(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            actual shouldBe forecast
        }

        test("TC-WEATHER-FETCH-DATA-007: 원격 조회 실패를 그대로 알린다") {
            listOf(
                HttpStatusCode.Unauthorized,
                HttpStatusCode.BadRequest,
                HttpStatusCode.InternalServerError,
            ).forEach { status ->
                val engine = MockEngine { respondError(status) }
                val dataSource = createDataSource(engine)

                shouldThrow<ResponseException> {
                    dataSource.getCurrentWeather(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())
                }
                shouldThrow<ResponseException> {
                    dataSource.getForecast(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())
                }
            }
        }

        test("TC-WEATHER-FETCH-DATA-021: 위치와 인증 정보를 담아 지역명을 요청한다") {
            val config = WeatherNetworkTestKoinModule.config
            val latitude = randomLatitude()
            val longitude = randomLongitude()
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getLocationName(latitude = latitude, longitude = longitude)

            val request = engine.requestHistory.single()
            request.url.parameters["lat"] shouldBe latitude.toString()
            request.url.parameters["lon"] shouldBe longitude.toString()
            request.url.parameters["appid"] shouldBe config.appId
        }

        test("TC-WEATHER-FETCH-DATA-022: 지역명 조회 실패를 그대로 알린다") {
            listOf(
                HttpStatusCode.Unauthorized,
                HttpStatusCode.BadRequest,
                HttpStatusCode.InternalServerError,
            ).forEach { status ->
                val engine = MockEngine { respondError(status) }
                val dataSource = createDataSource(engine)

                shouldThrow<ResponseException> {
                    dataSource.getLocationName(latitude = randomLatitude(), longitude = randomLongitude())
                }
            }
        }

        test("성공 응답의 지역 목록을 그대로 전달한다") {
            val locationNameList = List(2) { fixtureMonkey.giveMeOne<LocationNameRemoteEntity>() }
            val engine = createEngine(locationNameContent = Json.encodeToString(locationNameList))
            val dataSource = createDataSource(engine)

            val actual = dataSource.getLocationName(latitude = randomLatitude(), longitude = randomLongitude())

            actual shouldBe locationNameList
        }

        test("OpenWeatherMap 지역명 주소로 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getLocationName(latitude = randomLatitude(), longitude = randomLongitude())

            engine.requestHistory
                .single()
                .url
                .toString() shouldStartWith "https://api.openweathermap.org/geo/1.0/reverse"
        }

        test("OpenWeatherMap 현재 날씨 주소로 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getCurrentWeather(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            engine.requestHistory
                .single()
                .url
                .toString() shouldStartWith "https://api.openweathermap.org/data/2.5/weather"
        }

        test("OpenWeatherMap 시간대별 예보 주소로 요청한다") {
            val engine = createEngine()
            val dataSource = createDataSource(engine)

            dataSource.getForecast(latitude = randomLatitude(), longitude = randomLongitude(), language = fixtureMonkey.giveMeOne<WeatherLanguage>())

            engine.requestHistory
                .single()
                .url
                .toString() shouldStartWith "https://api.openweathermap.org/data/2.5/forecast"
        }
    }) {
    private companion object {
        // 2100-01-01T00:00:00Z
        private const val MAX_EPOCH_SECONDS = 4_102_444_800L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun createEngine(
            currentWeatherContent: String = Json.encodeToString(fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()),
            forecastContent: String = Json.encodeToString(fixtureMonkey.giveMeOne<ForecastRemoteEntity>()),
            locationNameContent: String = Json.encodeToString(listOf(fixtureMonkey.giveMeOne<LocationNameRemoteEntity>())),
        ): MockEngine =
            MockEngine { request ->
                val path = request.url.encodedPath
                val content =
                    when {
                        path.endsWith("reverse") -> locationNameContent
                        path.endsWith("forecast") -> forecastContent
                        else -> currentWeatherContent
                    }

                respond(
                    content = content,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        private fun createDataSource(engine: HttpClientEngine): WeatherRemoteDataSource =
            koinApplication<WeatherNetworkTestKoinApplication> {
                modules(
                    module {
                        single<HttpClientEngine>(qualifier = named<WeatherHttpClientEngine>()) { engine }
                    },
                )
            }.koin.get()

        private fun randomLatitude(): Double =
            Arbitraries
                .doubles()
                .between(-90.0, 90.0)
                .sample()

        private fun randomLongitude(): Double =
            Arbitraries
                .doubles()
                .between(-180.0, 180.0)
                .sample()
    }
}
