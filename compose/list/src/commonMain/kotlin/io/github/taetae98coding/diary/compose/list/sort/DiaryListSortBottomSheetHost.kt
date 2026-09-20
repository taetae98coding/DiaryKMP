@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort

@Composable
public fun DiaryListSortBottomSheetHost(
    onSelect: (ListSort) -> Unit,
    state: DialogState = rememberDialogState(),
    sortList: List<ListSort> = listSortList,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    if (!state.isVisible) return

    ModalBottomSheet(
        onDismissRequest = { state.hide() },
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    ) {
        DiaryListSortBottomSheetContent(
            onSelect = { sort ->
                onSelect(sort)
                state.hide()
            },
            sortList = sortList,
            sortProvider = sortProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun DiaryListSortBottomSheetHostPreview() {
    DiaryTheme {
        DiaryListSortBottomSheetHost(
            onSelect = {},
            state = rememberDialogState(initialVisible = true),
        )
    }
}
