package io.github.taetae98coding.diary.core.database.impl.webtag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.WebTagLocalEntity

internal fun DiaryDatabase.webTagTable(): EntityTagTable<WebTagLocalEntity, AccountWebTagLocalEntity> =
    EntityTagTable(
        linkDao = webTagDao(),
        accountLinkDao = accountWebTagDao(),
        syncDao = accountWebTagSyncDao(),
        toLink = { entityTag ->
            WebTagLocalEntity(
                webId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { webTag ->
            EntityTagLocalEntity(
                entityId = webTag.webId,
                tagId = webTag.tagId,
                isDeleted = webTag.isDeleted,
                updatedAt = webTag.updatedAt,
                createdAt = webTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountWebTagLocalEntity(
                accountId = accountId,
                webId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
