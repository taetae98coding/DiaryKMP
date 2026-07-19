package io.github.taetae98coding.diary.core.database.api.memo.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "memo",
    indices = [Index(value = ["primary_tag_id"])],
)
public data class MemoLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val id: Uuid,
    @Embedded
    val detail: MemoDetailLocalEntity,
    @ColumnInfo(name = "primary_tag_id", defaultValue = "NULL")
    val primaryTagId: Uuid?,
    @ColumnInfo(name = "is_finished", defaultValue = "0")
    val isFinished: Boolean,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean,
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Instant,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Instant,
)
