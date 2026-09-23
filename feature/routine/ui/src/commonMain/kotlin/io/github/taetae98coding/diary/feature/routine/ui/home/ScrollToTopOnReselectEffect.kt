package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun ScrollToTopOnReselectEffect(
    reselectEvent: Flow<Unit> = emptyFlow(),
    scrollState: ScrollState = rememberScrollState(),
) {
    CollectEffect(effect = reselectEvent) {
        scrollState.animateScrollTo(0)
    }
}
