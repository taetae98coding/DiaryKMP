package io.github.taetae98coding.diary.feature.contact.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.showEntityTagPickerOrNavigateToTagAdd
import kotlin.uuid.Uuid

internal fun handleContactFormEvent(
    event: ContactFormEvent,
    state: ContactFormState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is ContactFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is ContactFormEvent.ClickTagAdd ->
            showEntityTagPickerOrNavigateToTagAdd(
                tagPickerDialogState = state.tagPickerDialogState,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
            )
    }
}
