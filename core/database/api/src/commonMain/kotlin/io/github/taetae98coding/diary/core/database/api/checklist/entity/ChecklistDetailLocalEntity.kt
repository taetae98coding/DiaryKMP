package io.github.taetae98coding.diary.core.database.api.checklist.entity

import androidx.room3.ColumnInfo

public data class ChecklistDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
)
