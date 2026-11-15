package io.github.taetae98coding.diary.core.database.impl.routinetag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.AccountRoutineTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.RoutineTagLocalEntity

internal fun DiaryDatabase.routineTagTable(): EntityTagTable<RoutineTagLocalEntity, AccountRoutineTagLocalEntity> =
    EntityTagTable(
        linkDao = routineTagDao(),
        accountLinkDao = accountRoutineTagDao(),
        syncDao = accountRoutineTagSyncDao(),
        toLink = { entityTag ->
            RoutineTagLocalEntity(
                routineId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { routineTag ->
            EntityTagLocalEntity(
                entityId = routineTag.routineId,
                tagId = routineTag.tagId,
                isDeleted = routineTag.isDeleted,
                updatedAt = routineTag.updatedAt,
                createdAt = routineTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountRoutineTagLocalEntity(
                accountId = accountId,
                routineId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
