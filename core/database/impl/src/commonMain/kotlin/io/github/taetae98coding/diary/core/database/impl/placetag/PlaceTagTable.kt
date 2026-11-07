package io.github.taetae98coding.diary.core.database.impl.placetag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.PlaceTagLocalEntity

internal fun DiaryDatabase.placeTagTable(): EntityTagTable<PlaceTagLocalEntity, AccountPlaceTagLocalEntity> =
    EntityTagTable(
        linkDao = placeTagDao(),
        accountLinkDao = accountPlaceTagDao(),
        syncDao = accountPlaceTagSyncDao(),
        toLink = { entityTag ->
            PlaceTagLocalEntity(
                placeId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { placeTag ->
            EntityTagLocalEntity(
                entityId = placeTag.placeId,
                tagId = placeTag.tagId,
                isDeleted = placeTag.isDeleted,
                updatedAt = placeTag.updatedAt,
                createdAt = placeTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountPlaceTagLocalEntity(
                accountId = accountId,
                placeId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
