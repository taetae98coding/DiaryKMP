package io.github.taetae98coding.diary.core.database.api.taglink.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "tag_link",
    primaryKeys = ["from_tag_id", "to_tag_id"],
    indices = [Index(value = ["to_tag_id"])],
)
public data class TagLinkLocalEntity(
    @ColumnInfo(name = "from_tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val fromTagId: Uuid,
    @ColumnInfo(name = "to_tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val toTagId: Uuid,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean,
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Instant,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Instant,
)
