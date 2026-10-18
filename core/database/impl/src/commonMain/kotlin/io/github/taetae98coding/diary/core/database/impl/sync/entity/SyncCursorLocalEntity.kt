package io.github.taetae98coding.diary.core.database.impl.sync.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "sync_cursor",
    primaryKeys = ["account_id", "kind"],
)
internal data class SyncCursorLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "kind", defaultValue = "''")
    val kind: String,
    @ColumnInfo(name = "usn", defaultValue = "0")
    val usn: Long,
)
