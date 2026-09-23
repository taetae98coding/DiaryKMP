package io.github.taetae98coding.diary.feature.calendar.ui.home.search

import androidx.compose.ui.platform.UriHandler

private const val WEATHER_QUERY = "날씨"

internal fun weatherSearchUri(locationName: String): String = naverSearchUri(query = if (locationName.isEmpty()) WEATHER_QUERY else "$locationName $WEATHER_QUERY")

internal fun UriHandler.openWeatherSearch(locationName: String) {
    runCatching { openUri(weatherSearchUri(locationName = locationName)) }
}
