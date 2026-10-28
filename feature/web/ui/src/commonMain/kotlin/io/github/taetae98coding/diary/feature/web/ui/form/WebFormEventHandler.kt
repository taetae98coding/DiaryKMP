package io.github.taetae98coding.diary.feature.web.ui.form

import kotlin.uuid.Uuid

internal fun handleWebFormEvent(
    event: WebFormEvent,
    state: WebFormState,
    navigateToTagDetail: (Uuid) -> Unit,
) {
    when (event) {
        is WebFormEvent.ClickTag -> navigateToTagDetail(event.id)
        is WebFormEvent.ClickTagAdd -> state.tagPickerDialogState.show()
    }
}
