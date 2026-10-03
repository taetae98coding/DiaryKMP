package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReconcileFileUploadUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppFileUploadViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val reconcileFileUploadUseCase: ReconcileFileUploadUseCase,
) : ViewModel() {
    val uiState: StateFlow<AppFileUploadUiState> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .map { account -> AppFileUploadUiState.Confirmed(account = account) }
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
                        replayExpirationMillis = 0,
                    ),
                initialValue = AppFileUploadUiState.Loading,
            )

    fun reconcile(account: Account) {
        viewModelScope.launch {
            reconcileFileUploadUseCase(parameter = account)
        }
    }
}
