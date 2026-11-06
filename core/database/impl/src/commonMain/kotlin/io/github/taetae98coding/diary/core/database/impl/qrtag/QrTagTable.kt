package io.github.taetae98coding.diary.core.database.impl.qrtag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.AccountQrTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.QrTagLocalEntity

internal fun DiaryDatabase.qrTagTable(): EntityTagTable<QrTagLocalEntity, AccountQrTagLocalEntity> =
    EntityTagTable(
        linkDao = qrTagDao(),
        accountLinkDao = accountQrTagDao(),
        syncDao = accountQrTagSyncDao(),
        toLink = { entityTag ->
            QrTagLocalEntity(
                qrId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { qrTag ->
            EntityTagLocalEntity(
                entityId = qrTag.qrId,
                tagId = qrTag.tagId,
                isDeleted = qrTag.isDeleted,
                updatedAt = qrTag.updatedAt,
                createdAt = qrTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountQrTagLocalEntity(
                accountId = accountId,
                qrId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
