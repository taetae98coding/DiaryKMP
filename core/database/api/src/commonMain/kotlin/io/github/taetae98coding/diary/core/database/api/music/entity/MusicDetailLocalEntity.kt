package io.github.taetae98coding.diary.core.database.api.music.entity

import androidx.room3.ColumnInfo

public data class MusicDetailLocalEntity(
    @ColumnInfo(name = "link", defaultValue = "")
    val link: String,
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "artist", defaultValue = "")
    val artist: String,
    @ColumnInfo(name = "thumbnail", defaultValue = "")
    val thumbnail: String,
)
