package io.github.taetae98coding.diary.domain.qr.content

import io.github.taetae98coding.diary.library.kotlin.text.StructuredLines
import io.github.taetae98coding.diary.library.kotlin.text.iCalendarTimeZoneOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

public sealed interface QrEventSchedule {
    public data class AllDay(
        val start: LocalDate,
        val endInclusive: LocalDate,
    ) : QrEventSchedule

    public data class DateTime(
        val start: Instant,
        val end: Instant,
    ) : QrEventSchedule
}

internal fun readEventSchedule(
    raw: String,
    period: QrEventPeriod,
    deviceTimeZone: TimeZone,
): QrEventSchedule =
    when (period) {
        is QrEventPeriod.AllDay -> {
            QrEventSchedule.AllDay(start = period.start, endInclusive = period.endInclusive)
        }

        is QrEventPeriod.DateTime -> {
            val lines = StructuredLines.parse(raw)
            val start = lines.findEventEntry("DTSTART")?.property
            val end = lines.findEventEntry("DTEND")?.property ?: start
            QrEventSchedule.DateTime(
                start = period.start.toInstant(start.timeZoneOr(deviceTimeZone)),
                end = period.endInclusive.toInstant(end.timeZoneOr(deviceTimeZone)),
            )
        }
    }

private fun StructuredLines.Property?.timeZoneOr(deviceTimeZone: TimeZone): TimeZone = this?.iCalendarTimeZoneOrNull() ?: deviceTimeZone
