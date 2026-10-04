package io.github.taetae98coding.diary.feature.setting.ui.home

internal sealed interface SettingHomeUiState {
    data object Loading : SettingHomeUiState

    data class Content(
        val itemList: List<SettingHomeItem>,
    ) : SettingHomeUiState
}
