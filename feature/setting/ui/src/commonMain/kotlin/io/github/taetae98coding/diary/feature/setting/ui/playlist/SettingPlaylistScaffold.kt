package io.github.taetae98coding.diary.feature.setting.ui.playlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.playlist.form.SettingPlaylistAddressInput
import io.github.taetae98coding.diary.feature.setting.ui.playlist.form.SettingPlaylistFormState
import io.github.taetae98coding.diary.feature.setting.ui.playlist.form.rememberSettingPlaylistFormState
import io.github.taetae98coding.diary.feature.setting.ui.previewMusicDownloadProxyAddressList
import io.github.taetae98coding.diary.feature.setting.ui.previewMusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_playlist_save_button_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_playlist_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingPlaylistScaffold(
    onEvent: (SettingPlaylistScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingPlaylistFormState = rememberSettingPlaylistFormState(),
    uiStateProvider: () -> SettingPlaylistUiState = { SettingPlaylistUiState.Loading },
    componentVisibleProvider: () -> SettingPlaylistScaffoldComponentVisible = { SettingPlaylistScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val consumer = uiStateProvider() as? SettingPlaylistUiState.Consumer
            consumer != null && state.setting != consumer.setting
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_playlist_title),
                onNavigateUp = { onEvent(SettingPlaylistScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(SettingPlaylistScaffoldEvent.ClickSave) },
                    contentDescription = stringResource(Res.string.setting_playlist_save_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? SettingPlaylistUiState.Consumer)?.isInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        val contentModifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)

        when (val uiState = uiStateProvider()) {
            is SettingPlaylistUiState.Loading -> Unit

            is SettingPlaylistUiState.Serving ->
                DiaryInputColumn(modifier = contentModifier) {
                    SettingPlaylistProxyAddressSection(
                        modifier = Modifier.fillMaxWidth(),
                        addressList = uiState.addressList,
                    )
                }

            is SettingPlaylistUiState.Unavailable ->
                DiaryInputColumn(modifier = contentModifier) {
                    SettingPlaylistProxyAddressSection(
                        modifier = Modifier.fillMaxWidth(),
                        isUnavailable = true,
                    )
                }

            is SettingPlaylistUiState.Consumer ->
                DiaryInputColumn(modifier = contentModifier) {
                    SettingPlaylistAddressInput(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                    )
                }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingPlaylistScaffoldPreview(
    @PreviewParameter(SettingPlaylistUiStatePreviewParameter::class) uiState: SettingPlaylistUiState,
) {
    DiaryTheme {
        SettingPlaylistScaffold(
            onEvent = {},
            state = rememberSettingPlaylistFormState(initialSetting = previewMusicDownloadProxySetting()),
            uiStateProvider = { uiState },
        )
    }
}

private class SettingPlaylistUiStatePreviewParameter : PreviewParameterProvider<SettingPlaylistUiState> {
    override val values: Sequence<SettingPlaylistUiState> =
        sequenceOf(
            SettingPlaylistUiState.Loading,
            SettingPlaylistUiState.Serving(addressList = previewMusicDownloadProxyAddressList()),
            SettingPlaylistUiState.Serving(addressList = emptyList()),
            SettingPlaylistUiState.Unavailable,
            SettingPlaylistUiState.Consumer(setting = previewMusicDownloadProxySetting()),
            SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = true),
        )
}
