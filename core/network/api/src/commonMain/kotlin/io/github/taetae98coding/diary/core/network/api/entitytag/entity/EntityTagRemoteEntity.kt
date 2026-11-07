package io.github.taetae98coding.diary.core.network.api.entitytag.entity

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class EntityTagRemoteEntity(
    val entityId: Uuid,
    val tagId: Uuid,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
