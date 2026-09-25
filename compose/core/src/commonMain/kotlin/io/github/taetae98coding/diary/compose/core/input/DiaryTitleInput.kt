package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_title_input_label
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
public fun DiaryTitleInput(
    modifier: Modifier = Modifier,
    state: DiaryTitleInputState = rememberDiaryTitleInputState(),
    nextFocusProvider: () -> FocusRequester = { FocusRequester.Default },
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state.textFieldState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester)
                    .focusProperties { next = nextFocusProvider() },
            label = { Text(text = stringResource(Res.string.diary_title_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryTitleInputPreview() {
    DiaryTheme {
        Surface {
            DiaryTitleInput()
        }
    }
}
