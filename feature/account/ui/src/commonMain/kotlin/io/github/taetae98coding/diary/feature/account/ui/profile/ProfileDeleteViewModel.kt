package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.domain.account.usecase.CheckAppleReauthenticationRequiredUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.DeleteAccountUseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.logCrashlyticsFailure
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ProfileDeleteViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val checkAppleReauthenticationRequiredUseCase: CheckAppleReauthenticationRequiredUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProfileDeleteUiState>
        field =
        MutableStateFlow(
            ProfileDeleteUiState(isConfirmVisible = savedStateHandle[KEY_IS_CONFIRM_VISIBLE] ?: false),
        )

    private val _effect = Channel<ProfileDeleteEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileDeleteEffect> = _effect.receiveAsFlow()

    private var isDeleting = false
    private var isAwaitingAppleAuthorization = false

    fun showConfirm() {
        if (uiState.value.isInProgress) return

        updateConfirmVisible(isConfirmVisible = true)
    }

    // 탈퇴가 진행 중이면 확인 다이얼로그를 닫지 않는다.
    fun dismissConfirm() {
        if (uiState.value.isInProgress) return

        updateConfirmVisible(isConfirmVisible = false)
    }

    // 흐름의 끝을 알 수 있는 Apple 재인증은 화면이 흐름을 시작하기 전부터 끝날 때까지 진행 중으로 둔다.
    fun confirmDelete(isSignInEndDetectable: Boolean) {
        if (uiState.value.isInProgress) return
        uiState.update { state -> state.copy(isInProgress = true) }

        viewModelScope.launch {
            try {
                checkAppleReauthenticationRequiredUseCase(parameter = Unit)
                    .onSuccess { isRequired ->
                        if (isRequired) {
                            isAwaitingAppleAuthorization = isSignInEndDetectable
                            _effect.send(ProfileDeleteEffect.RequestAppleAuthorization)
                        } else {
                            isDeleting = true
                            try {
                                requestDelete(appleAuthorization = null)
                            } finally {
                                isDeleting = false
                            }
                        }
                    }.onFailure { fail() }
            } finally {
                if (!isAwaitingAppleAuthorization) endProgress()
            }
        }
    }

    fun delete(appleAuthorization: AppleAuthorization) {
        if (isDeleting) return
        isDeleting = true
        isAwaitingAppleAuthorization = false
        uiState.update { state -> state.copy(isInProgress = true) }

        viewModelScope.launch {
            try {
                requestDelete(appleAuthorization = appleAuthorization)
            } finally {
                isDeleting = false
                endProgress()
            }
        }
    }

    // 끝을 알 수 있는 Apple 재인증을 기다리던 중 화면이 사라지면 진행 중을 풀고 다이얼로그를 닫는다. 판정과 서버 요청 중에는 그대로 둔다.
    fun cancelAppleAuthorization() {
        if (!isAwaitingAppleAuthorization) return
        isAwaitingAppleAuthorization = false

        endAppleAuthorization()
    }

    // Apple 재인증이 인가 코드 없이 끝나면 탈퇴 대기로 돌아간다.
    fun endAppleAuthorization() {
        if (isDeleting) return
        isAwaitingAppleAuthorization = false

        endProgress()
        updateConfirmVisible(isConfirmVisible = false)
    }

    fun failAppleAuthorization(throwable: Throwable) {
        DiaryLogger.logCrashlyticsFailure(source = this, throwable = throwable)
        endAppleAuthorization()
        viewModelScope.launch { _effect.send(ProfileDeleteEffect.DeleteFailed) }
    }

    private suspend fun requestDelete(appleAuthorization: AppleAuthorization?) {
        deleteAccountUseCase(parameter = appleAuthorization)
            .onFailure { fail() }
    }

    private suspend fun fail() {
        updateConfirmVisible(isConfirmVisible = false)
        _effect.send(ProfileDeleteEffect.DeleteFailed)
    }

    private fun endProgress() {
        uiState.update { state -> state.copy(isInProgress = false) }
    }

    private fun updateConfirmVisible(isConfirmVisible: Boolean) {
        savedStateHandle[KEY_IS_CONFIRM_VISIBLE] = isConfirmVisible
        uiState.update { state -> state.copy(isConfirmVisible = isConfirmVisible) }
    }

    private companion object {
        const val KEY_IS_CONFIRM_VISIBLE = "isConfirmVisible"
    }
}
