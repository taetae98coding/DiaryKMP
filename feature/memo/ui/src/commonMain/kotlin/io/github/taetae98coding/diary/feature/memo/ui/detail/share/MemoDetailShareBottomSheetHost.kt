@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.memo.ui.detail.share

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.detail.MemoDetailScaffoldEvent

@Composable
internal fun MemoDetailShareBottomSheetHost(
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    sheetState: DialogState = rememberDialogState(),
) {
    if (!sheetState.isVisible) return

    ModalBottomSheet(
        onDismissRequest = { onEvent(MemoDetailScaffoldEvent.DismissShareSheet) },
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    ) {
        MemoDetailShareBottomSheetContent(onEvent = onEvent)
    }
}

@ScreenPreview
@Composable
private fun MemoDetailShareBottomSheetHostPreview() {
    DiaryTheme {
        MemoDetailShareBottomSheetHost(
            onEvent = {},
            sheetState = rememberDialogState(initialVisible = true),
        )
    }
}
