package io.github.taetae98coding.diary.feature.place.ui.form

import kotlin.uuid.Uuid

internal fun handlePlaceFormEvent(
    event: PlaceFormEvent,
    state: PlaceFormState,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is PlaceFormEvent.ClickTag -> navigateToTagDetail(event.id)
        is PlaceFormEvent.ClickTagAdd -> state.tagPickerDialogState.show()
    }
}
