package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistDetailRemoteEntity

internal fun ChecklistDetailLocalEntity.toRemote(): ChecklistDetailRemoteEntity =
    ChecklistDetailRemoteEntity(
        title = title,
        description = description,
    )

internal fun ChecklistDetailRemoteEntity.toLocal(): ChecklistDetailLocalEntity =
    ChecklistDetailLocalEntity(
        title = title,
        description = description,
    )
