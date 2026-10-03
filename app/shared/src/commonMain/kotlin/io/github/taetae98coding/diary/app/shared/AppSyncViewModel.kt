package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppSyncViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel() {
    // 인증되지 않은 상태도 내보내야 같은 계정으로 다시 인증될 때 값이 다시 바뀐다.
    val uiState: StateFlow<AppSyncUiState> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .map { account -> account.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
                        replayExpirationMillis = 0,
                    ),
                initialValue = AppSyncUiState.Loading,
            )

    fun requestSync() {
        viewModelScope.launch {
            requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)
        }
    }

    private fun Account.toUiState(): AppSyncUiState = if (this is Account.User && isSessionValid) AppSyncUiState.Authenticated(accountId = id) else AppSyncUiState.Unauthenticated
}
