package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class AppSyncViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
) : ViewModel() {
    // 인증되지 않은 상태를 null로 남겨 distinct를 끊어야 같은 계정으로 다시 인증될 때도 값이 나온다.
    val authenticatedAccountId: Flow<Uuid> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .map { account -> (account as? Account.User)?.takeIf { user -> user.isSessionValid }?.id }
            .distinctUntilChanged()
            .filterNotNull()
            .shareIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
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
