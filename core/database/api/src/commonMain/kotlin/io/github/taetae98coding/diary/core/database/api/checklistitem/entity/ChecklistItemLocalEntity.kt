package io.github.taetae98coding.diary.core.database.api.checklistitem.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "checklist_item",
    indices = [Index(value = ["checklist_id"])],
)
public data class ChecklistItemLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val id: Uuid,
    @ColumnInfo(name = "checklist_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val checklistId: Uuid,
    @ColumnInfo(name = "content", defaultValue = "")
    val content: String,
    @ColumnInfo(name = "is_checked", defaultValue = "0")
    val isChecked: Boolean,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean,
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Instant,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Instant,
)
