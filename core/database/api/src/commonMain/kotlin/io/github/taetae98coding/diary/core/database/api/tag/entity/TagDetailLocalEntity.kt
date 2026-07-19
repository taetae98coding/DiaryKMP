package io.github.taetae98coding.diary.core.database.api.tag.entity

import androidx.room3.ColumnInfo

public data class TagDetailLocalEntity(
    @ColumnInfo(name = "emoji", defaultValue = "")
    val emoji: String,
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "color", defaultValue = "0")
    val color: Long,
)
