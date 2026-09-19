package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.domain.account.usecase.SignInWithAppleUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class LoginHomeViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
) : ViewModel() {
    val uiState: StateFlow<LoginHomeUiState>
        field = MutableStateFlow(LoginHomeUiState())

    private val _effect = Channel<LoginHomeEffect>(Channel.BUFFERED)
    val effect: Flow<LoginHomeEffect> = _effect.receiveAsFlow()

    fun signInWithGoogle(credential: GoogleCredential) {
        signIn { signInWithGoogleUseCase(parameter = credential) }
    }

    fun signInWithApple(credential: AppleCredential) {
        signIn { signInWithAppleUseCase(parameter = credential) }
    }

    private fun signIn(request: suspend () -> Result<Unit>) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { it.copy(isInProgress = true) }
            try {
                request()
                    .onSuccess { _effect.send(LoginHomeEffect.SignInSucceeded) }
                    .onFailure { _effect.send(LoginHomeEffect.SignInFailed) }
            } finally {
                uiState.update { it.copy(isInProgress = false) }
            }
        }
    }
}
