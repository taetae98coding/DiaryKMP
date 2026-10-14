package io.github.taetae98coding.diary.feature.calendar.ui.home.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.core.uri.tryOpenUri
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_search_open_failed_message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Stable
internal class CalendarSearchOpener(
    private val uriHandler: UriHandler,
    private val coroutineScope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState,
    private val failedMessage: String,
) {
    fun openHolidaySearch(name: String) {
        open(uri = holidaySearchUri(name = name))
    }

    fun openWeatherSearch(locationName: String) {
        open(uri = weatherSearchUri(locationName = locationName))
    }

    private fun open(uri: String) {
        if (!uriHandler.tryOpenUri(uri)) {
            coroutineScope.launch { snackbarHostState.showImmediate(message = failedMessage) }
        }
    }
}

@Composable
internal fun rememberCalendarSearchOpener(snackbarHostState: SnackbarHostState): CalendarSearchOpener {
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
