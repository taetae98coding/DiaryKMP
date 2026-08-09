package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlin.uuid.Uuid

internal fun handlePlaceFormEvent(
    event: PlaceFormEvent,
    state: PlaceFormState,
    tagPagingItems: LazyPagingItems<Tag>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is PlaceFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is PlaceFormEvent.ClickTagAdd ->
            if (tagPagingItems.isConfirmedEmpty()) {
                navigateToTagAdd()
            } else {
                state.tagPickerDialogState.show()
            }
    }
}
