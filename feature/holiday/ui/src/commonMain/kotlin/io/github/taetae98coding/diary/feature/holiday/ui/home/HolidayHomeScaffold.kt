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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear

@Composable
internal fun HolidayHomeScaffold(
    onEvent: (HolidayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: HolidayHomeScaffoldState = rememberHolidayHomeScaffoldState(),
    yearContent: @Composable (year: Int, contentPadding: PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
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
