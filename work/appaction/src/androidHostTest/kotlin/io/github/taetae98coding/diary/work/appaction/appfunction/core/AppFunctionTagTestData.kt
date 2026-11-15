package io.github.taetae98coding.diary.work.appaction.appfunction.core

import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag

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
