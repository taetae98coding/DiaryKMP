package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.browser.usecase.FindChromeSessionImportSupportUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingHomeViewModel(
    findChromeSessionImportSupportUseCase: FindChromeSessionImportSupportUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingHomeUiState> =
        flow { emit(findChromeSessionImportSupportUseCase(parameter = Unit)) }
            .map { result ->
                result.fold(
                    onSuccess = { isChromeSessionImportSupported ->
                        SettingHomeUiState.Loaded(
                            itemList = settingHomeItemList.filter { item -> item.isProvided(isChromeSessionImportSupported = isChromeSessionImportSupported) },
                        )
                    },
                    onFailure = { SettingHomeUiState.Loading },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = SettingHomeUiState.Loading,
            )

    private fun SettingHomeItem.isProvided(isChromeSessionImportSupported: Boolean): Boolean =
        when (this) {
            SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI -> true
            SettingHomeItem.BROWSER -> isChromeSessionImportSupported
        }
}
