package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.work.appaction.core.toColorHex

public data class AppActionPlace(
    val id: String,
    val title: String,
    val description: String,
    val color: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String,
    val updatedAt: String,
)

internal fun Place.toAppActionPlace(): AppActionPlace =
    AppActionPlace(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        color = detail.color.toColorHex(),
        address = detail.address,
        latitude = detail.coordinate.latitude.takeIf(Double::isFinite),
        longitude = detail.coordinate.longitude.takeIf(Double::isFinite),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
