package io.github.taetae98coding.diary.core.database.impl.checklisttag.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_checklist_tag",
    primaryKeys = ["account_id", "checklist_id", "tag_id"],
)
internal data class AccountChecklistTagLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "checklist_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val checklistId: Uuid,
    @ColumnInfo(name = "tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val tagId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
