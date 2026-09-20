package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.account.usecase.ChangeProfileImageUseCase
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MoreHomeAccountViewModel(
    getAccountUseCase: GetAccountUseCase,
    private val changeProfileImageUseCase: ChangeProfileImageUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    val uiState: StateFlow<MoreHomeAccountUiState> =
        getAccountUseCase(parameter = Unit)
            .map { result ->
                result.fold(
                    onSuccess = { account -> account.toUiState() },
                    onFailure = { MoreHomeAccountUiState.Loading },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MoreHomeAccountUiState.Loading,
            )

    fun changeProfileImage(uri: FileUri) {
        viewModelScope.launch {
            changeProfileImageUseCase(parameter = uri)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase(parameter = Unit)
        }
    }
}
