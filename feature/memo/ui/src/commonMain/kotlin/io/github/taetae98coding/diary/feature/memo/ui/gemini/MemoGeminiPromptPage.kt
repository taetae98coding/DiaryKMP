package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_failed_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_invalid_api_key_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_prompt_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_prompt_placeholder
import org.jetbrains.compose.resources.stringResource

private const val MIN_HEIGHT_IN_LINES = 3
private const val MAX_HEIGHT_IN_LINES = 5

@Composable
internal fun MemoGeminiPromptPage(
    modifier: Modifier = Modifier,
    promptState: TextFieldState = rememberTextFieldState(),
    failureProvider: () -> MemoGeminiFailure? = { null },
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        ClearTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            state = promptState,
            label = { Text(text = stringResource(Res.string.memo_gemini_prompt_label)) },
            placeholder = { Text(text = stringResource(Res.string.memo_gemini_prompt_placeholder)) },
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = MIN_HEIGHT_IN_LINES, maxHeightInLines = MAX_HEIGHT_IN_LINES),
        )

        val failure = failureProvider()

        if (failure != null) {
            Text(
                text = failure.toMessage(),
                color = DiaryTheme.colorScheme.error,
                style = DiaryTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MemoGeminiFailure.toMessage(): String =
    when (this) {
        MemoGeminiFailure.INVALID_API_KEY -> stringResource(Res.string.memo_gemini_invalid_api_key_message)
        MemoGeminiFailure.UNKNOWN -> stringResource(Res.string.memo_gemini_failed_message)
    }

private class MemoGeminiFailurePreviewParameter : PreviewParameterProvider<MemoGeminiFailure?> {
    override val values: Sequence<MemoGeminiFailure?> = sequenceOf(null, MemoGeminiFailure.INVALID_API_KEY, MemoGeminiFailure.UNKNOWN)
}

@ComponentPreview
@Composable
private fun MemoGeminiPromptPagePreview(
    @PreviewParameter(MemoGeminiFailurePreviewParameter::class) failure: MemoGeminiFailure?,
) {
    DiaryTheme {
        Surface {
            MemoGeminiPromptPage(failureProvider = { failure })
        }
    }
}
