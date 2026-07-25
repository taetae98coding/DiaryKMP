package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_emoji_input_dialog_cancel
import io.github.taetae98coding.diary.compose.core.diary_emoji_input_dialog_confirm
import io.github.taetae98coding.diary.compose.core.diary_emoji_input_label
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.transparentIndicator
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiaryEmojiInputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = "",
) {
    val textFieldState = rememberTextFieldState(initialText = initialText)
    val focusRequester = remember { FocusRequester() }

    RequestFocusEffect(focusRequester = focusRequester)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(textFieldState.text.toString()) }) {
                Text(text = stringResource(Res.string.diary_emoji_input_dialog_confirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.diary_emoji_input_dialog_cancel))
            }
        },
        title = { Text(text = stringResource(Res.string.diary_emoji_input_label)) },
        text = {
            TextField(
                state = textFieldState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                inputTransformation = SingleGraphemeInputTransformation,
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors().transparentIndicator(),
            )
        },
    )
}

@ComponentPreview
@Composable
private fun DiaryEmojiInputDialogPreview() {
    DiaryTheme {
        DiaryEmojiInputDialog(
            onDismissRequest = {},
            onConfirm = {},
            initialText = "🏃",
        )
    }
}
