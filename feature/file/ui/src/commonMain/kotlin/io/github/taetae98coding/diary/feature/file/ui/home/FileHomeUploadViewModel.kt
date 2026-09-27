package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileHomeUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileHomeUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FileHomeUploadViewModel(
    private val requestFileUploadUseCase: RequestFileUploadUseCase,
    private val startViewingFileHomeUseCase: StartViewingFileHomeUseCase,
    private val stopViewingFileHomeUseCase: StopViewingFileHomeUseCase,
    getFileUploadStateUseCase: GetFileUploadStateUseCase,
    getFileUploadEventUseCase: GetFileUploadEventUseCase,
) : ViewModel() {
    private val isRequesting = MutableStateFlow(false)

    val uiState: StateFlow<FileHomeUploadUiState> =
        combine(
            getFileUploadStateUseCase(parameter = Unit).map { result -> result.getOrNull() is FileUploadState.Uploading },
            isRequesting,
        ) { isUploading, isRequesting ->
            FileHomeUploadUiState(isUploading = isUploading || isRequesting)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = FileHomeUploadUiState(),
        )

    private val requestFailedEffect = Channel<FileHomeUploadEffect>(Channel.BUFFERED)

    val effect: Flow<FileHomeUploadEffect> =
        merge(
            getFileUploadEventUseCase(parameter = Unit).mapNotNull { result -> result.getOrNull()?.toEffect() },
            requestFailedEffect.receiveAsFlow(),
        )

    fun upload(uri: FileUri) {
        isRequesting.value = true
        viewModelScope.launch {
            try {
                requestFileUploadUseCase(parameter = uri).onFailure { requestFailedEffect.send(FileHomeUploadEffect.UploadFailed) }
            } finally {
                isRequesting.value = false
            }
        }
    }

    fun startViewing() {
        viewModelScope.launch { startViewingFileHomeUseCase(parameter = Unit) }
    }

    fun stopViewing() {
        viewModelScope.launch { stopViewingFileHomeUseCase(parameter = Unit) }
    }

    private fun FileUploadEvent.toEffect(): FileHomeUploadEffect =
        when (this) {
            is FileUploadEvent.Succeeded -> FileHomeUploadEffect.UploadSucceeded(id = fileId)
            is FileUploadEvent.TooLarge -> FileHomeUploadEffect.UploadTooLarge
            is FileUploadEvent.Failed -> FileHomeUploadEffect.UploadFailed
        }
}
