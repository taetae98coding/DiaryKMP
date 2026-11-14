package io.github.taetae98coding.diary.core.database.api.routinerecord.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "routine_record",
    indices = [Index(value = ["routine_id", "date"])],
)
public data class RoutineRecordLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val id: Uuid,
    @ColumnInfo(name = "routine_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val routineId: Uuid,
    @ColumnInfo(name = "date", defaultValue = "'1970-01-01'")
    val date: LocalDate,
    @ColumnInfo(name = "amount", defaultValue = "1")
    val amount: Int,
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean,
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Instant,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Instant,
)
