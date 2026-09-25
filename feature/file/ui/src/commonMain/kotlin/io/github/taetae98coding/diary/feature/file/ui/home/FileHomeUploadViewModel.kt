package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FileHomeUploadViewModel(
    private val uploadFileUseCase: UploadFileUseCase,
) : ViewModel() {
    val uiState: StateFlow<FileHomeUploadUiState>
        field = MutableStateFlow(FileHomeUploadUiState())

    private val _effect = Channel<FileHomeUploadEffect>(Channel.BUFFERED)
    val effect: Flow<FileHomeUploadEffect> = _effect.receiveAsFlow()

    fun upload(uri: FileUri) {
        if (uiState.value.isUploading) return

        uiState.update { state -> state.copy(isUploading = true) }
        viewModelScope.launch {
            try {
                uploadFileUseCase(parameter = uri)
                    .onSuccess { file -> _effect.send(FileHomeUploadEffect.UploadSucceeded(id = file.id)) }
                    .onFailure { throwable -> throwable.toEffect()?.let { effect -> _effect.send(effect) } }
            } finally {
                uiState.update { state -> state.copy(isUploading = false) }
            }
        }
    }

    private fun Throwable.toEffect(): FileHomeUploadEffect? =
        when (this) {
            is FileTooLargeException -> FileHomeUploadEffect.UploadTooLarge
            is FileUploadAccountChangedException -> null
            else -> FileHomeUploadEffect.UploadFailed
        }
}
