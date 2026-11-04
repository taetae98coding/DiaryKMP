package io.github.taetae98coding.diary.feature.calendar.ui.home.birthday

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.birthday.CalendarBirthdayText
import io.github.taetae98coding.diary.compose.calendar.birthday.toDateRange
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.feature.core.contact.contactDisplayName

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
            CalendarBirthdayText(
                displayName = contactDisplayName(name = birthday.name, nameQualifier = birthday.nameQualifier),
                color = birthdayColor,
                onClick = { onBirthdayClick(birthday) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}
