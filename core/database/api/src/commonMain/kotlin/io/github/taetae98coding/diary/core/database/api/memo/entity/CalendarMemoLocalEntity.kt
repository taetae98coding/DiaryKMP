package io.github.taetae98coding.diary.core.database.api.memo.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

public data class CalendarMemoLocalEntity(
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "color")
    val color: Long,
    @ColumnInfo(name = "is_all_day")
    val isAllDay: Boolean,
    @ColumnInfo(name = "start")
    val start: LocalDateTime,
    @ColumnInfo(name = "end_inclusive")
    val endInclusive: LocalDateTime,
)
