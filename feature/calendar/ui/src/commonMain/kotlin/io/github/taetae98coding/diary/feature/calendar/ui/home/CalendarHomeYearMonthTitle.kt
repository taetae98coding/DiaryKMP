package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.DropDownIcon
import io.github.taetae98coding.diary.compose.core.icon.DropUpIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_drop_down_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_drop_up_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_month_titles
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_year_month_title
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarHomeYearMonthTitle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(CalendarHomeYearMonthTitleDefaults.CornerSize))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(
                    horizontal = CalendarHomeYearMonthTitleDefaults.HorizontalPadding,
                    vertical = CalendarHomeYearMonthTitleDefaults.VerticalPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val yearMonth = state.calendarState.currentYearMonth
        val monthTitle = stringArrayResource(Res.array.calendar_home_month_titles).getOrNull(yearMonth.month.number - 1)

        monthTitle?.let {
            Text(text = stringResource(Res.string.calendar_home_year_month_title, yearMonth.year.toString(), it))
        }
        DiaryCrossfade(targetState = state.isDatePickerVisible) { isDatePickerVisible ->
            if (isDatePickerVisible) {
                DropUpIcon(contentDescription = stringResource(Res.string.calendar_home_drop_up_content_description))
            } else {
                DropDownIcon(contentDescription = stringResource(Res.string.calendar_home_drop_down_content_description))
            }
        }
    }
}

@ComponentPreview
@Composable
private fun CalendarHomeYearMonthTitlePreview() {
    DiaryTheme {
        Surface {
            CalendarHomeYearMonthTitle(onClick = {})
        }
    }
}
