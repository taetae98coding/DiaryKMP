package io.github.taetae98coding.diary.core.database.impl.routinetag.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_routine_tag",
    primaryKeys = ["account_id", "routine_id", "tag_id"],
)
internal data class AccountRoutineTagLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "routine_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val routineId: Uuid,
    @ColumnInfo(name = "tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val tagId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
