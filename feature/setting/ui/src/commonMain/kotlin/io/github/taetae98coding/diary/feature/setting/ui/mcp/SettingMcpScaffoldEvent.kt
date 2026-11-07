package io.github.taetae98coding.diary.feature.setting.ui.mcp

internal sealed interface SettingMcpScaffoldEvent {
    data object ClickNavigateUp : SettingMcpScaffoldEvent

    data class ChangeEnabled(
        val isEnabled: Boolean,
    ) : SettingMcpScaffoldEvent

    data class ClickCopyAddress(
        val address: String,
    ) : SettingMcpScaffoldEvent
}
