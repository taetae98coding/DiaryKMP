package io.github.taetae98coding.diary.core.database.impl.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_contact",
    primaryKeys = ["account_id", "contact_id"],
)
internal data class AccountContactLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "contact_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val contactId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
