package io.github.taetae98coding.diary.core.database.impl.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_tag_link",
    primaryKeys = ["account_id", "from_tag_id", "to_tag_id"],
)
internal data class AccountTagLinkLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "from_tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val fromTagId: Uuid,
    @ColumnInfo(name = "to_tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val toTagId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
