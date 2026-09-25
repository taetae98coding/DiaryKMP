package io.github.taetae98coding.diary.feature.setting.ui.browser

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile

internal sealed interface SettingBrowserUiState {
    data object Loading : SettingBrowserUiState

    data class Loaded(
        val profileList: List<ChromeProfile>,
        val selectedProfileDirectory: String,
        val isProfileListUnavailable: Boolean = false,
        val hasStoredProfile: Boolean = selectedProfileDirectory.isNotEmpty(),
    ) : SettingBrowserUiState
}
