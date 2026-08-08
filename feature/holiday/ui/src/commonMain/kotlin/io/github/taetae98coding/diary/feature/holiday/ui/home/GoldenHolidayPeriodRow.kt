package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.NextIcon
import io.github.taetae98coding.diary.compose.core.icon.PreviousIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_golden_holiday_next_option_content_description
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_golden_holiday_option_position
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_golden_holiday_period
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_golden_holiday_previous_option_content_description
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_month_titles
import io.github.taetae98coding.diary.feature.holiday.ui.previewGoldenHoliday
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoldenHolidayPeriodRow(
    goldenHoliday: GoldenHoliday,
    optionIndex: Int,
    optionCount: Int,
    onOptionIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthTitleList = stringArrayResource(Res.array.holiday_month_titles)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onOptionIndexChange(optionIndex - 1) },
            enabled = optionIndex > 0,
        ) {
            PreviousIcon(contentDescription = stringResource(Res.string.holiday_golden_holiday_previous_option_content_description))
        }
        Text(
            text =
                stringResource(
                    Res.string.holiday_golden_holiday_period,
                    monthTitleList.monthTitle(date = goldenHoliday.dateRange.start),
                    goldenHoliday.dateRange.start.day
                        .toString(),
                    monthTitleList.monthTitle(date = goldenHoliday.dateRange.endInclusive),
                    goldenHoliday.dateRange.endInclusive.day
                        .toString(),
                ),
            style = DiaryTheme.typography.titleSmall,
        )
        IconButton(
            onClick = { onOptionIndexChange(optionIndex + 1) },
            enabled = optionIndex < optionCount - 1,
        ) {
            NextIcon(contentDescription = stringResource(Res.string.holiday_golden_holiday_next_option_content_description))
        }
        Text(
            text =
                stringResource(
                    Res.string.holiday_golden_holiday_option_position,
                    (optionIndex + 1).toString(),
                    optionCount.toString(),
                ),
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodySmall,
        )
    }
}

private fun List<String>.monthTitle(date: LocalDate): String = getOrNull(date.month.number - 1).orEmpty()

@ComponentPreview
@Composable
private fun GoldenHolidayPeriodRowPreview() {
    val goldenHoliday = remember { previewGoldenHoliday() }

    DiaryTheme {
        Surface {
            GoldenHolidayPeriodRow(
                goldenHoliday = goldenHoliday,
                optionIndex = 0,
                optionCount = 2,
                onOptionIndexChange = {},
            )
        }
    }
}
