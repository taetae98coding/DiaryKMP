package io.github.taetae98coding.diary.core.model.memo

import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime

public sealed interface MemoDateTime {
    public data class AllDay(
        val dateRange: LocalDateRange,
    ) : MemoDateTime

    public data class DateTime(
        val start: LocalDateTime,
        val endInclusive: LocalDateTime,
    ) : MemoDateTime
}
