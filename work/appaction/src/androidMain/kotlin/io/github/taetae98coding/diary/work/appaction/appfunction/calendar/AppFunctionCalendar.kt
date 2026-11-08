package io.github.taetae98coding.diary.work.appaction.appfunction.calendar

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.toAppFunctionMemoDateTime
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarBirthday
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDay
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarDDayLabel
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionCalendarMemo
import io.github.taetae98coding.diary.work.appaction.calendar.AppActionHoliday

/** A memo shown on the calendar. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionCalendarMemo(
    /** Id of the memo. */
    val id: String,
    /** Title of the memo. */
    val title: String,
    /** Color of the memo as a hex color like #4285F4. */
    val color: String,
    /** Period of the memo. */
    val dateTime: AppFunctionMemoDateTime,
)

/** A contact birthday shown on the calendar. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionCalendarBirthday(
    /** Id of the contact. */
    val contactId: String,
    /** Name of the contact. */
    val name: String,
    /** Text that tells contacts with the same name apart. Empty when not needed. */
    val nameQualifier: String,
    /** Solar date of the birthday, like 2026-01-31. */
    val date: String,
)

/** What a calendar D-Day marks. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionCalendarDDayLabel(
    /** base_day for the base date, or days, months or years for an anniversary. */
    val kind: String,
    /** Count of days, months or years of the anniversary. Absent for the base day. */
    val count: Int?,
)

/** A base date or anniversary of a D-Day shown on the calendar. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionCalendarDDay(
    /** Id of the D-Day. */
    val ddayId: String,
    /** Title of the D-Day. */
    val title: String,
    /** Color of the D-Day as a hex color like #4285F4. */
    val color: String,
    /** Date shown on the calendar, like 2026-01-31. */
    val date: String,
    /** What the date marks. */
    val label: AppFunctionCalendarDDayLabel,
)

/** A holiday shown on the calendar. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionHoliday(
    /** Name of the holiday. */
    val name: String,
    /** Whether the day is a day off. False for named days that are not days off. */
    val isHoliday: Boolean,
    /** First date of the holiday, like 2026-01-01. */
    val start: String,
    /** Last date of the holiday, inclusive. */
    val endInclusive: String,
)

internal fun AppActionCalendarMemo.toAppFunctionCalendarMemo(): AppFunctionCalendarMemo =
    AppFunctionCalendarMemo(
        id = id,
        title = title,
        color = color,
        dateTime = dateTime.toAppFunctionMemoDateTime(),
    )

internal fun AppActionCalendarBirthday.toAppFunctionCalendarBirthday(): AppFunctionCalendarBirthday =
    AppFunctionCalendarBirthday(
        contactId = contactId,
        name = name,
        nameQualifier = nameQualifier,
        date = date,
    )

internal fun AppActionCalendarDDay.toAppFunctionCalendarDDay(): AppFunctionCalendarDDay =
    AppFunctionCalendarDDay(
        ddayId = ddayId,
        title = title,
        color = color,
        date = date,
        label = label.toAppFunctionCalendarDDayLabel(),
    )

private fun AppActionCalendarDDayLabel.toAppFunctionCalendarDDayLabel(): AppFunctionCalendarDDayLabel = AppFunctionCalendarDDayLabel(kind = kind, count = count)

internal fun AppActionHoliday.toAppFunctionHoliday(): AppFunctionHoliday =
    AppFunctionHoliday(
        name = name,
        isHoliday = isHoliday,
        start = start,
        endInclusive = endInclusive,
    )
