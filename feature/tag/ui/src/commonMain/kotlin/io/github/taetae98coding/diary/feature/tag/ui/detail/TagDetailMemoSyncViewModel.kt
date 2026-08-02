package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TagDetailMemoSyncViewModel(
    getProgressReportedUseCase: GetProgressReportedUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel() {
    val uiState: StateFlow<MemoListUiState> =
        getProgressReportedUseCase(parameter = Unit)
            .map { result -> MemoListUiState(isRefreshing = result.getOrDefault(false)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = MemoListUiState(),
            )

    fun refresh() {
        viewModelScope.launch {
            requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED)
        }
    }
}
