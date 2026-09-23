package io.github.taetae98coding.diary.core.model.contact

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Contact(
    val id: Uuid,
    val detail: ContactDetail,
    val isFavorite: Boolean,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
