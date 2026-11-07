package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.showEntityTagPickerOrNavigateToTagAdd
import kotlin.uuid.Uuid

internal fun handleMusicFormEvent(
    event: MusicFormEvent,
    state: MusicFormState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is MusicFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is MusicFormEvent.ClickTagAdd ->
            showEntityTagPickerOrNavigateToTagAdd(
                tagPickerDialogState = state.tagPickerDialogState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
            )
    }
}
