package io.github.taetae98coding.diary.work.mcp.tool.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.json.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val CREATED_AT = "createdAt"
private const val UPDATED_AT = "updatedAt"

private const val ITEM_SORT = "itemSort"

internal val checklistItemSortList: List<ChecklistItemSort> = listOf(ChecklistItemSort.ADDED, ChecklistItemSort.UNCHECKED_FIRST)

private fun ChecklistItemSort.toArgument(): String =
    when (this) {
        ChecklistItemSort.ADDED -> "added"
        ChecklistItemSort.UNCHECKED_FIRST -> "unchecked_first"
    }

internal val checklistItemSortParameter: McpToolParameter =
    McpToolParameter(
        name = ITEM_SORT,
        type = McpToolParameterType.STRING,
        description =
            "Order of the checklist items. added keeps the order the items were added; unchecked_first puts unchecked items first, each group in added order. " +
                "Omit it to use the initial order (${checklistItemSortList.first().toArgument()}).",
        allowedValueList = checklistItemSortList.map { sort -> sort.toArgument() },
    )

internal fun McpToolArguments.checklistItemSort(): ChecklistItemSort {
    val argument = stringOrNull(ITEM_SORT) ?: return checklistItemSortList.first()

    return checklistItemSortList.firstOrNull { sort -> sort.toArgument() == argument }
        ?: throw McpToolArgumentException(message = "'$ITEM_SORT' must be one of ${checklistItemSortList.joinToString { sort -> sort.toArgument() }}.")
}

internal fun Checklist.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("title", detail.title)
        put("description", detail.description)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }

internal fun ChecklistSummary.toJson(): JsonObject =
    buildJsonObject {
        put("id", checklist.id)
        put("title", checklist.detail.title)
        put("description", checklist.detail.description)
        put("checkedCount", checkedCount)
        put("totalCount", totalCount)
        put(CREATED_AT, checklist.createdAt)
        put(UPDATED_AT, checklist.updatedAt)
    }

internal fun ChecklistItem.toJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("content", content)
        put("isChecked", isChecked)
        put(CREATED_AT, createdAt)
        put(UPDATED_AT, updatedAt)
    }
