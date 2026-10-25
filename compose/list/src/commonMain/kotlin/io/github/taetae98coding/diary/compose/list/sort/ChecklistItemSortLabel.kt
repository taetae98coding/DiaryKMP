package io.github.taetae98coding.diary.compose.list.sort

import io.github.taetae98coding.diary.compose.list.Res
import io.github.taetae98coding.diary.compose.list.checklist_item_sort_added_label
import io.github.taetae98coding.diary.compose.list.checklist_item_sort_unchecked_first_label
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import org.jetbrains.compose.resources.StringResource

internal fun checklistItemSortLabel(sort: ChecklistItemSort): StringResource =
    when (sort) {
        ChecklistItemSort.ADDED -> Res.string.checklist_item_sort_added_label
        ChecklistItemSort.UNCHECKED_FIRST -> Res.string.checklist_item_sort_unchecked_first_label
    }
