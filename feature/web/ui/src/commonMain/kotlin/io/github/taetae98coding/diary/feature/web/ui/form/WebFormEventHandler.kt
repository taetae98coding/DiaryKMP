package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlin.uuid.Uuid

internal fun handleWebFormEvent(
    event: WebFormEvent,
    state: WebFormState,
    tagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is WebFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is WebFormEvent.ClickTagAdd ->
            if (tagPagingItems.isConfirmedEmpty()) {
                navigateToTagAdd()
            } else {
                state.tagPickerDialogState.show()
            }
    }
}
