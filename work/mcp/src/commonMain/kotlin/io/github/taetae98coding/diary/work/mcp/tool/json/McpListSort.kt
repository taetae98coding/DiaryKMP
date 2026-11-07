package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType

private const val SORT = "sort"

private fun ListSort.toArgument(): String =
    when (this) {
        ListSort.DEFAULT -> "default"
        ListSort.TITLE -> "title"
        ListSort.NAME -> "name"
        ListSort.RECENTLY_UPDATED -> "recently_updated"
    }

// 목록마다 고를 수 있는 정렬 기준이 다르므로, 그 목록이 제공하는 기준만 받는다.
internal fun sortParameter(sortList: List<ListSort>): McpToolParameter =
    McpToolParameter(
        name = SORT,
        type = McpToolParameterType.STRING,
        description = "Sort order. Omit it to use the list's initial order (${sortList.first().toArgument()}).",
        allowedValueList = sortList.map { sort -> sort.toArgument() },
    )

internal fun McpToolArguments.sort(sortList: List<ListSort>): ListSort {
    val argument = stringOrNull(SORT) ?: return sortList.first()

    return sortList.firstOrNull { sort -> sort.toArgument() == argument }
        ?: throw McpToolArgumentException(message = "'$SORT' must be one of ${sortList.joinToString { sort -> sort.toArgument() }}.")
}
