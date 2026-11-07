package io.github.taetae98coding.diary.feature.core.tag

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.tag.Tag

public fun showEntityTagPickerOrNavigateToTagAdd(
    tagPickerDialogState: DialogState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
) {
    if (selectableTagPagingItems.isConfirmedEmpty()) {
        navigateToTagAdd()
    } else {
        tagPickerDialogState.show()
    }
}

public fun handleEntityTagPickerEvent(
    event: EntityTagPickerEvent,
    tagViewModel: EntityTagInputController,
    navigateToTagAdd: () -> Unit,
) {
    when (event) {
        is EntityTagPickerEvent.ClickAdd -> navigateToTagAdd()
        is EntityTagPickerEvent.Add -> tagViewModel.add(tagId = event.id)
        is EntityTagPickerEvent.Remove -> tagViewModel.remove(tagId = event.id)
        is EntityTagPickerEvent.ChangeQuery -> tagViewModel.updateQuery(query = event.query)
    }
}
