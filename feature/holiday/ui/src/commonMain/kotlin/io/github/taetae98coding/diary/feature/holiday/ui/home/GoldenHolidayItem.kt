package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.previewGoldenHolidayGroup
import kotlinx.datetime.LocalDateRange

private val CardPadding = 16.dp

@Composable
internal fun GoldenHolidayItem(
    group: GoldenHolidayGroup,
    onSelectDate: (LocalDateRange) -> Unit,
    modifier: Modifier = Modifier,
    colors: CalendarColor = CalendarDefault.colors(),
) {
    var optionIndex by rememberSaveable(group) { mutableIntStateOf(0) }
    val goldenHoliday = group.optionList[optionIndex]

    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            GoldenHolidaySummary(
                goldenHoliday = goldenHoliday,
                optionIndex = optionIndex,
                optionCount = group.optionList.size,
                modifier = Modifier.fillMaxWidth(),
                onOptionIndexChange = { index -> optionIndex = index },
            )
            GoldenHolidayWeekList(
                goldenHoliday = goldenHoliday,
                onSelectDate = onSelectDate,
                modifier = Modifier.fillMaxWidth(),
                colors = colors,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun GoldenHolidayItemPreview() {
    val group = remember { previewGoldenHolidayGroup() }

    DiaryTheme {
        Surface {
            GoldenHolidayItem(
                group = group,
                onSelectDate = {},
            )
        }
    }
}
