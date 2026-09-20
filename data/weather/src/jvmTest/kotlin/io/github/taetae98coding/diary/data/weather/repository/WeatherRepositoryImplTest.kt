package io.github.taetae98coding.diary.data.weather.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ipnetwork.api.entity.IpRemoteEntity
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherConditionRemoteEntity
import io.github.taetae98coding.diary.data.weather.datasource.WeatherLocalDataSource
import io.github.taetae98coding.diary.data.weather.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WeatherRepositoryImplTest :
    FunSpec({
        test("TC-WEATHER-FETCH-DOMAIN-008 디바이스 위치를 우선 사용한다") {
            val deviceLocation = fixtureMonkey.giveMeOne<Location>()
            val ipLocation = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } returns ipLocation
            val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
            every { weatherRemoteDataSource.forecastInterval } returns 3.hours
            coEvery { weatherRemoteDataSource.getCurrentWeather(any(), any()) } returns fixtureMonkey.giveMeOne()
            coEvery { weatherRemoteDataSource.getForecast(any(), any()) } returns fixtureMonkey.giveMeOne()
            val repository =
                repository(
                    locationProvider = locationProvider(location = deviceLocation),
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            repository.fetch()

            coVerify(exactly = 1) {
                weatherRemoteDataSource.getCurrentWeather(
                    latitude = deviceLocation.latitude,
                    longitude = deviceLocation.longitude,
                )
            }
            coVerify(exactly = 1) {
                weatherRemoteDataSource.getForecast(
                    latitude = deviceLocation.latitude,
                    longitude = deviceLocation.longitude,
                )
            }
            coVerify(exactly = 0) { ipRemoteDataSource.get() }
        }

        test("TC-WEATHER-FETCH-DOMAIN-009 디바이스에서 위치를 확인하지 못하면 공인 IP 기준 위치를 사용한다") {
            val ipLocation = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } returns ipLocation
            val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
            every { weatherRemoteDataSource.forecastInterval } returns 3.hours
            coEvery { weatherRemoteDataSource.getCurrentWeather(any(), any()) } returns fixtureMonkey.giveMeOne()
            coEvery { weatherRemoteDataSource.getForecast(any(), any()) } returns fixtureMonkey.giveMeOne()
            val repository =
                repository(
                    locationProvider = locationProvider(location = null),
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            repository.fetch()

            coVerify(exactly = 1) {
                weatherRemoteDataSource.getCurrentWeather(
                    latitude = ipLocation.latitude,
                    longitude = ipLocation.longitude,
                )
            }
            coVerify(exactly = 1) {
                weatherRemoteDataSource.getForecast(
                    latitude = ipLocation.latitude,
                    longitude = ipLocation.longitude,
                )
            }
        }

        test("TC-WEATHER-FETCH-DATA-008 확인한 위치 하나를 세 요청에 함께 사용한다") {
            val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val forecast = fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } returns location
            val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
            every { weatherRemoteDataSource.forecastInterval } returns 3.hours
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            coEvery {
                weatherRemoteDataSource.getCurrentWeather(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            } returns currentWeather
            coEvery {
                weatherRemoteDataSource.getForecast(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            } returns forecast

            repository.fetch()

            coVerify(exactly = 1) {
                weatherRemoteDataSource.getCurrentWeather(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
            coVerify(exactly = 1) {
                weatherRemoteDataSource.getForecast(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
            coVerify(exactly = 1) {
                weatherRemoteDataSource.getLocationName(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
        }

        test("TC-WEATHER-FETCH-DATA-009 현재 날씨, 시간대별 예보와 지역명을 동시에 요청한다") {
            val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val currentStarted = CompletableDeferred<Unit>()
            val forecastStarted = CompletableDeferred<Unit>()
            val locationNameStarted = CompletableDeferred<Unit>()
            val currentResponse = CompletableDeferred<CurrentWeatherRemoteEntity>()
            val forecastResponse = CompletableDeferred<ForecastRemoteEntity>()
            val locationNameResponse = CompletableDeferred<List<LocationNameRemoteEntity>>()
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } returns location
            val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
            every { weatherRemoteDataSource.forecastInterval } returns 3.hours
            coEvery { weatherRemoteDataSource.getCurrentWeather(any(), any()) } coAnswers {
                currentStarted.complete(Unit)
                currentResponse.await()
            }
            coEvery { weatherRemoteDataSource.getForecast(any(), any()) } coAnswers {
                forecastStarted.complete(Unit)
                forecastResponse.await()
            }
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } coAnswers {
                locationNameStarted.complete(Unit)
                locationNameResponse.await()
            }
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            coroutineScope {
                val fetch = async { repository.fetch() }

                withTimeout(5.seconds) {
                    currentStarted.await()
                    forecastStarted.await()
                    locationNameStarted.await()
                }

                currentResponse.complete(fixtureMonkey.giveMeOne())
                forecastResponse.complete(fixtureMonkey.giveMeOne())
                locationNameResponse.complete(emptyList())
                fetch.await()
            }
        }

        test("TC-WEATHER-FETCH-DATA-010 두 날씨 응답을 하나의 목록으로 저장한다") {
            val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val forecast =
                fixtureMonkey
                    .giveMeOne<ForecastRemoteEntity>()
                    .copy(
                        list =
                            listOf(
                                fixtureMonkey.giveMeOne<ForecastWeatherRemoteEntity>(),
                                fixtureMonkey.giveMeOne<ForecastWeatherRemoteEntity>(),
                            ),
                    )
            val repository =
                successfulRepository(
                    location = location,
                    currentWeather = currentWeather,
                    forecast = forecast,
                )

            repository.fetch()

            repository
                .get()
                .first()
                .shouldNotBeNull()
                .weatherList shouldBe
                buildList {
                    add(currentWeather.toDomain())
                    addAll(forecast.list.map { remote -> remote.toDomain() })
                }
        }

        test("TC-WEATHER-FETCH-DATA-011 다시 동기화하면 이전 날씨를 새 결과로 교체한다") {
            val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val oldCurrentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val oldForecast = fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
            val newCurrentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val newForecast = fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } returns location
            val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
            every { weatherRemoteDataSource.forecastInterval } returns 3.hours
            coEvery {
                weatherRemoteDataSource.getCurrentWeather(any(), any())
            } returns oldCurrentWeather andThen newCurrentWeather
            coEvery {
                weatherRemoteDataSource.getForecast(any(), any())
            } returns oldForecast andThen newForecast
            var now = fixtureMonkey.giveMeOne<Instant>()
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    clock = clock { now },
                )

            repository.fetch()
            now += 1.hours
            repository.fetch()

            repository
                .get()
                .first()
                .shouldNotBeNull()
                .weatherList shouldBe
                buildList {
                    add(newCurrentWeather.toDomain())
                    addAll(newForecast.list.map { remote -> remote.toDomain() })
                }
        }

        test("TC-WEATHER-FETCH-DATA-013 동기화 일부가 실패하면 이전 날씨를 유지한다") {
            FailurePoint.entries.forEach { failurePoint ->
                val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
                val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
                val forecast = fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
                val failure = WeatherRepositoryTestException(fixtureMonkey.giveMeOne())
                val ipRemoteDataSource = mockk<IpRemoteDataSource>()
                coEvery { ipRemoteDataSource.get() } returns location
                val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
                coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
                every { weatherRemoteDataSource.forecastInterval } returns 3.hours
                coEvery { weatherRemoteDataSource.getCurrentWeather(any(), any()) } returns currentWeather
                coEvery { weatherRemoteDataSource.getForecast(any(), any()) } returns forecast
                var now = fixtureMonkey.giveMeOne<Instant>()
                val repository =
                    repository(
                        ipRemoteDataSource = ipRemoteDataSource,
                        weatherRemoteDataSource = weatherRemoteDataSource,
                        clock = clock { now },
                    )
                repository.fetch()
                now += 1.hours
                val previousWeatherReport = repository.get().first()

                if (failurePoint == FailurePoint.IP) {
                    coEvery { ipRemoteDataSource.get() } throws failure
                }
                if (failurePoint == FailurePoint.CURRENT_WEATHER) {
                    coEvery {
                        weatherRemoteDataSource.getCurrentWeather(any(), any())
                    } throws failure
                }
                if (failurePoint == FailurePoint.FORECAST) {
                    coEvery {
                        weatherRemoteDataSource.getForecast(any(), any())
                    } throws failure
                }

                shouldThrowExactly<WeatherRepositoryTestException> {
                    repository.fetch()
                } shouldBeSameInstanceAs failure
                repository.get().first() shouldBe previousWeatherReport
            }
        }

        test("TC-WEATHER-FETCH-DATA-014 새 동기화 결과를 관찰 중인 캘린더 날씨에 전달한다") {
            val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val forecast = fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
            val repository =
                successfulRepository(
                    location = location,
                    currentWeather = currentWeather,
                    forecast = forecast,
                )
            val expected =
                buildList<Weather> {
                    add(currentWeather.toDomain())
                    addAll(forecast.list.map { remote -> remote.toDomain() })
                }

            repository.get().test {
                awaitItem() shouldBe null

                repository.fetch()

                awaitItem().shouldNotBeNull().weatherList shouldBe expected
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-WEATHER-FETCH-DATA-015 날씨 아이콘 식별자를 이미지 주소로 바꿔 저장한다") {
            val currentIcon = "01d"
            val forecastIcon = "10d"
            val currentWeather =
                fixtureMonkey
                    .giveMeOne<CurrentWeatherRemoteEntity>()
                    .withIcon(icon = currentIcon)
            val forecastWeather =
                fixtureMonkey
                    .giveMeOne<ForecastWeatherRemoteEntity>()
                    .withIcon(icon = forecastIcon)
            val forecast =
                fixtureMonkey
                    .giveMeOne<ForecastRemoteEntity>()
                    .copy(list = listOf(forecastWeather))
            val repository =
                successfulRepository(
                    location = fixtureMonkey.giveMeOne(),
                    currentWeather = currentWeather,
                    forecast = forecast,
                )

            repository.fetch()

            val weatherList =
                repository
                    .get()
                    .first()
                    .shouldNotBeNull()
                    .weatherList
            weatherList[0].conditionList.single().imageUrl shouldBe
                "https://openweathermap.org/img/wn/$currentIcon.png"
            weatherList[1].conditionList.single().imageUrl shouldBe
                "https://openweathermap.org/img/wn/$forecastIcon.png"
        }

        test("TC-WEATHER-FETCH-DATA-016 Night 아이콘 식별자를 Day 아이콘 이미지 주소로 바꿔 저장한다") {
            val currentWeather =
                fixtureMonkey
                    .giveMeOne<CurrentWeatherRemoteEntity>()
                    .withIcon(icon = "01n")
            val forecastWeather =
                fixtureMonkey
                    .giveMeOne<ForecastWeatherRemoteEntity>()
                    .withIcon(icon = "10n")
            val forecast =
                fixtureMonkey
                    .giveMeOne<ForecastRemoteEntity>()
                    .copy(list = listOf(forecastWeather))
            val repository =
                successfulRepository(
                    location = fixtureMonkey.giveMeOne(),
                    currentWeather = currentWeather,
                    forecast = forecast,
                )

            repository.fetch()

            val weatherList =
                repository
                    .get()
                    .first()
                    .shouldNotBeNull()
                    .weatherList
            weatherList[0].conditionList.single().imageUrl shouldBe
                "https://openweathermap.org/img/wn/01d.png"
            weatherList[1].conditionList.single().imageUrl shouldBe
                "https://openweathermap.org/img/wn/10d.png"
        }

        test("TC-WEATHER-FETCH-DOMAIN-018 마지막 동기화 성공에서 1시간이 지나야 다시 조회한다") {
            val elapsedList =
                listOf(
                    59.minutes to false,
                    1.hours to true,
                    61.minutes to true,
                )

            elapsedList.forEach { (elapsed, isRefetched) ->
                val weatherRemoteDataSource = weatherRemoteDataSource()
                var now = fixtureMonkey.giveMeOne<Instant>()
                val repository =
                    repository(
                        ipRemoteDataSource = ipRemoteDataSource(),
                        weatherRemoteDataSource = weatherRemoteDataSource,
                        clock = clock { now },
                    )

                repository.fetch()
                now += elapsed
                repository.fetch()

                weatherRemoteDataSource.verifyFetchCount(count = if (isRefetched) 2 else 1)
            }
        }

        test("TC-WEATHER-FETCH-DOMAIN-019 한 번도 동기화에 성공하지 않았으면 조회한다") {
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            repository.fetch()

            weatherRemoteDataSource.verifyFetchCount(count = 1)
        }

        test("TC-WEATHER-FETCH-DOMAIN-020 위치 권한 허용 계기는 간격과 무관하게 조회한다") {
            val weatherRemoteDataSource = weatherRemoteDataSource()
            var now = fixtureMonkey.giveMeOne<Instant>()
            val locationProvider = locationProvider(location = null)
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    locationProvider = locationProvider,
                    clock = clock { now },
                )

            repository.fetch()
            now += 59.minutes
            repository.refresh()

            coVerify(exactly = 2) { locationProvider.getCurrentLocation() }
            weatherRemoteDataSource.verifyFetchCount(count = 2)
        }

        test("TC-WEATHER-FETCH-DOMAIN-021 실패한 동기화는 다음 요청의 조회를 막지 않는다") {
            FailurePoint.entries.forEach { failurePoint ->
                val failure = WeatherRepositoryTestException(fixtureMonkey.giveMeOne())
                val ipRemoteDataSource = ipRemoteDataSource()
                val weatherRemoteDataSource = weatherRemoteDataSource()
                if (failurePoint == FailurePoint.IP) {
                    coEvery { ipRemoteDataSource.get() } throws failure andThen fixtureMonkey.giveMeOne<IpRemoteEntity>()
                }
                if (failurePoint == FailurePoint.CURRENT_WEATHER) {
                    coEvery {
                        weatherRemoteDataSource.getCurrentWeather(any(), any())
                    } throws failure andThen fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
                }
                if (failurePoint == FailurePoint.FORECAST) {
                    coEvery {
                        weatherRemoteDataSource.getForecast(any(), any())
                    } throws failure andThen fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
                }
                var now = fixtureMonkey.giveMeOne<Instant>()
                val repository =
                    repository(
                        ipRemoteDataSource = ipRemoteDataSource,
                        weatherRemoteDataSource = weatherRemoteDataSource,
                        clock = clock { now },
                    )

                shouldThrowExactly<WeatherRepositoryTestException> {
                    repository.fetch()
                } shouldBeSameInstanceAs failure
                now += 1.minutes
                repository.fetch()

                repository
                    .get()
                    .first()
                    .shouldNotBeNull()
                    .weatherList
                    .shouldNotBeEmpty()
            }
        }

        test("TC-WEATHER-FETCH-DOMAIN-022 조회한 동기화가 성공할 때마다 간격 기준 시각이 갱신된다") {
            val weatherRemoteDataSource = weatherRemoteDataSource()
            var now = fixtureMonkey.giveMeOne<Instant>()
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    clock = clock { now },
                )

            repository.fetch()
            now += 1.hours
            repository.fetch()
            now += 59.minutes
            repository.fetch()

            weatherRemoteDataSource.verifyFetchCount(count = 2)
        }

        test("TC-WEATHER-FETCH-DATA-017 조회하지 않은 동기화는 저장된 날씨를 그대로 유지한다") {
            val ipRemoteDataSource = ipRemoteDataSource()
            val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
            every { weatherRemoteDataSource.forecastInterval } returns 3.hours
            coEvery {
                weatherRemoteDataSource.getCurrentWeather(any(), any())
            } returns fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>() andThen fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            coEvery {
                weatherRemoteDataSource.getForecast(any(), any())
            } returns fixtureMonkey.giveMeOne<ForecastRemoteEntity>() andThen fixtureMonkey.giveMeOne<ForecastRemoteEntity>()
            var now = fixtureMonkey.giveMeOne<Instant>()
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    clock = clock { now },
                )
            repository.fetch()
            val previousWeatherReport = repository.get().first()

            now += 59.minutes
            repository.fetch()

            repository.get().first() shouldBe previousWeatherReport
        }

        test("TC-WEATHER-FETCH-DATA-018 앱 프로세스를 새로 시작하면 첫 동기화가 다시 조회한다") {
            val ipRemoteDataSource = ipRemoteDataSource()
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val previousRepository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    clock = clock(now = now),
                )
            previousRepository.fetch()

            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                    weatherLocalDataSource = WeatherLocalDataSource(),
                    clock = clock(now = now),
                )
            repository.fetch()

            weatherRemoteDataSource.verifyFetchCount(count = 2)
        }

        test("TC-WEATHER-FETCH-DATA-019 저장된 날씨의 시간 구간을 함께 제공한다") {
            val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val forecast =
                fixtureMonkey
                    .giveMeOne<ForecastRemoteEntity>()
                    .copy(
                        list =
                            listOf(
                                fixtureMonkey.giveMeOne<ForecastWeatherRemoteEntity>(),
                                fixtureMonkey.giveMeOne<ForecastWeatherRemoteEntity>(),
                            ),
                    )
            val weatherList =
                buildList {
                    add(currentWeather.toDomain())
                    addAll(forecast.list.map { remote -> remote.toDomain() })
                }
            val repository =
                successfulRepository(
                    location = fixtureMonkey.giveMeOne(),
                    currentWeather = currentWeather,
                    forecast = forecast,
                )

            repository.fetch()

            repository
                .get()
                .first()
                .shouldNotBeNull()
                .coverage shouldBe
                weatherList.minOf { weather -> weather.dateTime }..<weatherList.maxOf { weather -> weather.dateTime } + 3.hours
        }

        test("TC-WEATHER-FETCH-DATA-020 저장된 날씨가 없으면 시간 구간도 제공하지 않는다") {
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource(),
                )

            repository.get().first() shouldBe null
        }

        test("TC-WEATHER-FETCH-DATA-021 위치와 인증 정보를 담아 지역명을 요청한다") {
            val location = fixtureMonkey.giveMeOne<IpRemoteEntity>()
            val ipRemoteDataSource = mockk<IpRemoteDataSource>()
            coEvery { ipRemoteDataSource.get() } returns location
            val weatherRemoteDataSource = weatherRemoteDataSource()
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource,
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            repository.fetch()

            coVerify(exactly = 1) {
                weatherRemoteDataSource.getLocationName(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
        }

        test("TC-WEATHER-FETCH-DATA-023 응답에서 지역명 하나를 정해 날씨와 함께 저장한다") {
            val seongnam = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Seongnam-si", localNames = mapOf("ko" to "성남시"))
            val yongin = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Yongin-si", localNames = mapOf("ko" to "용인시"))
            val paris = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Paris", localNames = emptyMap())

            mapOf(
                listOf(seongnam) to "성남시",
                listOf(paris) to "Paris",
                listOf(seongnam, yongin) to "성남시",
                emptyList<LocationNameRemoteEntity>() to "",
            ).forEach { (response, expected) ->
                val weatherRemoteDataSource = weatherRemoteDataSource()
                coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns response
                val repository =
                    repository(
                        ipRemoteDataSource = ipRemoteDataSource(),
                        weatherRemoteDataSource = weatherRemoteDataSource,
                    )

                repository.fetch()

                repository
                    .get()
                    .first()
                    ?.locationName
                    .orEmpty() shouldBe expected
            }
        }

        test("TC-WEATHER-FETCH-DATA-024 지역명만 실패해도 동기화는 성공하고 날씨를 저장한다") {
            val currentWeather = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()
            val weatherRemoteDataSource = weatherRemoteDataSource()
            coEvery { weatherRemoteDataSource.getCurrentWeather(any(), any()) } returns currentWeather
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } throws WeatherRepositoryTestException(fixtureMonkey.giveMeOne())
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            repository.fetch()

            repository
                .get()
                .first()
                ?.locationName
                .orEmpty() shouldBe ""
            repository
                .get()
                .first()
                .shouldNotBeNull()
                .weatherList
                .first() shouldBe currentWeather.toDomain()
        }

        test("TC-WEATHER-FETCH-DATA-025 다시 동기화하면 지역명도 새 결과로 교체한다") {
            val seongnam = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Seongnam-si", localNames = mapOf("ko" to "성남시"))
            val yongin = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Yongin-si", localNames = mapOf("ko" to "용인시"))
            val fetchedAt = fixtureMonkey.giveMeOne<Instant>()

            listOf(
                Result.success(listOf(yongin)) to "용인시",
                Result.failure<List<LocationNameRemoteEntity>>(WeatherRepositoryTestException("지역명 조회 실패")) to "",
            ).forEach { (secondResponse, expected) ->
                var now = fetchedAt
                val weatherRemoteDataSource = weatherRemoteDataSource()
                coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns listOf(seongnam)
                val repository =
                    repository(
                        ipRemoteDataSource = ipRemoteDataSource(),
                        weatherRemoteDataSource = weatherRemoteDataSource,
                        clock = clock { now },
                    )

                repository.fetch()
                repository
                    .get()
                    .first()
                    ?.locationName
                    .orEmpty() shouldBe "성남시"

                coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } coAnswers { secondResponse.getOrThrow() }
                now = fetchedAt + 1.hours
                repository.fetch()

                repository
                    .get()
                    .first()
                    ?.locationName
                    .orEmpty() shouldBe expected
            }
        }

        test("TC-WEATHER-FETCH-DATA-026 날씨 조회가 실패하면 이전 지역명을 유지한다") {
            val seongnam = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Seongnam-si", localNames = mapOf("ko" to "성남시"))
            val fetchedAt = fixtureMonkey.giveMeOne<Instant>()

            val failWeatherRequestList =
                listOf<(WeatherRemoteDataSource, Throwable) -> Unit>(
                    { dataSource, failure -> coEvery { dataSource.getCurrentWeather(any(), any()) } throws failure },
                    { dataSource, failure -> coEvery { dataSource.getForecast(any(), any()) } throws failure },
                )

            failWeatherRequestList.forEach { failWeatherRequest ->
                var now = fetchedAt
                val weatherRemoteDataSource = weatherRemoteDataSource()
                coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns listOf(seongnam)
                val repository =
                    repository(
                        ipRemoteDataSource = ipRemoteDataSource(),
                        weatherRemoteDataSource = weatherRemoteDataSource,
                        clock = clock { now },
                    )

                repository.fetch()

                failWeatherRequest(weatherRemoteDataSource, WeatherRepositoryTestException(fixtureMonkey.giveMeOne()))
                coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
                now = fetchedAt + 1.hours

                shouldThrowExactly<WeatherRepositoryTestException> { repository.fetch() }

                repository
                    .get()
                    .first()
                    ?.locationName
                    .orEmpty() shouldBe "성남시"
            }
        }

        test("TC-WEATHER-FETCH-DATA-027 동기화 전에는 지역명을 제공하지 않는다") {
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource(),
                )

            repository
                .get()
                .first()
                ?.locationName
                .orEmpty() shouldBe ""
        }

        test("TC-WEATHER-FETCH-DATA-028 새 지역명을 관찰 중인 곳에 전달한다") {
            val seongnam = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Seongnam-si", localNames = mapOf("ko" to "성남시"))
            val weatherRemoteDataSource = weatherRemoteDataSource()
            coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns listOf(seongnam)
            val repository =
                repository(
                    ipRemoteDataSource = ipRemoteDataSource(),
                    weatherRemoteDataSource = weatherRemoteDataSource,
                )

            repository.get().test {
                awaitItem() shouldBe null

                repository.fetch()

                awaitItem()?.locationName shouldBe "성남시"
            }
        }
    })

