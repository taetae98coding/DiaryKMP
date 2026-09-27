package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun SyncEffect(
    requestSync: (SyncTrigger) -> Unit,
    schedulePeriodicSync: () -> Unit,
    account: Flow<Account> = emptyFlow(),
) {
    val syncTrigger = remember(account) { account.toSyncTrigger() }

    CollectEffect(
        effect = syncTrigger,
        minActiveState = syncMinActiveState,
    ) { trigger ->
        requestSync(trigger)
        schedulePeriodicSync()
    }
}
