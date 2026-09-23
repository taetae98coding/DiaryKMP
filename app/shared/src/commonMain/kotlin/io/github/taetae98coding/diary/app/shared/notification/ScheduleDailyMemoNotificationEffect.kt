package io.github.taetae98coding.diary.app.shared.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun ScheduleDailyMemoNotificationEffect(
    schedule: () -> Unit,
    submitUpcoming: (List<UpcomingDailyMemoNotification>) -> Unit,
    upcoming: Flow<List<UpcomingDailyMemoNotification>> = emptyFlow(),
) {
    LaunchedEffect(Unit) { schedule() }
    CollectEffect(effect = upcoming) { upcomingList -> submitUpcoming(upcomingList) }
}
