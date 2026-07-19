package io.github.taetae98coding.diary.core.database.api.memoweb.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "memo_web",
    primaryKeys = ["memo_id", "web_id"],
    indices = [Index(value = ["web_id"])],
)
public data class MemoWebLocalEntity(
    @ColumnInfo(name = "memo_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val memoId: Uuid,
    @ColumnInfo(name = "web_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val webId: Uuid,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean,
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Instant,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Instant,
)
