package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.drop

@Composable
internal fun SearchHomeQueryEffect(
    queryState: TextFieldState,
    onQueryShow: (String) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    val latestOnQueryShow by rememberUpdatedState(onQueryShow)
    val latestOnQueryChange by rememberUpdatedState(onQueryChange)

    LaunchedEffect(queryState) {
        latestOnQueryShow(queryState.text.toString())
        snapshotFlow { queryState.text.toString() }
            .drop(1)
            .collect { query -> latestOnQueryChange(query) }
    }
}
