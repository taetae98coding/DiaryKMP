package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun ScrollToFirstTagOnReselectEffect(
    reselectEvent: Flow<Unit> = emptyFlow(),
    gridState: LazyGridState = rememberLazyGridState(),
) {
    CollectEffect(effect = reselectEvent) {
        gridState.animateScrollToItem(0)
    }
}
