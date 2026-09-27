package io.github.taetae98coding.diary.domain.qr.content

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

public sealed interface QrEventPeriod {
    public data class AllDay(
        val start: LocalDate,
        val endInclusive: LocalDate,
    ) : QrEventPeriod

    public data class DateTime(
        val start: LocalDateTime,
        val endInclusive: LocalDateTime,
    ) : QrEventPeriod
}
