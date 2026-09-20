package io.github.taetae98coding.diary.domain.weather.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherReport
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private const val YEAR = 2026
private const val MONTH = 7
private const val FORECAST_INTERVAL_HOUR = 3
private const val LAST_FORECAST_HOUR = 21

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetCurrentCalendarWeatherUseCaseTest :
    BehaviorSpec({
        Given("TC-WEATHER-FETCH-DOMAIN-003 여러 날짜의 현재 날씨와 예보가 순서 없이 저장되어 있다") {
            val firstDate = LocalDate(year = YEAR, month = MONTH, day = 1)
            val secondDate = LocalDate(year = YEAR, month = MONTH, day = 2)
            val current = currentWeather(day = 1, hour = 6)
            val firstDateForecast = forecastWeather(day = 1, hour = 9)
            val earlySecondDateForecast = forecastWeather(day = 2, hour = 3)
            val lateSecondDateForecast = forecastWeather(day = 2, hour = 12)
            val useCase =
                useCase(
                    listOf(
                        lateSecondDateForecast,
                        firstDateForecast,
                        current,
                        earlySecondDateForecast,
                    ),
                )

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual = useCase(parameter = Unit).first().shouldBeSuccess().weatherList

                Then("날짜와 시각 순서로 모든 날씨를 묶어 제공한다") {
                    actual.map { calendarWeather -> calendarWeather.date } shouldBe listOf(firstDate, secondDate)
                    actual[0].weatherList shouldBe listOf(current, firstDateForecast)
                    actual[1].weatherList shouldBe listOf(earlySecondDateForecast, lateSecondDateForecast)
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-010 오늘의 현재 날씨와 예보가 서로 다른 시각과 현재 기온으로 저장되어 있다") {
            val fartherTemperature = fixtureMonkey.giveMeOne<Double>()
            val closestTemperature = fartherTemperature + positiveDouble()
            val current =
                currentWeather(
                    day = 1,
                    hour = 6,
                    currentTemperature = fartherTemperature,
                )
            val forecast =
                forecastWeather(
                    day = 1,
                    hour = 9,
                    currentTemperature = closestTemperature,
                )
            val useCase = useCase(weatherList = listOf(forecast, current), currentHour = 8)

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual =
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeSuccess()
                        .weatherList
                        .single()

                Then("오늘은 현재 시각과 가장 가까운 날씨의 현재 기온과 모든 날씨 정보를 제공한다") {
                    actual.temperature shouldBe
                        CalendarWeatherTemperature.Current(
                            value = closestTemperature,
                        )
                    actual.weatherList shouldBe listOf(current, forecast)
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-005 오늘이 아닌 날짜에 하루 전체에 걸친 여러 시간대별 예보가 저장되어 있다") {
            val current = currentWeather(day = 1, hour = 6)
            val lowestMin = fixtureMonkey.giveMeOne<Double>()
            val highestMax = lowestMin + positiveDouble()
            val earlyForecast =
                forecastWeather(
                    day = 2,
                    hour = 0,
                    min = lowestMin + positiveDouble(),
                    max = highestMax,
                )
            val lateForecast =
                forecastWeather(
                    day = 2,
                    hour = LAST_FORECAST_HOUR,
                    min = lowestMin,
                    max = highestMax - positiveDouble(),
                )
            val useCase = useCase(listOf(current, lateForecast, earlyForecast))

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual =
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeSuccess()
                        .weatherList
                        .single { calendarWeather -> calendarWeather.date.day == 2 }

                Then("가장 낮은 최저 기온과 가장 높은 최고 기온을 제공한다") {
                    actual.temperature shouldBe
                        CalendarWeatherTemperature.MinMax(
                            min = lowestMin,
                            max = highestMax,
                        )
                    actual.weatherList shouldBe listOf(earlyForecast, lateForecast)
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-006 저장된 날씨가 없다") {
            val useCase = useCase(emptyList())

            When("캘린더 날짜별 날씨를 관찰한다") {
                Then("빈 목록과 빈 지역명을 제공한다") {
                    val actual = useCase(parameter = Unit).first().shouldBeSuccess()

                    actual.weatherList shouldBe emptyList()
                    actual.locationName shouldBe ""
                }
            }
        }

        Given("저장된 날씨와 지역명이 함께 있다") {
            val locationName = fixtureMonkey.giveMeOne<String>()
            val current = currentWeather(day = 1, hour = 6)
            val useCase = useCase(weatherList = listOf(current), locationName = locationName)

            When("캘린더 날짜별 날씨를 관찰한다") {
                Then("날짜별 날씨와 같은 항목으로 지역명을 함께 제공한다") {
                    val actual = useCase(parameter = Unit).first().shouldBeSuccess()

                    actual.weatherList.single().weatherList shouldBe listOf(current)
                    actual.locationName shouldBe locationName
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-011 오늘의 현재 시각과 같은 거리인 두 날씨가 저장되어 있다") {
            val earlyTemperature = fixtureMonkey.giveMeOne<Double>()
            val lateTemperature = earlyTemperature + positiveDouble()
            val earlyForecast =
                forecastWeather(
                    day = 1,
                    hour = 6,
                    currentTemperature = earlyTemperature,
                )
            val lateForecast =
                forecastWeather(
                    day = 1,
                    hour = 10,
                    currentTemperature = lateTemperature,
                )
            val useCase = useCase(weatherList = listOf(lateForecast, earlyForecast), currentHour = 8)

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual =
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeSuccess()
                        .weatherList
                        .single()

                Then("시각이 이른 날씨의 현재 기온을 제공한다") {
                    actual.temperature shouldBe
                        CalendarWeatherTemperature.Current(
                            value = earlyTemperature,
                        )
                    actual.weatherList shouldBe listOf(earlyForecast, lateForecast)
                }
            }
        }

        listOf(
            LAST_FORECAST_HOUR to true,
            LAST_FORECAST_HOUR - FORECAST_INTERVAL_HOUR to false,
            0 to false,
        ).forEach { (lastHour, isTemperatureProvided) ->
            Given("TC-WEATHER-FETCH-DOMAIN-012 오늘이 아닌 마지막 날짜의 예보가 0시부터 ${lastHour}시까지만 저장되어 있다") {
                val forecastList = dayForecastList(day = 2, firstHour = 0, lastHour = lastHour)
                val useCase = useCase(listOf(currentWeather(day = 1, hour = 6)) + forecastList)

                When("캘린더 날짜별 날씨를 관찰한다") {
                    val actual =
                        useCase(parameter = Unit)
                            .first()
                            .shouldBeSuccess()
                            .weatherList
                            .single { calendarWeather -> calendarWeather.date.day == 2 }

                    if (isTemperatureProvided) {
                        Then("가장 낮은 최저 기온과 가장 높은 최고 기온을 제공한다") {
                            actual.temperature shouldBe
                                CalendarWeatherTemperature.MinMax(
                                    min = forecastList.minOf { forecast -> forecast.temperature.min },
                                    max = forecastList.maxOf { forecast -> forecast.temperature.max },
                                )
                        }
                    } else {
                        Then("온도 표시를 제공하지 않는다") {
                            actual.temperature shouldBe CalendarWeatherTemperature.None
                        }
                    }
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-013 가장 이른 날씨가 오늘이 아닌 날짜의 0시보다 늦은 시각의 예보다") {
            val useCase =
                useCase(
                    dayForecastList(day = 2, firstHour = FORECAST_INTERVAL_HOUR, lastHour = LAST_FORECAST_HOUR) +
                        dayForecastList(day = 3, firstHour = 0, lastHour = LAST_FORECAST_HOUR),
                )

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual = useCase(parameter = Unit).first().shouldBeSuccess().weatherList

                Then("하루 시작이 빠진 날짜는 온도 표시를 제공하지 않고 하루 전체가 들어온 날짜는 최저·최고 기온을 제공한다") {
                    actual.single { calendarWeather -> calendarWeather.date.day == 2 }.temperature shouldBe
                        CalendarWeatherTemperature.None
                    actual
                        .single { calendarWeather -> calendarWeather.date.day == 3 }
                        .temperature
                        .shouldBeInstanceOf<CalendarWeatherTemperature.MinMax>()
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-014 오늘이 아닌 마지막 날짜에 하루의 일부 시간대만 저장되어 있다") {
            val current = currentWeather(day = 1, hour = 6)
            val earlyForecast = forecastWeather(day = 2, hour = 0)
            val lateForecast = forecastWeather(day = 2, hour = FORECAST_INTERVAL_HOUR)
            val useCase = useCase(listOf(lateForecast, current, earlyForecast))

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual = useCase(parameter = Unit).first().shouldBeSuccess().weatherList

                Then("온도 표시가 없는 날짜도 날짜 순서로 남고 날씨 정보를 시각 순서대로 제공한다") {
                    actual.map { calendarWeather -> calendarWeather.date.day } shouldBe listOf(1, 2)
                    actual[1].temperature shouldBe CalendarWeatherTemperature.None
                    actual[1].weatherList shouldBe listOf(earlyForecast, lateForecast)
                }
            }
        }

        Given("TC-WEATHER-FETCH-DOMAIN-015 오늘의 날씨가 현재 시각 이후 일부 시간대만 저장되어 있다") {
            val closestTemperature = fixtureMonkey.giveMeOne<Double>()
            val closestForecast =
                forecastWeather(
                    day = 1,
                    hour = 9,
                    currentTemperature = closestTemperature,
                )
            val fartherForecast =
                forecastWeather(
                    day = 1,
                    hour = 12,
                    currentTemperature = closestTemperature + positiveDouble(),
                )
            val useCase = useCase(weatherList = listOf(fartherForecast, closestForecast), currentHour = 8)

            When("캘린더 날짜별 날씨를 관찰한다") {
                val actual =
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeSuccess()
                        .weatherList
                        .single()

                Then("하루 전체가 들어오지 않아도 현재 시각과 가장 가까운 날씨의 현재 기온을 제공한다") {
                    actual.temperature shouldBe
                        CalendarWeatherTemperature.Current(
                            value = closestTemperature,
                        )
                }
            }
        }

        Given("TC-WEATHER-FETCH-DATA-014 캘린더 날짜별 날씨를 관찰하고 있다") {
            val weatherState = MutableStateFlow<WeatherReport?>(null)
            val repository = mockk<WeatherRepository>()
            every { repository.get() } returns weatherState
            val useCase =
                GetCurrentCalendarWeatherUseCase(
                    weatherRepository = repository,
                    clock = clock(),
                )
            val current = currentWeather(day = 1, hour = 6)

            When("저장된 날씨가 새 동기화 결과로 바뀐다") {
                useCase(parameter = Unit).test {
                    awaitItem().shouldBeSuccess().weatherList shouldBe emptyList()

                    weatherState.value = weatherReport(weatherList = listOf(current))

                    Then("별도 조회 없이 새 날짜별 날씨를 제공한다") {
                        awaitItem().shouldBeSuccess().weatherList shouldBe
                            listOf(
                                CalendarWeather(
                                    date = LocalDate(year = YEAR, month = MONTH, day = 1),
                                    temperature =
                                        CalendarWeatherTemperature.Current(
                                            value = current.temperature.current,
                                        ),
                                    weatherList = listOf(current),
                                ),
                            )
                    }

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        Given("저장된 날씨 관찰이 실패한다") {
            val failure = GetCurrentCalendarWeatherTestException(fixtureMonkey.giveMeOne())
            val repository = mockk<WeatherRepository>()
            every { repository.get() } returns
                flow {
                    throw failure
                }
            val useCase =
                GetCurrentCalendarWeatherUseCase(
                    weatherRepository = repository,
                    clock = clock(),
                )

            When("캘린더 날짜별 날씨를 관찰한다") {
                Then("실패 원인을 그대로 제공한다") {
                    useCase(parameter = Unit).first().shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }
    })

private class GetCurrentCalendarWeatherTestException(
    message: String,
) : RuntimeException(message)

private fun useCase(
    weatherList: List<Weather>,
    currentHour: Int = 0,
    locationName: String = "",
): GetCurrentCalendarWeatherUseCase {
    val repository = mockk<WeatherRepository>()
    every { repository.get() } returns MutableStateFlow(weatherReport(weatherList = weatherList, locationName = locationName))

    return GetCurrentCalendarWeatherUseCase(
        weatherRepository = repository,
        clock = clock(hour = currentHour),
    )
}

/** 저장된 날씨의 시간 구간이 예보 간격으로 끝나는 저장 상태를 만든다. */
private fun weatherReport(
    weatherList: List<Weather>,
    locationName: String = "",
): WeatherReport? {
    if (weatherList.isEmpty()) return null

    return WeatherReport(
        weatherList = weatherList,
        coverage =
            weatherList.minOf { weather -> weather.dateTime }..<weatherList.maxOf { weather -> weather.dateTime } + FORECAST_INTERVAL_HOUR.hours,
        locationName = locationName,
    )
}

private fun clock(hour: Int = 0): Clock =
    mockk<Clock>().also { clock ->
        every { clock.now() } returns instant(day = 1, hour = hour)
    }

private fun currentWeather(
    day: Int,
    hour: Int,
    currentTemperature: Double = fixtureMonkey.giveMeOne(),
): Weather =
    fixtureMonkey
        .giveMeOne<Weather>()
        .copy(
            dateTime = instant(day = day, hour = hour),
            temperature =
                fixtureMonkey
                    .giveMeOne<WeatherTemperature>()
                    .copy(current = currentTemperature),
        )

private fun forecastWeather(
    day: Int,
    hour: Int,
    min: Double = fixtureMonkey.giveMeOne(),
    max: Double = fixtureMonkey.giveMeOne(),
    currentTemperature: Double = fixtureMonkey.giveMeOne(),
): Weather =
    fixtureMonkey
        .giveMeOne<Weather>()
        .copy(
            dateTime = instant(day = day, hour = hour),
            temperature =
                fixtureMonkey
                    .giveMeOne<WeatherTemperature>()
                    .copy(
                        current = currentTemperature,
                        min = min,
                        max = max,
                    ),
        )

private fun dayForecastList(
    day: Int,
    firstHour: Int,
    lastHour: Int,
): List<Weather> =
    (firstHour..lastHour step FORECAST_INTERVAL_HOUR).map { hour ->
        forecastWeather(day = day, hour = hour)
    }

private fun instant(
    day: Int,
    hour: Int,
): Instant =
    LocalDateTime(
        year = YEAR,
        month = MONTH,
        day = day,
        hour = hour,
        minute = 0,
    ).toInstant(TimeZone.currentSystemDefault())

private fun positiveDouble(): Double = fixtureMonkey.giveMeOne<Double>().absoluteValue % 100.0 + 1.0
