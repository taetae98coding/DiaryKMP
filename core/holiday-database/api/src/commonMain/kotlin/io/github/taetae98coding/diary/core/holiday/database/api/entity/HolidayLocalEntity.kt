package io.github.taetae98coding.diary.core.holiday.database.api.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "holiday",
    primaryKeys = ["year", "name", "start", "end_inclusive"],
)
public data class HolidayLocalEntity(
    @ColumnInfo(name = "year", defaultValue = "0")
    val year: Int,
    @ColumnInfo(name = "name", defaultValue = "")
    val name: String,
    @ColumnInfo(name = "is_holiday", defaultValue = "0")
    val isHoliday: Boolean,
    @ColumnInfo(name = "start", defaultValue = "1970-01-01")
    val start: LocalDate,
    @ColumnInfo(name = "end_inclusive", defaultValue = "1970-01-01")
    val endInclusive: LocalDate,
)
