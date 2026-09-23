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

    // 화면이 표시될 때마다 원격을 다시 부르되, 앞선 요청이 아직 진행 중이면 그 결과를 기다리고 새 요청을 겹치지 않는다.
    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob =
            viewModelScope.launch {
                refreshUserDataUseCase(parameter = Unit)
            }
    }
}
