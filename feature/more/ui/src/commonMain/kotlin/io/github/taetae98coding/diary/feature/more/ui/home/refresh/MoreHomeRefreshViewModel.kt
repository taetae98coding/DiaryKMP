package io.github.taetae98coding.diary.feature.more.ui.home.refresh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.account.usecase.RefreshUserDataUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MoreHomeRefreshViewModel(
    private val refreshUserDataUseCase: RefreshUserDataUseCase,
) : ViewModel() {
    private var refreshJob: Job? = null

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob =
            viewModelScope.launch {
                refreshUserDataUseCase(parameter = Unit)
            }
    }
}
