package io.github.taetae98coding.diary.core.database.api.checklist.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

public data class ChecklistSummaryLocalEntity(
    @Embedded
    val checklist: ChecklistLocalEntity,
    @ColumnInfo(name = "checked_count", defaultValue = "0")
    val checkedCount: Int,
    @ColumnInfo(name = "total_count", defaultValue = "0")
    val totalCount: Int,
)
