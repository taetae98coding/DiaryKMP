package io.github.taetae98coding.diary.feature.more.ui.home.refresh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.account.usecase.RefreshUserDataUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MoreHomeRefreshViewModel(
    private val refreshUserDataUseCase: RefreshUserDataUseCase,
) : ViewModel() {
    private var isRefreshInProgress = false

    fun refresh() {
        if (isRefreshInProgress) return

        isRefreshInProgress = true
        viewModelScope.launch {
            try {
                refreshUserDataUseCase()
            } finally {
                isRefreshInProgress = false
            }
        }
    }
}
