package io.github.taetae98coding.diary.compose.calendar.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.calendar.Res
import io.github.taetae98coding.diary.compose.calendar.calendar_search_open_failed_message
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.core.uri.tryOpenUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Stable
public class CalendarSearchOpener internal constructor(
    private val uriHandler: UriHandler,
    private val coroutineScope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState,
    private val failedMessage: String,
) {
    public fun openHolidaySearch(name: String) {
        open(uri = holidaySearchUri(name = name))
    }

    public fun openWeatherSearch(locationName: String) {
        open(uri = weatherSearchUri(locationName = locationName))
    }

    private fun open(uri: String) {
        if (!uriHandler.tryOpenUri(uri)) {
            coroutineScope.launch { snackbarHostState.showImmediate(message = failedMessage) }
        }
    }
}

@Composable
public fun rememberCalendarSearchOpener(snackbarHostState: SnackbarHostState): CalendarSearchOpener {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val failedMessage = stringResource(Res.string.calendar_search_open_failed_message)

    return remember(uriHandler, coroutineScope, snackbarHostState, failedMessage) {
        CalendarSearchOpener(
            uriHandler = uriHandler,
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState,
            failedMessage = failedMessage,
        )
    }
}
