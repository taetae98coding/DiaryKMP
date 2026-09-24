package io.github.taetae98coding.diary.feature.setting.ui.download

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
import io.github.taetae98coding.diary.feature.setting.ui.download.form.SettingDownloadAddressInput
import io.github.taetae98coding.diary.feature.setting.ui.download.form.SettingDownloadFormState
import io.github.taetae98coding.diary.feature.setting.ui.download.form.rememberSettingDownloadFormState
import io.github.taetae98coding.diary.feature.setting.ui.previewMusicDownloadProxyAddressList
import io.github.taetae98coding.diary.feature.setting.ui.previewMusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_save_button_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingDownloadScaffold(
    onEvent: (SettingDownloadScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingDownloadFormState = rememberSettingDownloadFormState(),
    uiStateProvider: () -> SettingDownloadUiState = { SettingDownloadUiState.Loading },
    componentVisibleProvider: () -> SettingDownloadScaffoldComponentVisible = { SettingDownloadScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val consumer = uiStateProvider() as? SettingDownloadUiState.Consumer
            consumer != null && state.setting != consumer.setting
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_download_title),
                onNavigateUp = { onEvent(SettingDownloadScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(SettingDownloadScaffoldEvent.ClickSave) },
                    contentDescription = stringResource(Res.string.setting_download_save_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? SettingDownloadUiState.Consumer)?.isInProgress == true },
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
            is SettingDownloadUiState.Loading -> Unit

            is SettingDownloadUiState.Serving ->
                DiaryInputColumn(modifier = contentModifier) {
                    SettingDownloadProxyAddressSection(
                        modifier = Modifier.fillMaxWidth(),
                        addressList = uiState.addressList,
                    )
                }

            is SettingDownloadUiState.Unavailable ->
                DiaryInputColumn(modifier = contentModifier) {
                    SettingDownloadProxyAddressSection(
                        modifier = Modifier.fillMaxWidth(),
                        isUnavailable = true,
                    )
                }

            is SettingDownloadUiState.Consumer ->
                DiaryInputColumn(modifier = contentModifier) {
                    SettingDownloadAddressInput(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                    )
                }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingDownloadScaffoldPreview(
    @PreviewParameter(SettingDownloadUiStatePreviewParameter::class) uiState: SettingDownloadUiState,
) {
    DiaryTheme {
        SettingDownloadScaffold(
            onEvent = {},
            state = rememberSettingDownloadFormState(initialSetting = previewMusicDownloadProxySetting()),
            uiStateProvider = { uiState },
        )
    }
}

private class SettingDownloadUiStatePreviewParameter : PreviewParameterProvider<SettingDownloadUiState> {
    override val values: Sequence<SettingDownloadUiState> =
        sequenceOf(
            SettingDownloadUiState.Loading,
            SettingDownloadUiState.Serving(addressList = previewMusicDownloadProxyAddressList()),
            SettingDownloadUiState.Serving(addressList = emptyList()),
            SettingDownloadUiState.Unavailable,
            SettingDownloadUiState.Consumer(setting = previewMusicDownloadProxySetting()),
            SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = true),
        )
}
