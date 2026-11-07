package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.icon.CopyIcon
import io.github.taetae98coding.diary.compose.core.icon.LinkIcon
import io.github.taetae98coding.diary.compose.core.icon.WarningIcon
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItem
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_copy_address
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_enabled_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_state_address_in_use
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_state_off
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_state_running
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_state_start_failed
import org.jetbrains.compose.resources.stringResource

private const val GROUP_ITEM_COUNT = 2

@Composable
internal fun SettingMcpConnectionGroup(
    isEnabled: Boolean,
    serverState: McpServerState,
    onEvent: (SettingMcpScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
    ) {
        SettingMcpEnabledItem(
            isEnabled = isEnabled,
            onCheckedChange = { checked -> onEvent(SettingMcpScaffoldEvent.ChangeEnabled(isEnabled = checked)) },
        )
        SettingMcpStateItem(
            serverState = serverState,
            onCopyAddress = { address -> onEvent(SettingMcpScaffoldEvent.ClickCopyAddress(address = address)) },
        )
    }
}

@Composable
private fun SettingMcpEnabledItem(
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    DiarySegmentedListItem(
        onClick = { onCheckedChange(!isEnabled) },
        modifier =
            modifier.semantics {
                role = Role.Switch
                toggleableState = ToggleableState(isEnabled)
            },
        index = 0,
        count = GROUP_ITEM_COUNT,
        trailingContent = { Switch(checked = isEnabled, onCheckedChange = null) },
    ) {
        Text(text = stringResource(Res.string.setting_mcp_enabled_label))
    }
}

@Composable
private fun SettingMcpStateItem(
    serverState: McpServerState,
    onCopyAddress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val copyLabel = stringResource(Res.string.setting_mcp_copy_address)
    val runningAddress = (serverState as? McpServerState.Running)?.address

    DiarySegmentedListItem(
        onClick = { runningAddress?.let(onCopyAddress) },
        modifier =
            if (runningAddress == null) {
                modifier
            } else {
                modifier.semantics {
                    onClick(label = copyLabel) {
                        onCopyAddress(runningAddress)
                        true
                    }
                }
            },
        enabled = runningAddress != null,
        index = 1,
        count = GROUP_ITEM_COUNT,
        leadingContent = { SettingMcpStateIcon(serverState = serverState) },
        supportingContent = serverState.address()?.let { address -> { Text(text = address, fontFamily = FontFamily.Monospace) } },
        trailingContent = runningAddress?.let { { CopyIcon() } },
    ) {
        Text(text = serverState.label())
    }
}

@Composable
private fun SettingMcpStateIcon(serverState: McpServerState) {
    val color =
        when (serverState) {
            is McpServerState.Off -> DiaryTheme.colorScheme.onSurfaceVariant
            is McpServerState.Running -> DiaryTheme.colorScheme.primary
            is McpServerState.AddressInUse, is McpServerState.StartFailed -> DiaryTheme.colorScheme.error
        }

    CompositionLocalProvider(LocalContentColor provides color) {
        when (serverState) {
            is McpServerState.Off, is McpServerState.Running -> LinkIcon()
            is McpServerState.AddressInUse, is McpServerState.StartFailed -> WarningIcon()
        }
    }
}

private fun McpServerState.address(): String? =
    when (this) {
        is McpServerState.Running -> address
        is McpServerState.AddressInUse -> address
        is McpServerState.Off, is McpServerState.StartFailed -> null
    }

@Composable
private fun McpServerState.label(): String =
    when (this) {
        is McpServerState.Off -> stringResource(Res.string.setting_mcp_state_off)
        is McpServerState.Running -> stringResource(Res.string.setting_mcp_state_running)
        is McpServerState.AddressInUse -> stringResource(Res.string.setting_mcp_state_address_in_use)
        is McpServerState.StartFailed -> stringResource(Res.string.setting_mcp_state_start_failed)
    }

private class McpServerStatePreviewParameter : PreviewParameterProvider<McpServerState> {
    override val values: Sequence<McpServerState> =
        sequenceOf(
            McpServerState.Off,
            McpServerState.Running(address = "http://127.0.0.1:38450/mcp"),
            McpServerState.AddressInUse(address = "http://127.0.0.1:38450/mcp"),
            McpServerState.StartFailed,
        )
}

@ComponentPreview
@Composable
private fun SettingMcpConnectionGroupPreview(
    @PreviewParameter(McpServerStatePreviewParameter::class) serverState: McpServerState,
) {
    DiaryTheme {
        SettingMcpConnectionGroup(
            isEnabled = serverState !is McpServerState.Off,
            serverState = serverState,
            onEvent = {},
        )
    }
}
