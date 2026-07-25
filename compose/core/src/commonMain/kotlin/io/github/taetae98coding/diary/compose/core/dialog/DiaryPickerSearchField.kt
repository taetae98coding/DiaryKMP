package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import io.github.taetae98coding.diary.compose.core.icon.SearchIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryPickerSearchField(
    placeholder: String,
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    ClearTextField(
        modifier = modifier,
        state = state,
        placeholder = { Text(text = placeholder) },
        leadingIcon = { SearchIcon() },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

@ComponentPreview
@Composable
private fun DiaryPickerSearchFieldPreview() {
    DiaryTheme {
        Surface {
            DiaryPickerSearchField(
                placeholder = "태그 검색",
                state = rememberTextFieldState(initialText = "업무"),
            )
        }
    }
}
