package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun Set<Uuid>.toEntityTagLocalList(
    entityId: Uuid,
    updatedAt: Instant,
    createdAt: Instant,
): List<EntityTagLocalEntity> =
    map { tagId ->
        EntityTagLocalEntity(
            entityId = entityId,
            tagId = tagId,
            isDeleted = false,
            updatedAt = updatedAt,
            createdAt = createdAt,
        )
    }
