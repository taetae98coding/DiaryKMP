package io.github.taetae98coding.diary.feature.playlist.ui.home

internal sealed interface PlaylistHomeDownloadEffect {
    data object ToolNotInstalled : PlaylistHomeDownloadEffect

    data object ToolPrepareFailed : PlaylistHomeDownloadEffect

    data object ProxyNotConfigured : PlaylistHomeDownloadEffect

    data object ProxyUnreachable : PlaylistHomeDownloadEffect
}
