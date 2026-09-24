package io.github.taetae98coding.diary.feature.setting.ui.home

internal sealed interface SettingHomeUiState {
    data object Loading : SettingHomeUiState

    data class Loaded(
        val itemList: List<SettingHomeItem>,
    ) : SettingHomeUiState
}
