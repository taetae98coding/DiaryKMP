package io.github.taetae98coding.diary.feature.calendar.ui.home.search

private const val WEATHER_QUERY = "날씨"

internal fun weatherSearchUri(locationName: String): String = naverSearchUri(query = if (locationName.isEmpty()) WEATHER_QUERY else "$locationName $WEATHER_QUERY")
