package io.github.taetae98coding.diary.core.testing.weather

import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import kotlinx.datetime.LocalDate

public fun Arb.Companion.calendarWeather(
    date: LocalDate,
    temperature: CalendarWeatherTemperature,
    descriptionList: List<String>,
    imageUrlList: List<String> = descriptionList.indices.map { index -> "https://example.com/weather/$index.png" },
): Arb<CalendarWeather> =
    arbitrary {
        CalendarWeather(
            date = date,
            temperature = temperature,
            weatherList =
                descriptionList.zip(imageUrlList).map { (description, imageUrl) ->
                    Weather(
                        dateTime = Arb.secondPrecisionInstant().bind(),
                        conditionList = listOf(WeatherCondition(description = description, imageUrl = imageUrl)),
                        temperature = Arb.weatherTemperature().bind(),
                    )
                },
        )
    }
