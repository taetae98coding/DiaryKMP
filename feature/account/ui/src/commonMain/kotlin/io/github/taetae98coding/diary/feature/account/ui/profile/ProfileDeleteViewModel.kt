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

    fun showConfirm() {
        if (uiState.value.isInProgress) return

        updateConfirmVisible(isConfirmVisible = true)
    }

    // 탈퇴가 진행 중이면 확인 다이얼로그를 닫지 않는다.
    fun dismissConfirm() {
        if (uiState.value.isInProgress) return

        updateConfirmVisible(isConfirmVisible = false)
    }

    // Apple 재인증은 화면이 진행하고 그동안 다이얼로그를 진행 중으로 두지 않으므로, 판정이 끝나면 요청만 보내고 진행 중을 푼다.
    fun confirmDelete() {
        if (uiState.value.isInProgress) return
        uiState.update { state -> state.copy(isInProgress = true) }

        viewModelScope.launch {
            try {
                checkAppleReauthenticationRequiredUseCase(parameter = Unit)
                    .onSuccess { isRequired ->
                        if (isRequired) {
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
                endProgress()
            }
        }
    }

    fun delete(appleAuthorization: AppleAuthorization) {
        if (isDeleting) return
        isDeleting = true
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

    // Apple 재인증이 인가 코드 없이 끝나면 탈퇴 대기로 돌아간다.
    fun endAppleAuthorization() {
        if (isDeleting) return

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
