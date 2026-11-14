package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_unit_label
import io.github.taetae98coding.diary.feature.routine.ui.routine_unit_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineUnitTextField(
    modifier: Modifier = Modifier,
    state: RoutineUnitInputState = rememberRoutineUnitInputState(),
) {
    OutlinedTextField(
        state = state.textFieldState,
        modifier = modifier.focusRequester(state.focusRequester),
        label = { Text(text = stringResource(Res.string.routine_unit_label)) },
        placeholder = { Text(text = stringResource(Res.string.routine_unit_placeholder)) },
        trailingIcon = {
            DiaryScaleFadeVisibility(visible = state.hasText) {
                ClearButton(
                    onClick = {
                        state.clear()
                        state.focusRequester.requestFocus()
                    },
                )
            }
        },
        inputTransformation = RoutineUnitInputTransformation,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

@ComponentPreview
@Composable
private fun RoutineUnitTextFieldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasUnit: Boolean,
) {
    DiaryTheme {
        Surface {
            RoutineUnitTextField(state = rememberRoutineUnitInputState(initialUnit = if (hasUnit) "ml" else ""))
        }
    }
}
