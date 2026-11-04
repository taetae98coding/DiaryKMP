package io.github.taetae98coding.diary.compose.calendar.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.calendar.Res
import io.github.taetae98coding.diary.compose.calendar.calendar_search_open_failed_message
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.core.uri.tryOpenUri
import org.jetbrains.compose.resources.stringResource

@Stable
public class CalendarSearchOpener internal constructor(
    private val uriHandler: UriHandler,
    private val snackbarHostState: SnackbarHostState,
    private val failedMessage: String,
) {
    public suspend fun openHolidaySearch(name: String) {
        open(uri = holidaySearchUri(name = name))
    }

    public suspend fun openWeatherSearch(locationName: String) {
        open(uri = weatherSearchUri(locationName = locationName))
    }

    private suspend fun open(uri: String) {
        if (!uriHandler.tryOpenUri(uri)) {
            snackbarHostState.showImmediate(message = failedMessage)
        }
    }
}

@Composable
public fun rememberCalendarSearchOpener(snackbarHostState: SnackbarHostState): CalendarSearchOpener {
    val uriHandler = LocalUriHandler.current
    val failedMessage = stringResource(Res.string.calendar_search_open_failed_message)

    return remember(uriHandler, snackbarHostState, failedMessage) {
        CalendarSearchOpener(
            uriHandler = uriHandler,
            snackbarHostState = snackbarHostState,
            failedMessage = failedMessage,
        )
    }
}
