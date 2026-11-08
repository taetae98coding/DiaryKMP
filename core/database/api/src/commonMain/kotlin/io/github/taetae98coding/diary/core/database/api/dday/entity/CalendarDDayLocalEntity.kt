package io.github.taetae98coding.diary.core.database.api.dday.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public data class CalendarDDayLocalEntity(
    @ColumnInfo(name = "dday_id")
    val ddayId: Uuid,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "color")
    val color: Long,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "kind")
    val kind: CalendarDDayKindLocalEntity,
    @ColumnInfo(name = "amount")
    val amount: Int,
)
