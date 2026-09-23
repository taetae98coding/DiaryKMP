package io.github.taetae98coding.diary.feature.contact.ui.detail

import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import kotlin.uuid.Uuid

internal sealed interface ContactDetailUiState {
    data object Loading : ContactDetailUiState

    data class Content(
        val id: Uuid,
        val detail: ContactDetail,
        val isFavorite: Boolean = false,
        val isUpdateInProgress: Boolean = false,
        val isFavoriteInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : ContactDetailUiState
}
