package io.github.taetae98coding.diary.library.room3.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate

public class LocalDateColumnTypeConverter {
    @ColumnTypeConverter
    public fun localDateToIsoString(localDate: LocalDate): String = localDate.toString()

    @ColumnTypeConverter
    public fun isoStringToLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
