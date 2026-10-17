package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear
import kotlinx.coroutines.launch

@Composable
internal fun HolidayHomeScaffold(
    onEvent: (HolidayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
    yearContent: @Composable (year: Int, contentPadding: PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.holidayHomeShortcut(state = state),
        topBar = {
            HolidayHomeTopBar(
                state = state,
                onEvent = onEvent,
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val yearContentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                    ),
        ) {
            AnnualLeaveStepper(
                state = state,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(DiaryTheme.dimens.screenPaddingValues),
            )
            HorizontalDivider()
            HorizontalPager(
                state = state.pagerState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1F),
            ) { page ->
                yearContent(page.toYear(), yearContentPadding)
            }
        }
    }
}

@Composable
private fun Modifier.holidayHomeShortcut(state: HolidayHomeScaffoldState): Modifier {
    val coroutineScope = rememberCoroutineScope()

    return keyShortcut { keyEvent ->
        when {
            keyEvent.isPreviousYearShortcut() -> {
                coroutineScope.launch { state.animateScrollTo(year = state.year - 1) }
                true
            }

            keyEvent.isNextYearShortcut() -> {
                coroutineScope.launch { state.animateScrollTo(year = state.year + 1) }
                true
            }

            keyEvent.isIncreaseAnnualLeaveShortcut() -> {
                state.increaseAnnualLeave()
                true
            }

            keyEvent.isDecreaseAnnualLeaveShortcut() -> {
                state.decreaseAnnualLeave()
                true
            }

            else -> false
        }
    }
}

private fun KeyEvent.isPreviousYearShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionLeft

private fun KeyEvent.isNextYearShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionRight

private fun KeyEvent.isIncreaseAnnualLeaveShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionUp

private fun KeyEvent.isDecreaseAnnualLeaveShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionDown

@ScreenPreview
@Composable
private fun HolidayHomeScaffoldPreview() {
    DiaryTheme {
        HolidayHomeScaffold(
            onEvent = {},
            yearContent = { _, contentPadding ->
                GoldenHolidayYear(
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            },
        )
    }
}
