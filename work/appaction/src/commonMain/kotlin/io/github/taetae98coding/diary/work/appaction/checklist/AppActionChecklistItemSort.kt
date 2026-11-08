package io.github.taetae98coding.diary.work.appaction.checklist

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument

private const val ITEM_SORT = "itemSort"

public object AppActionChecklistItemSortValue {
    public const val ADDED: String = "added"
    public const val UNCHECKED_FIRST: String = "unchecked_first"
}

internal val checklistItemSortList: List<ChecklistItemSort> = listOf(ChecklistItemSort.ADDED, ChecklistItemSort.UNCHECKED_FIRST)

private fun ChecklistItemSort.toItemSortValue(): String =
    when (this) {
        ChecklistItemSort.ADDED -> AppActionChecklistItemSortValue.ADDED
        ChecklistItemSort.UNCHECKED_FIRST -> AppActionChecklistItemSortValue.UNCHECKED_FIRST
    }

internal fun String?.toChecklistItemSortArgument(): ChecklistItemSort {
    val value = this ?: return checklistItemSortList.first()

    return checklistItemSortList.firstOrNull { sort -> sort.toItemSortValue() == value }
        ?: invalidArgument(message = "'$ITEM_SORT' must be one of ${checklistItemSortList.joinToString { sort -> sort.toItemSortValue() }}.")
}
