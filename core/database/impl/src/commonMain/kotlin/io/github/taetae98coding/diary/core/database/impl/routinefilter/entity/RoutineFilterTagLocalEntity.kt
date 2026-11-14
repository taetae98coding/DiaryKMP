package io.github.taetae98coding.diary.core.database.impl.routinefilter.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "routine_filter_tag",
    primaryKeys = ["account_id", "tag_id"],
)
internal data class RoutineFilterTagLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "tag_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val tagId: Uuid,
)
