@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.list.sort

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import org.jetbrains.compose.resources.StringResource

@Composable
public fun DiaryListSortBottomSheetHost(
    onSelect: (ListSort) -> Unit,
    state: DialogState = rememberDialogState(),
    sortList: List<ListSort> = listSortList,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    DiaryListSortBottomSheetHost(
        onSelect = onSelect,
        state = state,
        sortList = sortList,
        sortProvider = sortProvider,
        icon = { sort, iconModifier -> ListSortIcon(sort = sort, modifier = iconModifier) },
        label = ::listSortLabel,
    )
}

@Composable
internal fun <T> DiaryListSortBottomSheetHost(
    onSelect: (T) -> Unit,
    sortList: List<T>,
    sortProvider: () -> T,
    icon: @Composable (sort: T, modifier: Modifier) -> Unit,
    label: (T) -> StringResource,
    state: DialogState = rememberDialogState(),
) {
    if (!state.isVisible) return

    ModalBottomSheet(
        onDismissRequest = { state.hide() },
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    ) {
        DiaryListSortBottomSheetContent(
            onSelect = { sort: T ->
                onSelect(sort)
                state.hide()
            },
            sortList = sortList,
            sortProvider = sortProvider,
            icon = icon,
            label = label,
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
