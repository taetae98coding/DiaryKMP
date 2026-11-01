package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.drop

@Composable
internal fun SearchHomeQueryScrollEffect(
    listState: LazyListState,
    queryProvider: () -> String,
) {
    val latestQueryProvider by rememberUpdatedState(queryProvider)

    LaunchedEffect(listState) {
        snapshotFlow { latestQueryProvider() }
            .drop(1)
            // 보고 있지 않은 유형의 목록은 배치되지 않으므로, 배치를 기다리지 않고 다음 배치의 자리를 정한다.
            .collect { listState.requestScrollToItem(0) }
    }
}
