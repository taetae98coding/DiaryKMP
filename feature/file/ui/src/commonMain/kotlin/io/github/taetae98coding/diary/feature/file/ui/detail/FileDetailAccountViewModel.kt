package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

// 이 화면은 로그인한 계정의 파일 목록에서만 열리므로, 처음 확인한 사용자를 파일의 주인으로 본다.
// 계정을 확인하지 못한 결과는 계정이 바뀐 것이 아니므로 버린다.
@KoinViewModel
internal class FileDetailAccountViewModel(
    getAccountUseCase: GetAccountUseCase,
) : ViewModel() {
    private var ownerId: Uuid? = null

    val uiState: StateFlow<FileDetailAccountUiState> =
        getAccountUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .map { account -> FileDetailAccountUiState(isAccountChanged = account.isChangedFromOwner()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = FileDetailAccountUiState(),
            )

    private fun Account.isChangedFromOwner(): Boolean =
        when (this) {
            is Account.Guest -> {
                true
            }

            is Account.User -> {
                val owner = ownerId ?: id.also { id -> ownerId = id }

                owner != id
            }
        }
}
