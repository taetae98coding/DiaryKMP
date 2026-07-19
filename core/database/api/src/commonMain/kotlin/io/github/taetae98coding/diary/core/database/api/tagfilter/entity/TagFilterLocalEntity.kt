package io.github.taetae98coding.diary.core.database.api.tagfilter.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(tableName = "tag_filter")
public data class TagFilterLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "is_top_level_only", defaultValue = "0")
    val isTopLevelOnly: Boolean,
)
