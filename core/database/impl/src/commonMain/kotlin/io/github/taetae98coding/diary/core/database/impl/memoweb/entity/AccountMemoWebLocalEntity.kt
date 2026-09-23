package io.github.taetae98coding.diary.core.database.impl.memoweb.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_memo_web",
    primaryKeys = ["account_id", "memo_id", "web_id"],
)
internal data class AccountMemoWebLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "memo_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val memoId: Uuid,
    @ColumnInfo(name = "web_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val webId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
