package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.qr.QrDetail

@Stable
internal class QrAddFormState(
    tabState: MutableState<QrAddTab>,
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val contentState: QrContentFormState,
    val scrollState: ScrollState,
    val hostState: SnackbarHostState,
) {
    var tab: QrAddTab by tabState

    val detail: QrDetail
        get() =
            QrDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                value = contentState.qrValue,
            )

    fun applyScannedValue(value: String) {
        tab = QrAddTab.QR
        contentState.applyScannedValue(value)
    }

    fun undo() {
        tab = QrAddTab.QR
        contentState.undo()
    }

    fun clear() {
        titleState.clearText()
        descriptionState.clearText()
        contentState.clear()
    }
}

@Composable
internal fun rememberQrAddFormState(defaultProvider: MapProvider? = null): QrAddFormState {
    val tabState = rememberSaveable(stateSaver = QrAddTabSaver) { mutableStateOf(QrAddTab.INFO) }
    val titleState = rememberDiaryTitleInputState()
    val descriptionState = rememberDiaryDescriptionInputState()
    val contentState = rememberQrContentFormState(defaultProvider = defaultProvider)
    val scrollState = rememberScrollState()
    val hostState = remember { SnackbarHostState() }

    return remember(tabState, titleState, descriptionState, contentState, scrollState, hostState) {
        QrAddFormState(
            tabState = tabState,
            titleState = titleState,
            descriptionState = descriptionState,
            contentState = contentState,
            scrollState = scrollState,
            hostState = hostState,
        )
    }
}

private val QrAddTabSaver: Saver<QrAddTab, String> = Saver(save = { tab -> tab.name }, restore = QrAddTab::valueOf)
