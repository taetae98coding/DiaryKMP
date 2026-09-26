package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.qr.QrDetail

@Stable
internal class QrAddFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val valueState: QrValueInputState,
    val hostState: SnackbarHostState,
) {
    val detail: QrDetail
        get() =
            QrDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                value = valueState.text.toString(),
            )

    fun clearText() {
        titleState.clearText()
        descriptionState.clearText()
        valueState.clearText()
    }
}

@Composable
internal fun rememberQrAddFormState(): QrAddFormState {
    val titleState = rememberDiaryTitleInputState()
    val descriptionState = rememberDiaryDescriptionInputState()
    val valueState = rememberQrValueInputState()
    val hostState = remember { SnackbarHostState() }

    return remember(titleState, descriptionState, valueState, hostState) {
        QrAddFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            valueState = valueState,
            hostState = hostState,
        )
    }
}
