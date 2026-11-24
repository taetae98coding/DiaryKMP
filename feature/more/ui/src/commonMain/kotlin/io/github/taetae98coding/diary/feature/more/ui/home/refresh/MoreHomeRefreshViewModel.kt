package io.github.taetae98coding.diary.feature.more.ui.home.refresh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.domain.account.usecase.RefreshUserDataUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
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
