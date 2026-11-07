package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagRemoteEntity

internal fun EntityTagLocalEntity.toRemote(): EntityTagRemoteEntity =
    EntityTagRemoteEntity(
        entityId = entityId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun EntityTagRemoteEntity.toLocal(): EntityTagLocalEntity =
    EntityTagLocalEntity(
        entityId = entityId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun EntityTagKindLocalEntity.toRemote(): EntityTagKindRemoteEntity =
    when (this) {
        EntityTagKindLocalEntity.WEB -> EntityTagKindRemoteEntity.WEB
        EntityTagKindLocalEntity.PLACE -> EntityTagKindRemoteEntity.PLACE
        EntityTagKindLocalEntity.CONTACT -> EntityTagKindRemoteEntity.CONTACT
        EntityTagKindLocalEntity.CHECKLIST -> EntityTagKindRemoteEntity.CHECKLIST
        EntityTagKindLocalEntity.MUSIC -> EntityTagKindRemoteEntity.MUSIC
        EntityTagKindLocalEntity.QR -> EntityTagKindRemoteEntity.QR
    }
