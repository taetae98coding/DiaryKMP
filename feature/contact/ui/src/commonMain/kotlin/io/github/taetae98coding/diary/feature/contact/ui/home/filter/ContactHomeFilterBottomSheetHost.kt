@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.contact.ui.home.filter

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeFilterUiState
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeScaffoldEvent

@Composable
internal fun ContactHomeFilterBottomSheetHost(
    onEvent: (ContactHomeScaffoldEvent) -> Unit,
    state: DialogState = rememberDialogState(),
    uiStateProvider: () -> ContactHomeFilterUiState = { ContactHomeFilterUiState() },
) {
    if (!state.isVisible) return

    ModalBottomSheet(
        onDismissRequest = { state.hide() },
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    ) {
        ContactHomeFilterBottomSheetContent(
            onEvent = onEvent,
            uiStateProvider = uiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun ContactHomeFilterBottomSheetHostPreview() {
    DiaryTheme {
        ContactHomeFilterBottomSheetHost(
            onEvent = {},
            state = rememberDialogState(initialVisible = true),
        )
    }
}
