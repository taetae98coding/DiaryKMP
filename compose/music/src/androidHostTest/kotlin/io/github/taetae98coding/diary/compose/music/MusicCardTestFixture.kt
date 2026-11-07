package io.github.taetae98coding.diary.compose.music

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.playlist.musicDetail
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next

internal fun testMusic(
    title: String,
    artist: String = "가수-${Arb.int().next()}",
    link: String = "https://youtu.be/dQw4w9WgXcQ",
): Music =
    Arb.music().next().copy(
        detail = Arb.musicDetail().next().copy(title = title, artist = artist, link = link),
        isDeleted = false,
    )
