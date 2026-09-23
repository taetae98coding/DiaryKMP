package io.github.taetae98coding.diary.feature.more.ui.home.signout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.FindSyncPendingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MoreHomeSignOutViewModel(
    private val findSyncPendingUseCase: FindSyncPendingUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    val uiState: StateFlow<MoreHomeSignOutUiState>
        field = MutableStateFlow(MoreHomeSignOutUiState())

    fun signOut() {
        viewModelScope.launch {
            if (findSyncPendingUseCase(parameter = Unit).first().getOrDefault(false)) {
                uiState.update { state -> state.copy(isConfirmVisible = true) }
            } else {
                signOutUseCase(parameter = Unit)
            }
        }
    }

    fun confirmSignOut() {
        uiState.update { state -> state.copy(isConfirmVisible = false) }

        viewModelScope.launch {
            signOutUseCase(parameter = Unit)
        }
    }

    fun cancelSignOut() {
        uiState.update { state -> state.copy(isConfirmVisible = false) }
    }
}
