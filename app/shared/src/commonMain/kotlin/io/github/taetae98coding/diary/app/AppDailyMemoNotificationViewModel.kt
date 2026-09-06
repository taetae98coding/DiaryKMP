package io.github.taetae98coding.diary.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.memo.usecase.ScheduleDailyMemoNotificationUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppDailyMemoNotificationViewModel(
    private val scheduleDailyMemoNotificationUseCase: ScheduleDailyMemoNotificationUseCase,
) : ViewModel() {
    fun schedule() {
        viewModelScope.launch { scheduleDailyMemoNotificationUseCase(parameter = Unit) }
    }
}
