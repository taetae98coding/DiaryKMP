package io.github.taetae98coding.diary.data.weather.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.data.weather.cache.WeatherReportCache
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class StoredWeatherMapperTest :
    FunSpec({
        test("저장된 날씨가 없으면 날씨 보고도 없다") {
            WeatherReportCache.StoredWeather().toWeatherReport(forecastInterval = 3.hours).shouldBeNull()
        }

        test("가장 이른 시각부터 가장 늦은 시각에 예보 간격을 더한 시각까지를 구간으로 둔다") {
            val weatherList = List(3) { fixtureMonkey.giveMeOne<Weather>() }
            val stored = WeatherReportCache.StoredWeather(weatherList = weatherList, locationName = fixtureMonkey.giveMeOne())

            val report = stored.toWeatherReport(forecastInterval = 3.hours).shouldNotBeNull()

            report.weatherList shouldBe weatherList
            report.locationName shouldBe stored.locationName
            report.coverage.start shouldBe weatherList.minOf { weather -> weather.dateTime }
            report.coverage.endExclusive shouldBe weatherList.maxOf { weather -> weather.dateTime } + 3.hours
        }
    })
