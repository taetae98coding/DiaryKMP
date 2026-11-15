package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingMapViewModel(
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
    private val setDefaultMapProviderUseCase: SetDefaultMapProviderUseCase,
) : ViewModel() {
    private var isSaving = false

    val uiState: StateFlow<SettingMapUiState> =
        getDefaultMapProviderUseCase()
            .map { result ->
                result.fold(
                    onSuccess = { provider -> SettingMapUiState.Content(defaultProvider = provider) },
                    onFailure = { SettingMapUiState.Loading },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = SettingMapUiState.Loading,
            )

    fun selectDefaultProvider(provider: MapProvider) {
        val uiState = uiState.value
        if (isSaving || (uiState is SettingMapUiState.Content && uiState.defaultProvider == provider)) return
        isSaving = true

        viewModelScope.launch {
            try {
                setDefaultMapProviderUseCase(parameter = provider)
            } finally {
                isSaving = false
            }
        }
    }
}
