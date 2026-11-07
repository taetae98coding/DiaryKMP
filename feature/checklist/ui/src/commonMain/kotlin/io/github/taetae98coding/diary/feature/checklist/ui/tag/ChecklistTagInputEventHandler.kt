package io.github.taetae98coding.diary.feature.checklist.ui.tag

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.showEntityTagPickerOrNavigateToTagAdd
import kotlin.uuid.Uuid

internal fun handleChecklistTagInputEvent(
    event: ChecklistTagInputEvent,
    tagPickerDialogState: DialogState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is ChecklistTagInputEvent.ClickTag -> navigateToTagDetail(event.id)

        is ChecklistTagInputEvent.ClickTagAdd ->
            showEntityTagPickerOrNavigateToTagAdd(
                tagPickerDialogState = tagPickerDialogState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
            )
    }
}
