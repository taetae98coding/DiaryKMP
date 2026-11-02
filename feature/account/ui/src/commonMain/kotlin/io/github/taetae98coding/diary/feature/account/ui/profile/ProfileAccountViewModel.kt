package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class ProfileAccountViewModel(
    getAccountUseCase: GetAccountUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProfileAccountUiState> =
        getAccountUseCase(parameter = Unit)
            .map { result ->
                when (result.getOrNull()) {
                    null -> ProfileAccountUiState.Loading
                    is Account.Guest -> ProfileAccountUiState.Guest
                    is Account.User -> ProfileAccountUiState.User
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = ProfileAccountUiState.Loading,
            )
}
