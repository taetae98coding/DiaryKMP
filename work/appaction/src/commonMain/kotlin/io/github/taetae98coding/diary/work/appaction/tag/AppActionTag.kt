package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.work.appaction.core.toColorHex

public data class AppActionTag(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val color: String,
    val isFinished: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

internal fun Tag.toAppActionTag(): AppActionTag =
    AppActionTag(
        id = id.toString(),
        emoji = detail.emoji,
        title = detail.title,
        description = detail.description,
        color = detail.color.toColorHex(),
        isFinished = isFinished,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
