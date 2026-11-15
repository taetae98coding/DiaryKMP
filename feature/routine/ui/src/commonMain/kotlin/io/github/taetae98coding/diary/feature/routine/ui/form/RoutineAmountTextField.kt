package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun RoutineAmountTextField(
    label: String,
    modifier: Modifier = Modifier,
    state: RoutineAmountInputState = rememberRoutineAmountInputState(),
    unitProvider: () -> String = { "" },
    supportingText: String? = null,
    isClearButtonEnabled: Boolean = true,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    val unit = unitProvider()

    OutlinedTextField(
        state = state.textFieldState,
        modifier =
            modifier
                .bringIntoViewRequester(state.bringIntoViewRequester)
                .focusRequester(state.focusRequester),
        label = { Text(text = label) },
        trailingIcon =
            if (isClearButtonEnabled) {
                {
                    DiaryScaleFadeVisibility(visible = state.hasText) {
                        ClearButton(
                            onClick = {
                                state.clear()
                                state.focusRequester.requestFocus()
                            },
                        )
                    }
                }
            } else {
                null
            },
        suffix =
            if (unit.isEmpty()) {
                null
            } else {
                {
                    Text(
                        text = unit,
                        maxLines = 1,
                    )
                }
            },
        supportingText = supportingText?.let { text -> { Text(text = text) } },
        inputTransformation = RoutineAmountInputTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        onKeyboardAction = onKeyboardAction,
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

@ComponentPreview
@Composable
private fun RoutineAmountTextFieldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasAmount: Boolean,
) {
    DiaryTheme {
        Surface {
            RoutineAmountTextField(
                label = "목표량",
                state = rememberRoutineAmountInputState(initialAmount = if (hasAmount) 2000 else null),
                unitProvider = { "ml" },
                supportingText = "비워 두면 목표 없이 합계만 보여 줍니다",
            )
        }
    }
}
