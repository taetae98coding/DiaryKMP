package io.github.taetae98coding.diary.feature.setting.ui.download.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting

@Stable
internal class SettingDownloadFormState(
    val addressState: TextFieldState,
    val hostState: SnackbarHostState,
) {
    val setting: MusicDownloadProxySetting
        get() = MusicDownloadProxySetting(address = addressState.text.toString())
}

@Composable
internal fun rememberSettingDownloadFormState(initialSetting: MusicDownloadProxySetting = MusicDownloadProxySetting.EMPTY): SettingDownloadFormState {
    val addressState = rememberTextFieldState(initialText = initialSetting.address)
    val hostState = remember { SnackbarHostState() }

    return remember(addressState, hostState) {
        SettingDownloadFormState(
            addressState = addressState,
            hostState = hostState,
        )
    }
}
