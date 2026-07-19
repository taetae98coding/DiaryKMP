package io.github.taetae98coding.diary.core.database.api.memo.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDateTime

public data class MemoDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "color", defaultValue = "0")
    val color: Long,
    @ColumnInfo(name = "is_all_day", defaultValue = "NULL")
    val isAllDay: Boolean?,
    @ColumnInfo(name = "start", defaultValue = "NULL")
    val start: LocalDateTime?,
    @ColumnInfo(name = "end_inclusive", defaultValue = "NULL")
    val endInclusive: LocalDateTime?,
)
