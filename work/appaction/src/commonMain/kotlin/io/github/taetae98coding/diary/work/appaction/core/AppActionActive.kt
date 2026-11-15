package io.github.taetae98coding.diary.work.appaction.core

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlin.uuid.Uuid

internal suspend fun <T : Any> FlowUseCase<Uuid, T?>.readActive(
    id: Uuid,
    isDeleted: (T) -> Boolean,
): T? = readFirst(parameter = id)?.takeUnless(isDeleted)

internal suspend fun <T : Any> FlowUseCase<Uuid, T?>.isActive(
    id: Uuid,
    isDeleted: (T) -> Boolean,
): Boolean = readActive(id = id, isDeleted = isDeleted) != null

internal suspend fun <T : Any> FlowUseCase<Uuid, T?>.changeActive(
    id: Uuid,
    isDeleted: (T) -> Boolean,
    change: suspend () -> Result<Int>,
): AppActionChangedCount {
    if (readActive(id = id, isDeleted = isDeleted) == null) return 0.toAppActionChangedCount()

    return change().getOrThrow().toAppActionChangedCount()
}

internal suspend fun <T : Any, R> FlowUseCase<Uuid, T?>.pageActive(
    id: Uuid,
    isDeleted: (T) -> Boolean,
    page: suspend () -> AppActionPage<R>,
): AppActionPage<R> {
    if (readActive(id = id, isDeleted = isDeleted) == null) return AppActionPage(items = emptyList(), nextCursor = null)

    return page()
}
