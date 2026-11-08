package io.github.taetae98coding.diary.data.tag.mapper

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.domain.tag.EntityTagKind

internal fun EntityTagKind.toLocal(): EntityTagKindLocalEntity =
    when (this) {
        EntityTagKind.WEB -> EntityTagKindLocalEntity.WEB
        EntityTagKind.PLACE -> EntityTagKindLocalEntity.PLACE
        EntityTagKind.CONTACT -> EntityTagKindLocalEntity.CONTACT
        EntityTagKind.CHECKLIST -> EntityTagKindLocalEntity.CHECKLIST
        EntityTagKind.MUSIC -> EntityTagKindLocalEntity.MUSIC
        EntityTagKind.QR -> EntityTagKindLocalEntity.QR
        EntityTagKind.DDAY -> EntityTagKindLocalEntity.DDAY
    }
