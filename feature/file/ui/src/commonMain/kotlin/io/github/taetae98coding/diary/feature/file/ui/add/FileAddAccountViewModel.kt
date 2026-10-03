package io.github.taetae98coding.diary.feature.file.ui.add

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
internal class FileAddAccountViewModel(
    getAccountUseCase: GetAccountUseCase,
) : ViewModel() {
    val uiState: StateFlow<FileAddAccountUiState> =
        getAccountUseCase(parameter = Unit)
            .map { result -> FileAddAccountUiState(isGuest = result.getOrNull() is Account.Guest) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = FileAddAccountUiState(),
            )
}
