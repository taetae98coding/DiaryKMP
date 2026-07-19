package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity

internal class ContactBirthdayCalendarColumnTypeConverter {
    @ColumnTypeConverter
    fun calendarToText(calendar: ContactBirthdayCalendarLocalEntity): String = calendar.persistentValue

    @ColumnTypeConverter
    fun textToCalendar(value: String): ContactBirthdayCalendarLocalEntity = requireNotNull(ContactBirthdayCalendarLocalEntity.fromPersistentValue(value)) { "Unknown birthday calendar: $value" }
}
