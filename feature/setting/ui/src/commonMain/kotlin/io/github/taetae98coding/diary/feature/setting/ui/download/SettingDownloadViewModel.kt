package io.github.taetae98coding.diary.feature.setting.ui.download

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
internal class SettingDownloadViewModel(
    getMusicDownloadProxyStatusUseCase: GetMusicDownloadProxyStatusUseCase,
    getMusicDownloadProxySettingUseCase: GetMusicDownloadProxySettingUseCase,
    private val setMusicDownloadProxySettingUseCase: SetMusicDownloadProxySettingUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    private val _effect = Channel<SettingDownloadEffect>(Channel.BUFFERED)
    val effect: Flow<SettingDownloadEffect> = _effect.receiveAsFlow()

    val uiState: StateFlow<SettingDownloadUiState> =
        combine(
            getMusicDownloadProxyStatusUseCase(parameter = Unit),
            getMusicDownloadProxySettingUseCase(parameter = Unit),
            isInProgress,
        ) { statusResult, settingResult, isInProgress ->
            when (val status = statusResult.getOrNull()) {
                null -> SettingDownloadUiState.Loading

                is MusicDownloadProxyStatus.Serving -> SettingDownloadUiState.Serving(addressList = status.addressList)

                is MusicDownloadProxyStatus.Unavailable -> SettingDownloadUiState.Unavailable

                is MusicDownloadProxyStatus.NotProvided ->
                    settingResult.fold(
                        onSuccess = { setting -> SettingDownloadUiState.Consumer(setting = setting, isInProgress = isInProgress) },
                        onFailure = { SettingDownloadUiState.Loading },
                    )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingDownloadUiState.Loading,
        )

    fun save(setting: MusicDownloadProxySetting) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
            try {
                setMusicDownloadProxySettingUseCase(parameter = setting)
                    .onSuccess { _effect.send(SettingDownloadEffect.SaveSucceeded) }
                    .onFailure { _effect.send(SettingDownloadEffect.SaveFailed) }
            } finally {
                isInProgress.value = false
            }
        }
    }
}
