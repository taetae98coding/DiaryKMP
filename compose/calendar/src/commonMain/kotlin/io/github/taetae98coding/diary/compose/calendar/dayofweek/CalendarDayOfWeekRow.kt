package io.github.taetae98coding.diary.compose.calendar.dayofweek

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.Res
import io.github.taetae98coding.diary.compose.calendar.calendar_day_of_week_titles
import io.github.taetae98coding.diary.compose.calendar.dayOfWeekColor
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayBasedDayOfWeek
import org.jetbrains.compose.resources.stringArrayResource

@Composable
internal fun CalendarDayOfWeekRow(
    modifier: Modifier = Modifier,
    colors: CalendarColor = CalendarDefault.colors(),
) {
    val defaultContentColor = LocalContentColor.current

    Row(modifier = modifier) {
        stringArrayResource(Res.array.calendar_day_of_week_titles).forEachIndexed { index, title ->
            Text(
                text = title,
                modifier = Modifier.weight(1F),
                color =
                    colors.dayOfWeekColor(
                        dayOfWeek = sundayBasedDayOfWeek(number = index),
                        defaultColor = defaultContentColor,
                    ),
                textAlign = TextAlign.Center,
                style = DiaryTheme.typography.labelSmallEmphasized,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun CalendarDayOfWeekRowPreview() {
    DiaryTheme {
        Surface {
            CalendarDayOfWeekRow(modifier = Modifier.fillMaxWidth())
        }
    }
}
