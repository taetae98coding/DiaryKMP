package io.github.taetae98coding.diary.core.database.api.dday.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

public data class DDayDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "color", defaultValue = "0")
    val color: Long,
    @ColumnInfo(name = "base_date", defaultValue = "'1970-01-01'")
    val baseDate: LocalDate,
    @ColumnInfo(name = "counts_base_day", defaultValue = "1")
    val countsBaseDay: Boolean,
    @ColumnInfo(name = "repeats", defaultValue = "'[]'")
    val repeats: Set<DateTimeUnit.DateBased>,
    @ColumnInfo(name = "end_date", defaultValue = "NULL")
    val endDate: LocalDate?,
    @ColumnInfo(name = "overflow_policy", defaultValue = "'clamp_to_month_end'")
    val overflowPolicy: DDayOverflowPolicyLocalEntity,
)
