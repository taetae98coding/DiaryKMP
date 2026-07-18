package io.github.taetae98coding.diary.core.model.place

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Place(
    val id: Uuid,
    val detail: PlaceDetail,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
