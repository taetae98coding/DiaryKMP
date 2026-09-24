package io.github.taetae98coding.diary.work.musicdownload.work

import kotlin.uuid.Uuid

internal const val MUSIC_FILE_DIRECTORY: String = "music"

internal fun Uuid.toMusicFileName(): String = "$this.mp4"
