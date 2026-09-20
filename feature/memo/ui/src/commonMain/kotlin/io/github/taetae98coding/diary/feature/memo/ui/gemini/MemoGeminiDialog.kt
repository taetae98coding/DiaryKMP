package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.submitShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_cancel_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_close_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_generate_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_title
import io.github.taetae98coding.diary.feature.memo.ui.previewMemoDraft
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiDialog(
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    promptState: TextFieldState = rememberTextFieldState(),
    uiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            MemoGeminiDialogAction(
                onEvent = onEvent,
                onDismissRequest = onDismissRequest,
                promptState = promptState,
                uiStateProvider = uiStateProvider,
            )
        },
        modifier =
            modifier.submitShortcut(isEnabledProvider = { uiStateProvider().step == MemoGeminiStep.PROMPT }) {
                onEvent(MemoGeminiDialogEvent.ClickGenerate(prompt = promptState.text.toString()))
            },
        title = { Text(text = stringResource(Res.string.memo_gemini_title)) },
        text = {
            DiaryCrossfade(
                targetState = uiStateProvider().step,
                modifier = Modifier.animateContentSize(),
            ) { step ->
                MemoGeminiDialogPage(
                    step = step,
                    onEvent = onEvent,
                    promptState = promptState,
                    uiStateProvider = uiStateProvider,
                )
            }
        },
    )
}

@Composable
private fun MemoGeminiDialogPage(
    step: MemoGeminiStep,
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    promptState: TextFieldState,
    uiStateProvider: () -> MemoGeminiUiState,
    modifier: Modifier = Modifier,
) {
    when (step) {
        MemoGeminiStep.CLOSED, MemoGeminiStep.PROMPT ->
            MemoGeminiPromptPage(
                modifier = modifier,
                promptState = promptState,
                failureProvider = { uiStateProvider().failure },
            )

        MemoGeminiStep.GENERATING -> MemoGeminiGeneratingPage(modifier = modifier)

        MemoGeminiStep.RESULT ->
            MemoGeminiResultPage(
                onEvent = onEvent,
                modifier = modifier,
                uiStateProvider = uiStateProvider,
            )
    }
}

@Composable
private fun MemoGeminiDialogAction(
    onEvent: (MemoGeminiDialogEvent) -> Unit,
    onDismissRequest: () -> Unit,
    promptState: TextFieldState,
    uiStateProvider: () -> MemoGeminiUiState,
    modifier: Modifier = Modifier,
) {
    when (uiStateProvider().step) {
        MemoGeminiStep.CLOSED, MemoGeminiStep.PROMPT ->
            TextButton(
                onClick = { onEvent(MemoGeminiDialogEvent.ClickGenerate(prompt = promptState.text.toString())) },
                modifier = modifier,
            ) {
                Text(text = stringResource(Res.string.memo_gemini_generate_action))
            }

        MemoGeminiStep.GENERATING ->
            TextButton(
                onClick = { onEvent(MemoGeminiDialogEvent.ClickCancel) },
                modifier = modifier,
            ) {
                Text(text = stringResource(Res.string.memo_gemini_cancel_action))
            }

        MemoGeminiStep.RESULT ->
            TextButton(
                onClick = onDismissRequest,
                modifier = modifier,
            ) {
                Text(text = stringResource(Res.string.memo_gemini_close_action))
            }
    }
}

@ScreenPreview
@Composable
private fun MemoGeminiDialogPreview() {
    DiaryTheme {
        MemoGeminiDialog(
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
