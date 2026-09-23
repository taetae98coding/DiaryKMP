package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_birthday_calendar_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_birthday_calendar_lunar
import io.github.taetae98coding.diary.feature.contact.ui.contact_birthday_calendar_solar
import org.jetbrains.compose.resources.stringResource

internal val contactBirthdayCalendarList: List<ContactBirthdayCalendar> =
    listOf(
        ContactBirthdayCalendar.SOLAR,
        ContactBirthdayCalendar.LUNAR,
    )

@Composable
internal fun ContactBirthdayCalendarSelector(
    calendar: ContactBirthdayCalendar,
    onSelect: (ContactBirthdayCalendar) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectorContentDescription = stringResource(Res.string.contact_birthday_calendar_content_description)

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.semantics { contentDescription = selectorContentDescription },
    ) {
        contactBirthdayCalendarList.forEachIndexed { index, item ->
            SegmentedButton(
                selected = item == calendar,
                onClick = { onSelect(item) },
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = contactBirthdayCalendarList.size,
                    ),
                label = { Text(text = item.label()) },
            )
        }
    }
}

@Composable
private fun ContactBirthdayCalendar.label(): String =
    when (this) {
        ContactBirthdayCalendar.SOLAR -> stringResource(Res.string.contact_birthday_calendar_solar)
        ContactBirthdayCalendar.LUNAR -> stringResource(Res.string.contact_birthday_calendar_lunar)
    }

@ComponentPreview
@Composable
private fun ContactBirthdayCalendarSelectorPreview() {
    DiaryTheme {
        Surface {
            ContactBirthdayCalendarSelector(
                calendar = ContactBirthdayCalendar.LUNAR,
                onSelect = {},
            )
        }
    }
}
