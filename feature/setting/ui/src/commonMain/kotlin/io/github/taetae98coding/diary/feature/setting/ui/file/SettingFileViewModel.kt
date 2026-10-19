package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.file.usecase.GetFileDownloadSettingUseCase
import io.github.taetae98coding.diary.domain.file.usecase.SetFileDownloadDirectoryUseCase
import io.github.taetae98coding.diary.domain.file.usecase.UnsetFileDownloadDirectoryUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingFileViewModel(
    private val setFileDownloadDirectoryUseCase: SetFileDownloadDirectoryUseCase,
    private val unsetFileDownloadDirectoryUseCase: UnsetFileDownloadDirectoryUseCase,
    getFileDownloadSettingUseCase: GetFileDownloadSettingUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingFileUiState> =
        getFileDownloadSettingUseCase(parameter = Unit)
            .map { result ->
                result.fold(
                    onSuccess = { setting -> SettingFileUiState.Content(directory = setting.directory) },
                    onFailure = { SettingFileUiState.Loading },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = SettingFileUiState.Loading,
            )

    private val _effect = Channel<SettingFileEffect>(Channel.BUFFERED)
    val effect: Flow<SettingFileEffect> = _effect.receiveAsFlow()

    private var isChanging = false

    fun setDirectory(uri: String) {
        change { setFileDownloadDirectoryUseCase(parameter = uri) }
    }

    fun unsetDirectory() {
        change { unsetFileDownloadDirectoryUseCase(parameter = Unit) }
    }

    private fun change(block: suspend () -> Result<Unit>) {
        if (isChanging) return
        isChanging = true

        viewModelScope.launch {
            try {
                block().onFailure { _effect.send(SettingFileEffect.ChangeFailed) }
            } finally {
                isChanging = false
            }
        }
    }
}
