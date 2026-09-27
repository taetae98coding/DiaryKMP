package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.file.usecase.RefreshFileUseCase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class FileHomeRefreshViewModel(
    private val refreshFileUseCase: RefreshFileUseCase,
) : ViewModel() {
    val uiState: StateFlow<FileHomeRefreshUiState>
        field = MutableStateFlow(FileHomeRefreshUiState())

    private val _effect = Channel<FileHomeRefreshEffect>(Channel.BUFFERED)
    val effect: Flow<FileHomeRefreshEffect> = _effect.receiveAsFlow()

    private var job: Job? = null

    fun refresh() {
        if (job?.isActive == true) return

        uiState.update { state -> state.copy(isRefreshing = true) }
        startRefresh(scrollToTopOnSuccess = FileHomeScrollToTop.None, reportsFailure = true)
    }

    fun refreshAfterUpload(firstFileIdBefore: Uuid) {
        refreshAfterUpload(firstFileIdBefore = firstFileIdBefore, reportsFailure = true)
    }

    fun refreshAfterUploadOnFileAdd(firstFileIdBefore: Uuid) {
        refreshAfterUpload(firstFileIdBefore = firstFileIdBefore, reportsFailure = false)
    }

    private fun refreshAfterUpload(
        firstFileIdBefore: Uuid,
        reportsFailure: Boolean,
    ) {
        job?.cancel()
        uiState.update { state -> state.copy(isRefreshing = false) }
        startRefresh(
            scrollToTopOnSuccess = FileHomeScrollToTop.AfterFirstFileChanges(firstFileIdBefore = firstFileIdBefore),
            reportsFailure = reportsFailure,
        )
    }

    fun cancelRefresh() {
        job?.cancel()
        uiState.value = FileHomeRefreshUiState()
    }

    fun onScrolledToTop() {
        uiState.update { state -> state.copy(scrollToTop = FileHomeScrollToTop.None) }
    }

    // 취소된 이전 불러오기가 끝나며 새 불러오기의 진행 표시를 지우지 않도록, 자기 작업이 최신일 때만 정리한다.
    // 곧바로 끝나는 작업도 최신 작업으로 기록된 뒤에 실행되게 시작을 미룬다.
    private fun startRefresh(
        scrollToTopOnSuccess: FileHomeScrollToTop,
        reportsFailure: Boolean,
    ) {
        val current =
            viewModelScope.launch(start = CoroutineStart.LAZY) {
                try {
                    refreshFileUseCase(parameter = Unit)
                        .onSuccess {
                            if (scrollToTopOnSuccess is FileHomeScrollToTop.AfterFirstFileChanges) {
                                uiState.update { state -> state.copy(scrollToTop = scrollToTopOnSuccess) }
                            }
                        }.onFailure { if (reportsFailure) _effect.send(FileHomeRefreshEffect.RefreshFailed) }
                } finally {
                    if (job === coroutineContext[Job]) uiState.update { state -> state.copy(isRefreshing = false) }
                }
            }

        job = current
        current.start()
    }
}
