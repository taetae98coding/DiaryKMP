package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaceDetailMemoSyncViewModel(
    getProgressReportedUseCase: GetProgressReportedUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel() {
    val uiState: StateFlow<MemoListUiState> =
        getProgressReportedUseCase(parameter = Unit)
            .map { result -> MemoListUiState(isRefreshing = result.getOrDefault(false)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoListUiState(),
            )

    fun refresh() {
        viewModelScope.launch {
            requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED)
        }
    }
}
