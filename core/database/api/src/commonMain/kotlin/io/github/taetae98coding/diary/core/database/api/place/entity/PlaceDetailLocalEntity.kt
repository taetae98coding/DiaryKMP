package io.github.taetae98coding.diary.core.database.api.place.entity

import androidx.room3.ColumnInfo

public data class PlaceDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "color", defaultValue = "0")
    val color: Long,
    @ColumnInfo(name = "latitude", defaultValue = "0.0")
    val latitude: Double,
    @ColumnInfo(name = "longitude", defaultValue = "0.0")
    val longitude: Double,
    @ColumnInfo(name = "address", defaultValue = "")
    val address: String,
)
