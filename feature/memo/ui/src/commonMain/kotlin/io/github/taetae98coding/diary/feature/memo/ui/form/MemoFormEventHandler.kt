package io.github.taetae98coding.diary.feature.memo.ui.form

import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.uuid.Uuid

internal fun handleMemoFormEvent(
    event: MemoFormEvent,
    state: MemoFormState,
    selectablePagingItems: MemoFormSelectablePagingItems,
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
            if (selectablePagingItems.tag.isConfirmedEmpty()) {
                navigateToTagAdd()
            } else {
                state.tagPickerDialogState.show()
            }

        is MemoFormEvent.ClickWeb -> navigateToWebDetail(event.id)

        is MemoFormEvent.ClickWebAdd ->
            if (selectablePagingItems.web.isConfirmedEmpty()) {
                navigateToWebAdd()
            } else {
                state.webPickerDialogState.show()
            }

        is MemoFormEvent.ClickContact -> navigateToContactDetail(event.id)

        is MemoFormEvent.ClickContactAdd ->
            if (selectablePagingItems.contact.isConfirmedEmpty()) {
                navigateToContactAdd()
            } else {
                state.contactPickerDialogState.show()
            }

        is MemoFormEvent.ClickPlace -> navigateToPlaceDetail(event.id)

        is MemoFormEvent.ClickPlaceAdd ->
            if (selectablePagingItems.place.isConfirmedEmpty()) {
                navigateToPlaceAdd(event.coordinate)
            } else {
                state.placePickerDialogState.show()
            }
    }
}
