package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.usecase.FetchGeminiModelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingGeminiModelViewModel(
    private val fetchGeminiModelUseCase: FetchGeminiModelUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingGeminiModelUiState>
        field = MutableStateFlow(SettingGeminiModelUiState())

    fun fetch(apiKey: String) {
        if (uiState.value.isInProgress) return

        uiState.value = SettingGeminiModelUiState(isInProgress = true)
        viewModelScope.launch {
            uiState.value =
                fetchGeminiModelUseCase(parameter = apiKey).fold(
                    onSuccess = { modelList -> SettingGeminiModelUiState(isLoaded = true, modelList = modelList) },
                    onFailure = { throwable -> SettingGeminiModelUiState(failure = throwable.toFailure()) },
                )
        }
    }

    private fun Throwable.toFailure(): SettingGeminiModelFailure =
        if (this is GeminiApiKeyInvalidException) {
            SettingGeminiModelFailure.INVALID_API_KEY
        } else {
            SettingGeminiModelFailure.UNKNOWN
        }
}
