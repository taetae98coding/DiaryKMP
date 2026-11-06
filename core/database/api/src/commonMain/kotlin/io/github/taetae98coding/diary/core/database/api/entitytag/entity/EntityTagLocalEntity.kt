package io.github.taetae98coding.diary.core.database.api.entitytag.entity

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class EntityTagLocalEntity(
    val entityId: Uuid,
    val tagId: Uuid,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
