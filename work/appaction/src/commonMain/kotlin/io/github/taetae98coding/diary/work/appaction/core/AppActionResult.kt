package io.github.taetae98coding.diary.work.appaction.core

import kotlin.uuid.Uuid

public data class AppActionId(
    val id: String,
)

public data class AppActionChangedCount(
    val changedCount: Int,
)

internal fun Uuid.toAppActionId(): AppActionId = AppActionId(id = toString())

internal fun Int.toAppActionChangedCount(): AppActionChangedCount = AppActionChangedCount(changedCount = this)
