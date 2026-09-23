package io.github.taetae98coding.diary.feature.calendar.ui.home.search

import androidx.compose.ui.platform.UriHandler

internal fun holidaySearchUri(name: String): String = naverSearchUri(query = name)

internal fun UriHandler.openHolidaySearch(name: String) {
    runCatching { openUri(holidaySearchUri(name = name)) }
}
