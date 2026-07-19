package io.github.taetae98coding.diary.core.database.api.placetag.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "place_tag",
    primaryKeys = ["place_id", "tag_id"],
    indices = [Index(value = ["tag_id"])],
)
public data class PlaceTagLocalEntity(
    @ColumnInfo(name = "place_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val placeId: Uuid,
    @ColumnInfo(name = "tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val tagId: Uuid,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean,
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Instant,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Instant,
)
