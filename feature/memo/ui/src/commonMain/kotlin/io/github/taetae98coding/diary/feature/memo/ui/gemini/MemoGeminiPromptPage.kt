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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_blocked_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_failed_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_invalid_api_key_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_model_not_found_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_network_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_prompt_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_prompt_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_quota_exceeded_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_server_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_timeout_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_truncated_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiPromptPage(
    modifier: Modifier = Modifier,
    promptState: TextFieldState = rememberTextFieldState(),
    failureProvider: () -> MemoGeminiFailure? = { null },
    failureDescriptionProvider: () -> String = { "" },
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
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = MemoGeminiPromptPageDefaults.MIN_HEIGHT_IN_LINES, maxHeightInLines = MemoGeminiPromptPageDefaults.MAX_HEIGHT_IN_LINES),
        )

        val failure = failureProvider()

        if (failure != null) {
            MemoGeminiFailureText(
                failure = failure,
                descriptionProvider = failureDescriptionProvider,
            )
        }
    }
}

@Composable
private fun MemoGeminiFailureText(
    failure: MemoGeminiFailure,
    descriptionProvider: () -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = failure.toMessage(),
            color = DiaryTheme.colorScheme.error,
            style = DiaryTheme.typography.bodySmall,
        )

        val description = descriptionProvider()

        if (failure == MemoGeminiFailure.UNKNOWN && description.isNotBlank()) {
            Text(
                text = description,
                color = DiaryTheme.colorScheme.error,
                maxLines = MemoGeminiPromptPageDefaults.FAILURE_DESCRIPTION_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                style = DiaryTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MemoGeminiFailure.toMessage(): String =
    when (this) {
        MemoGeminiFailure.INVALID_API_KEY -> stringResource(Res.string.memo_gemini_invalid_api_key_message)
        MemoGeminiFailure.QUOTA_EXCEEDED -> stringResource(Res.string.memo_gemini_quota_exceeded_message)
        MemoGeminiFailure.MODEL_NOT_FOUND -> stringResource(Res.string.memo_gemini_model_not_found_message)
        MemoGeminiFailure.BLOCKED -> stringResource(Res.string.memo_gemini_blocked_message)
        MemoGeminiFailure.TRUNCATED -> stringResource(Res.string.memo_gemini_truncated_message)
        MemoGeminiFailure.TIMEOUT -> stringResource(Res.string.memo_gemini_timeout_message)
        MemoGeminiFailure.NETWORK -> stringResource(Res.string.memo_gemini_network_message)
        MemoGeminiFailure.SERVER -> stringResource(Res.string.memo_gemini_server_message)
        MemoGeminiFailure.UNKNOWN -> stringResource(Res.string.memo_gemini_failed_message)
    }

private class MemoGeminiFailurePreviewParameter : PreviewParameterProvider<MemoGeminiFailure?> {
    override val values: Sequence<MemoGeminiFailure?> = sequenceOf(null) + MemoGeminiFailure.entries.asSequence()
}

@ComponentPreview
@Composable
private fun MemoGeminiPromptPagePreview(
    @PreviewParameter(MemoGeminiFailurePreviewParameter::class) failure: MemoGeminiFailure?,
) {
    DiaryTheme {
        Surface {
            MemoGeminiPromptPage(
                failureProvider = { failure },
                failureDescriptionProvider = { "Request contains an invalid argument." },
            )
        }
    }
}
