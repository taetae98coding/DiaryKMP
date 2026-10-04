package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddScaffoldDefaults
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddTab
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddTabRow
import io.github.taetae98coding.diary.feature.qr.ui.add.qrAddTabList
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeImage

@Composable
internal fun QrAddForm(
    modifier: Modifier = Modifier,
    state: QrAddFormState = rememberQrAddFormState(),
    isMapDisplayedProvider: () -> Boolean = { false },
) {
    DiaryInputColumn(
        modifier = modifier,
        scrollState = state.scrollState,
    ) {
        QrCodeImage(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(QrAddScaffoldDefaults.QrCodeImageSize),
            valueProvider = { state.contentState.qrValue },
        )
        QrAddTabRow(
            state = state,
            modifier = Modifier.fillMaxWidth(),
        )
        when (state.tab) {
            QrAddTab.INFO -> {
                DiaryTitleInput(
                    state = state.titleState,
                    modifier = Modifier.fillMaxWidth(),
                    nextFocusProvider = { state.descriptionState.focusTarget },
                )
                DiaryDescriptionInput(
                    state = state.descriptionState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            QrAddTab.QR -> {
                QrContentForm(
                    state = state.contentState,
                    modifier = Modifier.fillMaxWidth(),
                    isMapDisplayedProvider = isMapDisplayedProvider,
                )
            }
        }
    }
}

private class QrAddTabPreviewParameter : PreviewParameterProvider<QrAddTab> {
    override val values: Sequence<QrAddTab> = qrAddTabList.asSequence()
}

@ScreenPreview
@Composable
private fun QrAddFormPreview(
    @PreviewParameter(QrAddTabPreviewParameter::class) tab: QrAddTab,
) {
    val state = rememberQrAddFormState()
    remember(tab) { state.tab = tab }

    DiaryTheme {
        Surface {
            QrAddForm(state = state)
        }
    }
}
