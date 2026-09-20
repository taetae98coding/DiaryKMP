package io.github.taetae98coding.diary.core.database.api.memo.entity

import androidx.room3.ColumnInfo
import kotlin.uuid.Uuid

public data class DailyMemoLocalEntity(
    @ColumnInfo(name = "id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val id: Uuid,
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
)
