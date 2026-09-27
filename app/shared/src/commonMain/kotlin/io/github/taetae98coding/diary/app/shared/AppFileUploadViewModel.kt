package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReconcileFileUploadUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppFileUploadViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val reconcileFileUploadUseCase: ReconcileFileUploadUseCase,
) : ViewModel() {
    val account: Flow<Account> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
                        replayExpirationMillis = 0,
                    ),
                replay = 1,
            )

    fun reconcile(account: Account) {
        viewModelScope.launch {
            reconcileFileUploadUseCase(parameter = account)
        }
    }
}
