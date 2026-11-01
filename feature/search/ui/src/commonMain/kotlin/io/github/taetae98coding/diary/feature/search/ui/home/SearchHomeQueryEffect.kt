package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow

@Composable
internal fun SearchHomeQueryEffect(
    queryState: TextFieldState,
    onQueryChange: (String) -> Unit,
) {
    val latestOnQueryChange by rememberUpdatedState(onQueryChange)

    LaunchedEffect(queryState) {
        snapshotFlow { queryState.text.toString() }
            .collect { query -> latestOnQueryChange(query) }
    }
}
