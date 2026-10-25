package io.github.taetae98coding.diary.core.database.impl.checklistitem.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_checklist_item",
    primaryKeys = ["account_id", "checklist_item_id"],
)
internal data class AccountChecklistItemLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "checklist_item_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val checklistItemId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
