package io.github.taetae98coding.diary.feature.calendar.ui.home.birthday

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_birthday_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_birthday_text
import io.github.taetae98coding.diary.feature.core.contact.contactDisplayName
import kotlinx.datetime.LocalDateRange
import org.jetbrains.compose.resources.stringResource

internal fun CalendarWeekOfMonthGridGroupScope.birthdayItem(
    birthdayProvider: () -> List<CalendarContactBirthday>,
    birthdayColor: Color,
    onBirthdayClick: (CalendarContactBirthday) -> Unit,
) {
    birthdayProvider().forEach { birthday ->
        item(
            dateRange = birthday.toDateRange(),
            key = birthday.contactId,
        ) {
            val displayName = contactDisplayName(name = birthday.name, nameQualifier = birthday.nameQualifier)
            val name = stringResource(Res.string.calendar_home_birthday_content_description, displayName)

            CalendarText(
                text = stringResource(Res.string.calendar_home_birthday_text, displayName),
                modifier =
                    Modifier
                        .animateItem()
                        .clip(CalendarDefaults.itemShape)
                        .clickable(role = Role.Button) { onBirthdayClick(birthday) }
                        .semantics { contentDescription = name },
                color = birthdayColor,
            )
        }
    }
}

internal fun CalendarContactBirthday.toDateRange(): LocalDateRange = LocalDateRange(start = date, endInclusive = date)
