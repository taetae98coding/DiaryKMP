package io.github.taetae98coding.diary.feature.setting.ui.map

import io.github.taetae98coding.diary.core.model.map.MapProvider

internal sealed interface SettingMapUiState {
    data object Loading : SettingMapUiState

    data class Loaded(
        val defaultProvider: MapProvider,
    ) : SettingMapUiState
}
