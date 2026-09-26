package io.github.taetae98coding.diary.core.database.api.qr.entity

import androidx.room3.ColumnInfo

public data class QrDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "value", defaultValue = "")
    val value: String,
)
