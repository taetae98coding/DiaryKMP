package io.github.taetae98coding.diary.work.appfunction.core

import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appfunction.item.AppFunctionTag

internal fun AppActionTag.expectedTag(): AppFunctionTag =
    AppFunctionTag(
        id = id,
        emoji = emoji,
        title = title,
        description = description,
        color = color,
        isFinished = isFinished,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
