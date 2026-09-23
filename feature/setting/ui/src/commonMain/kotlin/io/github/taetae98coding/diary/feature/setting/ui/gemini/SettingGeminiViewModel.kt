package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.domain.setting.usecase.GetGeminiSettingUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.SetGeminiSettingUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingGeminiViewModel(
    getGeminiSettingUseCase: GetGeminiSettingUseCase,
    private val setGeminiSettingUseCase: SetGeminiSettingUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    private val _effect = Channel<SettingGeminiEffect>(Channel.BUFFERED)
    val effect: Flow<SettingGeminiEffect> = _effect.receiveAsFlow()

    val uiState: StateFlow<SettingGeminiUiState> =
        combine(
            getGeminiSettingUseCase(parameter = Unit),
            isInProgress,
        ) { result, isInProgress ->
            result.fold(
                onSuccess = { setting -> SettingGeminiUiState.Loaded(setting = setting, isInProgress = isInProgress) },
                onFailure = { SettingGeminiUiState.Loading },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingGeminiUiState.Loading,
        )

    fun save(setting: GeminiSetting) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
            try {
                setGeminiSettingUseCase(parameter = setting)
                    .onSuccess { _effect.send(SettingGeminiEffect.SaveSucceeded) }
                    .onFailure { _effect.send(SettingGeminiEffect.SaveFailed) }
            } finally {
                isInProgress.value = false
            }
        }
    }
}
