package io.github.taetae98coding.diary.core.database.impl.musictag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.EntityTagTable
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.AccountMusicTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.MusicTagLocalEntity

internal fun DiaryDatabase.musicTagTable(): EntityTagTable<MusicTagLocalEntity, AccountMusicTagLocalEntity> =
    EntityTagTable(
        linkDao = musicTagDao(),
        accountLinkDao = accountMusicTagDao(),
        syncDao = accountMusicTagSyncDao(),
        toLink = { entityTag ->
            MusicTagLocalEntity(
                musicId = entityTag.entityId,
                tagId = entityTag.tagId,
                isDeleted = entityTag.isDeleted,
                updatedAt = entityTag.updatedAt,
                createdAt = entityTag.createdAt,
            )
        },
        toEntityTag = { musicTag ->
            EntityTagLocalEntity(
                entityId = musicTag.musicId,
                tagId = musicTag.tagId,
                isDeleted = musicTag.isDeleted,
                updatedAt = musicTag.updatedAt,
                createdAt = musicTag.createdAt,
            )
        },
        toAccountLink = { accountId, entityId, tagId, isDirty ->
            AccountMusicTagLocalEntity(
                accountId = accountId,
                musicId = entityId,
                tagId = tagId,
                isDirty = isDirty,
            )
        },
    )
