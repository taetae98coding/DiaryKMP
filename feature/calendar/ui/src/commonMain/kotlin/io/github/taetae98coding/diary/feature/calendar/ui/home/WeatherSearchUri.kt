package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.platform.UriHandler

private const val WEATHER_QUERY = "날씨"

internal fun weatherSearchUri(locationName: String): String = naverSearchUri(query = if (locationName.isEmpty()) WEATHER_QUERY else "$locationName $WEATHER_QUERY")

// 브라우저를 열 수 없는 환경에서도 화면을 그대로 유지하고 사용자에게 알리지 않는다.
internal fun UriHandler.openWeatherSearch(locationName: String) {
    runCatching { openUri(weatherSearchUri(locationName = locationName)) }
}
