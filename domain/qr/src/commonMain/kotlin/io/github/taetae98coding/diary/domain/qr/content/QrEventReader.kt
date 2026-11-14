package io.github.taetae98coding.diary.domain.qr.content

import io.github.taetae98coding.diary.library.kotlin.text.ICalendarTime
import io.github.taetae98coding.diary.library.kotlin.text.StructuredLines
import io.github.taetae98coding.diary.library.kotlin.text.toICalendarTime
import io.github.taetae98coding.diary.library.kotlin.text.unescapeStructuredText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

internal fun StructuredLines.eventRange(): IntRange? {
    val start = indexOfLine("BEGIN:VEVENT") ?: return null
    val end = indexOfLine("END:VEVENT", from = start) ?: size

    return start until end
}

internal fun StructuredLines.findEventEntry(name: String): StructuredLines.Entry? = blockEntries(EVENT_COMPONENT).firstOrNull { entry -> entry.property?.name == name }

internal fun readEvent(raw: String): QrContent.Event {
    val lines = StructuredLines.parse(raw)

    fun property(name: String): StructuredLines.Property? = lines.findEventEntry(name)?.property

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
    when (val startTime = start?.toICalendarTime()) {
        null -> {
            null
        }

        is ICalendarTime.Date -> {
            val endDate = (end?.toICalendarTime() as? ICalendarTime.Date)?.date?.minus(1, DateTimeUnit.DAY)
            QrEventPeriod.AllDay(start = startTime.date, endInclusive = endDate?.takeIf { date -> date >= startTime.date } ?: startTime.date)
        }

        is ICalendarTime.DateTime -> {
            val endDateTime = (end?.toICalendarTime() as? ICalendarTime.DateTime)?.dateTime
            QrEventPeriod.DateTime(start = startTime.dateTime, endInclusive = endDateTime?.takeIf { dateTime -> dateTime >= startTime.dateTime } ?: startTime.dateTime)
        }
    }

private const val EVENT_COMPONENT = "VEVENT"
