package io.github.taetae98coding.diary.core.database.api.memofilter.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "memo_existence_filter")
public data class MemoExistenceFilterLocalEntity(
    @ColumnInfo(name = "has_date", defaultValue = "NULL")
    val hasDate: Boolean?,
    @ColumnInfo(name = "has_tag", defaultValue = "NULL")
    val hasTag: Boolean?,
    @ColumnInfo(name = "has_place", defaultValue = "NULL")
    val hasPlace: Boolean?,
    @PrimaryKey
    @ColumnInfo(name = "id", defaultValue = "0")
    val id: Int = SINGLETON_ID,
) {
    public companion object {
        public const val SINGLETON_ID: Int = 0
    }
}
