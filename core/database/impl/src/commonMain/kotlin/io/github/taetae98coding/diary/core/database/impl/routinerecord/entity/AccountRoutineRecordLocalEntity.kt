package io.github.taetae98coding.diary.core.database.impl.routinerecord.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_routine_record",
    primaryKeys = ["account_id", "routine_record_id"],
)
internal data class AccountRoutineRecordLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "routine_record_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val routineRecordId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
