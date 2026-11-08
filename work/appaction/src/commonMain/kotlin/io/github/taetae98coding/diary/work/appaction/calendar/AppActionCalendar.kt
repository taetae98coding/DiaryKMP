package io.github.taetae98coding.diary.work.appaction.calendar

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
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

public object AppActionCalendarDDayLabelKindValue {
    public const val BASE_DAY: String = "base_day"
    public const val DAYS: String = "days"
    public const val MONTHS: String = "months"
    public const val YEARS: String = "years"
}

public data class AppActionCalendarDDayLabel(
    val kind: String,
    val count: Int?,
)

public data class AppActionCalendarDDay(
    val ddayId: String,
    val title: String,
    val color: String,
    val date: String,
    val label: AppActionCalendarDDayLabel,
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

// 데스크톱 MCP 연결도 이 값으로 결과를 만들어 두 경로의 캘린더 디데이 결과가 같게 한다.
public fun CalendarDDay.toAppActionCalendarDDay(): AppActionCalendarDDay =
    AppActionCalendarDDay(
        ddayId = ddayId.toString(),
        title = title,
        color = color.toColorHex(),
        date = date.toString(),
        label = label.toAppActionCalendarDDayLabel(),
    )

private fun CalendarDDayLabel.toAppActionCalendarDDayLabel(): AppActionCalendarDDayLabel =
    when (this) {
        CalendarDDayLabel.BaseDay -> AppActionCalendarDDayLabel(kind = AppActionCalendarDDayLabelKindValue.BASE_DAY, count = null)
        is CalendarDDayLabel.Days -> AppActionCalendarDDayLabel(kind = AppActionCalendarDDayLabelKindValue.DAYS, count = days)
        is CalendarDDayLabel.Months -> AppActionCalendarDDayLabel(kind = AppActionCalendarDDayLabelKindValue.MONTHS, count = months)
        is CalendarDDayLabel.Years -> AppActionCalendarDDayLabel(kind = AppActionCalendarDDayLabelKindValue.YEARS, count = years)
    }

internal fun Holiday.toAppActionHoliday(): AppActionHoliday =
    AppActionHoliday(
        name = name,
        isHoliday = isHoliday,
        start = dateRange.start.toString(),
        endInclusive = dateRange.endInclusive.toString(),
    )
