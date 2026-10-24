package io.github.taetae98coding.diary.feature.setting.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadProxyStatusUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetMusicDownloadProxySettingUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetMusicDownloadProxySettingUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingPlaylistViewModel(
    getMusicDownloadProxyStatusUseCase: GetMusicDownloadProxyStatusUseCase,
    getMusicDownloadProxySettingUseCase: GetMusicDownloadProxySettingUseCase,
    private val setMusicDownloadProxySettingUseCase: SetMusicDownloadProxySettingUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    private val _effect = Channel<SettingPlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<SettingPlaylistEffect> = _effect.receiveAsFlow()

    val uiState: StateFlow<SettingPlaylistUiState> =
        combine(
            getMusicDownloadProxyStatusUseCase(parameter = Unit),
            getMusicDownloadProxySettingUseCase(parameter = Unit),
            isInProgress,
        ) { statusResult, settingResult, isInProgress ->
            when (val status = statusResult.getOrNull()) {
                null -> SettingPlaylistUiState.Loading

                is MusicDownloadProxyStatus.Serving -> SettingPlaylistUiState.Serving(addressList = status.addressList)

                is MusicDownloadProxyStatus.Unavailable -> SettingPlaylistUiState.Unavailable

                is MusicDownloadProxyStatus.NotProvided ->
                    settingResult.fold(
                        onSuccess = { setting -> SettingPlaylistUiState.Consumer(setting = setting, isInProgress = isInProgress) },
                        onFailure = { SettingPlaylistUiState.Loading },
                    )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingPlaylistUiState.Loading,
        )

    fun save(setting: MusicDownloadProxySetting) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
            try {
                setMusicDownloadProxySettingUseCase(parameter = setting)
                    .onSuccess { _effect.send(SettingPlaylistEffect.SaveSucceeded) }
                    .onFailure { _effect.send(SettingPlaylistEffect.SaveFailed) }
            } finally {
                isInProgress.value = false
            }
        }
    }
}
