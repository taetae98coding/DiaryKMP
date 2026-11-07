package io.github.taetae98coding.diary.data.weather.cache

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.testing.weather.weather
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlin.time.Instant

class WeatherReportCacheTest :
    FunSpec({
        test("TC-e37ef2aa-ce7f-4715-aa1a-781696d60dd7 앱 프로세스를 새로 시작하면 빈 날씨에서 시작한다") {
            val previousCache = WeatherReportCache()
            previousCache.update(
                weatherList = listOf(Arb.weather().next()),
                locationName = Arb.string().next(),
                fetchedAt = Arb.secondPrecisionInstant().next(),
            )

            WeatherReportCache().get().test {
                awaitItem().weatherList shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-864efbba-00be-438d-a80e-c35b4896e572 앱 프로세스를 새로 시작하면 마지막 동기화 시각이 비워진다") {
            val previousCache = WeatherReportCache()
            previousCache.update(
                weatherList = listOf(Arb.weather().next()),
                locationName = Arb.string().next(),
                fetchedAt = Arb.secondPrecisionInstant().next(),
            )

            WeatherReportCache().getFetchedAt() shouldBe null
        }

        test("TC-1ce154ba-a748-47f1-a95b-d7ac555563aa 앱 프로세스를 새로 시작하면 지역명 없음에서 시작한다") {
            val previousCache = WeatherReportCache()
            previousCache.update(
                weatherList = listOf(Arb.weather().next()),
                locationName = Arb.string().next(),
                fetchedAt = Arb.secondPrecisionInstant().next(),
            )

            WeatherReportCache().get().test {
                awaitItem().locationName shouldBe ""
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("날씨 목록을 교체할 때마다 지역명도 교체한다") {
            val oldLocationName = Arb.string().next()
            val newLocationName = Arb.string().next()
            val dataSource = WeatherReportCache()

            dataSource.get().test {
                awaitItem().locationName shouldBe ""

                dataSource.update(
                    weatherList = listOf(Arb.weather().next()),
                    locationName = oldLocationName,
                    fetchedAt = Arb.secondPrecisionInstant().next(),
                )
                awaitItem().locationName shouldBe oldLocationName

                dataSource.update(
                    weatherList = listOf(Arb.weather().next()),
                    locationName = newLocationName,
                    fetchedAt = Arb.secondPrecisionInstant().next(),
                )
                awaitItem().locationName shouldBe newLocationName

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("새 날씨 목록으로 이전 목록을 교체한다") {
            val oldWeatherList = listOf(Arb.weather().next())
            val newWeatherList = listOf(Arb.weather().next())
            val dataSource = WeatherReportCache()

            dataSource.get().test {
                awaitItem().weatherList shouldBe emptyList()

                dataSource.update(weatherList = oldWeatherList, locationName = Arb.string().next(), fetchedAt = Arb.secondPrecisionInstant().next())
                awaitItem().weatherList shouldBe oldWeatherList

                dataSource.update(weatherList = newWeatherList, locationName = Arb.string().next(), fetchedAt = Arb.secondPrecisionInstant().next())
                awaitItem().weatherList shouldBe newWeatherList

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("날씨 목록을 교체할 때마다 마지막 동기화 시각도 교체한다") {
            val oldFetchedAt = Arb.secondPrecisionInstant().next()
            val newFetchedAt = Arb.secondPrecisionInstant().next()
            val dataSource = WeatherReportCache()
            dataSource.getFetchedAt() shouldBe null

            dataSource.update(weatherList = listOf(Arb.weather().next()), locationName = Arb.string().next(), fetchedAt = oldFetchedAt)
            dataSource.getFetchedAt() shouldBe oldFetchedAt

            dataSource.update(weatherList = listOf(Arb.weather().next()), locationName = Arb.string().next(), fetchedAt = newFetchedAt)
            dataSource.getFetchedAt() shouldBe newFetchedAt
        }
    })
