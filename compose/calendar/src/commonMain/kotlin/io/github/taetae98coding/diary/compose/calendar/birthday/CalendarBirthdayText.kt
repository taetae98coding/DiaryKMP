package io.github.taetae98coding.diary.compose.calendar.birthday

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
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.CalendarItemDefaults
import io.github.taetae98coding.diary.compose.calendar.Res
import io.github.taetae98coding.diary.compose.calendar.calendar_home_birthday_content_description
import io.github.taetae98coding.diary.compose.calendar.calendar_home_birthday_text
import io.github.taetae98coding.diary.compose.calendar.previewCalendarContactBirthday
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import kotlinx.datetime.LocalDateRange
import org.jetbrains.compose.resources.stringResource

@Composable
public fun CalendarBirthdayText(
    displayName: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = stringResource(Res.string.calendar_home_birthday_content_description, displayName)

    CalendarText(
        text = stringResource(Res.string.calendar_home_birthday_text, displayName),
        modifier =
            modifier
                .clip(CalendarDefaults.itemShape)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = name },
        color = color,
    )
}

public fun CalendarContactBirthday.toDateRange(): LocalDateRange = LocalDateRange(start = date, endInclusive = date)

@ComponentPreview
@Composable
private fun CalendarBirthdayTextPreview() {
    val birthday = remember { previewCalendarContactBirthday() }

    DiaryTheme {
        Surface {
            CalendarBirthdayText(
                displayName = birthday.name,
                color = CalendarItemDefaults.birthdayColor(),
                onClick = {},
            )
        }
    }
}
