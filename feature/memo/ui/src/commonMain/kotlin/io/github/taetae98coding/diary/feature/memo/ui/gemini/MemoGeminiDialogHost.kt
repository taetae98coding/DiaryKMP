package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.previewMemoDraft

@Composable
internal fun MemoGeminiDialogHost(
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    onDismissRequest: () -> Unit,
    uiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    // 도우미가 닫혀 있는 동안에도 프롬프트를 들고 있어야, 시스템이 앱을 되살려 닫힌 채 돌아온 경우에 저장된 프롬프트를 비울 수 있다.
    val promptState = rememberTextFieldState()

    if (uiStateProvider().step == MemoGeminiStep.CLOSED) {
        LaunchedEffect(Unit) { promptState.clearText() }
        return
    }

    MemoGeminiDialog(
        onEvent = onEvent,
        onDismissRequest = onDismissRequest,
        promptState = promptState,
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
