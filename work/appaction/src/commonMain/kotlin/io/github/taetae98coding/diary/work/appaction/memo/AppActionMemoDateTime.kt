package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.core.missingArgument
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateArgument
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateTimeArgumentOrNull

private const val START = "start"
private const val END_INCLUSIVE = "endInclusive"
private const val DATE_TIME_RANGE_MESSAGE = "'dateTime.endInclusive' must not be before 'dateTime.start'."

public data class AppActionMemoDateTime(
    val allDay: Boolean,
    val start: String,
    val endInclusive: String,
)

public data class AppActionMemoDateTimeInput(
    val allDay: Boolean?,
    val start: String?,
    val endInclusive: String?,
)

internal fun MemoDateTime.toAppActionMemoDateTime(): AppActionMemoDateTime =
    when (this) {
        is MemoDateTime.AllDay -> AppActionMemoDateTime(allDay = true, start = dateRange.start.toString(), endInclusive = dateRange.endInclusive.toString())
        is MemoDateTime.DateTime -> AppActionMemoDateTime(allDay = false, start = start.toString(), endInclusive = endInclusive.toString())
    }

internal fun AppActionMemoDateTimeInput.toMemoDateTime(): MemoDateTime {
    val isAllDay = allDay ?: invalidArgument(message = "'dateTime.allDay' is required.")

    return if (isAllDay) toAllDay() else toTimed()
}

private fun AppActionMemoDateTimeInput.toAllDay(): MemoDateTime.AllDay {
    val startDate = (start ?: missingArgument(name = START)).toLocalDateArgument(name = START)
    val endDate = (endInclusive ?: missingArgument(name = END_INCLUSIVE)).toLocalDateArgument(name = END_INCLUSIVE)
    if (endDate < startDate) invalidArgument(message = DATE_TIME_RANGE_MESSAGE)

    return MemoDateTime.AllDay(dateRange = startDate..endDate)
}

private fun AppActionMemoDateTimeInput.toTimed(): MemoDateTime.DateTime {
    val startDateTime = start.toLocalDateTimeArgumentOrNull(name = START) ?: invalidArgument(message = "'dateTime.start' is required.")
    val endDateTime = endInclusive.toLocalDateTimeArgumentOrNull(name = END_INCLUSIVE) ?: invalidArgument(message = "'dateTime.endInclusive' is required.")
    if (endDateTime < startDateTime) invalidArgument(message = DATE_TIME_RANGE_MESSAGE)

    return MemoDateTime.DateTime(start = startDateTime, endInclusive = endDateTime)
}
