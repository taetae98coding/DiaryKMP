package io.github.taetae98coding.diary.work.mcp.tool.paging

import androidx.paging.PagingData
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.work.appaction.core.APP_ACTION_PAGE_SIZE
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.readSnapshot
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val CURSOR_PARAMETER = "cursor"

internal val mcpPageCursorParameter: McpToolParameter =
    McpToolParameter(
        name = CURSOR_PARAMETER,
        type = McpToolParameterType.STRING,
        description = "The nextCursor value from the previous page. Omit it to read the first page.",
    )

internal data class McpPage<T>(
    val itemList: List<T>,
    val nextOffset: Int?,
)

internal fun McpToolArguments.pageOffset(): Int {
    val cursor = stringOrNull(CURSOR_PARAMETER) ?: return 0

    return cursor.toIntOrNull()?.takeIf { offset -> offset >= 0 } ?: throw McpToolArgumentException(message = "'$CURSOR_PARAMETER' must be a nextCursor value from a previous page.")
}

internal fun <T> McpPage<T>.toJson(encode: (T) -> JsonElement): JsonObject =
    buildJsonObject {
        put("items", JsonArray(itemList.map(encode)))
        nextOffset?.let { offset -> put("nextCursor", offset.toString()) }
    }

internal fun <T> AppActionPage<T>.toJson(encode: (T) -> JsonElement): JsonObject =
    buildJsonObject {
        put("items", JsonArray(items.map(encode)))
        nextCursor?.let { cursor -> put("nextCursor", cursor) }
    }

internal suspend fun <Parameter, T : Any> FlowUseCase<Parameter, PagingData<T>>.readPage(
    parameter: Parameter,
    offset: Int,
): McpPage<T> =
    invoke(parameter = parameter)
        .first()
        .getOrThrow()
        .readPage(offset = offset, size = APP_ACTION_PAGE_SIZE)

internal suspend fun <T : Any> PagingData<T>.readPage(
    offset: Int,
    size: Int,
): McpPage<T> {
    val snapshot = readSnapshot(endExclusive = offset + size + 1)
    val endExclusive = minOf(offset + size, snapshot.size)
    val itemList = (offset until endExclusive).mapNotNull { index -> snapshot[index] }

    return McpPage(
        itemList = itemList,
        nextOffset = (offset + size).takeIf { nextOffset -> snapshot.size > nextOffset },
    )
}
