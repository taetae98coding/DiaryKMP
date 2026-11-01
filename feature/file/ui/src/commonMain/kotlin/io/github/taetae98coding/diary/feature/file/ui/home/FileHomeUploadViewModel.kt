package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadSucceededEventUseCase
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
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FileHomeUploadViewModel(
    getFileUploadStateUseCase: GetFileUploadStateUseCase,
    getFileUploadEventUseCase: GetFileUploadEventUseCase,
    getFileUploadSucceededEventUseCase: GetFileUploadSucceededEventUseCase,
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

    // 성공은 FileHome이 가려진 동안 끝났어도 돌아와서 받아야 하므로 쌓아 두는 성공 결과에서 받고, 실패는 보이는 동안 끝난 것만 받는다.
    val effect: Flow<FileHomeUploadEffect> =
        merge(
            getFileUploadSucceededEventUseCase(parameter = Unit).mapNotNull { result -> result.getOrNull()?.let { FileHomeUploadEffect.UploadSucceeded } },
            getFileUploadEventUseCase(parameter = Unit).mapNotNull { result -> result.getOrNull()?.toFailureEffect() },
            addEffect.receiveAsFlow(),
        )

    fun requestAdd() {
        if (uiState.value.isUploading) return

        addEffect.trySend(FileHomeUploadEffect.NavigateToAdd)
    }

    private fun FileUploadEvent.toFailureEffect(): FileHomeUploadEffect? =
        when (this) {
            is FileUploadEvent.Succeeded -> null
            is FileUploadEvent.TooLarge -> FileHomeUploadEffect.UploadTooLarge
            is FileUploadEvent.Failed -> FileHomeUploadEffect.UploadFailed
        }
}
