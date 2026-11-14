package io.github.taetae98coding.diary.library.kotlin.text

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

public sealed interface ICalendarTime {
    public data class Date(
        val date: LocalDate,
    ) : ICalendarTime

    // dateTime은 적힌 숫자 그대로이고, timeZone은 적혀 있지 않거나 알 수 없는 이름이면 null이다.
    public data class DateTime(
        val dateTime: LocalDateTime,
        val timeZone: TimeZone?,
    ) : ICalendarTime
}

public fun StructuredLines.Property.isICalendarDate(): Boolean = parameters.split(';').any { parameter -> parameter.equals(DATE_PARAMETER, ignoreCase = true) } || value.trim().length == DATE_LENGTH

public fun StructuredLines.Property.toICalendarTime(): ICalendarTime? {
    val text = value.trim()

    return if (isICalendarDate()) {
        text.toICalendarDate()?.let { date -> ICalendarTime.Date(date = date) }
    } else {
        text.toICalendarDateTime()?.let { dateTime -> ICalendarTime.DateTime(dateTime = dateTime, timeZone = iCalendarTimeZoneOrNull()) }
    }
}

public fun StructuredLines.Property.iCalendarTimeZoneOrNull(): TimeZone? =
    if (value.trim().endsWith(UTC_DESIGNATOR, ignoreCase = true)) {
        TimeZone.UTC
    } else {
        parameters
            .split(';')
            .firstOrNull { parameter -> parameter.startsWith(TZID_PARAMETER, ignoreCase = true) }
            ?.drop(TZID_PARAMETER.length)
            ?.trim('"')
            ?.let { zoneId -> runCatching { TimeZone.of(zoneId) }.getOrNull() }
    }

private fun String.toICalendarDate(): LocalDate? =
    runCatching {
        LocalDate(
            year = substring(0, YEAR_END).toInt(),
            month = substring(YEAR_END, MONTH_END).toInt(),
            day = substring(MONTH_END, DATE_LENGTH).toInt(),
        )
    }.getOrNull()

private fun String.toICalendarDateTime(): LocalDateTime? =
    runCatching {
        check(this[DATE_LENGTH] == 'T')
        val date = checkNotNull(toICalendarDate())
        LocalDateTime(
            year = date.year,
            month = date.month,
            day = date.day,
            hour = substring(DATE_LENGTH + 1, DATE_LENGTH + HOUR_END).toInt(),
            minute = substring(DATE_LENGTH + HOUR_END, DATE_LENGTH + MINUTE_END).toInt(),
        )
    }.getOrNull()

private const val DATE_PARAMETER = "VALUE=DATE"
private const val TZID_PARAMETER = "TZID="
private const val UTC_DESIGNATOR = "Z"
private const val YEAR_END = 4
private const val MONTH_END = 6
private const val DATE_LENGTH = 8
private const val HOUR_END = 3
private const val MINUTE_END = 5
