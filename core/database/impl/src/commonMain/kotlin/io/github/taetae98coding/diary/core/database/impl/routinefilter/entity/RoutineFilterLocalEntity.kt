package io.github.taetae98coding.diary.core.database.impl.routinefilter.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import io.github.taetae98coding.diary.core.database.api.routinefilter.entity.RoutineViewScopeLocalEntity
import kotlin.uuid.Uuid

@Entity(tableName = "routine_filter")
internal data class RoutineFilterLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "view_scope", defaultValue = "'today'")
    val viewScope: RoutineViewScopeLocalEntity,
)
