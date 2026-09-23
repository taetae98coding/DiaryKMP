package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.core.model.tag.TagScope

@Composable
internal fun TagDetailScopeEffect(
    onSelect: (TagScope) -> Unit,
    state: TagDetailScopeState = rememberTagDetailScopeState(),
) {
    val currentOnSelect by rememberUpdatedState(onSelect)

    LaunchedEffect(state) {
        snapshotFlow { state.scope }
            .collect { scope -> currentOnSelect(scope) }
    }
}
