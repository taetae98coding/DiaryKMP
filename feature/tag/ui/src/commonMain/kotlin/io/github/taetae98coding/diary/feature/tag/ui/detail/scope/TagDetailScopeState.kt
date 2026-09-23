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
        val Saver: Saver<TagDetailScopeState, String> =
            Saver(
                save = { state -> state.scope.name },
                restore = { saved -> TagDetailScopeState(sheetState = DialogState(), initialScope = TagScope.valueOf(saved)) },
            )
    }
}

@Composable
internal fun rememberTagDetailScopeState(): TagDetailScopeState {
    val sheetState = rememberDialogState()

    return rememberSaveable(sheetState, saver = TagDetailScopeState.Saver) {
        TagDetailScopeState(sheetState = sheetState)
    }
}
