package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_golden_holiday_annual_leave_count
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_golden_holiday_day_count
import io.github.taetae98coding.diary.feature.holiday.ui.previewGoldenHoliday
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoldenHolidaySummary(
    goldenHoliday: GoldenHoliday,
    optionIndex: Int,
    optionCount: Int,
    onOptionIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text =
                goldenHoliday.holidayList
                    .map { holiday -> holiday.name }
                    .distinct()
                    .joinToString(separator = ", "),
            style = DiaryTheme.typography.titleMediumEmphasized,
        )
        GoldenHolidayPeriodRow(
            goldenHoliday = goldenHoliday,
            optionIndex = optionIndex,
            optionCount = optionCount,
            onOptionIndexChange = onOptionIndexChange,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing)) {
            Text(
                text = stringResource(Res.string.holiday_golden_holiday_day_count, goldenHoliday.dateRange.size.toString()),
                color = DiaryTheme.colorScheme.onSurfaceVariant,
                style = DiaryTheme.typography.bodySmall,
            )
            if (goldenHoliday.annualLeaveCount > 0) {
                Text(
                    text = stringResource(Res.string.holiday_golden_holiday_annual_leave_count, goldenHoliday.annualLeaveCount.toString()),
                    color = DiaryTheme.colorScheme.onSurfaceVariant,
                    style = DiaryTheme.typography.bodySmall,
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun GoldenHolidaySummaryPreview() {
    val goldenHoliday = remember { previewGoldenHoliday() }

    DiaryTheme {
        Surface {
            GoldenHolidaySummary(
                goldenHoliday = goldenHoliday,
                optionIndex = 0,
                optionCount = 2,
                onOptionIndexChange = {},
            )
        }
    }
}
