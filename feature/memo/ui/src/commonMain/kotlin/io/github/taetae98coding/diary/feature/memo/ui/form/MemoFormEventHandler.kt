package io.github.taetae98coding.diary.feature.memo.ui.form

import kotlin.uuid.Uuid

internal fun handleMemoFormEvent(
    event: MemoFormEvent,
    state: MemoFormState,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToWebDetail: (Uuid) -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToPlaceDetail: (Uuid) -> Unit,
) {
    when (event) {
        is MemoFormEvent.ClickTag -> navigateToTagDetail(event.id)
        is MemoFormEvent.ClickTagAdd -> state.tagPickerDialogState.show()
        is MemoFormEvent.ClickWeb -> navigateToWebDetail(event.id)
        is MemoFormEvent.ClickWebAdd -> state.webPickerDialogState.show()
        is MemoFormEvent.ClickContact -> navigateToContactDetail(event.id)
        is MemoFormEvent.ClickContactAdd -> state.contactPickerDialogState.show()
        is MemoFormEvent.ClickPlace -> navigateToPlaceDetail(event.id)
        is MemoFormEvent.ClickPlaceAdd -> state.placePickerDialogState.show()
    }
}
