package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_proxy_not_configured_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_proxy_unreachable_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_tool_not_installed_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_tool_prepare_failed_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaylistHomeScreenEffect(
    effect: Flow<PlaylistHomeDownloadEffect> = emptyFlow(),
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val coroutineScope = rememberCoroutineScope()
    val toolNotInstalledMessage = stringResource(Res.string.playlist_home_download_tool_not_installed_message)
    val toolPrepareFailedMessage = stringResource(Res.string.playlist_home_download_tool_prepare_failed_message)
    val proxyNotConfiguredMessage = stringResource(Res.string.playlist_home_download_proxy_not_configured_message)
    val proxyUnreachableMessage = stringResource(Res.string.playlist_home_download_proxy_unreachable_message)

    CollectEffect(effect) { value ->
        val message =
            when (value) {
                is PlaylistHomeDownloadEffect.ToolNotInstalled -> toolNotInstalledMessage
                is PlaylistHomeDownloadEffect.ToolPrepareFailed -> toolPrepareFailedMessage
                is PlaylistHomeDownloadEffect.ProxyNotConfigured -> proxyNotConfiguredMessage
                is PlaylistHomeDownloadEffect.ProxyUnreachable -> proxyUnreachableMessage
            }

        coroutineScope.launch { hostState.showImmediate(message = message) }
    }
}
