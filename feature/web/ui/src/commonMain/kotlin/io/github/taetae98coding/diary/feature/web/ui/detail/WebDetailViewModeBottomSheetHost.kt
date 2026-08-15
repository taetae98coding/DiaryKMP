@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun WebDetailViewModeBottomSheetHost(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
) {
    if (!state.viewModeSheetState.isVisible) return

    ModalBottomSheet(
        onDismissRequest = { state.viewModeSheetState.hide() },
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    ) {
        WebDetailViewModeBottomSheetContent(
            onEvent = { event ->
                onEvent(event)
                state.viewModeSheetState.hide()
            },
            state = state,
        )
    }
}

@ScreenPreview
@Composable
private fun WebDetailViewModeBottomSheetHostPreview() {
    DiaryTheme {
        WebDetailViewModeBottomSheetHost(
            onEvent = {},
            state = rememberWebDetailScaffoldState().apply { viewModeSheetState.show() },
        )
    }
}
