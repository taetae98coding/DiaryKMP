package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.showEntityTagPickerOrNavigateToTagAdd
import kotlin.uuid.Uuid

internal fun handleWebFormEvent(
    event: WebFormEvent,
    state: WebFormState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is WebFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is WebFormEvent.ClickTagAdd ->
            showEntityTagPickerOrNavigateToTagAdd(
                tagPickerDialogState = state.tagPickerDialogState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
            )
    }
}
