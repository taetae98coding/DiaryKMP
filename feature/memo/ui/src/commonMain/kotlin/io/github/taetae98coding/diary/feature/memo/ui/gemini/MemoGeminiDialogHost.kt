package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.previewMemoDraft

@Composable
internal fun MemoGeminiDialogHost(
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    onDismissRequest: () -> Unit,
    uiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    if (uiStateProvider().step == MemoGeminiStep.CLOSED) return

    MemoGeminiDialog(
        onEvent = onEvent,
        onDismissRequest = onDismissRequest,
        uiStateProvider = uiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun MemoGeminiDialogHostPreview() {
    DiaryTheme {
        MemoGeminiDialogHost(
            onEvent = {},
            onDismissRequest = {},
            uiStateProvider = {
                MemoGeminiUiState(
                    step = MemoGeminiStep.RESULT,
                    draft = previewMemoDraft(),
                )
            },
        )
    }
}
