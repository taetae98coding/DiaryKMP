package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ScrollToTopOnReselectEffect(
    reselectEvent: Flow<Unit>,
    scrollState: ScrollState,
) {
    CollectEffect(effect = reselectEvent) {
        scrollState.animateScrollTo(0)
    }
}
