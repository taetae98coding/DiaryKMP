package io.github.taetae98coding.diary.work.musicdownload.work

import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource

internal const val MUSIC_FILE_DIRECTORY: String = "music"

internal fun String.toMusicFileName(): String = "$this.mp4"

// 받는 도중의 파일은 완성된 파일과 이름이 달라야 파일이 있다는 것이 끝까지 받았다는 뜻이 된다.
internal fun String.toMusicDownloadingFileName(): String = "$this.downloading.mp4"

internal data class MusicFilePath(
    val downloading: String,
    val completed: String,
)

internal suspend fun AppFileLocalDataSource.resolveMusicFilePath(videoId: String): MusicFilePath =
    MusicFilePath(
        downloading = resolve(directory = MUSIC_FILE_DIRECTORY, name = videoId.toMusicDownloadingFileName()),
        completed = resolve(directory = MUSIC_FILE_DIRECTORY, name = videoId.toMusicFileName()),
    )
