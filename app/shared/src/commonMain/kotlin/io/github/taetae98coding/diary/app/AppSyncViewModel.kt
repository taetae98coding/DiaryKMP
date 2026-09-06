package io.github.taetae98coding.diary.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppSyncViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel() {
    val account: Flow<Account> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = 0,
                    ),
                replay = 1,
            )

    fun requestSync() {
        viewModelScope.launch {
            requestSyncUseCase(parameter = SyncTrigger.ACCOUNT_CONFIRMED)
        }
    }
}
