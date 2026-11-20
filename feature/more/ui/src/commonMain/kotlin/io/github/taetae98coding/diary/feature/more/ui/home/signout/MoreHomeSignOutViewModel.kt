package io.github.taetae98coding.diary.feature.more.ui.home.signout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.sync.usecase.GetSyncPendingExistsUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MoreHomeSignOutViewModel(
    savedStateHandle: SavedStateHandle,
    private val getSyncPendingExistsUseCase: GetSyncPendingExistsUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    private val isConfirmVisible = savedStateHandle.getMutableStateFlow(key = KEY_IS_CONFIRM_VISIBLE, initialValue = false)

    val uiState: StateFlow<MoreHomeSignOutUiState> =
        isConfirmVisible
            .map { isVisible -> MoreHomeSignOutUiState(isConfirmVisible = isVisible) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MoreHomeSignOutUiState(isConfirmVisible = isConfirmVisible.value),
            )

    private var isInProgress = false

    fun signOut() {
        if (isInProgress) return
        isInProgress = true

        viewModelScope.launch {
            try {
                if (getSyncPendingExistsUseCase().first().getOrDefault(false)) {
                    isConfirmVisible.value = true
                } else {
                    signOutUseCase()
                }
            } finally {
                isInProgress = false
            }
        }
    }

    fun confirmSignOut() {
        if (isInProgress) return
        isInProgress = true
        isConfirmVisible.value = false

        viewModelScope.launch {
            try {
                signOutUseCase()
            } finally {
                isInProgress = false
            }
        }
    }

    fun cancelSignOut() {
        isConfirmVisible.value = false
    }

    private companion object {
        const val KEY_IS_CONFIRM_VISIBLE = "isConfirmVisible"
    }
}
