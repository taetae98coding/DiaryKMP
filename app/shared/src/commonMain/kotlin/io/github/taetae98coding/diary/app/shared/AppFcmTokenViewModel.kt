package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SubmitFcmTokenUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppFcmTokenViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val submitFcmTokenUseCase: SubmitFcmTokenUseCase,
) : ViewModel() {
    val uiState: StateFlow<AppFcmTokenUiState> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .map { account -> AppFcmTokenUiState.Confirmed(account = account) }
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
                        replayExpirationMillis = 0,
                    ),
                initialValue = AppFcmTokenUiState.Loading,
            )

    fun submit() {
        viewModelScope.launch {
            submitFcmTokenUseCase(parameter = Unit)
        }
    }
}
