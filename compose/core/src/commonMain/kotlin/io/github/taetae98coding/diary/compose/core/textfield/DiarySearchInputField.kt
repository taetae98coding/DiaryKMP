@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.icon.SearchIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiarySearchInputField(
    placeholder: String,
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val hasText by remember(state) { derivedStateOf { state.text.isNotEmpty() } }

    SearchBarDefaults.InputField(
        state = state,
        onSearch = { softwareKeyboardController?.hide() },
        expanded = true,
        onExpandedChange = {},
        modifier = modifier.focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(text = placeholder) },
        leadingIcon = { SearchIcon() },
        trailingIcon = {
            DiaryScaleFadeVisibility(visible = hasText) {
                ClearButton(
                    onClick = {
                        state.clearText()
                        focusRequester.requestFocus()
                    },
                )
            }
        },
        colors =
            SearchBarDefaults.inputFieldColors(
                focusedContainerColor = SearchBarDefaults.collapsedContainedSearchBarColor,
                unfocusedContainerColor = SearchBarDefaults.collapsedContainedSearchBarColor,
                disabledContainerColor = SearchBarDefaults.collapsedContainedSearchBarColor,
            ),
    )
}

@ComponentPreview
@Composable
private fun DiarySearchInputFieldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasQuery: Boolean,
) {
    DiaryTheme {
        Surface {
            DiarySearchInputField(
                placeholder = "검색어를 입력하세요",
                state = rememberTextFieldState(initialText = if (hasQuery) "검색어" else ""),
            )
        }
    }
}
