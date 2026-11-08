package io.github.taetae98coding.diary.work.appaction.calendar

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.work.appaction.core.toColorHex
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.memo.toAppActionMemoDateTime

public data class AppActionCalendarMemo(
    val id: String,
    val title: String,
    val color: String,
    val dateTime: AppActionMemoDateTime,
)

public data class AppActionCalendarBirthday(
    val contactId: String,
    val name: String,
    val nameQualifier: String,
    val date: String,
)

public data class AppActionHoliday(
    val name: String,
    val isHoliday: Boolean,
    val start: String,
    val endInclusive: String,
)

internal fun CalendarMemo.toAppActionCalendarMemo(): AppActionCalendarMemo =
    AppActionCalendarMemo(
        id = id.toString(),
        title = title,
        color = color.toColorHex(),
        dateTime = dateTime.toAppActionMemoDateTime(),
    )

internal fun CalendarContactBirthday.toAppActionCalendarBirthday(): AppActionCalendarBirthday =
    AppActionCalendarBirthday(
        contactId = contactId.toString(),
        name = name,
        nameQualifier = nameQualifier,
        date = date.toString(),
    )

internal fun Holiday.toAppActionHoliday(): AppActionHoliday =
    AppActionHoliday(
        name = name,
        isHoliday = isHoliday,
        start = dateRange.start.toString(),
        endInclusive = dateRange.endInclusive.toString(),
    )
