package io.github.taetae98coding.diary.core.database.impl.checklisttag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.AccountChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.ChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable

internal fun DiaryDatabase.checklistTagTable(): EntityTagTable<ChecklistTagLocalEntity, AccountChecklistTagLocalEntity> =
    EntityTagTable(
        linkDao = checklistTagDao(),
        accountLinkDao = accountChecklistTagDao(),
        syncDao = accountChecklistTagSyncDao(),
        toLink = { entityTag ->
            ChecklistTagLocalEntity(
                checklistId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { checklistTag ->
            EntityTagLocalEntity(
                entityId = checklistTag.checklistId,
                tagId = checklistTag.tagId,
                isDeleted = checklistTag.isDeleted,
                updatedAt = checklistTag.updatedAt,
                createdAt = checklistTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountChecklistTagLocalEntity(
                accountId = accountId,
                checklistId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
