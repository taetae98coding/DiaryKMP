package io.github.taetae98coding.diary.feature.setting.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.usecase.FindChromeProfileListUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.GetChromeSessionProfileDirectoryUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.SelectChromeSessionProfileUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.UnselectChromeSessionProfileUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingBrowserViewModel(
    getChromeSessionProfileDirectoryUseCase: GetChromeSessionProfileDirectoryUseCase,
    findChromeProfileListUseCase: FindChromeProfileListUseCase,
    private val selectChromeSessionProfileUseCase: SelectChromeSessionProfileUseCase,
    private val unselectChromeSessionProfileUseCase: UnselectChromeSessionProfileUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingBrowserUiState> =
        combine(
            getChromeSessionProfileDirectoryUseCase(parameter = Unit),
            flow { emit(findChromeProfileListUseCase(parameter = Unit)) },
        ) { directoryResult, profileListResult ->
            val profileList = profileListResult.getOrDefault(emptyList())

            directoryResult.fold(
                onSuccess = { directory ->
                    SettingBrowserUiState.Loaded(
                        profileList = profileList,
                        selectedProfileDirectory = directory.takeIf { selected -> profileList.isListed(directory = selected) }.orEmpty(),
                        isProfileListUnavailable = profileListResult.isFailure,
                        hasStoredProfile = directory.isNotEmpty(),
                    )
                },
                onFailure = { SettingBrowserUiState.Loading },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingBrowserUiState.Loading,
        )

    fun selectProfile(directory: String) {
        if (isSelected(directory = directory)) return

        viewModelScope.launch {
            selectChromeSessionProfileUseCase(parameter = directory)
        }
    }

    fun unselectProfile() {
        if ((uiState.value as? SettingBrowserUiState.Loaded)?.hasStoredProfile == false) return

        viewModelScope.launch {
            unselectChromeSessionProfileUseCase(parameter = Unit)
        }
    }

    private fun isSelected(directory: String): Boolean = (uiState.value as? SettingBrowserUiState.Loaded)?.selectedProfileDirectory == directory

    private fun List<ChromeProfile>.isListed(directory: String): Boolean = any { profile -> profile.directory == directory }
}
