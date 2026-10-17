package io.github.taetae98coding.diary.domain.qr.content

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
            val range = lines.eventRange()
            val start = range?.let { lines.find("DTSTART", it) }?.property
            val end = range?.let { lines.find("DTEND", it) }?.property ?: start
            QrEventSchedule.DateTime(
                start = period.start.toInstant(start.timeZoneOr(deviceTimeZone)),
                end = period.endInclusive.toInstant(end.timeZoneOr(deviceTimeZone)),
            )
        }
    }

private fun StructuredLines.Property?.timeZoneOr(deviceTimeZone: TimeZone): TimeZone =
    when {
        this == null -> deviceTimeZone
        value.trim().endsWith(UTC_DESIGNATOR, ignoreCase = true) -> TimeZone.UTC
        else -> zoneId()?.let { zoneId -> runCatching { TimeZone.of(zoneId) }.getOrNull() } ?: deviceTimeZone
    }

private fun StructuredLines.Property.zoneId(): String? =
    parameters
        .split(';')
        .firstOrNull { parameter -> parameter.startsWith(TZID_PARAMETER, ignoreCase = true) }
        ?.drop(TZID_PARAMETER.length)
        ?.trim('"')

private const val UTC_DESIGNATOR = "Z"
private const val TZID_PARAMETER = "TZID="