private enum class FailurePoint {
    IP,
    CURRENT_WEATHER,
    FORECAST,
}

private class WeatherRepositoryTestException(
    message: String,
) : RuntimeException(message)

private fun locationProvider(location: Location?): LocationProvider {
    val locationProvider = mockk<LocationProvider>()
    coEvery { locationProvider.getCurrentLocation() } returns location

    return locationProvider
}

private fun ipRemoteDataSource(): IpRemoteDataSource =
    mockk<IpRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.get() } returns fixtureMonkey.giveMeOne()
    }

private fun weatherRemoteDataSource(): WeatherRemoteDataSource =
    mockk<WeatherRemoteDataSource>().also { dataSource ->
        coEvery { dataSource.getLocationName(any(), any()) } returns emptyList()
        every { dataSource.forecastInterval } returns 3.hours
        coEvery { dataSource.getCurrentWeather(any(), any()) } returns fixtureMonkey.giveMeOne()
        coEvery { dataSource.getForecast(any(), any()) } returns fixtureMonkey.giveMeOne()
    }

private fun WeatherRemoteDataSource.verifyFetchCount(count: Int) {
    coVerify(exactly = count) { getCurrentWeather(any(), any()) }
    coVerify(exactly = count) { getForecast(any(), any()) }
}

private fun clock(now: () -> Instant): Clock =
    mockk<Clock>().also { clock ->
        every { clock.now() } answers { now() }
    }

