package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_address_copied_message
import org.jetbrains.compose.resources.stringResource

internal class SettingMcpAddressCopier(
    private val clipboard: Clipboard,
    private val snackbarHostState: SnackbarHostState,
    private val copiedMessage: String,
) {
    suspend fun copy(address: String) {
        clipboard.setClipEntry(plainTextClipEntry(text = address))
        snackbarHostState.showImmediate(message = copiedMessage)
    }
}

@Composable
internal fun rememberSettingMcpAddressCopier(snackbarHostState: SnackbarHostState): SettingMcpAddressCopier {
    val clipboard = LocalClipboard.current
    val copiedMessage = stringResource(Res.string.setting_mcp_address_copied_message)

    return remember(clipboard, snackbarHostState, copiedMessage) {
        SettingMcpAddressCopier(
            clipboard = clipboard,
            snackbarHostState = snackbarHostState,
            copiedMessage = copiedMessage,
        )
    }
}

internal expect fun plainTextClipEntry(text: String): ClipEntry
