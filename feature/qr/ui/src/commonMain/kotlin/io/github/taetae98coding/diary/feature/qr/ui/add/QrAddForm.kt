package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeImage

@Composable
internal fun QrAddForm(
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
) {
    DiaryInputColumn(modifier = modifier) {
        QrCodeImage(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(QrAddScaffoldDefaults.QrCodeImageSize),
            valueProvider = { state.valueState.text.toString() },
        )
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
            nextFocusProvider = { state.descriptionState.focusTarget },
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        QrValueInput(
            state = state.valueState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ScreenPreview
@Composable
private fun QrAddFormPreview() {
    DiaryTheme {
        Surface {
            QrAddForm()
        }
    }
}
