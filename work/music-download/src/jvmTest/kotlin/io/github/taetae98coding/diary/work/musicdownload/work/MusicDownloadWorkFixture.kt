package io.github.taetae98coding.diary.work.musicdownload.work

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.uuid.Uuid

internal val musicDownloadFixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private const val VIDEO_ID_LENGTH = 11
private val VIDEO_ID_CHARS = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_')

internal fun testVideoId(): String = List(VIDEO_ID_LENGTH) { VIDEO_ID_CHARS.random() }.joinToString(separator = "")

internal fun testDownloadTarget(videoId: String = testVideoId()): MusicDownloadTarget =
    MusicDownloadTarget(
        id = Uuid.random(),
        videoId = videoId,
    )

internal fun testMusicFilePath(videoId: String): MusicFilePath =
    MusicFilePath(
        downloading = "/tmp/music/${videoId.toMusicDownloadingFileName()}",
        completed = "/tmp/music/${videoId.toMusicFileName()}",
    )
