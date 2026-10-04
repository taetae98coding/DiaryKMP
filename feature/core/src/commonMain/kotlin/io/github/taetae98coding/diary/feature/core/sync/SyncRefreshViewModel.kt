package io.github.taetae98coding.diary.feature.core.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
public class SyncRefreshViewModel internal constructor(
    getProgressReportedUseCase: GetProgressReportedUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel() {
    public val uiState: StateFlow<SyncRefreshUiState> =
        getProgressReportedUseCase(parameter = Unit)
            .map { result -> SyncRefreshUiState(isRefreshing = result.getOrDefault(false)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = SyncRefreshUiState(),
            )

    private var isRequesting = false

    public fun refresh() {
        if (isRequesting) return
        isRequesting = true

        viewModelScope.launch {
            try {
                requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED)
            } finally {
                isRequesting = false
            }
        }
    }
}
