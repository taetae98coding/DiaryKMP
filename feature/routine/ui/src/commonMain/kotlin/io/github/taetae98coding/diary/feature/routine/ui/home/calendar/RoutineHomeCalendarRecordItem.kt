package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.routineAmountText
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.home.press.longPress
import io.github.taetae98coding.diary.feature.routine.ui.previewCalendarRoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_calendar_record_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_calendar_record_text
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import kotlinx.datetime.LocalDateRange
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

internal const val ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG: String = "RoutineHomeCalendarRecord"

internal fun CalendarWeekOfMonthGridGroupScope.routineRecordItem(
    recordProvider: () -> List<CalendarRoutineRecord>,
    onRecordClick: (Uuid) -> Unit,
) {
    recordProvider().forEach { record ->
        item(
            dateRange = LocalDateRange(start = record.date, endInclusive = record.date),
            // 한 루틴이 여러 날짜에 놓이므로 날짜까지 더해 구분하고, 목록 키로 저장할 수 있도록 문자열로 둔다.
            key = "${record.routineId}/${record.date}",
        ) {
            RoutineHomeCalendarRecordText(
                onClick = { onRecordClick(record.routineId) },
                record = record,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
internal fun RoutineHomeCalendarRecordText(
    onClick: () -> Unit,
    record: CalendarRoutineRecord,
    modifier: Modifier = Modifier,
) {
    val amountText = routineAmountText(amount = record.amount, unit = record.unit)
    val name = stringResource(Res.string.routine_home_calendar_record_content_description, record.title, amountText)

    CalendarText(
        text = stringResource(Res.string.routine_home_calendar_record_text, record.title, amountText),
        modifier =
            modifier
                .testTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG)
                .clip(CalendarDefaults.itemShape)
                .longPress { }
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = name },
        color = record.color.toColor(),
    )
}

@ComponentPreview
@Composable
private fun RoutineHomeCalendarRecordTextPreview() {
    DiaryTheme {
        Surface {
            RoutineHomeCalendarRecordText(
                onClick = {},
                record = previewCalendarRoutineRecord(),
            )
        }
    }
}
