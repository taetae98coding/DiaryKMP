package io.github.taetae98coding.diary.core.database.api.web.entity

import androidx.room3.ColumnInfo

public data class WebDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "url", defaultValue = "")
    val url: String,
    @ColumnInfo(name = "header_list", defaultValue = "'[]'")
    val headerList: List<WebHeaderLocalEntity>,
)
