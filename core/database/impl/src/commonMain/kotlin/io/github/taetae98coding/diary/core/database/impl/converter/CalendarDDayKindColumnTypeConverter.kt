package io.github.taetae98coding.diary.core.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity

internal class CalendarDDayKindColumnTypeConverter {
    @ColumnTypeConverter
    fun kindToText(kind: CalendarDDayKindLocalEntity): String = kind.queryValue

    @ColumnTypeConverter
    fun textToKind(value: String): CalendarDDayKindLocalEntity = requireNotNull(CalendarDDayKindLocalEntity.fromQueryValue(value)) { "Unknown calendar dday kind: $value" }
}
