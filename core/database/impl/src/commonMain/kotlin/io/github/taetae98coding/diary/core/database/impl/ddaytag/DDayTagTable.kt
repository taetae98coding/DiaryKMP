package io.github.taetae98coding.diary.core.database.impl.ddaytag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.AccountDDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.DDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable

internal fun DiaryDatabase.ddayTagTable(): EntityTagTable<DDayTagLocalEntity, AccountDDayTagLocalEntity> =
    EntityTagTable(
        linkDao = ddayTagDao(),
        accountLinkDao = accountDDayTagDao(),
        syncDao = accountDDayTagSyncDao(),
        toLink = { entityTag ->
            DDayTagLocalEntity(
                ddayId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { ddayTag ->
            EntityTagLocalEntity(
                entityId = ddayTag.ddayId,
                tagId = ddayTag.tagId,
                isDeleted = ddayTag.isDeleted,
                updatedAt = ddayTag.updatedAt,
                createdAt = ddayTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountDDayTagLocalEntity(
                accountId = accountId,
                ddayId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
