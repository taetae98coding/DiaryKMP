package io.github.taetae98coding.diary.feature.more.ui.home.signout

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScaffoldEvent

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
