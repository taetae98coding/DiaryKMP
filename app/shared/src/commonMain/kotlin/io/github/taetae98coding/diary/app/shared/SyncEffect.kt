package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.uuid.Uuid

@Composable
internal fun SyncEffect(
    requestSync: () -> Unit,
    authenticatedAccountId: Flow<Uuid> = emptyFlow(),
) {
    CollectEffect(
        effect = authenticatedAccountId,
        minActiveState = syncMinActiveState,
    ) {
        requestSync()
    }
}
