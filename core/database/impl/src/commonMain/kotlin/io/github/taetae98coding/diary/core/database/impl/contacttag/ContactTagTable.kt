package io.github.taetae98coding.diary.core.database.impl.contacttag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.AccountContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.ContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable

internal fun DiaryDatabase.contactTagTable(): EntityTagTable<ContactTagLocalEntity, AccountContactTagLocalEntity> =
    EntityTagTable(
        linkDao = contactTagDao(),
        accountLinkDao = accountContactTagDao(),
        syncDao = accountContactTagSyncDao(),
        toLink = { entityTag ->
            ContactTagLocalEntity(
                contactId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { contactTag ->
            EntityTagLocalEntity(
                entityId = contactTag.contactId,
                tagId = contactTag.tagId,
                isDeleted = contactTag.isDeleted,
                updatedAt = contactTag.updatedAt,
                createdAt = contactTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountContactTagLocalEntity(
                accountId = accountId,
                contactId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
