package io.github.taetae98coding.diary.core.database.api.entitytag.entity

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity

public enum class EntityTagKindLocalEntity(
    public val syncKind: SyncKindLocalEntity,
) {
    WEB(SyncKindLocalEntity.WEB_TAG),
    PLACE(SyncKindLocalEntity.PLACE_TAG),
    CONTACT(SyncKindLocalEntity.CONTACT_TAG),
    CHECKLIST(SyncKindLocalEntity.CHECKLIST_TAG),
    MUSIC(SyncKindLocalEntity.MUSIC_TAG),
    QR(SyncKindLocalEntity.QR_TAG),
    DDAY(SyncKindLocalEntity.DDAY_TAG),
}
