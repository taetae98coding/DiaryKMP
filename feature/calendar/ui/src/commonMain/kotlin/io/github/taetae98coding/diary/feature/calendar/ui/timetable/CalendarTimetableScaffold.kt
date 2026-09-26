package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.Timetable
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarTimedMemo
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
internal fun CalendarTimetableScaffold(
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarTimetableScaffoldState = rememberCalendarTimetableScaffoldState(),
    memoProvider: () -> List<CalendarMemo> = { emptyList() },
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier =
            modifier.keyShortcut { keyEvent ->
                when {
                    keyEvent.isPreviousShortcut() -> {
                        coroutineScope.launch { state.timetableState.animateScrollToPrevious() }
                        true
                    }

                    keyEvent.isNextShortcut() -> {
                        coroutineScope.launch { state.timetableState.animateScrollToNext() }
                        true
                    }

                    else -> false
                }
            },
        topBar = {
            CalendarTimetableTopBar(
                onEvent = onEvent,
                state = state,
            )
        },
    ) { paddingValues ->
        Timetable(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state.timetableState,
            nowProvider = { state.now },
        ) {
            calendarTimetableItems(
                memoProvider = memoProvider,
                onEvent = onEvent,
            )
        }
    }
}

private fun KeyEvent.isPreviousShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionLeft

private fun KeyEvent.isNextShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionRight

private class CalendarTimetableTypePreviewParameter : PreviewParameterProvider<CalendarTimetableNavKey.Type> {
    override val values: Sequence<CalendarTimetableNavKey.Type> = CalendarTimetableNavKey.Type.entries.asSequence()
}

@ScreenPreview
@Composable
private fun CalendarTimetableScaffoldPreview(
    @PreviewParameter(CalendarTimetableTypePreviewParameter::class) type: CalendarTimetableNavKey.Type,
) {
    val memoList = remember { listOf(previewCalendarMemo(), previewCalendarTimedMemo()) }

    DiaryTheme {
        CalendarTimetableScaffold(
            onEvent = {},
            state = rememberCalendarTimetableScaffoldState(type = type, initialDate = LocalDate(year = 2026, month = 7, day = 19)),
            memoProvider = { memoList },
        )
    }
}
