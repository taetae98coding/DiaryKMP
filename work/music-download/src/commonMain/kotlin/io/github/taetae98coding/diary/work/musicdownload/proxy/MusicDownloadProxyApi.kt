package io.github.taetae98coding.diary.work.musicdownload.proxy

internal const val MUSIC_DOWNLOAD_PROXY_HEALTH_PATH: String = "/health"
internal const val MUSIC_DOWNLOAD_PROXY_MUSIC_PATH: String = "/music"

internal fun String.toMusicDownloadProxyBaseUrl(): String = trim().trimEnd('/')

internal fun String.toMusicDownloadProxyHealthUrl(): String = "${toMusicDownloadProxyBaseUrl()}$MUSIC_DOWNLOAD_PROXY_HEALTH_PATH"

internal fun String.toMusicDownloadProxyMusicUrl(videoId: String): String = "${toMusicDownloadProxyBaseUrl()}$MUSIC_DOWNLOAD_PROXY_MUSIC_PATH/$videoId"
