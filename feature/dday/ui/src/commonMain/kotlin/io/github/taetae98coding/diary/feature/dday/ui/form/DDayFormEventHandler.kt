package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.showEntityTagPickerOrNavigateToTagAdd
import kotlin.uuid.Uuid

internal fun handleDDayFormEvent(
    event: DDayFormEvent,
    state: DDayFormState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is DDayFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is DDayFormEvent.ClickTagAdd ->
            showEntityTagPickerOrNavigateToTagAdd(
                tagPickerDialogState = state.tagPickerDialogState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
            )
    }
}
