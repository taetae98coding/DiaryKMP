package io.github.taetae98coding.diary.domain.memo.share

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun memoICalendarOf(
    id: Uuid,
    title: String,
    description: String,
    dateTime: MemoDateTime,
    timeZone: TimeZone,
    sharedAt: Instant,
): String =
    buildList {
        add("BEGIN:VCALENDAR")
        add("VERSION:2.0")
        add("PRODID:$PRODUCT_ID")
        add("BEGIN:VEVENT")
        add("UID:$id$UID_SUFFIX")
        add("DTSTAMP:${sharedAt.toLocalDateTime(TimeZone.UTC).toUtcText()}")
        add("SUMMARY:${title.trim().escapeICalendarText()}")
        addAll(dateTime.periodLineList(timeZone = timeZone))
        description.trim().takeIf(String::isNotEmpty)?.let { value -> add("DESCRIPTION:${value.escapeICalendarText()}") }
        add("END:VEVENT")
        add("END:VCALENDAR")
    }.joinToString(separator = LINE_BREAK, postfix = LINE_BREAK) { line -> line.foldICalendarLine() }

private fun MemoDateTime.periodLineList(timeZone: TimeZone): List<String> =
    when (this) {
        is MemoDateTime.AllDay -> {
            listOf(
                "DTSTART;VALUE=DATE:${dateRange.start.toDateText()}",
                "DTEND;VALUE=DATE:${dateRange.endInclusive.plus(1, DateTimeUnit.DAY).toDateText()}",
            )
        }

        is MemoDateTime.DateTime -> {
            listOf(
                "DTSTART:${start.toUtc(timeZone = timeZone).toUtcText()}",
                "DTEND:${endInclusive.toUtc(timeZone = timeZone).toUtcText()}",
            )
        }
    }

private fun LocalDateTime.toUtc(timeZone: TimeZone): LocalDateTime = toInstant(timeZone).toLocalDateTime(TimeZone.UTC)

private fun LocalDate.toDateText(): String = ICALENDAR_DATE_FORMAT.format(this)

private fun LocalDateTime.toUtcText(): String = ICALENDAR_UTC_DATE_TIME_FORMAT.format(this)

private fun String.escapeICalendarText(): String =
    replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace(LINE_BREAK_REGEX, "\\\\n")

// 서로게이트 쌍은 두 Char지만 한 글자이므로 함께 옮긴다.
private fun String.foldICalendarLine(): String {
    val builder = StringBuilder()
    var lineOctets = 0
    var index = 0
    while (index < length) {
        val unitLength = if (this[index].isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) 2 else 1
        val unit = substring(index, index + unitLength)
        val unitOctets = unit.encodeToByteArray().size
        if (lineOctets + unitOctets > MAX_LINE_OCTETS) {
            builder.append(LINE_BREAK).append(FOLD_PREFIX)
            lineOctets = FOLD_PREFIX.length
        }
        builder.append(unit)
        lineOctets += unitOctets
        index += unitLength
    }
    return builder.toString()
}

private const val PRODUCT_ID = "-//Diary//Diary//EN"
private const val UID_SUFFIX = "@diary"
private const val LINE_BREAK = "\r\n"
private const val FOLD_PREFIX = " "
private const val MAX_LINE_OCTETS = 75
private val LINE_BREAK_REGEX = Regex("\r\n|\r|\n")

private val ICALENDAR_DATE_FORMAT =
    LocalDate.Format {
        year()
        monthNumber()
        day()
    }

private val ICALENDAR_UTC_DATE_TIME_FORMAT =
    LocalDateTime.Format {
        date(ICALENDAR_DATE_FORMAT)
        char('T')
        hour()
        minute()
        second()
        char('Z')
    }
