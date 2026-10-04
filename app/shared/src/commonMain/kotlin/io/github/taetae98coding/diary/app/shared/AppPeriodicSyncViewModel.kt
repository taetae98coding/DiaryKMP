package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.SchedulePeriodicSyncUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppPeriodicSyncViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val schedulePeriodicSyncUseCase: SchedulePeriodicSyncUseCase,
) : ViewModel() {
    val uiState: StateFlow<AppPeriodicSyncUiState> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .map { account -> AppPeriodicSyncUiState.Confirmed(account = account) }
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
                        replayExpirationMillis = 0,
                    ),
                initialValue = AppPeriodicSyncUiState.Loading,
            )

    private var isScheduling = false

    fun schedulePeriodicSync() {
        if (isScheduling) return
        isScheduling = true

        viewModelScope.launch {
            try {
                schedulePeriodicSyncUseCase(parameter = Unit)
            } finally {
                isScheduling = false
            }
        }
    }
}
