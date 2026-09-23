package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear

@Composable
internal fun HolidayHomeScaffold(
    onEvent: (HolidayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
    yearContent: @Composable (year: Int) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            HolidayHomeTopBar(
                state = state,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                yearContent(page.toYear())
            }
        }
    }
}

@ScreenPreview
@Composable
private fun HolidayHomeScaffoldPreview() {
    DiaryTheme {
        HolidayHomeScaffold(
            onEvent = {},
            yearContent = { _ ->
                GoldenHolidayYear(
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )
    }
}
