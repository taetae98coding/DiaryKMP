package io.github.taetae98coding.diary.core.database.impl.entitytag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal suspend fun EntityTagTable<*, *>.restoreLink(
    accountId: Uuid,
    entityId: Uuid,
    tagId: Uuid,
    updatedAt: Instant,
) {
    val restoredCount =
        updateDeleted(
            entityId = entityId,
            tagId = tagId,
            isDeleted = false,
            updatedAt = updatedAt,
        )
    val entityTag =
        EntityTagLocalEntity(
            entityId = entityId,
            tagId = tagId,
            isDeleted = false,
            updatedAt = updatedAt,
            createdAt = updatedAt,
        )

    if (restoredCount == 0) {
        upsert(entityTagList = listOf(entityTag))
    }

    upsertAccountPending(accountId = accountId, entityTagList = listOf(entityTag))
}

internal suspend fun EntityTagTable<*, *>.deleteLink(
    accountId: Uuid,
    entityId: Uuid,
    tagId: Uuid,
    updatedAt: Instant,
) {
    val updatedCount =
        updateDeleted(
            entityId = entityId,
            tagId = tagId,
            isDeleted = true,
            updatedAt = updatedAt,
        )

    if (updatedCount > 0) {
        markPending(accountId = accountId, entityId = entityId, tagId = tagId)
    }

    clearPrimaryTagIdIfMatched(accountId, entityId, tagId, updatedAt)
}
