package io.github.taetae98coding.diary.core.database.impl.dday.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_dday",
    primaryKeys = ["account_id", "dday_id"],
)
internal data class AccountDDayLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "dday_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val ddayId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
