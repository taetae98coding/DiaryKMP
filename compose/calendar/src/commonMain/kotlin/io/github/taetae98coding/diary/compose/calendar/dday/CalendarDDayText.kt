package io.github.taetae98coding.diary.compose.calendar.dday

import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.Res
import io.github.taetae98coding.diary.compose.calendar.calendar_home_dday_base_day_label
import io.github.taetae98coding.diary.compose.calendar.calendar_home_dday_content_description
import io.github.taetae98coding.diary.compose.calendar.calendar_home_dday_days_label
import io.github.taetae98coding.diary.compose.calendar.calendar_home_dday_months_label
import io.github.taetae98coding.diary.compose.calendar.calendar_home_dday_text
import io.github.taetae98coding.diary.compose.calendar.calendar_home_dday_years_label
import io.github.taetae98coding.diary.compose.calendar.previewCalendarDDay
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import kotlinx.datetime.LocalDateRange
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
public fun CalendarDDayText(
    title: String,
    label: CalendarDDayLabel,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelText = label.toText()
    val name = stringResource(Res.string.calendar_home_dday_content_description, title, labelText)

    CalendarText(
        text = stringResource(Res.string.calendar_home_dday_text, title, labelText),
        modifier =
            modifier
                .clip(CalendarDefaults.itemShape)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = name },
        color = color,
    )
}

public fun CalendarDDay.toDateRange(): LocalDateRange = LocalDateRange(start = date, endInclusive = date)

// 한 디데이가 여러 날짜에 놓이므로 날짜까지 더해 구분하고, 목록 키로 저장할 수 있도록 문자열로 둔다.
public fun CalendarDDay.itemKey(): String = "$ddayId/$date"

@Composable
private fun CalendarDDayLabel.toText(): String =
    when (this) {
        is CalendarDDayLabel.BaseDay -> stringResource(Res.string.calendar_home_dday_base_day_label)
        is CalendarDDayLabel.Days -> pluralStringResource(Res.plurals.calendar_home_dday_days_label, days, days)
        is CalendarDDayLabel.Months -> pluralStringResource(Res.plurals.calendar_home_dday_months_label, months, months)
        is CalendarDDayLabel.Years -> pluralStringResource(Res.plurals.calendar_home_dday_years_label, years, years)
    }

private class CalendarDDayLabelPreviewParameter : PreviewParameterProvider<CalendarDDayLabel> {
    override val values: Sequence<CalendarDDayLabel> =
        sequenceOf(
            CalendarDDayLabel.BaseDay,
            CalendarDDayLabel.Days(days = 100),
            CalendarDDayLabel.Months(months = 3),
            CalendarDDayLabel.Years(years = 1),
        )
}

@ComponentPreview
@Composable
private fun CalendarDDayTextPreview(
    @PreviewParameter(CalendarDDayLabelPreviewParameter::class) label: CalendarDDayLabel,
) {
    val dday = remember(label) { previewCalendarDDay(label = label) }

    DiaryTheme {
        Surface {
            CalendarDDayText(
                title = dday.title,
                label = dday.label,
                color = Color(color = dday.color.toInt()),
                onClick = {},
            )
        }
    }
}
