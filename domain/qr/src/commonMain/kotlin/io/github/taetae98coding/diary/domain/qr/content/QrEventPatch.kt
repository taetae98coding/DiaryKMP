package io.github.taetae98coding.diary.domain.qr.content

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

internal fun QrContent.Event.patchEvent(
    raw: String,
    previous: QrContent.Event,
): String {
    val lines = StructuredLines.parse(raw)

    if (title != previous.title) lines.setEventValue("SUMMARY", title.trim().escapeICalendarText())
    if (location != previous.location) lines.setEventValue("LOCATION", location.trim().escapeICalendarText())
    if (description != previous.description) lines.setEventValue("DESCRIPTION", description.trim().escapeICalendarText())
    if (period != previous.period) lines.setEventPeriod(period = period, previous = previous.period)

    return lines.toString()
}

private fun StructuredLines.setEventValue(
    property: String,
    value: String,
) {
    val range = eventRange() ?: return
    val entry = find(property, range)
    when {
        entry != null && value.isEmpty() -> remove(entry)
        entry != null -> replace(entry, "${checkNotNull(entry.property).head}:$value")
        value.isNotEmpty() -> insert(range.last + 1, "$property:$value")
    }
}

private fun StructuredLines.setEventPeriod(
    period: QrEventPeriod?,
    previous: QrEventPeriod?,
) {
    val range = eventRange() ?: return
    val startEntry = find("DTSTART", range)
    val endEntry = find("DTEND", range)

    val canKeepZone = startEntry != null && endEntry != null

    if (period != null && period.isSameKind(previous) && canKeepZone) {
        val (startValue, endValue) = period.iCalendarValues()
        replace(checkNotNull(endEntry), checkNotNull(endEntry.property).withValue(endValue))
        replace(checkNotNull(startEntry), checkNotNull(startEntry.property).withValue(startValue))
    } else {
        listOfNotNull(endEntry, startEntry).sortedByDescending(StructuredLines.Entry::start).forEach(::remove)
        val insertIndex = startEntry?.start ?: (eventRange()?.let { newRange -> newRange.last + 1 } ?: size)
        period?.iCalendarLines()?.reversed()?.forEach { line -> insert(insertIndex, line) }
    }
}

private fun QrEventPeriod.isSameKind(other: QrEventPeriod?): Boolean = other != null && this is QrEventPeriod.AllDay == other is QrEventPeriod.AllDay

// 종일 여부가 같으면 매개변수와 끝의 Z 같은 시간대 표기를 두고 날짜와 시각 숫자만 바꾼다.
private fun StructuredLines.Property.withValue(value: String): String {
    val zone = if (!isDate() && this.value.endsWith('Z')) "Z" else ""
    return "$head:$value$zone"
}

private fun QrEventPeriod.iCalendarValues(): Pair<String, String> =
    when (this) {
        is QrEventPeriod.AllDay -> start.toICalendarText() to endInclusive.plus(1, DateTimeUnit.DAY).toICalendarText()
        is QrEventPeriod.DateTime -> start.toICalendarText() to endInclusive.toICalendarText()
    }

internal fun QrEventPeriod.iCalendarLines(): List<String> {
    val (start, end) = iCalendarValues()

    return when (this) {
        is QrEventPeriod.AllDay -> listOf("DTSTART;VALUE=DATE:$start", "DTEND;VALUE=DATE:$end")
        is QrEventPeriod.DateTime -> listOf("DTSTART:$start", "DTEND:$end")
    }
}
