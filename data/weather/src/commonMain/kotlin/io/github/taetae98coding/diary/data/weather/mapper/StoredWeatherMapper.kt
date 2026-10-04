package io.github.taetae98coding.diary.data.weather.mapper

import io.github.taetae98coding.diary.core.model.weather.WeatherReport
import io.github.taetae98coding.diary.data.weather.cache.WeatherReportCache
import kotlin.time.Duration

internal fun WeatherReportCache.StoredWeather.toWeatherReport(forecastInterval: Duration): WeatherReport? {
    if (weatherList.isEmpty()) return null

    return WeatherReport(
        weatherList = weatherList,
        coverage = weatherList.minOf { weather -> weather.dateTime }..<weatherList.maxOf { weather -> weather.dateTime } + forecastInterval,
        locationName = locationName,
    )
}
