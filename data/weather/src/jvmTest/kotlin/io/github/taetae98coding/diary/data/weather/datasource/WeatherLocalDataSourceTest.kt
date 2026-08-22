package io.github.taetae98coding.diary.data.weather.datasource

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WeatherLocalDataSourceTest :
    FunSpec({
        test("TC-WEATHER-FETCH-DATA-012 앱 프로세스를 새로 시작하면 빈 날씨에서 시작한다") {
            val previousDataSource = WeatherLocalDataSource()
            previousDataSource.update(
                weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()),
                locationName = fixtureMonkey.giveMeOne(),
                fetchedAt = fixtureMonkey.giveMeOne(),
            )

            WeatherLocalDataSource().get().test {
                awaitItem().weatherList shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-WEATHER-FETCH-DATA-018 앱 프로세스를 새로 시작하면 마지막 동기화 시각이 비워진다") {
            val previousDataSource = WeatherLocalDataSource()
            previousDataSource.update(
                weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()),
                locationName = fixtureMonkey.giveMeOne(),
                fetchedAt = fixtureMonkey.giveMeOne(),
            )

            WeatherLocalDataSource().getFetchedAt() shouldBe null
        }

        test("TC-WEATHER-FETCH-DATA-027 앱 프로세스를 새로 시작하면 지역명 없음에서 시작한다") {
            val previousDataSource = WeatherLocalDataSource()
            previousDataSource.update(
                weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()),
                locationName = fixtureMonkey.giveMeOne(),
                fetchedAt = fixtureMonkey.giveMeOne(),
            )

            WeatherLocalDataSource().get().test {
                awaitItem().locationName shouldBe ""
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("날씨 목록을 교체할 때마다 지역명도 교체한다") {
            val oldLocationName = fixtureMonkey.giveMeOne<String>()
            val newLocationName = fixtureMonkey.giveMeOne<String>()
            val dataSource = WeatherLocalDataSource()

            dataSource.get().test {
                awaitItem().locationName shouldBe ""

                dataSource.update(
                    weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()),
                    locationName = oldLocationName,
                    fetchedAt = fixtureMonkey.giveMeOne(),
                )
                awaitItem().locationName shouldBe oldLocationName

                dataSource.update(
                    weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()),
                    locationName = newLocationName,
                    fetchedAt = fixtureMonkey.giveMeOne(),
                )
                awaitItem().locationName shouldBe newLocationName

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("새 날씨 목록으로 이전 목록을 교체한다") {
            val oldWeatherList = listOf(fixtureMonkey.giveMeOne<Weather>())
            val newWeatherList = listOf(fixtureMonkey.giveMeOne<Weather>())
            val dataSource = WeatherLocalDataSource()

            dataSource.get().test {
                awaitItem().weatherList shouldBe emptyList()

                dataSource.update(weatherList = oldWeatherList, locationName = fixtureMonkey.giveMeOne(), fetchedAt = fixtureMonkey.giveMeOne())
                awaitItem().weatherList shouldBe oldWeatherList

                dataSource.update(weatherList = newWeatherList, locationName = fixtureMonkey.giveMeOne(), fetchedAt = fixtureMonkey.giveMeOne())
                awaitItem().weatherList shouldBe newWeatherList

                cancelAndIgnoreRemainingEvents()
            }
        }

        test("날씨 목록을 교체할 때마다 마지막 동기화 시각도 교체한다") {
            val oldFetchedAt = fixtureMonkey.giveMeOne<Instant>()
            val newFetchedAt = fixtureMonkey.giveMeOne<Instant>()
            val dataSource = WeatherLocalDataSource()
            dataSource.getFetchedAt() shouldBe null

            dataSource.update(weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()), locationName = fixtureMonkey.giveMeOne(), fetchedAt = oldFetchedAt)
            dataSource.getFetchedAt() shouldBe oldFetchedAt

            dataSource.update(weatherList = listOf(fixtureMonkey.giveMeOne<Weather>()), locationName = fixtureMonkey.giveMeOne(), fetchedAt = newFetchedAt)
            dataSource.getFetchedAt() shouldBe newFetchedAt
        }
    })
