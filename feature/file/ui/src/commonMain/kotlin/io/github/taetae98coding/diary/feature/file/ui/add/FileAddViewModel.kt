package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.file.exception.FileNotSelectedException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadEventUseCase
import io.github.taetae98coding.diary.domain.file.usecase.GetFileUploadStateUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReadFileUploadSourceUseCase
import io.github.taetae98coding.diary.domain.file.usecase.RequestFileUploadUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StartViewingFileScreenUseCase
import io.github.taetae98coding.diary.domain.file.usecase.StopViewingFileScreenUseCase
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

// 고른 파일은 화면이 다시 만들어져도 남아야 하지만, 앱이 다시 시작되면 다시 읽을 수 있다고 보장할 수 없어 ViewModel에만 둔다.
@KoinViewModel
internal class FileAddViewModel(
    private val readFileUploadSourceUseCase: ReadFileUploadSourceUseCase,
    private val requestFileUploadUseCase: RequestFileUploadUseCase,
    private val startViewingFileScreenUseCase: StartViewingFileScreenUseCase,
    private val stopViewingFileScreenUseCase: StopViewingFileScreenUseCase,
    getFileUploadStateUseCase: GetFileUploadStateUseCase,
    getFileUploadEventUseCase: GetFileUploadEventUseCase,
) : ViewModel() {
    private val selectedFile = MutableStateFlow<FileUploadSource?>(null)
    private val isRequesting = MutableStateFlow(false)

    val uiState: StateFlow<FileAddUiState> =
        combine(
            selectedFile,
            getFileUploadStateUseCase(parameter = Unit).map { result -> result.getOrNull() is FileUploadState.Uploading },
            isRequesting,
        ) { selectedFile, isUploading, isRequesting ->
            FileAddUiState(selectedFile = selectedFile, isUploading = isUploading || isRequesting)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = FileAddUiState(),
        )

    private val _effect = Channel<FileAddEffect>(Channel.BUFFERED)

    val effect: Flow<FileAddEffect> =
        merge(
            _effect.receiveAsFlow(),
            getFileUploadEventUseCase(parameter = FileScreen.ADD).mapNotNull { result -> result.getOrNull()?.toEffect() },
        )

    private val inProgressSelectUriSet = mutableSetOf<FileUri>()
    private var isViewing = false

    fun select(uri: FileUri) {
        if (!inProgressSelectUriSet.add(uri)) return

        viewModelScope.launch {
            try {
                readFileUploadSourceUseCase(parameter = uri)
                    .onSuccess { source -> selectedFile.value = source }
                    .onFailure { throwable ->
                        _effect.send(if (throwable is FileTooLargeException) FileAddEffect.FileTooLarge else FileAddEffect.FileUnreadable)
                    }
            } finally {
                inProgressSelectUriSet.remove(uri)
            }
        }
    }

    fun upload(
        title: String,
        description: String,
    ) {
        if (uiState.value.isUploading) return

        isRequesting.value = true
        viewModelScope.launch {
            try {
                requestFileUploadUseCase(
                    parameter =
                        RequestFileUploadUseCase.Parameter(
                            uri = selectedFile.value?.uri,
                            title = title,
                            description = description,
                        ),
                ).onSuccess {
                    selectedFile.value = null
                    _effect.send(FileAddEffect.UploadStarted)
                }.onFailure { throwable -> _effect.send(throwable.toUploadFailureEffect()) }
            } finally {
                isRequesting.value = false
            }
        }
    }

    fun startViewing() {
        if (isViewing) return
        isViewing = true

        viewModelScope.launch { startViewingFileScreenUseCase(parameter = FileScreen.ADD) }
    }

    fun stopViewing() {
        if (!isViewing) return
        isViewing = false

        viewModelScope.launch { stopViewingFileScreenUseCase(parameter = FileScreen.ADD) }
    }

    private fun Throwable.toUploadFailureEffect(): FileAddEffect =
        when (this) {
            is FileTitleBlankException -> FileAddEffect.TitleBlank
            is FileNotSelectedException -> FileAddEffect.FileNotSelected
            else -> FileAddEffect.UploadFailed
        }

    private fun FileUploadEvent.toEffect(): FileAddEffect? =
        when (this) {
            is FileUploadEvent.Succeeded -> FileAddEffect.UploadSucceeded
            is FileUploadEvent.SucceededOnFileAdd -> null
            is FileUploadEvent.TooLarge -> FileAddEffect.FileTooLarge
            is FileUploadEvent.Failed -> FileAddEffect.UploadFailed
        }
}
