package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.previewGoldenHolidayGroup

// 카드 안 주별 날짜가 일곱 칸을 나눠 쓰므로 열을 더 좁히면 날짜와 이름을 읽기 어렵다.
private val MinColumnWidth = 360.dp

@Composable
internal fun GoldenHolidayList(
    onEvent: (HolidayHomeYearContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    groupList: List<GoldenHolidayGroup> = emptyList(),
) {
    val colors = CalendarDefault.colors()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(MinColumnWidth),
        modifier = modifier,
        contentPadding = DiaryTheme.dimens.screenPaddingValues,
        verticalItemSpacing = DiaryTheme.dimens.itemSpacing,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        items(
            items = groupList,
            key = { group -> group.key() },
        ) { group ->
            GoldenHolidayItem(
                group = group,
                onSelectDate = { dateRange -> onEvent(HolidayHomeYearContentEvent.SelectDate(dateRange = dateRange)) },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                colors = colors,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun GoldenHolidayListPreview() {
    val groupList = remember { listOf(previewGoldenHolidayGroup()) }

    DiaryTheme {
        GoldenHolidayList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            groupList = groupList,
        )
    }
}
