package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.core.model.tag.TagScope

@Stable
internal class TagDetailScopeState(
    val sheetState: DialogState,
    initialScope: TagScope = TagScope.SELF,
) {
    var scope: TagScope by mutableStateOf(initialScope)
        private set

    val isApplied: Boolean
        get() = scope != TagScope.SELF

    fun select(scope: TagScope) {
        this.scope = scope
    }

    companion object {
        // 표시 범위 Bottom Sheet의 표시 여부는 sheetState가 따로 저장하므로, 복원할 때도 그 복원된 sheetState를 그대로 쓴다.
        fun saver(sheetState: DialogState): Saver<TagDetailScopeState, String> =
            Saver(
                save = { state -> state.scope.name },
                restore = { saved -> TagDetailScopeState(sheetState = sheetState, initialScope = TagScope.valueOf(saved)) },
            )
    }
}

@Composable
internal fun rememberTagDetailScopeState(): TagDetailScopeState {
    val sheetState = rememberDialogState()

    return rememberSaveable(sheetState, saver = TagDetailScopeState.saver(sheetState = sheetState)) {
        TagDetailScopeState(sheetState = sheetState)
    }
}
