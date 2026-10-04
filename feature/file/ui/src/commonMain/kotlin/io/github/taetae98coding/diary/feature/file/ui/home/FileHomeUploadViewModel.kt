package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileScreenUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileScreenUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FileHomeUploadViewModel(
    private val startViewingFileScreenUseCase: StartViewingFileScreenUseCase,
    private val stopViewingFileScreenUseCase: StopViewingFileScreenUseCase,
    getFileUploadStateUseCase: GetFileUploadStateUseCase,
    getFileUploadEventUseCase: GetFileUploadEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<FileHomeUploadUiState> =
        getFileUploadStateUseCase(parameter = Unit)
            .map { result -> FileHomeUploadUiState(isUploading = result.getOrNull() is FileUploadState.Uploading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = FileHomeUploadUiState(),
            )

    private val addEffect = Channel<FileHomeUploadEffect>(Channel.BUFFERED)
    private var isViewing = false

    val effect: Flow<FileHomeUploadEffect> =
        merge(
            getFileUploadEventUseCase(parameter = FileScreen.HOME).mapNotNull { result -> result.getOrNull()?.toEffect() },
            addEffect.receiveAsFlow(),
        )

    fun requestAdd() {
        if (uiState.value.isUploading) return

        addEffect.trySend(FileHomeUploadEffect.NavigateToAdd)
    }

    fun startViewing() {
        if (isViewing) return
        isViewing = true

        viewModelScope.launch { startViewingFileScreenUseCase(parameter = FileScreen.HOME) }
    }

    fun stopViewing() {
        if (!isViewing) return
        isViewing = false

        viewModelScope.launch { stopViewingFileScreenUseCase(parameter = FileScreen.HOME) }
    }

    private fun FileUploadEvent.toEffect(): FileHomeUploadEffect =
        when (this) {
            is FileUploadEvent.Succeeded -> FileHomeUploadEffect.UploadSucceeded(id = fileId)
            is FileUploadEvent.SucceededOnFileAdd -> FileHomeUploadEffect.UploadSucceededOnFileAdd(id = fileId)
            is FileUploadEvent.TooLarge -> FileHomeUploadEffect.UploadTooLarge
            is FileUploadEvent.Failed -> FileHomeUploadEffect.UploadFailed
        }
}
