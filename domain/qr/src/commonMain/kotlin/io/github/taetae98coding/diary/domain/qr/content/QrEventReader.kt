package io.github.taetae98coding.diary.domain.qr.content

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus

internal fun StructuredLines.eventRange(): IntRange? {
    val start = indexOfLine("BEGIN:VEVENT") ?: return null
    val end = indexOfLine("END:VEVENT", from = start) ?: size

    return start until end
}

internal fun readEvent(raw: String): QrContent.Event {
    val lines = StructuredLines.parse(raw)
    val range = lines.eventRange() ?: return QrContent.Event()

    fun property(name: String): StructuredLines.Property? = lines.find(name, range)?.property

    return QrContent.Event(
        title = property("SUMMARY")?.value?.unescapeStructuredText().orEmpty(),
        period = readPeriod(start = property("DTSTART"), end = property("DTEND")),
        location = property("LOCATION")?.value?.unescapeStructuredText().orEmpty(),
        description = property("DESCRIPTION")?.value?.unescapeStructuredText().orEmpty(),
    )
}

private fun readPeriod(
    start: StructuredLines.Property?,
    end: StructuredLines.Property?,
): QrEventPeriod? =
    when {
        start == null -> null
        start.isDate() -> readAllDayPeriod(start = start, end = end)
        else -> readDateTimePeriod(start = start, end = end)
    }

private fun readAllDayPeriod(
    start: StructuredLines.Property,
    end: StructuredLines.Property?,
): QrEventPeriod? =
    start.value.toICalendarDate()?.let { startDate ->
        val endDate =
            end
                ?.value
                ?.toICalendarDate()
                ?.minus(1, DateTimeUnit.DAY)
                ?.takeIf { date -> date >= startDate } ?: startDate
        QrEventPeriod.AllDay(start = startDate, endInclusive = endDate)
    }

private fun readDateTimePeriod(
    start: StructuredLines.Property,
    end: StructuredLines.Property?,
): QrEventPeriod? =
    start.value.toICalendarDateTime()?.let { startDateTime ->
        val endDateTime = end?.value?.toICalendarDateTime()?.takeIf { dateTime -> dateTime >= startDateTime } ?: startDateTime
        QrEventPeriod.DateTime(start = startDateTime, endInclusive = endDateTime)
    }

internal fun StructuredLines.Property.isDate(): Boolean = parameters.split(';').any { parameter -> parameter.equals("VALUE=DATE", ignoreCase = true) }

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

private const val YEAR_END = 4
private const val MONTH_END = 6
private const val DATE_LENGTH = 8
private const val HOUR_END = 3
private const val MINUTE_END = 5
