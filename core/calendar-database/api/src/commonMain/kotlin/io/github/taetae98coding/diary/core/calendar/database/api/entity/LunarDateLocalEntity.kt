package io.github.taetae98coding.diary.core.calendar.database.api.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "lunar_date",
    primaryKeys = ["solar"],
)
public data class LunarDateLocalEntity(
    @ColumnInfo(name = "solar", defaultValue = "1970-01-01")
    val solar: LocalDate,
    @ColumnInfo(name = "solar_year", defaultValue = "0")
    val solarYear: Int,
    @ColumnInfo(name = "lunar_year", defaultValue = "0")
    val lunarYear: Int,
    @ColumnInfo(name = "lunar_month", defaultValue = "0")
    val lunarMonth: Int,
    @ColumnInfo(name = "lunar_day", defaultValue = "0")
    val lunarDay: Int,
    @ColumnInfo(name = "is_leap_month", defaultValue = "0")
    val isLeapMonth: Boolean,
)
