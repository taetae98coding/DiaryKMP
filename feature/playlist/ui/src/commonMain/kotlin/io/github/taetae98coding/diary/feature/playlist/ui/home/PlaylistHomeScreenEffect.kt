package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_deleted_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_proxy_not_configured_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_proxy_unreachable_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_tool_not_installed_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_download_tool_prepare_failed_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DownloadFailureSnackbarEffect(
    effect: Flow<PlaylistHomeDownloadEffect>,
    snackbarHostState: SnackbarHostState,
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

        coroutineScope.launch { snackbarHostState.showImmediate(message = message) }
    }
}

@Composable
internal fun DeleteUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<PlaylistHomeEffect>,
    snackbarHostState: SnackbarHostState,
) {
    UndoSnackbarEffect(
        actionLabel = stringResource(Res.string.playlist_home_undo_action),
        message = stringResource(Res.string.playlist_home_deleted_message),
        onUndo = { value ->
            when (value) {
                is PlaylistHomeEffect.Deleted -> onRestore(value.id)
            }
        },
        effect = effect,
        snackbarHostState = snackbarHostState,
    )
}
