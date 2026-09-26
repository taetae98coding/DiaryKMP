package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_value_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrValueInput(
    modifier: Modifier = Modifier,
    state: QrValueInputState = rememberQrValueInputState(),
) {
    OutlinedTextField(
        state = state.textFieldState,
        modifier = modifier.focusRequester(state.focusRequester),
        label = { Text(text = stringResource(Res.string.qr_add_value_label)) },
        inputTransformation = QrValueInputTransformation,
    )
}

@ComponentPreview
@Composable
private fun QrValueInputPreview() {
    DiaryTheme {
        Surface {
            QrValueInput()
        }
    }
}
