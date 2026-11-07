package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.domain.mcp.usecase.DisableMcpServerUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.EnableMcpServerUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.GetMcpServerEnabledUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.GetMcpServerStateUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingMcpViewModel(
    getMcpServerEnabledUseCase: GetMcpServerEnabledUseCase,
    getMcpServerStateUseCase: GetMcpServerStateUseCase,
    private val enableMcpServerUseCase: EnableMcpServerUseCase,
    private val disableMcpServerUseCase: DisableMcpServerUseCase,
) : ViewModel() {
    private var isSaving = false

    val uiState: StateFlow<SettingMcpUiState> =
        combine(
            getMcpServerEnabledUseCase(parameter = Unit),
            getMcpServerStateUseCase(parameter = Unit),
        ) { enabledResult, stateResult ->
            enabledResult.fold(
                onSuccess = { isEnabled ->
                    SettingMcpUiState.Content(
                        isEnabled = isEnabled,
                        serverState = stateResult.getOrDefault(McpServerState.Off),
                    )
                },
                onFailure = { SettingMcpUiState.Loading },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingMcpUiState.Loading,
        )

    fun changeEnabled(isEnabled: Boolean) {
        val uiState = uiState.value
        if (isSaving || uiState !is SettingMcpUiState.Content || uiState.isEnabled == isEnabled) return
        isSaving = true

        viewModelScope.launch {
            try {
                if (isEnabled) {
                    enableMcpServerUseCase(parameter = Unit)
                } else {
                    disableMcpServerUseCase(parameter = Unit)
                }
            } finally {
                isSaving = false
            }
        }
    }
}
