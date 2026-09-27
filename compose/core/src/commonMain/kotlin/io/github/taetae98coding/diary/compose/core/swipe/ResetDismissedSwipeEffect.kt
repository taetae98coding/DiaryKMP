package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun ResetDismissedSwipeEffect(state: SwipeToDismissBoxState) {
    LaunchedEffect(state) {
        snapshotFlow { state.settledValue }
            .collectLatest { value ->
                if (value != SwipeToDismissBoxValue.Settled) {
                    delay(SwipeToFinishAndDeleteBoxDefaults.DismissedResetDelay)
                    state.reset()
                }
            }
    }
}
