package io.github.taetae98coding.diary.feature.qr.ui.add

import io.github.taetae98coding.diary.core.model.map.MapProvider

internal data class QrAddUiState(
    val isInProgress: Boolean = false,
    val defaultProvider: MapProvider? = null,
) {
    val isMapDisplayed: Boolean
        get() = defaultProvider != null
}
