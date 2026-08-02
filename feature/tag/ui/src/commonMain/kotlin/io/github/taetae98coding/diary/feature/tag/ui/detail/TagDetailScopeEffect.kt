package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.core.model.tag.TagScope

// 표시 범위는 세 목록 탭이 함께 쓰는 화면 상태이므로, 화면이 소유한 값을 각 탭의 목록 조회로 흘려보낸다.
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
