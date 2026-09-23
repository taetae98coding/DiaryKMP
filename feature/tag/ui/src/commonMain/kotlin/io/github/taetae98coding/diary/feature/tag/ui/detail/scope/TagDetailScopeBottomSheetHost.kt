@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagScope

@Composable
internal fun TagDetailScopeBottomSheetHost(
    onSelect: (TagScope) -> Unit,
    state: TagDetailScopeState = rememberTagDetailScopeState(),
) {
    if (!state.sheetState.isVisible) return

    ModalBottomSheet(
        onDismissRequest = { state.sheetState.hide() },
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    ) {
        TagDetailScopeBottomSheetContent(
            onSelect = { scope ->
                onSelect(scope)
                state.sheetState.hide()
            },
            scopeProvider = { state.scope },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailScopeBottomSheetHostPreview() {
    val sheetState = rememberDialogState(initialVisible = true)
    val state = remember(sheetState) { TagDetailScopeState(sheetState = sheetState) }

    DiaryTheme {
        TagDetailScopeBottomSheetHost(
            onSelect = {},
            state = state,
        )
    }
}
