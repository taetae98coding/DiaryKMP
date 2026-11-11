package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.paging.McpPage
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlin.uuid.Uuid

internal suspend fun <Parameter, Output> FlowUseCase<Parameter, Output>.readFirst(parameter: Parameter): Output = invoke(parameter = parameter).first().getOrThrow()

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
    change: suspend (Uuid) -> Result<Int>,
): JsonObject {
    if (readActive(id = id, isDeleted = isDeleted) == null) return changedCountResult(count = 0)

    return changedCountResult(count = change(id).getOrThrow())
}

internal suspend fun <T : Any> FlowUseCase<Uuid, T?>.pageActive(
    id: Uuid,
    isDeleted: (T) -> Boolean,
    page: suspend () -> JsonObject,
): JsonObject {
    if (readActive(id = id, isDeleted = isDeleted) == null) return McpPage<Nothing>(itemList = emptyList(), nextOffset = null).toJson { JsonNull }

    return page()
}
