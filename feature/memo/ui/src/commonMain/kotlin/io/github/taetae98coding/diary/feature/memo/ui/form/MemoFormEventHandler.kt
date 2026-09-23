package io.github.taetae98coding.diary.feature.memo.ui.form

import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import kotlin.uuid.Uuid

internal fun handleMemoFormEvent(
    event: MemoFormEvent,
    state: MemoFormState,
    tagPagingItems: LazyPagingItems<Tag>,
    webPagingItems: LazyPagingItems<Web>,
    contactPagingItems: LazyPagingItems<Contact>,
    placePagingItems: LazyPagingItems<Place>,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToWebAdd: () -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToContactAdd: () -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToPlaceAdd: (Coordinate?) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
) {
    when (event) {
        is MemoFormEvent.ClickTag -> navigateToTagDetail(event.id)

        is MemoFormEvent.ClickTagAdd ->
            if (tagPagingItems.isConfirmedEmpty()) {
                navigateToTagAdd()
            } else {
                state.tagPickerDialogState.show()
            }

        is MemoFormEvent.ClickWeb -> navigateToWebDetail(event.id)

        is MemoFormEvent.ClickWebAdd ->
            if (webPagingItems.isConfirmedEmpty()) {
                navigateToWebAdd()
            } else {
                state.webPickerDialogState.show()
            }

        is MemoFormEvent.ClickContact -> navigateToContactDetail(event.id)

        is MemoFormEvent.ClickContactAdd ->
            if (contactPagingItems.isConfirmedEmpty()) {
                navigateToContactAdd()
            } else {
                state.contactPickerDialogState.show()
            }

        is MemoFormEvent.ClickPlace -> navigateToPlaceDetail(event.id)

        is MemoFormEvent.ClickPlaceAdd ->
            if (placePagingItems.isConfirmedEmpty()) {
                navigateToPlaceAdd(event.coordinate)
            } else {
                state.placePickerDialogState.show()
            }
    }
}
