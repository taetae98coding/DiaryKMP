package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort

@Composable
public fun DiaryChecklistItemSortBottomSheetHost(
    onSelect: (ChecklistItemSort) -> Unit,
    state: DialogState = rememberDialogState(),
    sortProvider: () -> ChecklistItemSort = { ChecklistItemSort.ADDED },
) {
    DiaryListSortBottomSheetHost(
        onSelect = onSelect,
        state = state,
        sortList = checklistItemSortList,
        sortProvider = sortProvider,
        icon = { sort, iconModifier -> ChecklistItemSortIcon(sort = sort, modifier = iconModifier) },
        label = ::checklistItemSortLabel,
    )
}

@ScreenPreview
@Composable
private fun DiaryChecklistItemSortBottomSheetHostPreview() {
    DiaryTheme {
        DiaryChecklistItemSortBottomSheetHost(
            onSelect = {},
            state = rememberDialogState(initialVisible = true),
        )
    }
}
