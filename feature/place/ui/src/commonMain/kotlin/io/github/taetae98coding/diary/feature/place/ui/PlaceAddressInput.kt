package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceAddressInput(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    Card(modifier = modifier) {
        ClearTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(Res.string.place_address_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@ComponentPreview
@Composable
private fun PlaceAddressInputPreview() {
    DiaryTheme {
        Surface {
            PlaceAddressInput(state = rememberTextFieldState(initialText = "서울특별시 중구 세종대로 110"))
        }
    }
}
