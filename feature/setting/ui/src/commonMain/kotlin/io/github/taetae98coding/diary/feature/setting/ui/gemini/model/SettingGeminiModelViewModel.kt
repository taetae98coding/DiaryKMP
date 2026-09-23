package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.usecase.FetchGeminiModelUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingGeminiModelViewModel(
    private val fetchGeminiModelUseCase: FetchGeminiModelUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingGeminiModelUiState>
        field = MutableStateFlow(SettingGeminiModelUiState())

    private val _effect = Channel<SettingGeminiModelFailure>(Channel.BUFFERED)
    val effect: Flow<SettingGeminiModelFailure> = _effect.receiveAsFlow()

    fun fetch(apiKey: String) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { it.copy(isInProgress = true, failure = null) }
            try {
                fetchGeminiModelUseCase(parameter = apiKey)
                    .onSuccess { modelList -> uiState.update { it.copy(isLoaded = true, modelList = modelList) } }
                    .onFailure { throwable -> onFetchFailure(throwable = throwable) }
            } finally {
                uiState.update { it.copy(isInProgress = false) }
            }
        }
    }

    private suspend fun onFetchFailure(throwable: Throwable) {
        val failure =
            if (throwable is GeminiApiKeyInvalidException) {
                SettingGeminiModelFailure.INVALID_API_KEY
            } else {
                SettingGeminiModelFailure.UNKNOWN
            }

        uiState.update { it.copy(failure = failure) }

        if (uiState.value.isLoaded) {
            _effect.send(failure)
        }
    }
}
