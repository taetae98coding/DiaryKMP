package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.usecase.PageFileUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FileHomeViewModel(
    getAccountUseCase: GetAccountUseCase,
    pageFileUseCase: PageFileUseCase,
) : ViewModel() {
    val uiState: StateFlow<FileHomeUiState> =
        getAccountUseCase(parameter = Unit)
            .map { result ->
                result.fold(
                    onSuccess = { account -> account.toUiState() },
                    onFailure = { FileHomeUiState.Loading },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = FileHomeUiState.Loading,
            )

    val filePagingData: Flow<PagingData<DiaryFile>> =
        pageFileUseCase(parameter = Unit)
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private fun Account.toUiState(): FileHomeUiState =
        when (this) {
            is Account.Guest -> FileHomeUiState.Guest
            is Account.User -> FileHomeUiState.User
        }
}
