package io.github.taetae98coding.diary.work.appaction.core

import kotlinx.datetime.LocalDateRange

private const val START_DATE = "startDate"
private const val END_DATE = "endDate"

internal fun dateRangeArgument(
    startDate: String,
    endDate: String,
): LocalDateRange {
    val start = startDate.toLocalDateArgument(name = START_DATE)
    val end = endDate.toLocalDateArgument(name = END_DATE)

    if (end < start) invalidArgument(message = "'$END_DATE' must not be before '$START_DATE'.")

    return start..end
}
