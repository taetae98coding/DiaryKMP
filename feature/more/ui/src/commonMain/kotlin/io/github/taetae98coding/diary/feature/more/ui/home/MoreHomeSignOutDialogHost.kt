package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun MoreHomeSignOutDialogHost(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    uiStateProvider: () -> MoreHomeSignOutUiState = { MoreHomeSignOutUiState() },
) {
    if (!uiStateProvider().isConfirmVisible) return

    MoreHomeSignOutDialog(onEvent = onEvent)
}

@ComponentPreview
@Composable
private fun MoreHomeSignOutDialogHostPreview() {
    DiaryTheme {
        MoreHomeSignOutDialogHost(
            onEvent = {},
            uiStateProvider = { MoreHomeSignOutUiState(isConfirmVisible = true) },
        )
    }
}
