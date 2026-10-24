package io.github.taetae98coding.diary.feature.checklist.ui.item

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_item_add_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_item_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistItemInputRow(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistItemInputState = rememberChecklistItemInputState(),
    isInProgressProvider: () -> Boolean = { false },
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            state = state.textFieldState,
            modifier =
                Modifier
                    .weight(1F)
                    .focusRequester(state.focusRequester),
            label = { Text(text = stringResource(Res.string.checklist_item_input_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                if (state.isAddable) {
                    onAdd()
                }
            },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        ChecklistItemAddButton(
            onClick = onAdd,
            contentDescription = stringResource(Res.string.checklist_item_add_button_content_description),
            isEnabledProvider = { state.isAddable },
            isInProgressProvider = isInProgressProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun ChecklistItemInputRowPreview() {
    DiaryTheme {
        Surface {
            ChecklistItemInputRow(onAdd = {})
        }
    }
}
