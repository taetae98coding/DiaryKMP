package io.github.taetae98coding.diary.core.testing.playlist

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.music(): Arb<Music> =
    arbitrary {
        Music(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.musicDetail().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.musicDetail(): Arb<MusicDetail> =
    arbitrary {
        MusicDetail(
            title = Arb.string().bind(),
            artist = Arb.string().bind(),
            link = Arb.string().bind(),
        )
    }

public fun Arb.Companion.youtubeVideo(): Arb<YoutubeVideo> =
    arbitrary {
        YoutubeVideo(
            title = Arb.string().bind(),
            channelName = Arb.string().bind(),
        )
    }
