package io.github.taetae98coding.diary.compose.calendar.search

private const val WEATHER_QUERY = "날씨"

public fun weatherSearchUri(locationName: String): String = naverSearchUri(query = if (locationName.isEmpty()) WEATHER_QUERY else "$locationName $WEATHER_QUERY")
