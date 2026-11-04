package io.github.taetae98coding.diary.core.testing.weather

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import kotlinx.datetime.LocalDate

public fun FixtureMonkey.calendarWeather(
    date: LocalDate,
    temperature: CalendarWeatherTemperature,
    descriptionList: List<String>,
    imageUrlList: List<String> = descriptionList.indices.map { index -> "https://example.com/weather/$index.png" },
): CalendarWeather =
    CalendarWeather(
        date = date,
        temperature = temperature,
        weatherList =
            descriptionList.zip(imageUrlList).map { (description, imageUrl) ->
                Weather(
                    dateTime = giveMeOne(),
                    conditionList = listOf(WeatherCondition(description = description, imageUrl = imageUrl)),
                    temperature = giveMeOne<WeatherTemperature>(),
                )
            },
    )
