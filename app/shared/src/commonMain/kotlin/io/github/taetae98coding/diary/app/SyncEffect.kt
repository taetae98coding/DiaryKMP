package io.github.taetae98coding.diary.app

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.core.model.account.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun SyncEffect(
    requestSync: () -> Unit,
    account: Flow<Account> = emptyFlow(),
) {
    CollectEffect(account) { requestSync() }
}