private fun clock(now: Instant): Clock = clock { now }

private fun repository(
    ipRemoteDataSource: IpRemoteDataSource,
    weatherRemoteDataSource: WeatherRemoteDataSource,
    locationProvider: LocationProvider = locationProvider(location = null),
    weatherLocalDataSource: WeatherLocalDataSource = WeatherLocalDataSource(),
    clock: Clock = clock(now = fixtureMonkey.giveMeOne<Instant>()),
): WeatherRepositoryImpl =
    WeatherRepositoryImpl(
        locationProvider = locationProvider,
        ipRemoteDataSource = ipRemoteDataSource,
        weatherRemoteDataSource = weatherRemoteDataSource,
        weatherLocalDataSource = weatherLocalDataSource,
        clock = clock,
    )

private fun successfulRepository(
    location: IpRemoteEntity,
    currentWeather: CurrentWeatherRemoteEntity,
    forecast: ForecastRemoteEntity,
): WeatherRepositoryImpl {
    val ipRemoteDataSource = mockk<IpRemoteDataSource>()
    coEvery { ipRemoteDataSource.get() } returns location
    val weatherRemoteDataSource = mockk<WeatherRemoteDataSource>()
    coEvery { weatherRemoteDataSource.getLocationName(any(), any()) } returns emptyList()
    every { weatherRemoteDataSource.forecastInterval } returns 3.hours
    coEvery { weatherRemoteDataSource.getCurrentWeather(any(), any()) } returns currentWeather
    coEvery { weatherRemoteDataSource.getForecast(any(), any()) } returns forecast

    return repository(
        ipRemoteDataSource = ipRemoteDataSource,
        weatherRemoteDataSource = weatherRemoteDataSource,
    )
}

private fun CurrentWeatherRemoteEntity.withIcon(icon: String): CurrentWeatherRemoteEntity = copy(weather = listOf(fixtureMonkey.giveMeOne<WeatherConditionRemoteEntity>().copy(icon = icon)))

private fun ForecastWeatherRemoteEntity.withIcon(icon: String): ForecastWeatherRemoteEntity = copy(weather = listOf(fixtureMonkey.giveMeOne<WeatherConditionRemoteEntity>().copy(icon = icon)))
