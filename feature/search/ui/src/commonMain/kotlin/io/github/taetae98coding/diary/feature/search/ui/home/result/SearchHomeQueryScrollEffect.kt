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
    queryProvider: () -> String = { "" },
) {
    val latestQueryProvider by rememberUpdatedState(queryProvider)

    LaunchedEffect(listState) {
        snapshotFlow { latestQueryProvider() }
            .drop(1)
            .collect { listState.scrollToItem(0) }
    }
}
