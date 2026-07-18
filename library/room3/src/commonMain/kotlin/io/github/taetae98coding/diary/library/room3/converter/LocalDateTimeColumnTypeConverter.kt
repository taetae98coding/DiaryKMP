package io.github.taetae98coding.diary.library.room3.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDateTime

public class LocalDateTimeColumnTypeConverter {
    @ColumnTypeConverter
    public fun localDateTimeToIsoString(localDateTime: LocalDateTime): String = localDateTime.toString()

    @ColumnTypeConverter
    public fun isoStringToLocalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value)
}
