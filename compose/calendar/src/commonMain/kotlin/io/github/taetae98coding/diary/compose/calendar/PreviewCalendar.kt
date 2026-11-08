package io.github.taetae98coding.diary.compose.calendar

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewWeatherCondition(): WeatherCondition = WeatherCondition(description = "맑음", imageUrl = "")

internal fun previewCalendarWeather(date: LocalDate = LocalDate(year = 2026, month = 7, day = 19)): CalendarWeather =
    CalendarWeather(
        date = date,
        temperature = CalendarWeatherTemperature.MinMax(min = 21.3, max = 29.8),
        weatherList =
            listOf(
                Weather(
                    dateTime = Instant.DISTANT_PAST,
                    conditionList = listOf(previewWeatherCondition()),
                    temperature = WeatherTemperature(current = 25.0, min = 21.3, max = 29.8),
                ),
            ),
    )

internal fun previewCalendarContactBirthday(
    name: String = "홍길동",
    nameQualifier: String = "",
): CalendarContactBirthday =
    CalendarContactBirthday(
        contactId = Uuid.random(),
        name = name,
        nameQualifier = nameQualifier,
        date = LocalDate(year = 2026, month = 7, day = 22),
    )

internal fun previewCalendarDDay(
    title: String = "수능",
    label: CalendarDDayLabel = CalendarDDayLabel.BaseDay,
): CalendarDDay =
    CalendarDDay(
        ddayId = Uuid.random(),
        title = title,
        color = 0xFFE57373,
        date = LocalDate(year = 2026, month = 7, day = 15),
        label = label,
    )
