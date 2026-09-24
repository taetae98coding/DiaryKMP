package io.github.taetae98coding.diary.work.musicdownload.work

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.uuid.Uuid

internal val musicDownloadFixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

internal fun testDownloadTarget(): MusicDownloadTarget =
    MusicDownloadTarget(
        id = Uuid.random(),
        link = "https://youtu.be/video${musicDownloadFixtureMonkey.giveMeOne<Int>()}",
    )
