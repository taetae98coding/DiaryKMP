package io.github.taetae98coding.diary.feature.contact.ui.home

import io.github.taetae98coding.diary.core.model.contact.ContactFilter

internal data class ContactHomeFilterUiState(
    val filter: ContactFilter = ContactFilter(),
) {
    val isApplied: Boolean = filter != ContactFilter()
}
