package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.domain.file.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.domain.file.exception.FileNameBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileNameTooLongException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.usecase.DownloadFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.FetchFileUseCase
import io.github.taetae98coding.diary.domain.file.usecase.UpdateFileUseCase
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
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class FileDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val fetchFileUseCase: FetchFileUseCase,
    private val updateFileUseCase: UpdateFileUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
) : ViewModel() {
    private val fetchState = MutableStateFlow<FileDetailFetchState>(FileDetailFetchState.None)
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDownloadInProgress = MutableStateFlow(false)

    val uiState: StateFlow<FileDetailUiState> =
        combine(
            fetchState,
            isUpdateInProgress,
            isDownloadInProgress,
        ) { fetchState, isUpdateInProgress, isDownloadInProgress ->
            when (fetchState) {
                is FileDetailFetchState.None, is FileDetailFetchState.Loading -> {
                    FileDetailUiState.Loading
                }

                is FileDetailFetchState.Failed -> {
                    FileDetailUiState.Failed
                }

                is FileDetailFetchState.Loaded -> {
                    FileDetailUiState.Content(
                        file = fetchState.file,
                        isUpdateInProgress = isUpdateInProgress,
                        isDownloadInProgress = isDownloadInProgress,
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = FileDetailUiState.Loading,
        )

    private val _effect = Channel<FileDetailEffect>(Channel.BUFFERED)
    val effect: Flow<FileDetailEffect> = _effect.receiveAsFlow()

    // 화면에 들어올 때마다 불리므로 아직 불러오지 않았을 때만 불러온다.
    fun fetch() {
        if (fetchState.value != FileDetailFetchState.None) return

        startFetch()
    }

    fun retry() {
        if (fetchState.value != FileDetailFetchState.Failed) return

        startFetch()
    }

    fun update(detail: DiaryFileDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateFileUseCase(parameter = UpdateFileUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { file ->
                        fetchState.value = FileDetailFetchState.Loaded(file = file)
                        _effect.send(FileDetailEffect.UpdateSucceeded)
                    }.onFailure { throwable -> _effect.send(throwable.toUpdateFailureEffect()) }
            } finally {
                isUpdateInProgress.value = false
            }
        }
    }

    fun download() {
        val file = (fetchState.value as? FileDetailFetchState.Loaded)?.file ?: return
        if (isDownloadInProgress.value) return

        viewModelScope.launch {
            isDownloadInProgress.value = true
            try {
                downloadFileUseCase(parameter = file)
                    .onSuccess { result -> if (result is FileDownloadResult.Saved) _effect.send(FileDetailEffect.DownloadSucceeded(name = result.name)) }
                    .onFailure { throwable -> _effect.send(throwable.toDownloadFailureEffect()) }
            } finally {
                isDownloadInProgress.value = false
            }
        }
    }

    private fun startFetch() {
        fetchState.value = FileDetailFetchState.Loading

        viewModelScope.launch {
            fetchState.value =
                fetchFileUseCase(parameter = id).fold(
                    onSuccess = { file -> FileDetailFetchState.Loaded(file = file) },
                    onFailure = { FileDetailFetchState.Failed },
                )
        }
    }

    private fun Throwable.toUpdateFailureEffect(): FileDetailEffect =
        when (this) {
            is FileTitleBlankException -> FileDetailEffect.TitleBlank
            is FileNameBlankException -> FileDetailEffect.NameBlank
            is FileNameTooLongException -> FileDetailEffect.NameTooLong
            else -> FileDetailEffect.UpdateFailed
        }

    private fun Throwable.toDownloadFailureEffect(): FileDetailEffect = if (this is FileDownloadDirectoryUnavailableException) FileDetailEffect.DownloadDirectoryUnavailable else FileDetailEffect.DownloadFailed
}

private sealed interface FileDetailFetchState {
    data object None : FileDetailFetchState

    data object Loading : FileDetailFetchState

    data object Failed : FileDetailFetchState

    data class Loaded(
        val file: DiaryFile,
    ) : FileDetailFetchState
}
