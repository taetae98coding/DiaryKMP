package io.github.taetae98coding.diary.core.model.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.uuid.Uuid

public data class SearchedPlace(
    val id: Uuid,
    val name: String,
    val address: String,
    val coordinate: Coordinate,
)
