package io.github.taetae98coding.diary.feature.qr.ui.detail

import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import kotlin.uuid.Uuid

internal sealed interface QrDetailUiState {
    data object Loading : QrDetailUiState

    data class Content(
        val id: Uuid,
        val detail: QrDetail,
        val isUpdateInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
        val defaultProvider: MapProvider? = null,
    ) : QrDetailUiState {
        val isMapDisplayed: Boolean
            get() = defaultProvider != null
    }
}
