package io.github.taetae98coding.diary.feature.setting.ui.mcp

import io.github.taetae98coding.diary.core.model.mcp.McpServerState

internal sealed interface SettingMcpUiState {
    data object Loading : SettingMcpUiState

    data class Content(
        val isEnabled: Boolean,
        val serverState: McpServerState,
    ) : SettingMcpUiState
}
