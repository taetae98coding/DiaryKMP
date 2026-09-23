package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun ScrollToFirstMemoOnReselectEffect(
    reselectEvent: Flow<Unit> = emptyFlow(),
    listState: LazyListState = rememberLazyListState(),
) {
    CollectEffect(effect = reselectEvent) {
        listState.animateScrollToItem(0)
    }
}
