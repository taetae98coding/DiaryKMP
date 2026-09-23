package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.memo.usecase.GetUpcomingDailyMemoNotificationUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.ScheduleDailyMemoNotificationUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SubmitUpcomingDailyMemoNotificationUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppDailyMemoNotificationViewModel(
    private val scheduleDailyMemoNotificationUseCase: ScheduleDailyMemoNotificationUseCase,
    getUpcomingDailyMemoNotificationUseCase: GetUpcomingDailyMemoNotificationUseCase,
    private val submitUpcomingDailyMemoNotificationUseCase: SubmitUpcomingDailyMemoNotificationUseCase,
) : ViewModel() {
    val upcoming: Flow<List<UpcomingDailyMemoNotification>> =
        getUpcomingDailyMemoNotificationUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS,
                        replayExpirationMillis = 0,
                    ),
                replay = 1,
            )

    fun schedule() {
        viewModelScope.launch { scheduleDailyMemoNotificationUseCase(parameter = Unit) }
    }

    fun submitUpcoming(upcomingList: List<UpcomingDailyMemoNotification>) {
        viewModelScope.launch { submitUpcomingDailyMemoNotificationUseCase(parameter = upcomingList) }
    }
}
