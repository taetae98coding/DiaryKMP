package io.github.taetae98coding.diary.feature.more.ui.home.signout

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
    private val findSyncPendingUseCase: FindSyncPendingUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    val uiState: StateFlow<MoreHomeSignOutUiState>
        field =
        MutableStateFlow(
            MoreHomeSignOutUiState(isConfirmVisible = savedStateHandle[KEY_IS_CONFIRM_VISIBLE] ?: false),
        )

    private var isInProgress = false

    fun signOut() {
        if (isInProgress) return
        isInProgress = true

        viewModelScope.launch {
            try {
                if (findSyncPendingUseCase(parameter = Unit).first().getOrDefault(false)) {
                    updateConfirmVisible(isConfirmVisible = true)
                } else {
                    signOutUseCase(parameter = Unit)
                }
            } finally {
                isInProgress = false
            }
        }
    }

    fun confirmSignOut() {
        if (isInProgress) return
        isInProgress = true
        updateConfirmVisible(isConfirmVisible = false)

        viewModelScope.launch {
            try {
                signOutUseCase(parameter = Unit)
            } finally {
                isInProgress = false
            }
        }
    }

    fun cancelSignOut() {
        updateConfirmVisible(isConfirmVisible = false)
    }

    private fun updateConfirmVisible(isConfirmVisible: Boolean) {
        savedStateHandle[KEY_IS_CONFIRM_VISIBLE] = isConfirmVisible
        uiState.update { state -> state.copy(isConfirmVisible = isConfirmVisible) }
    }

    private companion object {
        const val KEY_IS_CONFIRM_VISIBLE = "isConfirmVisible"
    }
}
