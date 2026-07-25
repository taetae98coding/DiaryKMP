package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime

public sealed interface DiaryDateTimeInputValue {
    @Immutable
    public data class AllDay(
        val dateRange: LocalDateRange,
    ) : DiaryDateTimeInputValue

    @Immutable
    public data class DateTime(
        val start: LocalDateTime,
        val endInclusive: LocalDateTime,
    ) : DiaryDateTimeInputValue
}
