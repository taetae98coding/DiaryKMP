package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.weather.CalendarWeatherItem
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather

private const val WEATHER_CONTENT_TYPE = "weather"

internal fun CalendarWeekOfMonthGridGroupScope.weatherItem(
    onWeatherClick: () -> Unit,
    weatherProvider: () -> List<CalendarWeather>,
) {
    weatherProvider().forEach { weather ->
        item(
            dateRange = weather.date..weather.date,
            key = weather.date,
            contentType = WEATHER_CONTENT_TYPE,
        ) {
            CalendarWeatherItem(
                weather = weather,
                onClick = onWeatherClick,
                modifier = Modifier.animateItem(),
            )
        }
    }
}
