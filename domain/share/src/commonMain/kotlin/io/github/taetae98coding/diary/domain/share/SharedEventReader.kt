package io.github.taetae98coding.diary.domain.share

import io.github.taetae98coding.diary.library.kotlin.text.ICalendarTime
import io.github.taetae98coding.diary.library.kotlin.text.StructuredLines
import io.github.taetae98coding.diary.library.kotlin.text.toICalendarTime
import io.github.taetae98coding.diary.library.kotlin.text.unescapeStructuredText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal fun readSharedEvent(
    iCalendar: String,
    timeZone: TimeZone,
): ShareTarget.Memo {
    val propertyList = StructuredLines.parse(iCalendar).firstBlockPropertyList(component = "VEVENT")

    fun text(name: String): String =
        propertyList
            .first(name)
            ?.value
            ?.unescapeStructuredText()
            .orEmpty()

    val description = text("DESCRIPTION")
    val location = text("LOCATION")

    return ShareTarget.Memo(
        title = text("SUMMARY"),
        description =
            when {
                location.isEmpty() -> description
                description.isEmpty() -> "$LOCATION_LABEL$location"
                else -> "$description\n$LOCATION_LABEL$location"
            },
        period = readPeriod(start = propertyList.first("DTSTART"), end = propertyList.first("DTEND"), timeZone = timeZone),
    )
}

private fun readPeriod(
    start: StructuredLines.Property?,
    end: StructuredLines.Property?,
    timeZone: TimeZone,
): SharePeriod? =
    when (val startTime = start?.toICalendarTime()) {
        null -> {
            null
        }

        is ICalendarTime.Date -> {
            val endDate = (end?.toICalendarTime() as? ICalendarTime.Date)?.date?.minus(1, DateTimeUnit.DAY)
            SharePeriod.AllDay(start = startTime.date, endInclusive = endDate?.takeIf { date -> date >= startTime.date } ?: startTime.date)
        }

        is ICalendarTime.DateTime -> {
            val startDateTime = startTime.toLocalDateTime(timeZone)
            val endDateTime = (end?.toICalendarTime() as? ICalendarTime.DateTime)?.toLocalDateTime(timeZone)
            SharePeriod.DateTime(start = startDateTime, endInclusive = endDateTime?.takeIf { dateTime -> dateTime >= startDateTime } ?: startDateTime)
        }
    }

private fun ICalendarTime.DateTime.toLocalDateTime(deviceTimeZone: TimeZone): LocalDateTime = timeZone?.let { sourceTimeZone -> dateTime.toInstant(sourceTimeZone).toLocalDateTime(deviceTimeZone) } ?: dateTime

private const val LOCATION_LABEL = "장소: "
