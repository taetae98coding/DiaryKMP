package io.github.taetae98coding.diary.feature.setting.ui.playlist.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting

@Stable
internal class SettingPlaylistFormState(
    val addressState: TextFieldState,
    val snackbarHostState: SnackbarHostState,
) {
    val setting: MusicDownloadProxySetting
        get() = MusicDownloadProxySetting(address = addressState.text.toString())
}

@Composable
internal fun rememberSettingPlaylistFormState(initialSetting: MusicDownloadProxySetting = MusicDownloadProxySetting.EMPTY): SettingPlaylistFormState {
    val addressState = rememberTextFieldState(initialText = initialSetting.address)
    val snackbarHostState = remember { SnackbarHostState() }

    return remember(addressState, snackbarHostState) {
        SettingPlaylistFormState(
            addressState = addressState,
            snackbarHostState = snackbarHostState,
        )
    }
}
